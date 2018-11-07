use catalogdb;

UPDATE rainy_day_handler_macro SET POLICY = 'Rollback' WHERE FLOW_NAME = 'CreateNetworkBB';
UPDATE rainy_day_handler_macro SET POLICY = 'Rollback' WHERE FLOW_NAME = 'CreateNetworkCollectionBB';
UPDATE rainy_day_handler_macro SET POLICY = 'Rollback' WHERE FLOW_NAME = 'CreateVfModuleBB';
UPDATE rainy_day_handler_macro SET POLICY = 'Rollback' WHERE FLOW_NAME = 'CreateVolumeGroupBB';
UPDATE rainy_day_handler_macro SET POLICY = 'Abort' WHERE FLOW_NAME = 'DeleteNetworkBB';
UPDATE rainy_day_handler_macro SET POLICY = 'Abort' WHERE FLOW_NAME = 'DeleteNetworkCollectionBB';
UPDATE rainy_day_handler_macro SET POLICY = 'Abort' WHERE FLOW_NAME = 'DeleteVfModuleBB';
UPDATE rainy_day_handler_macro SET POLICY = 'Abort' WHERE FLOW_NAME = 'DeleteVolumeGroupBB';
UPDATE rainy_day_handler_macro SET POLICY = 'Abort' WHERE FLOW_NAME = 'UnassignNetwork1802BB';
UPDATE rainy_day_handler_macro SET POLICY = 'Abort' WHERE FLOW_NAME = 'UnassignNetworkBB';
UPDATE rainy_day_handler_macro SET POLICY = 'Abort' WHERE FLOW_NAME = 'UnassignServiceInstanceBB';
UPDATE rainy_day_handler_macro SET POLICY = 'Abort' WHERE FLOW_NAME = 'UnassignVfModuleBB';
UPDATE rainy_day_handler_macro SET POLICY = 'Abort' WHERE FLOW_NAME = 'UnassignVnfBB';
UPDATE rainy_day_handler_macro SET POLICY = 'Abort' WHERE FLOW_NAME = 'UnassignVolumeGroupBB';
UPDATE rainy_day_handler_macro SET POLICY = 'Abort' WHERE FLOW_NAME = 'UpdateNetworkBB';
UPDATE rainy_day_handler_macro SET POLICY = 'Abort' WHERE FLOW_NAME = 'VnfAdapterBB';