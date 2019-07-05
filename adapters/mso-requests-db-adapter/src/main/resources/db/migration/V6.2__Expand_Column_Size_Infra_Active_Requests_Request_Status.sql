use requestdb;

ALTER TABLE infra_active_requests MODIFY COLUMN REQUEST_STATUS varchar(25) NULL;