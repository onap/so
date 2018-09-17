use catalogdb;

INSERT INTO `vnf_recipe` (`NF_ROLE`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `RECIPE_TIMEOUT`) 
VALUES
('GR-API-DEFAULT', 'recreateInstance', '1', 'Gr api recipe to recreate vnf', '/mso/async/services/WorkflowActionBB', 180);