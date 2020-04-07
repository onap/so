use requestdb;

ALTER TABLE infra_active_requests ADD COLUMN IF NOT EXISTS WORKFLOW_NAME varchar(200);
ALTER TABLE archived_infra_requests ADD COLUMN IF NOT EXISTS WORKFLOW_NAME varchar(200);

ALTER TABLE infra_active_requests ADD COLUMN IF NOT EXISTS OPERATION_NAME varchar(200);
ALTER TABLE archived_infra_requests ADD COLUMN IF NOT EXISTS OPERATION_NAME varchar(200);