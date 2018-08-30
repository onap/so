use catalogdb;

INSERT INTO building_block_detail (BUILDING_BLOCK_NAME, RESOURCE_TYPE, TARGET_ACTION)
VALUES
('ConfigurationScaleOutBB', 'VF_MODULE', 'CREATE'),
('GenericVnfHealthCheckBB', 'VF_MODULE', 'CREATE');

INSERT INTO northbound_request_ref_lookup(MACRO_ACTION, ACTION, REQUEST_SCOPE, IS_ALACARTE, IS_TOPLEVELFLOW, MIN_API_VERSION, MAX_API_VERSION) VALUES
('VFModule-ScaleOut', 'createInstance', 'VfModule', true, true, '7','7');

INSERT INTO orchestration_flow_reference(COMPOSITE_ACTION, SEQ_NO, FLOW_NAME, FLOW_VERSION, NB_REQ_REF_LOOKUP_ID) VALUES
('VFModule-ScaleOut', '1', 'GenericVnfHealthCheckBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-ScaleOut')),
('VFModule-ScaleOut', '2', 'AssignVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-ScaleOut')),
('VFModule-ScaleOut', '3', 'CreateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-ScaleOut')),
('VFModule-ScaleOut', '4', 'ActivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-ScaleOut')),
('VFModule-ScaleOut', '5', 'ConfigurationScaleOutBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-ScaleOut')),
('VFModule-ScaleOut', '6', 'GenericVnfHealthCheckBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-ScaleOut'));

INSERT INTO rainy_day_handler_macro (FLOW_NAME, SERVICE_TYPE, VNF_TYPE, ERROR_CODE, WORK_STEP, POLICY)
VALUES
('GenericVnfHealthCheckBB', '*', '*', '*', "*" , 'Retry'),
('ConfigurationScaleOutBB', '*', '*', '*', "*" , 'Retry');
