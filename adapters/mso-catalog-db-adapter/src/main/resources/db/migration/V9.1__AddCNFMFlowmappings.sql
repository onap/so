USE catalogdb;

INSERT INTO northbound_request_ref_lookup(MACRO_ACTION, ACTION, REQUEST_SCOPE, IS_ALACARTE, IS_TOPLEVELFLOW, MIN_API_VERSION, MAX_API_VERSION,cloud_owner,service_type) VALUES
('Cnf-Create', 'createInstance', 'Cnf', true, true, '1','1','DEFAULT', '*')

INSERT INTO orchestration_flow_reference(COMPOSITE_ACTION, SEQ_NO, FLOW_NAME, FLOW_VERSION, NB_REQ_REF_LOOKUP_ID) VALUES
('CNF-Create', '1', 'CnfInstantiateBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Cnf-Create'))

INSERT INTO building_block_detail (BUILDING_BLOCK_NAME, RESOURCE_TYPE, TARGET_ACTION) VALUES
('CnfInstantiateBB', 'CNF', 'ACTIVATE')
