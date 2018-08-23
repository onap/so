use catalogdb;

INSERT IGNORE INTO vnf_components_recipe (VNF_COMPONENT_TYPE, ACTION, VERSION, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT, VF_MODULE_MODEL_UUID)
VALUES
('vfModule', 'scaleOut', '1', 'Gr api recipe to scale out vfModule', '/mso/async/services/WorkflowActionBB', '180', 'GR-API-DEFAULT');               
