use catalogdb;

ALTER TABLE identity_services ADD COLUMN ADMIN_PROJECT_DOMAIN_NAME varchar(255) DEFAULT 'Default';