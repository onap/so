use catalogdb;

ALTER TABLE cloud_sites
    ADD COLUMN IF NOT EXISTS SUPPORT_FABRIC bit(1)
    NOT NULL DEFAULT 1
