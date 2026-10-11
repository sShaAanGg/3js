package com.example.digitaltwin.asset;

import com.example.digitaltwin.history.HistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetRepository repository;
    private final HistoryService history;

    public AssetController(AssetRepository repository, HistoryService history) {
        this.repository = repository;
        this.history = history;
    }

    /** Current assets, or the room as it was at {@code at} (ISO-8601, e.g. 2026-10-10T14:03:00Z). */
    @GetMapping
    public List<Asset> list(@RequestParam(required = false) Instant at) {
        return at == null ? repository.findAll() : history.stateAt(at);
    }

    @GetMapping("/{id}")
    public Asset get(@PathVariable Long id) {
        return find(id);
    }

    /** Move an asset in the scene. */
    @PutMapping("/{id}/position")
    public Asset move(@PathVariable Long id, @RequestBody Position position) {
        Asset asset = find(id);
        asset.setPosX(position.x());
        asset.setPosY(position.y());
        asset.setPosZ(position.z());
        Asset saved = repository.save(asset);
        history.recordMove(saved);
        return saved;
    }

    private Asset find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset " + id + " not found"));
    }

    public record Position(double x, double y, double z) {
    }
}
