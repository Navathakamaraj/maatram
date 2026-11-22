package org.maatram.service;

import org.maatram.dto.PaginatedResponse;
import org.maatram.exception.ApiException;
import org.maatram.model.DomainEvent;
import org.maatram.model.EventCheckpoint;
import org.maatram.model.ProcessedEvent;
import org.maatram.repository.DataStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DomainEventService {

    private final DataStore dataStore;

    public DomainEventService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public PaginatedResponse<DomainEvent> listEvents(String aggregateType, String eventType, int page, int pageSize) {
        List<DomainEvent> list = dataStore.domainEvents.stream()
                .filter(e -> aggregateType == null || aggregateType.equalsIgnoreCase(e.getAggregate_type()))
                .filter(e -> eventType == null || eventType.equalsIgnoreCase(e.getEvent_type()))
                .sorted(Comparator.comparing(DomainEvent::getId).reversed())
                .toList();

        return PaginatedResponse.of(list, page, pageSize);
    }

    public List<Map<String, Object>> listConsumers() {
        long maxId = dataStore.domainEvents.stream().mapToLong(DomainEvent::getId).max().orElse(0L);

        List<Map<String, Object>> list = new ArrayList<>();
        for (EventCheckpoint c : dataStore.eventCheckpoints) {
            Set<String> done = dataStore.processedEvents.stream()
                    .filter(p -> p.getConsumer_name().equals(c.getConsumer_name()))
                    .map(ProcessedEvent::getEvent_uuid)
                    .collect(Collectors.toSet());

            long pending = dataStore.domainEvents.stream().filter(e -> !done.contains(e.getEvent_uuid())).count();

            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("consumer_name", c.getConsumer_name());
            map.put("last_processed_event_id", c.getLast_processed_event_id());
            map.put("updated_at", c.getUpdated_at());
            map.put("max_event_id", maxId);
            map.put("pending", pending);
            list.add(map);
        }

        return list;
    }

    public Map<String, Object> replay(String consumerName, String mode, String fromTime) {
        EventCheckpoint cp = dataStore.eventCheckpoints.stream()
                .filter(c -> c.getConsumer_name().equals(consumerName))
                .findFirst()
                .orElse(null);

        if (cp == null) {
            throw new ApiException(404, "Unknown consumer");
        }

        List<DomainEvent> candidates = new ArrayList<>(dataStore.domainEvents);
        candidates.sort(Comparator.comparing(DomainEvent::getId));

        if ("bookmark".equals(mode)) {
            long threshold = cp.getLast_processed_event_id() - 200;
            candidates = candidates.stream().filter(e -> e.getId() > threshold).toList();
        } else if ("time".equals(mode)) {
            if (fromTime == null || fromTime.isBlank()) {
                throw new ApiException(422, "Pick a start time");
            }
            candidates = candidates.stream().filter(e -> e.getOccurred_at().compareTo(fromTime) >= 0).toList();
        }

        Set<String> done = dataStore.processedEvents.stream()
                .filter(p -> p.getConsumer_name().equals(consumerName))
                .map(ProcessedEvent::getEvent_uuid)
                .collect(Collectors.toSet());

        int processed = 0;
        int duplicates = 0;
        List<Long> processedIds = new ArrayList<>();
        String now = Instant.now().toString();

        for (DomainEvent e : candidates) {
            if (done.contains(e.getEvent_uuid())) {
                duplicates++;
                continue;
            }
            dataStore.processedEvents.add(new ProcessedEvent((long) (dataStore.processedEvents.size() + 1), consumerName, e.getEvent_uuid(), now));
            done.add(e.getEvent_uuid());
            processed++;
            if (processedIds.size() < 20) {
                processedIds.add(e.getId());
            }
        }

        long newLast = Math.max(cp.getLast_processed_event_id(), candidates.stream().mapToLong(DomainEvent::getId).max().orElse(0L));
        cp.setLast_processed_event_id(newLast);
        cp.setUpdated_at(now);

        Map<String, Object> res = new HashMap<>();
        res.put("scanned", candidates.size());
        res.put("processed", processed);
        res.put("skipped_duplicates", duplicates);
        res.put("processed_ids", processedIds);

        return res;
    }

    public DomainEvent appendEvent(String eventName, String occurredAt, String description) {
        if (eventName == null || eventName.isBlank()) {
            throw new ApiException(422, "Event name is required");
        }
        if (occurredAt == null || occurredAt.isBlank()) {
            throw new ApiException(422, "Pick the date and time");
        }
        if (description == null || description.isBlank()) {
            throw new ApiException(422, "Description is required");
        }

        String type = eventName.trim().toUpperCase().replaceAll("[^A-Z0-9]+", "_").replaceAll("^_+|_+$", "");
        if (type.isEmpty()) type = "MANUAL_EVENT";

        DomainEvent e = dataStore.emit("manual", 1L, type, Map.of(
                "event_name", eventName.trim(),
                "description", description.trim(),
                "added_by", 1L
        ));
        e.setOccurred_at(occurredAt);

        return e;
    }

    public DomainEvent lateCommit() {
        Set<Long> used = dataStore.domainEvents.stream().map(DomainEvent::getId).collect(Collectors.toSet());
        long minCp = dataStore.eventCheckpoints.stream()
                .mapToLong(EventCheckpoint::getLast_processed_event_id)
                .min()
                .orElse(100L);

        long gap = minCp - 1;
        while (gap > 0 && used.contains(gap)) {
            gap--;
        }

        if (gap <= 0) {
            throw new ApiException(409, "No free id gap to simulate with");
        }

        String now = Instant.now().toString();
        DomainEvent e = new DomainEvent(
                gap,
                UUID.randomUUID().toString(),
                "application",
                1L,
                "APPLICATION_NOTE_ADDED",
                Map.of("note", "Committed late (out of order)"),
                1,
                now,
                now
        );
        dataStore.domainEvents.add(e);
        dataStore.domainEvents.sort(Comparator.comparing(DomainEvent::getId));
        return e;
    }
}
