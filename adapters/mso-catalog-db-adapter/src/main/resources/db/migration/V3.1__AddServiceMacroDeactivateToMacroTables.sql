USE catalogdb;

INSERT INTO northbound_request_ref_lookup (REQUEST_SCOPE, MACRO_ACTION, ACTION, IS_ALACARTE, MIN_API_VERSION, MAX_API_VERSION, IS_TOPLEVELFLOW) 
values ( 'Service', 'Service-Macro-Deactivate', 'deactivateInstance', '0', '7', '7', '1');

INSERT INTO orchestration_flow_reference (COMPOSITE_ACTION, SEQ_NO, FLOW_NAME, FLOW_VERSION, NB_REQ_REF_LOOKUP_ID)
values ( 'Service-Macro-Deactivate', '1', 'DeactivateServiceInstanceBB', '1', (SELECT id FROM northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Deactivate'));