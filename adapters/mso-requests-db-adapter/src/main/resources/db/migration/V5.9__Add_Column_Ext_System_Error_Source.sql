use requestdb;

ALTER TABLE infra_active_requests ADD COLUMN IF NOT EXISTS EXT_SYSTEM_ERROR_SOURCE varchar(80);
ALTER TABLE archived_infra_requests ADD COLUMN IF NOT EXISTS EXT_SYSTEM_ERROR_SOURCE varchar(80);