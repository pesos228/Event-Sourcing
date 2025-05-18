package org.bank.accountcommandservice.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaConfig {

    @Value("${app.kafka.topic-name}")
    private String topicName;

    @Value("${app.kafka.topic-partitions}")
    private int partitions;

    private final ConsumerFactory<String, Object> defaultConsumerFactory;

    public KafkaConfig(ConsumerFactory<String, Object> defaultConsumerFactory) {
        this.defaultConsumerFactory = defaultConsumerFactory;
    }

    @Bean
    public NewTopic accountEventsTopic() {
        return TopicBuilder.name(topicName)
                .partitions(partitions)
                .replicas(1)
                .build();
    }

    @Bean
    @Scope("prototype")
    public KafkaConsumer<String, Object> replayKafkaConsumer() {
        Map<String, Object> props = new HashMap<>(defaultConsumerFactory.getConfigurationProperties());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "replay-group-" + UUID.randomUUID().toString());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        return new KafkaConsumer<>(props);
    }


}
