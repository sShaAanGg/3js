package com.example.digitaltwin.history;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AssetEventRepository extends JpaRepository<AssetEvent, Long> {

    /** The most recent event for an asset, used to decide whether a new reading is worth storing. */
    Optional<AssetEvent> findTopByAssetIdOrderByIdDesc(Long assetId);

    /**
     * Newest event per asset at or before {@code at}.
     * Uses max(id) rather than max(recordedAt): ids grow with insert order, which avoids ties
     * when two events share a timestamp.
     */
    @Query("""
            select e from AssetEvent e
            where e.id in (select max(e2.id) from AssetEvent e2
                           where e2.recordedAt <= :at
                           group by e2.assetId)
            order by e.assetId
            """)
    List<AssetEvent> findStateAt(@Param("at") Instant at);

    List<AssetEvent> findByRecordedAtBetweenOrderByRecordedAtAsc(Instant from, Instant to, Limit limit);

    @Query("select min(e.recordedAt) from AssetEvent e")
    Optional<Instant> findEarliestRecordedAt();

    /**
     * Deletes events older than {@code cutoff}, except the newest one per asset before it,
     * so the room can still be rebuilt for any time after the cutoff.
     */
    @Modifying
    @Query("""
            delete from AssetEvent e
            where e.recordedAt < :cutoff
              and e.id not in (select max(e2.id) from AssetEvent e2
                               where e2.recordedAt < :cutoff
                               group by e2.assetId)
            """)
    int deleteOlderThanKeepingLatest(@Param("cutoff") Instant cutoff);
}
