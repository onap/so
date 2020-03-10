USE catalogdb;

ALTER TABLE `vnf_recipe` 

INSERT INTO `vnf_recipe` (`NF_ROLE`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `RECIPE_TIMEOUT`) VALUES ('UNI-UNI', 'createInstance', '1.0', 'UNI-UNI resource recipe', '/mso/async/services/CreateSDNCNetworkResource', 300000);
INSERT INTO `vnf_recipe` (`NF_ROLE`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `RECIPE_TIMEOUT`) VALUES ('UNI-ENNI', 'createInstance', '1.0', 'UNI-ENNI resource recipe', '/mso/async/services/CreateSDNCNetworkResource', 300000);
