USE catalogdb;

INSERT INTO northbound_request_ref_lookup(MACRO_ACTION, ACTION, REQUEST_SCOPE, IS_ALACARTE, MIN_API_VERSION, MAX_API_VERSION, IS_TOPLEVELFLOW) VALUES
('VFModule-DeactivateAndCloudDelete', 'deactivateAndCloudDelete', 'VfModule', true, '7','7', true);

INSERT INTO orchestration_flow_reference(COMPOSITE_ACTION, SEQ_NO, FLOW_NAME, FLOW_VERSION, NB_REQ_REF_LOOKUP_ID) VALUES
('VFModule-DeactivateAndCloudDelete', '1', 'DeactivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-DeactivateAndCloudDelete')),
('VFModule-DeactivateAndCloudDelete', '2', 'DeleteVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-DeactivateAndCloudDelete'));
