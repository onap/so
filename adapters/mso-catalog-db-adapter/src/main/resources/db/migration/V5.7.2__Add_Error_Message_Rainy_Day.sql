use catalogdb;
ALTER TABLE rainy_day_handler_macro ADD COLUMN IF NOT EXISTS REG_EX_ERROR_MESSAGE varchar(300) DEFAULT NULL;