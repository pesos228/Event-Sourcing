package org.bank.accountcommandservice.infrastructure.eventStore;


import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Utils;
import org.bank.accountcommandservice.domain.event.AccountCreatedEvent;
import org.bank.accountcommandservice.domain.event.BaseEvent;
import org.bank.accountcommandservice.domain.event.MoneyDepositedEvent;
import org.bank.accountcommandservice.domain.event.MoneyWithdrawnEvent;
import org.bank.accountcommandservice.domain.model.EventStream;
import org.bank.accountcommandservice.domain.repository.EventStore;
import org.bank.accountcommandservice.infrastructure.exception.EventPersistenceException;
import org.bank.accountcommandservice.infrastructure.exception.EventReplayException;
import org.bank.accountcommandservice.infrastructure.exception.InconsistentEventStreamException;
import org.bank.accountcommandservice.infrastructure.exception.UnsupportedEventTypeException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class KafkaEventStore implements EventStore {

    private final String topicName;
    private final int numPartitions;
    private final ObjectFactory<KafkaConsumer<String, Object>> replayConsumerFactory;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final StringSerializer keySerializer = new StringSerializer();

    public KafkaEventStore(
            @Value("${app.kafka.topic-name}") String topicName,
            @Value("${app.kafka.topic-partitions}") int numPartitions,
            @Qualifier("replayKafkaConsumer") ObjectFactory<KafkaConsumer<String, Object>> replayConsumerFactory, KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.topicName = topicName;
        this.numPartitions = numPartitions;
        this.replayConsumerFactory = replayConsumerFactory;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public long saveEvents(UUID accountId, List<Object> events) {
        if (events == null || events.isEmpty()) {
            return -1L;
        }

        long lastOffset = -1L;

        try {
            for (Object event : events) {
                var result = kafkaTemplate.send(topicName, accountId.toString(), event).get();
                var metadata = result.getRecordMetadata();
                if (metadata.hasOffset()){
                    lastOffset = metadata.offset();
                }
            }
        } catch (Exception e) {
            throw new EventPersistenceException("Unexpected error while saving event for accountId: " + accountId + ": " +  e.getMessage());
        }

        return lastOffset;
    }

    @Override
    public EventStream loadEventStream(UUID aggregateId) {
        try {
            return readEventsInternal(aggregateId, 0L, 0);
        }catch (Exception e) {
            throw new EventReplayException("Unexpected error during event replay for aggregateId: " + aggregateId + ": " +  e.getMessage());
        }
    }

    @Override
    public EventStream loadEventStreamAfter(UUID aggregateId, long offset, int version) {
        try {
            return readEventsInternal(aggregateId, offset + 1, version);
        }
        catch (Exception e) {
            throw new EventReplayException("Unexpected error during event replay after offset for aggregateId: " + aggregateId + ": " +  e.getMessage());
        }
    }

    @Override
    public EventStream loadEventStreamUpToVersion(UUID aggregateId, int targetVersionExclusive) {
        int partitionId = getPartitionId(aggregateId);
        List<Object> events = new ArrayList<>();
        long lastReadOffset = -1L;
        int currentVersion = 0;

        if (targetVersionExclusive == 0) {
            return new EventStream(Collections.emptyList(), partitionId, -1L, 0);
        }

        try (var consumer = replayConsumerFactory.getObject()) {
            var partition = new TopicPartition(topicName, partitionId);
            consumer.assign(List.of(partition));
            consumer.seekToBeginning(List.of(partition));

            while (currentVersion < targetVersionExclusive) {
                var records = consumer.poll(Duration.ofMillis(200));
                if (records.isEmpty()) {
                    continue;
                }

                for (var record : records) {
                    Object event = record.value();
                    if (!isEventForAggregate(event, aggregateId)) {
                        continue;
                    }

                    var baseEvent = getBaseEventFromRaw(event);
                    if (baseEvent == null) {
                        continue;
                    }

                    var eventVersion = baseEvent.aggregateVersion();
                    if (eventVersion >= targetVersionExclusive) {
                        return new EventStream(events, partitionId, lastReadOffset, currentVersion);
                    }

                    if (eventVersion != currentVersion) {
                        throw new InconsistentEventStreamException(
                                String.format("Inconsistent event stream for aggregateId %s: expected version %d, got %d at offset %d.",
                                        aggregateId, currentVersion, eventVersion, record.offset()));
                    }

                    events.add(event);
                    lastReadOffset = record.offset();
                    currentVersion = eventVersion + 1;
                }
            }

            return new EventStream(events, partitionId, lastReadOffset, currentVersion);

        } catch (Exception e) {
            throw new EventReplayException("Unexpected error during event replay up to version for aggregateId: " + aggregateId + ": " +  e.getMessage());
        }
    }

    private EventStream readEventsInternal(UUID accountId, long startOffset, int initVersionForStream) {
        List<Object> deserializedEvents = new ArrayList<>();
        var lastSuccessfullyReadOffset = (startOffset > 0) ? startOffset -1 : -1;
        var targetPartitionId = getPartitionId(accountId);
        var currentVersion = initVersionForStream;

        try (var consumer = replayConsumerFactory.getObject()) {

            var targetTopicPartition = new TopicPartition(topicName, targetPartitionId);
            consumer.assign(List.of(targetTopicPartition));

            if (startOffset > 0) {
                consumer.seek(targetTopicPartition, startOffset);
            }
            else{
                consumer.seekToBeginning(List.of(targetTopicPartition));
            }

            var polls = 0;
            var maxPolls = 5;
            var consecutiveEmptyPolls = 0;
            var maxConsecutiveEmptyPolls = 1;

            while (polls < maxPolls && consecutiveEmptyPolls < maxConsecutiveEmptyPolls) {
                var records = consumer.poll(Duration.ofMillis(200));
                polls++;

                if (records.isEmpty()) {
                    consecutiveEmptyPolls++;
                }
                else {
                    consecutiveEmptyPolls = 0;
                    for (var record : records) {
                        var rawEvent = record.value();
                        if (checkEvent(rawEvent, accountId)) {
                            int eventVersion = getEventVersion(rawEvent);
                            if (eventVersion == currentVersion) {
                                deserializedEvents.add(rawEvent);
                                lastSuccessfullyReadOffset = record.offset();
                                currentVersion = getEventVersion(rawEvent) + 1;
                            }
                            else{
                                throw new InconsistentEventStreamException(
                                        String.format("Inconsistent event stream for accountId %s: expected version %d, got %d at offset %d.",
                                                accountId, currentVersion, eventVersion, record.offset()));
                            }
                        }
                    }
                }
            }

        }
        catch (Exception e) {
            throw new EventReplayException("Unexpected error during event replay for accountId: " + accountId + ": " + e.getMessage());
        }

        return new EventStream(deserializedEvents, targetPartitionId, lastSuccessfullyReadOffset, currentVersion);

    }

    private int getEventVersion(Object rawEvent) {
        if (rawEvent instanceof AccountCreatedEvent event) {
            return event.baseEvent().aggregateVersion();
        }
        if (rawEvent instanceof MoneyDepositedEvent event) {
            return event.baseEvent().aggregateVersion();
        }
        if (rawEvent instanceof MoneyWithdrawnEvent event) {
            return event.baseEvent().aggregateVersion();
        }
        throw new UnsupportedEventTypeException("Unknown event type encountered: " + rawEvent.getClass().getName());
    }

    private boolean isEventForAggregate(Object event, UUID aggregateId) {
        if (event instanceof AccountCreatedEvent e) {
            return aggregateId.equals(e.baseEvent().accountId());
        }
        if (event instanceof MoneyDepositedEvent e) {
            return aggregateId.equals(e.baseEvent().accountId());
        }
        if (event instanceof MoneyWithdrawnEvent e) {
            return aggregateId.equals(e.baseEvent().accountId());
        }
        return false;
    }

    private BaseEvent getBaseEventFromRaw(Object event) {
        return switch (event) {
            case AccountCreatedEvent e -> e.baseEvent();
            case MoneyDepositedEvent e -> e.baseEvent();
            case MoneyWithdrawnEvent e -> e.baseEvent();
            default -> null;
        };
    }

    private boolean checkEvent(Object rawEvent, UUID accountId) {
        if (rawEvent instanceof AccountCreatedEvent event) {
            if (accountId.equals(event.baseEvent().accountId())) {
                return true;
            }
        }
        if (rawEvent instanceof MoneyDepositedEvent event) {
            if (accountId.equals(event.baseEvent().accountId())) {
                return true;
            }
        }
        if (rawEvent instanceof MoneyWithdrawnEvent event) {
            if (accountId.equals(event.baseEvent().accountId())) {
                return true;
            }
        }

        return false;
    }

    private int getPartitionId(UUID accountId) {
        byte[] keyBytes = keySerializer.serialize(topicName, accountId.toString());
        return Utils.toPositive(Utils.murmur2(keyBytes)) % numPartitions;
    }
}
