use requestdb;

ALTER TABLE archived_infra_requests MODIFY COLUMN REQUEST_STATUS varchar(25) NULL;