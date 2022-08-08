package com.techbank.account.cmd.infrastructure;

import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techbank.account.cmd.domain.AccountAggregate;
import com.techbank.cqrs.core.domain.AggregateRoot;
import com.techbank.cqrs.core.handlers.EventSorucingHandler;
import com.techbank.cqrs.core.infrastructure.EventStore;
import com.techbank.cqrs.core.producers.EventProducer;

@Service
public class AccountEventSourcingHandler implements EventSorucingHandler<AccountAggregate> {

	@Autowired
	private EventStore eventStore;

	@Autowired
	private EventProducer eventProducer;

	@Override
	public void save(AggregateRoot aggregate) {

		eventStore.saveEvents(aggregate.getId(), aggregate.getUncommittedChanges(), aggregate.getVersion());
		aggregate.markChangesAsCommitted();
	}

	@Override
	public AccountAggregate getById(String id) {
		var aggregate = new AccountAggregate();
		var events = eventStore.getEvents(id);
		if (events != null && !events.isEmpty()) {
			aggregate.replayEvents(events);
			var latestVersion = events.stream().map(x -> x.getVersion()).max(Comparator.naturalOrder());
			aggregate.setVersion(latestVersion.get());

		}

		return aggregate;

	}

	@Override
	public void republishedEvents() {
		var aggregaIds = eventStore.getAggregateIds();
		for (var aggregateId : aggregaIds) {
			var aggregate = getById(aggregateId);
			if (aggregate == null || !aggregate.getActive())
				continue;
			var events = eventStore.getEvents(aggregateId);
			for (var event : events) {
				eventProducer.produce(event.getClass().getSimpleName(), event);

			}
		}

	}

}
