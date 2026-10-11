package com.example.digitaltwin.history;

import com.example.digitaltwin.asset.Asset;
import com.example.digitaltwin.asset.AssetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/** Records asset changes as events and rebuilds the room for any moment in the past. */
@Service
public class HistoryService {

    /** Caps how many events one request can return, to keep memory use predictable. */
    static final int MAX_EVENTS = 1000;

    private static final Logger log = LoggerFactory.getLogger(HistoryService.class);

    private final AssetEventRepository events;
    private final AssetRepository assets;
    private final HistoryProperties properties;

    public HistoryService(AssetEventRepository events, AssetRepository assets, HistoryProperties properties) {
        this.events = events;
        this.assets = assets;
        this.properties = properties;
    }

    /**
     * Stores a sensor reading only if it matters: the asset's first event, a status change,
     * or a temperature change of at least {@code twin.history.min-temperature-delta}.
     */
    @Transactional
    public void recordReading(Asset asset) {
        Optional<AssetEvent> last = events.findTopByAssetIdOrderByIdDesc(asset.getId());
        AssetEvent.Kind kind;
        if (last.isEmpty()) {
            kind = AssetEvent.Kind.CREATED;
        } else if (last.get().getStatus() != asset.getStatus()) {
            kind = AssetEvent.Kind.STATUS_CHANGED;
        } else if (Math.abs(last.get().getTemperature() - asset.getTemperature()) >= properties.minTemperatureDelta()) {
            kind = AssetEvent.Kind.READING;
        } else {
            return;
        }
        events.save(AssetEvent.of(asset, kind, Instant.now()));
    }

    @Transactional
    public void recordMove(Asset asset) {
        events.save(AssetEvent.of(asset, AssetEvent.Kind.MOVED, Instant.now()));
    }

    /** Gives every asset a starting point in history, e.g. right after init.sql seeded the table. */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void recordMissingAssets() {
        for (Asset asset : assets.findAll()) {
            if (events.findTopByAssetIdOrderByIdDesc(asset.getId()).isEmpty()) {
                events.save(AssetEvent.of(asset, AssetEvent.Kind.CREATED, Instant.now()));
            }
        }
    }

    /** The room as it was at {@code at}. Assets with no event yet at that time are left out. */
    @Transactional(readOnly = true)
    public List<Asset> stateAt(Instant at) {
        return events.findStateAt(at).stream().map(AssetEvent::toAsset).toList();
    }

    @Transactional(readOnly = true)
    public List<AssetEvent> eventsBetween(Instant from, Instant to) {
        return events.findByRecordedAtBetweenOrderByRecordedAtAsc(from, to, Limit.of(MAX_EVENTS));
    }

    /**
     * The time span the UI timeline can scrub over: from the oldest event up to now.
     * Rounded to whole milliseconds, which is what JavaScript dates can represent. The start
     * rounds up, so the oldest moment shown always has its event.
     */
    @Transactional(readOnly = true)
    public Range range() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        Instant earliest = events.findEarliestRecordedAt().map(HistoryService::ceilToMillis).orElse(now);
        return new Range(earliest, now);
    }

    private static Instant ceilToMillis(Instant t) {
        Instant floor = t.truncatedTo(ChronoUnit.MILLIS);
        return floor.equals(t) ? floor : floor.plusMillis(1);
    }

    @Scheduled(initialDelayString = "${twin.history.cleanup-interval-ms}",
            fixedDelayString = "${twin.history.cleanup-interval-ms}")
    @Transactional
    public void applyRetention() {
        int deleted = events.deleteOlderThanKeepingLatest(Instant.now().minus(properties.retention()));
        if (deleted > 0) {
            log.info("History cleanup removed {} events older than {}", deleted, properties.retention());
        }
    }

    public record Range(Instant earliest, Instant latest) {
    }
}
