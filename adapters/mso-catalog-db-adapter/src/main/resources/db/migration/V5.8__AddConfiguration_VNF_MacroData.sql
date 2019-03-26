USE catalogdb;

INSERT INTO orchestration_flow_reference(COMPOSITE_ACTION, SEQ_NO, FLOW_NAME, FLOW_VERSION, NB_REQ_REF_LOOKUP_ID) VALUES
('Service-Macro-Assign', '6', 'ConfigAssignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Assign'));

UPDATE orchestration_flow_reference
SET FLOW_NAME = 'ConfigAssignVnfBB'
WHERE FLOW_NAME = 'CreateNetworkBB' AND COMPOSITE_ACTION = 'Service-Macro-Create';

UPDATE orchestration_flow_reference
SET FLOW_NAME = 'CreateNetworkBB'
WHERE FLOW_NAME = 'ActivateNetworkBB' AND COMPOSITE_ACTION = 'Service-Macro-Create';

UPDATE orchestration_flow_reference
SET FLOW_NAME = 'ActivateNetworkBB'
WHERE FLOW_NAME = 'CreateVolumeGroupBB' AND COMPOSITE_ACTION = 'Service-Macro-Create';

UPDATE orchestration_flow_reference
SET FLOW_NAME = 'CreateVolumeGroupBB'
WHERE FLOW_NAME = 'ActivateVolumeGroupBB' AND COMPOSITE_ACTION = 'Service-Macro-Create';

UPDATE orchestration_flow_reference
SET FLOW_NAME = 'ActivateVolumeGroupBB'
WHERE FLOW_NAME = 'CreateVfModuleBB' AND COMPOSITE_ACTION = 'Service-Macro-Create';

UPDATE orchestration_flow_reference
SET FLOW_NAME = 'CreateVfModuleBB'
WHERE FLOW_NAME = 'ActivateVfModuleBB' AND COMPOSITE_ACTION = 'Service-Macro-Create';

UPDATE orchestration_flow_reference
SET FLOW_NAME = 'ActivateVfModuleBB'
WHERE FLOW_NAME = 'ActivateVnfBB' AND COMPOSITE_ACTION = 'Service-Macro-Create';

UPDATE orchestration_flow_reference
SET FLOW_NAME = 'ConfigDeployVnfBB'
WHERE FLOW_NAME = 'ActivateNetworkCollectionBB' AND COMPOSITE_ACTION = 'Service-Macro-Create';

UPDATE orchestration_flow_reference
SET FLOW_NAME = 'ActivateVnfBB'
WHERE FLOW_NAME = 'ActivateServiceInstanceBB' AND COMPOSITE_ACTION = 'Service-Macro-Create';

INSERT INTO orchestration_flow_reference(COMPOSITE_ACTION, SEQ_NO, FLOW_NAME, FLOW_VERSION, NB_REQ_REF_LOOKUP_ID) VALUES
('Service-Macro-Create', '16', 'ActivateNetworkCollectionBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create'));

INSERT INTO orchestration_flow_reference(COMPOSITE_ACTION, SEQ_NO, FLOW_NAME, FLOW_VERSION, NB_REQ_REF_LOOKUP_ID) VALUES
('Service-Macro-Create', '17', 'ActivateServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create'));
