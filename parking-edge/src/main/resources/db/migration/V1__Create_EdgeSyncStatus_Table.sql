-- ============ Tabla para rastrear estado de sincronización ============
CREATE TABLE IF NOT EXISTS edge_sync_status (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sync_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payload JSONB NOT NULL,
    last_attempt_at TIMESTAMP,
    next_retry_at TIMESTAMP,
    error_message TEXT,
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_edge_sync_status_pending
    ON edge_sync_status(status, next_retry_at)
    WHERE status IN ('PENDING', 'FAILED');

CREATE INDEX idx_edge_sync_status_entity
    ON edge_sync_status(entity_id, entity_type);

-- ============ Modificar tabla vehicle_entries existente para sincronización ============
ALTER TABLE vehicle_entries
ADD COLUMN IF NOT EXISTS synced_to_cloud BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS cloud_entity_id UUID,
ADD COLUMN IF NOT EXISTS sync_error_message TEXT;

CREATE INDEX IF NOT EXISTS idx_vehicle_entries_synced_to_cloud
    ON vehicle_entries(synced_to_cloud);

