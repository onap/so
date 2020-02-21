use requestdb;

ALTER TABLE infra_active_requests ADD COLUMN IF NOT EXISTS TENANT_NAME varchar(200);
ALTER TABLE archived_infra_requests ADD COLUMN IF NOT EXISTS TENANT_NAME varchar(200);

ALTER TABLE infra_active_requests ADD COLUMN IF NOT EXISTS PRODUCT_FAMILY_NAME varchar(200);
ALTER TABLE archived_infra_requests ADD COLUMN IF NOT EXISTS PRODUCT_FAMILY_NAME varchar(200);