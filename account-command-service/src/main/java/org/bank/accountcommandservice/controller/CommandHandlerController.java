package org.bank.accountcommandservice.controller;

import org.bank.accountcommandservice.application.AccountApplicationService;
import org.bank.accountcommandservice.domain.command.AccountCreateCommand;
import org.bank.accountcommandservice.domain.command.MoneyDepositCommand;
import org.bank.accountcommandservice.domain.command.MoneyWithdrawCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
public class CommandHandlerController {
    private final AccountApplicationService accountApplicationService;

    @Autowired
    public CommandHandlerController(AccountApplicationService accountApplicationService) {
        this.accountApplicationService = accountApplicationService;
    }

    @PostMapping("/create")
    public String createAccount(@RequestBody AccountCreateCommand command) {
        var accountId = accountApplicationService.accountCreate(command);
        return "Account created: " + accountId;
    }

    @PostMapping("/deposit")
    public void deposit(@RequestBody MoneyDepositCommand command) {
        accountApplicationService.accountDeposit(command);
    }

    @PostMapping("/withdraw")
    public void withdraw(@RequestBody MoneyWithdrawCommand command) {
        accountApplicationService.accountWithdraw(command);
    }
}
