USE catalogdb;

ALTER TABLE `vnf_recipe` 
CHANGE COLUMN `VNF_TYPE` `NF_ROLE` VARCHAR(200) NULL DEFAULT NULL ;

INSERT INTO `vnf_recipe` (`NF_ROLE`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `RECIPE_TIMEOUT`) 
VALUES ('vCE', 'replaceInstance', '1', 'custom bpmn for vCE recreate via POLO', '/mso/async/services/RecreateInfraVce', '180');

DELETE FROM `vnf_recipe` WHERE `NF_ROLE`='POLO_DEFAULT';