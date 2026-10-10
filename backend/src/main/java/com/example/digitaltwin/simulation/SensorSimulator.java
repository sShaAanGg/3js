package com.example.digitaltwin.simulation;

import com.example.digitaltwin.asset.Asset;
import com.example.digitaltwin.asset.AssetRepository;
import com.example.digitaltwin.history.HistoryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Stands in for real sensors: drifts each asset's temperature and derives its status.
 * Replace with an MQTT/OPC-UA/Kafka consumer when real data is available.
 */
@Component
@ConditionalOnProperty(name = "twin.simulation.enabled", havingValue = "true")
public class SensorSimulator {

    private static final double WARNING_TEMP = 35.0;
    private static final double ALARM_TEMP = 45.0;

    private final AssetRepository repository;
    private final HistoryService history;

    public SensorSimulator(AssetRepository repository, HistoryService history) {
        this.repository = repository;
        this.history = history;
    }

    @Scheduled(fixedRateString = "${twin.simulation.interval-ms}")
    @Transactional
    public void tick() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (Asset asset : repository.findAll()) {
            double next = asset.getTemperature() + random.nextDouble(-1.5, 1.6);
            next = Math.max(10.0, Math.min(55.0, next));
            asset.setTemperature(Math.round(next * 10.0) / 10.0);
            asset.setStatus(next >= ALARM_TEMP ? Asset.Status.ALARM
                    : next >= WARNING_TEMP ? Asset.Status.WARNING
                    : Asset.Status.OK);
            history.recordReading(asset);
        }
    }
}
