package com.example.digitaltwin.asset;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetRepository repository;

    public AssetController(AssetRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Asset> list() {
        return repository.findAll();
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
        return repository.save(asset);
    }

    private Asset find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset " + id + " not found"));
    }

    public record Position(double x, double y, double z) {
    }
}
