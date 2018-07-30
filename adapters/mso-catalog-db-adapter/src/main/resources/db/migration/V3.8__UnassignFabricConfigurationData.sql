use catalogdb;

INSERT INTO building_block_detail (building_block_name, resource_type, target_action)
VALUES
('UnassignFabricConfigurationBB', 'CONFIGURATION', 'UNASSIGN');

INSERT INTO orchestration_status_state_transition_directive(RESOURCE_TYPE, ORCHESTRATION_STATUS, TARGET_ACTION, FLOW_DIRECTIVE)
VALUES
('CONFIGURATION', 'PRECREATED', 'UNASSIGN', 'SILENT SUCCESS'),
('CONFIGURATION', 'INVENTORIED', 'UNASSIGN', 'CONTINUE'),
('CONFIGURATION', 'ASSIGNED', 'UNASSIGN', 'CONTINUE'),
('CONFIGURATION', 'CREATED', 'UNASSIGN', 'FAIL'),
('CONFIGURATION', 'ACTIVE', 'UNASSIGN', 'FAIL'),
('CONFIGURATION', 'PENDING CREATE', 'UNASSIGN', 'FAIL'),
('CONFIGURATION', 'PENDING DELETE', 'UNASSIGN', 'FAIL'),
('CONFIGURATION', 'PENDING ACTIVATION', 'UNASSIGN', 'FAIL'),
('CONFIGURATION', 'PENDING', 'UNASSIGN', 'FAIL');

UPDATE orchestration_flow_reference
SET SEQ_NO = SEQ_NO + 2 WHERE COMPOSITE_ACTION = 'SERVICE-MACRO-DELETE';

INSERT INTO orchestration_flow_reference(COMPOSITE_ACTION, SEQ_NO, FLOW_NAME, FLOW_VERSION, NB_REQ_REF_LOOKUP_ID) 
VALUES
('Service-Macro-Delete', '1', 'DeactivateFabricConfigurationBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '2', 'UnassignFabricConfigurationBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete'));

INSERT INTO rainy_day_handler_macro (FLOW_NAME, SERVICE_TYPE, VNF_TYPE, ERROR_CODE, WORK_STEP, POLICY)
VALUES
('UnassignFabricConfigurationBB', '*', '*', '*', '*' , 'Retry');


