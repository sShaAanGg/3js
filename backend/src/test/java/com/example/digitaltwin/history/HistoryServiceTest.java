package com.example.digitaltwin.history;

import com.example.digitaltwin.asset.Asset;
import com.example.digitaltwin.asset.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

// Runs against in-memory H2; the schema is generated from the entities instead of init.sql
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import({HistoryService.class, HistoryServiceTest.Config.class})
class HistoryServiceTest {

    @TestConfiguration
    static class Config {
        @Bean
        HistoryProperties historyProperties() {
            return new HistoryProperties(0.5, Duration.ofDays(7));
        }
    }

    private static final Instant T0 = Instant.parse("2026-01-01T12:00:00Z");

    @Autowired
    private HistoryService history;
    @Autowired
    private AssetEventRepository events;
    @Autowired
    private AssetRepository assets;

    private Asset pump;
    private Asset tank;

    @BeforeEach
    void setUp() {
        pump = assets.save(asset("Pump A", 20.0));
        tank = assets.save(asset("Tank B", 18.0));
    }

    @Test
    void stateAtReturnsNewestEventPerAssetAtOrBeforeTheGivenTime() {
        pump.setTemperature(20.0);
        events.save(AssetEvent.of(pump, AssetEvent.Kind.CREATED, T0));
        pump.setTemperature(30.0);
        events.save(AssetEvent.of(pump, AssetEvent.Kind.READING, T0.plusSeconds(60)));
        pump.setTemperature(40.0);
        events.save(AssetEvent.of(pump, AssetEvent.Kind.READING, T0.plusSeconds(120)));
        // Tank only appears later, so it must be missing from earlier states
        events.save(AssetEvent.of(tank, AssetEvent.Kind.CREATED, T0.plusSeconds(90)));

        List<Asset> at61 = history.stateAt(T0.plusSeconds(61));
        assertThat(at61).extracting(Asset::getName).containsExactly("Pump A");
        assertThat(at61.get(0).getTemperature()).isEqualTo(30.0);
        assertThat(at61.get(0).getUpdatedAt()).isEqualTo(T0.plusSeconds(60));

        // Exactly on an event's timestamp counts as "at or before"
        assertThat(history.stateAt(T0.plusSeconds(120)))
                .extracting(Asset::getName, Asset::getTemperature)
                .containsExactly(
                        tuple("Pump A", 40.0),
                        tuple("Tank B", 18.0));

        assertThat(history.stateAt(T0.minusSeconds(1))).isEmpty();
    }

    @Test
    void recordReadingStoresOnlyMeaningfulChanges() {
        history.recordReading(pump);                         // first event -> CREATED
        pump.setTemperature(20.3);
        history.recordReading(pump);                         // +0.3 °C -> skipped
        pump.setTemperature(20.6);
        history.recordReading(pump);                         // +0.6 °C vs last stored -> READING
        pump.setTemperature(20.7);
        pump.setStatus(Asset.Status.WARNING);
        history.recordReading(pump);                         // status changed -> STATUS_CHANGED

        assertThat(events.findAll())
                .extracting(AssetEvent::getKind, AssetEvent::getTemperature)
                .containsExactly(
                        tuple(AssetEvent.Kind.CREATED, 20.0),
                        tuple(AssetEvent.Kind.READING, 20.6),
                        tuple(AssetEvent.Kind.STATUS_CHANGED, 20.7));
    }

    @Test
    void recordMissingAssetsAddsCreatedEventOnlyForAssetsWithoutHistory() {
        events.save(AssetEvent.of(pump, AssetEvent.Kind.CREATED, T0));

        history.recordMissingAssets();

        assertThat(events.findAll())
                .extracting(AssetEvent::getAssetId)
                .containsExactlyInAnyOrder(pump.getId(), tank.getId());
    }

    @Test
    void retentionKeepsNewestEventPerAssetBeforeCutoff() {
        Instant cutoff = T0.plusSeconds(300);
        AssetEvent oldest = events.save(AssetEvent.of(pump, AssetEvent.Kind.CREATED, T0));
        AssetEvent lastBeforeCutoff = events.save(AssetEvent.of(pump, AssetEvent.Kind.READING, T0.plusSeconds(100)));
        AssetEvent afterCutoff = events.save(AssetEvent.of(pump, AssetEvent.Kind.READING, T0.plusSeconds(400)));
        AssetEvent tankOnly = events.save(AssetEvent.of(tank, AssetEvent.Kind.CREATED, T0.plusSeconds(50)));

        int deleted = events.deleteOlderThanKeepingLatest(cutoff);

        assertThat(deleted).isEqualTo(1);
        assertThat(events.findAll())
                .extracting(AssetEvent::getId)
                .containsExactlyInAnyOrder(lastBeforeCutoff.getId(), afterCutoff.getId(), tankOnly.getId())
                .doesNotContain(oldest.getId());
        // The room just after the cutoff can still be rebuilt completely
        assertThat(history.stateAt(cutoff)).hasSize(2);
    }

    @Test
    void rangeStartsAtOldestEvent() {
        events.save(AssetEvent.of(pump, AssetEvent.Kind.CREATED, T0));
        events.save(AssetEvent.of(tank, AssetEvent.Kind.CREATED, T0.plusSeconds(10)));

        HistoryService.Range range = history.range();

        assertThat(range.earliest()).isEqualTo(T0);
        assertThat(range.latest()).isAfter(T0);
    }

    private static Asset asset(String name, double temperature) {
        Asset asset = new Asset();
        asset.setName(name);
        asset.setShape(Asset.Shape.BOX);
        asset.setColor("#0000ff");
        asset.setTemperature(temperature);
        return asset;
    }
}
