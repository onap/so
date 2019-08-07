use catalogdb;
ALTER TABLE rainy_day_handler_macro ADD COLUMN IF NOT EXISTS SERVICE_ROLE varchar(300) DEFAULT NULL;