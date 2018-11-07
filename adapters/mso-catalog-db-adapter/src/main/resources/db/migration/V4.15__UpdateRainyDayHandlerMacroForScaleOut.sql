use catalogdb;

UPDATE rainy_day_handler_macro SET POLICY = 'Abort' WHERE FLOW_NAME = 'GenericVnfHealthCheckBB';
UPDATE rainy_day_handler_macro SET POLICY = 'Abort' WHERE FLOW_NAME = 'ConfigurationScaleOutBB';