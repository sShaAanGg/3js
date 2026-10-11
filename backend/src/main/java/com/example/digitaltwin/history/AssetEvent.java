package com.example.digitaltwin.history;

import com.example.digitaltwin.asset.Asset;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * One row of history: the full state of one asset at one moment.
 * Storing full snapshots (not deltas) means "the room at time T" is simply
 * the newest event per asset recorded at or before T.
 */
@Entity
@Table(name = "asset_event")
public class AssetEvent {

    public enum Kind { CREATED, READING, STATUS_CHANGED, MOVED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Kind kind;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Asset.Shape shape;

    @Column(name = "pos_x", nullable = false)
    private double posX;

    @Column(name = "pos_y", nullable = false)
    private double posY;

    @Column(name = "pos_z", nullable = false)
    private double posZ;

    @Column(nullable = false, length = 7)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Asset.Status status;

    @Column(nullable = false)
    private double temperature;

    protected AssetEvent() {
    }

    public static AssetEvent of(Asset asset, Kind kind, Instant recordedAt) {
        AssetEvent event = new AssetEvent();
        event.assetId = asset.getId();
        event.recordedAt = recordedAt;
        event.kind = kind;
        event.name = asset.getName();
        event.shape = asset.getShape();
        event.posX = asset.getPosX();
        event.posY = asset.getPosY();
        event.posZ = asset.getPosZ();
        event.color = asset.getColor();
        event.status = asset.getStatus();
        event.temperature = asset.getTemperature();
        return event;
    }

    /** Rebuilds the asset as it was at this moment. The result is detached, never saved. */
    public Asset toAsset() {
        Asset asset = new Asset();
        asset.setId(assetId);
        asset.setName(name);
        asset.setShape(shape);
        asset.setPosX(posX);
        asset.setPosY(posY);
        asset.setPosZ(posZ);
        asset.setColor(color);
        asset.setStatus(status);
        asset.setTemperature(temperature);
        asset.setUpdatedAt(recordedAt);
        return asset;
    }

    public Long getId() { return id; }
    public Long getAssetId() { return assetId; }
    public Instant getRecordedAt() { return recordedAt; }
    public Kind getKind() { return kind; }
    public String getName() { return name; }
    public Asset.Shape getShape() { return shape; }
    public double getPosX() { return posX; }
    public double getPosY() { return posY; }
    public double getPosZ() { return posZ; }
    public String getColor() { return color; }
    public Asset.Status getStatus() { return status; }
    public double getTemperature() { return temperature; }
}
