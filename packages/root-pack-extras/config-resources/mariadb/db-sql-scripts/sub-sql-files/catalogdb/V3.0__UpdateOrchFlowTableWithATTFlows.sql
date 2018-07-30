USE catalogdb;

UPDATE orchestration_flow_reference SET FLOW_NAME = 'AssignServiceInstanceATTBB' WHERE FLOW_NAME = 'AssignServiceInstanceBB';
UPDATE orchestration_flow_reference SET FLOW_NAME = 'ActivateServiceInstanceATTBB' WHERE FLOW_NAME = 'ActivateServiceInstanceBB';
UPDATE orchestration_flow_reference SET FLOW_NAME = 'DeactivateServiceInstanceATTBB' WHERE FLOW_NAME = 'DeactivateServiceInstanceBB';
UPDATE orchestration_flow_reference SET FLOW_NAME = 'UnassignServiceInstanceATTBB' WHERE FLOW_NAME = 'UnassignServiceInstanceBB';

UPDATE rainy_day_handler_macro SET FLOW_NAME = 'AssignServiceInstanceATTBB' WHERE FLOW_NAME = 'AssignServiceInstanceBB';
UPDATE rainy_day_handler_macro SET FLOW_NAME = 'ActivateServiceInstanceATTBB' WHERE FLOW_NAME = 'ActivateServiceInstanceBB';
UPDATE rainy_day_handler_macro SET FLOW_NAME = 'DeactivateServiceInstanceATTBB' WHERE FLOW_NAME = 'DeactivateServiceInstanceBB';
UPDATE rainy_day_handler_macro SET FLOW_NAME = 'UnassignServiceInstanceATTBB' WHERE FLOW_NAME = 'UnassignServiceInstanceBB';

UPDATE building_block_detail SET BUILDING_BLOCK_NAME = 'AssignServiceInstanceATTBB' WHERE BUILDING_BLOCK_NAME = 'AssignServiceInstanceBB';
UPDATE building_block_detail SET BUILDING_BLOCK_NAME = 'ActivateServiceInstanceATTBB' WHERE BUILDING_BLOCK_NAME = 'ActivateServiceInstanceBB';
UPDATE building_block_detail SET BUILDING_BLOCK_NAME = 'DeactivateServiceInstanceATTBB' WHERE BUILDING_BLOCK_NAME = 'DeactivateServiceInstanceBB';
UPDATE building_block_detail SET BUILDING_BLOCK_NAME = 'UnassignServiceInstanceATTBB' WHERE BUILDING_BLOCK_NAME = 'UnassignServiceInstanceBB';