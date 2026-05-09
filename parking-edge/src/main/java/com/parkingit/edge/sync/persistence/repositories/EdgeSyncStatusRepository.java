package com.parkingit.edge.sync.persistence.repositories;

import com.parkingit.edge.sync.persistence.entities.EdgeSyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EdgeSyncStatusRepository extends JpaRepository<EdgeSyncStatus, UUID> {

    /**
     * Obtener registros pendientes de sincronización ordenados por prioridad de reintento
     */
    @Query("""
        SELECT e FROM EdgeSyncStatus e 
        WHERE e.status IN ('PENDING', 'FAILED') 
        AND (e.nextRetryAt IS NULL OR e.nextRetryAt <= CURRENT_TIMESTAMP)
        ORDER BY CASE 
            WHEN e.nextRetryAt IS NULL THEN 0 
            ELSE 1 
        END, e.nextRetryAt ASC
    """)
    List<EdgeSyncStatus> findPendingSyncs();

    /**
     * Obtener registros por tipo de sincronización
     */
    List<EdgeSyncStatus> findBySyncTypeOrderByCreatedAtDesc(String syncType);

    /**
     * Obtener registros fallidos que pueden reintentar
     */
    @Query("""
        SELECT e FROM EdgeSyncStatus e 
        WHERE e.status = 'FAILED' 
        AND e.retryCount < :maxRetries
        ORDER BY e.nextRetryAt ASC
    """)
    List<EdgeSyncStatus> findRetriableFailed(@Param("maxRetries") int maxRetries);

    /**
     * Obtener por entidad ID
     */
    Optional<EdgeSyncStatus> findByEntityId(UUID entityId);

    /**
     * Obtener sincronizaciones recientes
     */
    @Query("""
        SELECT e FROM EdgeSyncStatus e 
        WHERE e.createdAt >= :since 
        ORDER BY e.createdAt DESC
    """)
    List<EdgeSyncStatus> findRecentSyncs(@Param("since") LocalDateTime since);

    /**
     * Contar por estado
     */
    long countByStatus(String status);

    /**
     * Contar pendientes
     */
    long countByStatusIn(List<String> statuses);

    /**
     * Obtener todos SYNCED para archivo
     */
    @Query("""
        SELECT e FROM EdgeSyncStatus e 
        WHERE e.status = 'SYNCED' 
        AND e.createdAt < :before
        ORDER BY e.createdAt ASC
    """)
    List<EdgeSyncStatus> findOldSynced(@Param("before") LocalDateTime before);
}

