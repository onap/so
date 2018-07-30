
USE catalogdb;

INSERT INTO vnf_components_recipe (VNF_COMPONENT_TYPE, ACTION, VERSION, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT, VF_MODULE_MODEL_UUID)
VALUES
('vfModule', 'deactivateAndCloudDelete', '1', 'Gr api recipe to soft delete vf-module', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT');

UPDATE vnf_components_recipe
SET description = 'Vnf api recipe to soft delete vf-module'
WHERE description = 'VID_DEFAULT vfModule soft delete';