-- MySQL Workbench Synchronization
-- Generated: 2017-07-10 12:52
-- Model: New Model
-- Version: 1.0
-- Project: Name of the project
-- Author: mz1936

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

ALTER SCHEMA `mso_catalog`  DEFAULT CHARACTER SET latin1  DEFAULT COLLATE latin1_swedish_ci ;

ALTER TABLE `mso_catalog`.`heat_nested_template`
    DROP FOREIGN KEY `fk_heat_nested_template__child_heat_temp_uuid__heat_template1`;

ALTER TABLE `mso_catalog`.`heat_template_params`
    DROP FOREIGN KEY `fk_heat_template_params__heat_template1`;

ALTER TABLE `mso_catalog`.`service_recipe`
    DROP FOREIGN KEY `fk_service_recipe__service1`;

ALTER TABLE `mso_catalog`.`vf_module`
    DROP FOREIGN KEY `fk_vf_module__vol_heat_template_art_uuid__heat_template2`,
    DROP FOREIGN KEY `fk_vf_module__heat_template_art_uuid__heat_template1`;

ALTER TABLE `mso_catalog`.`vf_module_to_heat_files`
    DROP FOREIGN KEY `fk_vf_module_to_heat_files__heat_files__artifact_uuid1`,
    DROP FOREIGN KEY `fk_vf_module_to_heat_files__vf_module__model_uuid1`;

ALTER TABLE `mso_catalog`.`network_resource`
    DROP FOREIGN KEY `fk_network_resource__heat_template1`;

ALTER TABLE `mso_catalog`.`temp_network_heat_template_lookup`
    DROP FOREIGN KEY `fk_temp_network_heat_template_lookup__heat_template1`;

ALTER TABLE `mso_catalog`.`vf_module_customization`
    DROP FOREIGN KEY `fk_vf_module_customization__vol_env__heat_environment2`,
    DROP FOREIGN KEY `fk_vf_module_customization__heat_env__heat_environment1`;

ALTER TABLE `mso_catalog`.`heat_environment`
    MODIFY COLUMN `ARTIFACT_UUID` VARCHAR(200) NOT NULL FIRST,
    MODIFY COLUMN `BODY` LONGTEXT NOT NULL AFTER `DESCRIPTION`,
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (`ARTIFACT_UUID`);

ALTER TABLE `mso_catalog`.`heat_files`
    MODIFY COLUMN `ARTIFACT_UUID` VARCHAR(200) NOT NULL FIRST,
    MODIFY COLUMN `NAME` VARCHAR(200) NOT NULL AFTER `ARTIFACT_UUID`,
    MODIFY COLUMN `BODY` LONGTEXT NOT NULL AFTER `DESCRIPTION`,
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (`ARTIFACT_UUID`);

ALTER TABLE `mso_catalog`.`heat_nested_template`
    MODIFY COLUMN `PARENT_HEAT_TEMPLATE_UUID` VARCHAR(200) NOT NULL FIRST,
    MODIFY COLUMN `CHILD_HEAT_TEMPLATE_UUID` VARCHAR(200) NOT NULL AFTER `PARENT_HEAT_TEMPLATE_UUID`,
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (`PARENT_HEAT_TEMPLATE_UUID`, `CHILD_HEAT_TEMPLATE_UUID`),
    DROP INDEX `fk_heat_nested_template__heat_template2_idx`,
    ADD INDEX `fk_heat_nested_template__heat_template2_idx` (`CHILD_HEAT_TEMPLATE_UUID` ASC);

ALTER TABLE `mso_catalog`.`heat_template`
    MODIFY COLUMN `ARTIFACT_UUID` VARCHAR(200) NOT NULL FIRST,
    MODIFY COLUMN `NAME` VARCHAR(200) NOT NULL AFTER `ARTIFACT_UUID`,
    MODIFY COLUMN `BODY` LONGTEXT NOT NULL AFTER `DESCRIPTION`,
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (`ARTIFACT_UUID`);

ALTER TABLE `mso_catalog`.`heat_template_params`
    MODIFY COLUMN `HEAT_TEMPLATE_ARTIFACT_UUID` VARCHAR(200) NOT NULL FIRST,
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (`HEAT_TEMPLATE_ARTIFACT_UUID`, `PARAM_NAME`);

ALTER TABLE `mso_catalog`.`network_recipe`
    MODIFY COLUMN `MODEL_NAME` VARCHAR(20) NOT NULL AFTER `id`,
    DROP INDEX `UK_rl4f296i0p8lyokxveaiwkayi`,
    ADD UNIQUE INDEX `UK_rl4f296i0p8lyokxveaiwkayi` (`MODEL_NAME` ASC, `ACTION` ASC, `VERSION_STR` ASC);

ALTER TABLE `mso_catalog`.`service`
    ADD COLUMN `SERVICE_TYPE` VARCHAR(200) NULL DEFAULT NULL AFTER `DESCRIPTION`,
    ADD COLUMN `SERVICE_ROLE` VARCHAR(200) NULL DEFAULT NULL AFTER `SERVICE_TYPE`,
    MODIFY COLUMN `MODEL_UUID` VARCHAR(200) NOT NULL FIRST,
    MODIFY COLUMN `MODEL_NAME` VARCHAR(200) NOT NULL AFTER `MODEL_UUID`,
    MODIFY COLUMN `MODEL_VERSION` VARCHAR(20) NOT NULL AFTER `MODEL_INVARIANT_UUID`,
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (`MODEL_UUID`),
    ADD INDEX `fk_service__tosca_csar1_idx` (`TOSCA_CSAR_ARTIFACT_UUID` ASC),
    DROP INDEX `fk_service__tosca_csar1_idx`;

ALTER TABLE `mso_catalog`.`service_recipe`
    MODIFY COLUMN `SERVICE_MODEL_UUID` VARCHAR(200) NOT NULL AFTER `CREATION_TIMESTAMP`,
    DROP INDEX `fk_service_recipe__service1_idx`,
    ADD INDEX `fk_service_recipe__service1_idx` (`SERVICE_MODEL_UUID` ASC),
    DROP INDEX `UK_7fav5dkux2v8g9d2i5ymudlgc`,
    ADD UNIQUE INDEX `UK_7fav5dkux2v8g9d2i5ymudlgc` (`SERVICE_MODEL_UUID` ASC, `ACTION` ASC);

ALTER TABLE `mso_catalog`.`vf_module`
    MODIFY COLUMN `MODEL_UUID` VARCHAR(200) NOT NULL FIRST,
    MODIFY COLUMN `HEAT_TEMPLATE_ARTIFACT_UUID` VARCHAR(200) NULL DEFAULT NULL AFTER `IS_BASE`,
    MODIFY COLUMN `VOL_HEAT_TEMPLATE_ARTIFACT_UUID` VARCHAR(200) NULL DEFAULT NULL AFTER `HEAT_TEMPLATE_ARTIFACT_UUID`,
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (`MODEL_UUID`, `VNF_RESOURCE_MODEL_UUID`),
    ADD INDEX `fk_vf_module__heat_template_art_uuid__heat_template1_idx` (`HEAT_TEMPLATE_ARTIFACT_UUID` ASC),
    ADD INDEX `fk_vf_module__vol_heat_template_art_uuid__heat_template2_idx` (`VOL_HEAT_TEMPLATE_ARTIFACT_UUID` ASC),
    DROP INDEX `fk_vf_module__vol_heat_template_art_uuid__heat_template2_idx`,
    DROP INDEX `fk_vf_module__heat_template_art_uuid__heat_template1_idx`;

ALTER TABLE `mso_catalog`.`vf_module_to_heat_files`
    MODIFY COLUMN `VF_MODULE_MODEL_UUID` VARCHAR(200) NOT NULL FIRST,
    MODIFY COLUMN `HEAT_FILES_ARTIFACT_UUID` VARCHAR(200) NOT NULL AFTER `VF_MODULE_MODEL_UUID`,
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (`VF_MODULE_MODEL_UUID`, `HEAT_FILES_ARTIFACT_UUID`),
    DROP INDEX `fk_vf_module_to_heat_files__heat_files__artifact_uuid1_idx`,
    ADD INDEX `fk_vf_module_to_heat_files__heat_files__artifact_uuid1_idx` (`HEAT_FILES_ARTIFACT_UUID` ASC),
    COMMENT = '';

ALTER TABLE `mso_catalog`.`vnf_components_recipe`
    MODIFY COLUMN `VF_MODULE_MODEL_UUID` VARCHAR(200) NULL DEFAULT NULL AFTER `CREATION_TIMESTAMP`,
    CHANGE COLUMN `VERSION` `VERSION` VARCHAR(20) NOT NULL,
    DROP INDEX `UK_4dpdwddaaclhc11wxsb7h59ma`,
    ADD UNIQUE INDEX `UK_4dpdwddaaclhc11wxsb7h59ma` (`VF_MODULE_MODEL_UUID` ASC, `VNF_COMPONENT_TYPE` ASC, `ACTION` ASC, `VERSION` ASC);

ALTER TABLE `mso_catalog`.`vnf_resource`
    MODIFY COLUMN `MODEL_UUID` VARCHAR(200) NOT NULL FIRST,
    CHANGE COLUMN `DESCRIPTION` `DESCRIPTION` VARCHAR(1200) NULL DEFAULT NULL AFTER `TOSCA_NODE_TYPE`,
    CHANGE COLUMN `ORCHESTRATION_MODE` `ORCHESTRATION_MODE` VARCHAR(20) NOT NULL DEFAULT 'HEAT' AFTER `DESCRIPTION`,
    CHANGE COLUMN `AIC_VERSION_MIN` `AIC_VERSION_MIN` VARCHAR(20) NULL DEFAULT NULL AFTER `ORCHESTRATION_MODE`,
    CHANGE COLUMN `AIC_VERSION_MAX` `AIC_VERSION_MAX` VARCHAR(20) NULL DEFAULT NULL AFTER `AIC_VERSION_MIN`,
    CHANGE COLUMN `CREATION_TIMESTAMP` `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `AIC_VERSION_MAX`,
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (`MODEL_UUID`),
    DROP INDEX `fk_vnf_resource__heat_template1`,
    ADD INDEX `fk_vnf_resource__heat_template1_idx` (`HEAT_TEMPLATE_ARTIFACT_UUID` ASC);

ALTER TABLE `mso_catalog`.`allotted_resource_customization`
    MODIFY COLUMN `PROVIDING_SERVICE_MODEL_INVARIANT_UUID` VARCHAR(200) NULL DEFAULT NULL AFTER `MODEL_INSTANCE_NAME`,
    MODIFY COLUMN `TARGET_NETWORK_ROLE` VARCHAR(200) NULL DEFAULT NULL AFTER `PROVIDING_SERVICE_MODEL_INVARIANT_UUID`,
    MODIFY COLUMN `NF_NAMING_CODE` VARCHAR(200) NULL DEFAULT NULL AFTER `NF_FUNCTION`,
    CHANGE COLUMN `CREATION_TIMESTAMP` `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `MAX_INSTANCES`;

ALTER TABLE `mso_catalog`.`vnf_resource_customization`
    MODIFY COLUMN `NF_NAMING_CODE` VARCHAR(200) NULL DEFAULT NULL AFTER `NF_FUNCTION`;

ALTER TABLE `mso_catalog`.`network_resource`
    CHANGE COLUMN `NEUTRON_NETWORK_TYPE` `NEUTRON_NETWORK_TYPE` VARCHAR(20) NULL DEFAULT NULL AFTER `TOSCA_NODE_TYPE`,
    CHANGE COLUMN `DESCRIPTION` `DESCRIPTION` VARCHAR(1200) NULL DEFAULT NULL AFTER `NEUTRON_NETWORK_TYPE`,
    CHANGE COLUMN `HEAT_TEMPLATE_ARTIFACT_UUID` `HEAT_TEMPLATE_ARTIFACT_UUID` VARCHAR(200) NOT NULL AFTER `CREATION_TIMESTAMP`,
    CHANGE COLUMN `MODEL_INVARIANT_UUID` `MODEL_INVARIANT_UUID` VARCHAR(200) NULL DEFAULT NULL;

ALTER TABLE `mso_catalog`.`temp_network_heat_template_lookup`
    ADD INDEX `fk_temp_network_heat_template_lookup__heat_template1_idx` (`HEAT_TEMPLATE_ARTIFACT_UUID` ASC),
    DROP INDEX `fk_temp_network_heat_template_lookup__heat_template1_idx`;

ALTER TABLE `mso_catalog`.`vf_module_customization`
    ADD INDEX `fk_vf_module_customization__heat_env__heat_environment1_idx` (`HEAT_ENVIRONMENT_ARTIFACT_UUID` ASC),
    ADD INDEX `fk_vf_module_customization__vol_env__heat_environment2_idx` (`VOL_ENVIRONMENT_ARTIFACT_UUID` ASC),
    DROP INDEX `fk_vf_module_customization__vol_env__heat_environment2_idx`,
    DROP INDEX `fk_vf_module_customization__heat_env__heat_environment1_idx`;

ALTER TABLE `mso_catalog`.`service_to_resource_customizations`
    DROP INDEX `fk_service_to_resource_cust__resource_model_customiz_uuid_idx`;

ALTER TABLE `mso_catalog`.`heat_nested_template`
    DROP FOREIGN KEY `fk_heat_nested_template__parent_heat_temp_uuid__heat_template1`;

ALTER TABLE `mso_catalog`.`heat_nested_template`
    ADD CONSTRAINT `fk_heat_nested_template__parent_heat_temp_uuid__heat_template1`
  FOREIGN KEY (`PARENT_HEAT_TEMPLATE_UUID`)
  REFERENCES `mso_catalog`.`heat_template` (`ARTIFACT_UUID`)
  ON DELETE CASCADE
  ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_heat_nested_template__child_heat_temp_uuid__heat_template1`
  FOREIGN KEY (`CHILD_HEAT_TEMPLATE_UUID`)
  REFERENCES `mso_catalog`.`heat_template` (`ARTIFACT_UUID`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

ALTER TABLE `mso_catalog`.`heat_template_params`
    ADD CONSTRAINT `fk_heat_template_params__heat_template1`
  FOREIGN KEY (`HEAT_TEMPLATE_ARTIFACT_UUID`)
  REFERENCES `mso_catalog`.`heat_template` (`ARTIFACT_UUID`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

ALTER TABLE `mso_catalog`.`service_recipe`
    ADD CONSTRAINT `fk_service_recipe__service1`
  FOREIGN KEY (`SERVICE_MODEL_UUID`)
  REFERENCES `mso_catalog`.`service` (`MODEL_UUID`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

ALTER TABLE `mso_catalog`.`vf_module`
    ADD CONSTRAINT `fk_vf_module__heat_template_art_uuid__heat_template1`
  FOREIGN KEY (`HEAT_TEMPLATE_ARTIFACT_UUID`)
  REFERENCES `mso_catalog`.`heat_template` (`ARTIFACT_UUID`)
  ON DELETE CASCADE
  ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_vf_module__vol_heat_template_art_uuid__heat_template2`
  FOREIGN KEY (`VOL_HEAT_TEMPLATE_ARTIFACT_UUID`)
  REFERENCES `mso_catalog`.`heat_template` (`ARTIFACT_UUID`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

ALTER TABLE `mso_catalog`.`vf_module_to_heat_files`
    ADD CONSTRAINT `fk_vf_module_to_heat_files__heat_files__artifact_uuid1`
  FOREIGN KEY (`HEAT_FILES_ARTIFACT_UUID`)
  REFERENCES `mso_catalog`.`heat_files` (`ARTIFACT_UUID`)
  ON DELETE CASCADE
  ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_vf_module_to_heat_files__vf_module__model_uuid1`
  FOREIGN KEY (`VF_MODULE_MODEL_UUID`)
  REFERENCES `mso_catalog`.`vf_module` (`MODEL_UUID`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

ALTER TABLE `mso_catalog`.`network_resource`
    ADD CONSTRAINT `fk_network_resource__heat_template1`
  FOREIGN KEY (`HEAT_TEMPLATE_ARTIFACT_UUID`)
  REFERENCES `mso_catalog`.`heat_template` (`ARTIFACT_UUID`)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

ALTER TABLE `mso_catalog`.`temp_network_heat_template_lookup`
    ADD CONSTRAINT `fk_temp_network_heat_template_lookup__heat_template1`
  FOREIGN KEY (`HEAT_TEMPLATE_ARTIFACT_UUID`)
  REFERENCES `mso_catalog`.`heat_template` (`ARTIFACT_UUID`)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

ALTER TABLE `mso_catalog`.`vf_module_customization`
    ADD CONSTRAINT `fk_vf_module_customization__heat_env__heat_environment1`
  FOREIGN KEY (`HEAT_ENVIRONMENT_ARTIFACT_UUID`)
  REFERENCES `mso_catalog`.`heat_environment` (`ARTIFACT_UUID`)
  ON DELETE CASCADE
  ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_vf_module_customization__vol_env__heat_environment2`
  FOREIGN KEY (`VOL_ENVIRONMENT_ARTIFACT_UUID`)
  REFERENCES `mso_catalog`.`heat_environment` (`ARTIFACT_UUID`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

INSERT INTO mso_catalog.SERVICE_RECIPE (ACTION, VERSION_STR, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT, SERVICE_MODEL_UUID)
VALUES ('activateInstance', '1.0', 'VID_DEFAULT activate', '/mso/async/services/ActivateGenericMacroService', 180, (SELECT model_uuid from mso_catalog.SERVICE where MODEL_NAME = 'VID_DEFAULT'));

INSERT INTO mso_catalog.SERVICE_RECIPE (ACTION, VERSION_STR, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT, SERVICE_MODEL_UUID)
VALUES ('deactivateInstance', '1.0', 'VID_DEFAULT deactivate', '/mso/async/services/DeactivateGenericMacroService', 180, (SELECT model_uuid from mso_catalog.SERVICE where MODEL_NAME = 'VID_DEFAULT'));

INSERT INTO mso_catalog.VNF_RECIPE(VNF_TYPE, ACTION, VERSION_STR, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT)
VALUES ('VID_DEFAULT', 'updateInstance', '1', 'VID_DEFAULT update', '/mso/async/services/UpdateVnfInfra', 180);

INSERT INTO mso_catalog.VNF_RECIPE(VNF_TYPE, ACTION, VERSION_STR, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT)
VALUES ('VID_DEFAULT', 'replaceInstance', '1', 'VID_DEFAULT replace', '/mso/async/services/ReplaceVnfInfra', 180);

INSERT INTO mso_catalog.VNF_COMPONENTS_RECIPE(VNF_COMPONENT_TYPE, ACTION, VERSION, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT, VF_MODULE_MODEL_UUID)
VALUES ('vfModule', 'replaceInstance', '1', 'VID_DEFAULT vfModule replace', '/mso/async/services/ReplaceVfModuleInfra', 180, 'VID_DEFAULT');

ALTER TABLE mso_requests.infra_active_requests modify LAST_MODIFIED_BY VARCHAR(100);

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
