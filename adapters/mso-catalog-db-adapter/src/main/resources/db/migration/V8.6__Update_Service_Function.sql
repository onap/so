use catalogdb;

ALTER TABLE service
    ADD COLUMN IF NOT EXISTS service_function varchar(200) DEFAULT NULL;