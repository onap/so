use requestdb;

ALTER TABLE infra_active_requests ADD COLUMN IF NOT EXISTS ORIGINAL_REQUEST_ID varchar(45);
ALTER TABLE archived_infra_requests ADD COLUMN IF NOT EXISTS ORIGINAL_REQUEST_ID varchar(45);