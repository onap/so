use requestdb;

ALTER TABLE infra_active_requests ADD INSTANCE_GROUP_ID varchar(45);
ALTER TABLE infra_active_requests ADD INSTANCE_GROUP_NAME varchar(200);

ALTER TABLE archived_infra_requests ADD INSTANCE_GROUP_ID varchar(45);
ALTER TABLE archived_infra_requests ADD INSTANCE_GROUP_NAME varchar(200);