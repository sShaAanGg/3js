package com.example.digitaltwin.history;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/** Time machine endpoints. The room at a past moment is served by {@code GET /api/assets?at=}. */
@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService history;

    public HistoryController(HistoryService history) {
        this.history = history;
    }

    /** Oldest and newest moment the timeline can show. */
    @GetMapping("/range")
    public HistoryService.Range range() {
        return history.range();
    }

    /** Raw events, oldest first. Defaults to the last hour; at most 1000 are returned. */
    @GetMapping("/events")
    public List<AssetEvent> events(@RequestParam(required = false) Instant from,
                                   @RequestParam(required = false) Instant to) {
        Instant end = to != null ? to : Instant.now();
        Instant start = from != null ? from : end.minus(Duration.ofHours(1));
        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' must not be after 'to'");
        }
        return history.eventsBetween(start, end);
    }
}
