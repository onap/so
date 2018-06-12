USE catalogdb;

UPDATE vnf_recipe
SET ORCHESTRATION_URI = '/mso/async/services/VnfInPlaceUpdate'
WHERE VNF_TYPE = 'GR-API-DEFAULT' AND ACTION = 'inPlaceSoftwareUpdate';

UPDATE vnf_recipe
SET ORCHESTRATION_URI = '/mso/async/services/VnfConfigUpdate'
WHERE VNF_TYPE = 'GR-API-DEFAULT' AND ACTION = 'applyUpdatedConfig';

UPDATE service
SET MODEL_UUID = 'd88da85c-d9e8-4f73-b837-3a72a431622b'
WHERE MODEL_UUID = 'DummyGRApiDefaultModelUUID?';

UPDATE service
SET MODEL_INVARIANT_UUID = '944862ae-bb65-4429-8330-a6c9170d6672'
WHERE MODEL_INVARIANT_UUID = 'DummyGRApiDefaultModelInvariantUUID?';

UPDATE service_recipe
SET SERVICE_MODEL_UUID = 'd88da85c-d9e8-4f73-b837-3a72a431622b'
WHERE SERVICE_MODEL_UUID = 'DummyGRApiDefaultModelUUID?';

INSERT INTO service_recipe (ACTION, VERSION_STR, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT, SERVICE_MODEL_UUID)
VALUES
('assignInstance', '1.0', 'Gr api recipe to assign service-instance', '/mso/async/services/WorkflowActionBB', 180, 'd88da85c-d9e8-4f73-b837-3a72a431622b'),
('unassignInstance', '1.0', 'Gr api recipe to unassign service-instance', '/mso/async/services/WorkflowActionBB', 180, 'd88da85c-d9e8-4f73-b837-3a72a431622b');

UPDATE service_recipe
SET DESCRIPTION = 'Vnf api recipe to assign service-instance'
WHERE ACTION = 'assignInstance' AND DESCRIPTION LIKE '%VID_DEFAULT%';

UPDATE service_recipe
SET DESCRIPTION = 'Vnf api recipe to unassign service-instance'
WHERE ACTION = 'unassignInstance' AND DESCRIPTION LIKE '%VID_DEFAULT%';