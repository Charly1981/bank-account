package com.techbank.account.cmd.api.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techbank.account.cmd.domain.AccountAggregate;
import com.techbank.cqrs.core.handlers.EventSorucingHandler;

@Service
public class AccountCommandHandler implements CommandHandler {

	@Autowired
	private EventSorucingHandler<AccountAggregate> eventSorucingHandler;

	@Override
	public void handle(OpenAccountCommand command) {
		var aggregate = new AccountAggregate(command);
		eventSorucingHandler.save(aggregate);

	}

	@Override
	public void handle(DepositFundsCommand command) {
		var aggregate = eventSorucingHandler.getById(command.getId());
		aggregate.depositFunds(command.getAmount());
		eventSorucingHandler.save(aggregate);

	}

	@Override
	public void handle(WithdrawFundsCommand command) {
		var aggregate = eventSorucingHandler.getById(command.getId());
		if (command.getAmount() > aggregate.getBalance()) {
			throw new IllegalStateException("Withdrawal declined, insufficient funds!");
		}

		aggregate.withdrawFunds(command.getAmount());
		eventSorucingHandler.save(aggregate);

	}

	@Override
	public void handle(CloseAccountCommand command) {
		var aggregate = eventSorucingHandler.getById(command.getId());
		aggregate.closeAccount();
		eventSorucingHandler.save(aggregate);

	}

}
