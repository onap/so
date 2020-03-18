USE catalogdb;

ALTER TABLE `vnf_recipe`

INSERT INTO `vnf_recipe` (`NF_ROLE`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `RECIPE_TIMEOUT`) VALUES ('UNI-UNI', 'createInstance', '2.0', 'UNI-UNI create resource recipe', '/mso/async/services/CreateSDNCNetworkResource', 300000);
INSERT INTO `vnf_recipe` (`NF_ROLE`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `RECIPE_TIMEOUT`) VALUES ('UNI-ENNI', 'createInstance', '2.0', 'UNI-ENNI create resource recipe', '/mso/async/services/CreateSDNCNetworkResource', 300000);
INSERT INTO `vnf_recipe` (`NF_ROLE`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `RECIPE_TIMEOUT`) VALUES ('UNI-UNI', 'deleteInstance', '2.0', 'UNI-UNI delete resource recipe', '/mso/async/services/DeleteSDNCNetworkResource', 300000);
INSERT INTO `vnf_recipe` (`NF_ROLE`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `RECIPE_TIMEOUT`) VALUES ('UNI-ENNI', 'deleteInstance', '2.0', 'UNI-ENNI delete resource recipe', '/mso/async/services/DeleteSDNCNetworkResource', 300000);
