package com.example.digitaltwin.history;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Settings under {@code twin.history} in application.yml.
 *
 * @param minTemperatureDelta store a new reading only when temperature moved at least this much (°C)
 * @param retention           how long history is kept before the cleanup job removes it
 */
@ConfigurationProperties("twin.history")
public record HistoryProperties(
        @DefaultValue("0.5") double minTemperatureDelta,
        @DefaultValue("7d") Duration retention) {
}
