use requestdb;

ALTER TABLE infra_active_requests ADD COLUMN IF NOT EXISTS PNF_NAME varchar(45);
ALTER TABLE archived_infra_requests ADD COLUMN IF NOT EXISTS PNF_NAME varchar(45);
