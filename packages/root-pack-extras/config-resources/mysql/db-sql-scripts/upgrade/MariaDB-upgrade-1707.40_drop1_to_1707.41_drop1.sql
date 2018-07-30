-- MySQL Workbench Synchronization <<<1
-- Generated: April 2017
-- MariaDB-upgrade-1707.40_drop1_to_1707.41_drop1.sql

-- Turn off validation and alter schema <<<1
BEGIN;

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

ALTER SCHEMA `mso_catalog`  DEFAULT CHARACTER SET latin1  DEFAULT COLLATE latin1_swedish_ci ;
-- >>>1

-- FOREIGN KEYS <<<1
ALTER TABLE `mso_catalog`.`heat_template`  --                                      K <<<2
DROP FOREIGN KEY `FK_ek5sot1q07taorbdmkvnveu98`;

ALTER TABLE `mso_catalog`.`heat_template_params`  --                               K <<<2
DROP FOREIGN KEY `FK_8sxvm215cw3tjfh3wni2y3myx`;

ALTER TABLE `mso_catalog`.`service_recipe`  --                                     K <<<2
DROP FOREIGN KEY `FK_kv13yx013qtqkn94d5gkwbu3s`;

ALTER TABLE `mso_catalog`.`network_resource_customization`  --                     K <<<2
DROP FOREIGN KEY `fk_network_resource_customization__network_resource__id`;
-- >>>1

UPDATE mso_catalog.heat_environment -- 7 UUID()                                    * <<<1
SET
	description = CONCAT(description, '1707MIGRATED'),
	asdc_uuid = (SELECT UUID())
WHERE
	asdc_uuid LIKE "MAN%" OR asdc_uuid is NULL OR asdc_uuid = '';

-- DEBUGGING E2E <<<1
-- ERROR 1062 (23000) at line 40: Duplicate entry '53a70d06-f598-4375-9c3c-fcca1dea3f51' for key 'PRIMARY'
DELETE FROM `mso_catalog`.`heat_environment` where `ASDC_UUID` IN ('53a70d06-f598-4375-9c3c-fcca1dea3f51', 'adc9f8d5-e9d2-4180-994d-cbd59d6eb405');
-- >>>1

-- heat_environment -                                                              * <<<1
CREATE TABLE `mso_catalog`.`hetemp` ( -- <<<2
	`id` int(11),
	`ARTIFACT_UUID` VARCHAR(200)
   ) ENGINE = InnoDB DEFAULT CHARACTER SET = latin1;

INSERT INTO mso_catalog.hetemp SELECT id, asdc_uuid artifact_uuid FROM mso_catalog.heat_environment; -- <<<2

ALTER TABLE `mso_catalog`.`heat_environment`  -- <<<2
DROP COLUMN `ASDC_LABEL`,
DROP COLUMN `ASDC_RESOURCE_NAME`,
DROP COLUMN `id`,
CHANGE COLUMN `ASDC_UUID` `ARTIFACT_UUID` VARCHAR(200) NOT NULL FIRST,
CHANGE COLUMN `ARTIFACT_CHECKSUM` `ARTIFACT_CHECKSUM` VARCHAR(200) NOT NULL DEFAULT 'MANUAL RECORD' AFTER `BODY`,
CHANGE COLUMN `ENVIRONMENT` `BODY` LONGTEXT NOT NULL ,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`ARTIFACT_UUID`),
DROP INDEX `UK_a4jkta7hgpa99brceaxasnfqp` ;
-- >>>1

UPDATE mso_catalog.heat_files -- 7 UUID()                                          * <<<1
SET
	description = CONCAT(description, '1707MIGRATED'),
	asdc_uuid = (SELECT UUID())
WHERE
	asdc_uuid LIKE "MAN%" OR asdc_uuid is NULL OR asdc_uuid = '';

ALTER TABLE `mso_catalog`.`heat_files`  --                                         ^ <<<1
MODIFY `id` INT,
DROP COLUMN `ASDC_RESOURCE_NAME`,
DROP COLUMN `ASDC_LABEL`,
DROP COLUMN `VNF_RESOURCE_ID`,
CHANGE COLUMN `ASDC_UUID` `ARTIFACT_UUID` VARCHAR(200) NOT NULL FIRST,
CHANGE COLUMN `FILE_NAME` `NAME` VARCHAR(200) NOT NULL AFTER `ARTIFACT_UUID`,
CHANGE COLUMN `VERSION` `VERSION` VARCHAR(20) NOT NULL AFTER `NAME`,
CHANGE COLUMN `ARTIFACT_CHECKSUM` `ARTIFACT_CHECKSUM` VARCHAR(200) NOT NULL DEFAULT 'MANUAL RECORD' AFTER `BODY`,
CHANGE COLUMN `FILE_BODY` `BODY` LONGTEXT NOT NULL ,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`ARTIFACT_UUID`),
DROP INDEX `UK_m23vfqc1tdvj7d6f0jjo4cl7e` ;

CREATE TABLE IF NOT EXISTS `mso_catalog`.`temp_network_heat_template_lookup` ( --  V <<<1
  `NETWORK_RESOURCE_MODEL_NAME` VARCHAR(200) NOT NULL,
  `HEAT_TEMPLATE_ARTIFACT_UUID` VARCHAR(200) NOT NULL,
  `AIC_VERSION_MIN` VARCHAR(20) NOT NULL,
  `AIC_VERSION_MAX` VARCHAR(20) NULL DEFAULT NULL,
  PRIMARY KEY (`NETWORK_RESOURCE_MODEL_NAME`),
  INDEX `fk_temp_network_heat_template_lookup__heat_template1_idx` (`HEAT_TEMPLATE_ARTIFACT_UUID` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = latin1;

UPDATE mso_catalog.heat_template -- 7 UUID()                                       V <<<1
SET
	description = CONCAT(description, '1707MIGRATED'),
	asdc_uuid = (SELECT UUID())
WHERE
	asdc_uuid LIKE "MAN%" OR asdc_uuid is NULL OR asdc_uuid = ''; 

-- delete where network_resource_model_name is CONTRAIL_EXTERNAL or CONTRAIL_SHARED. Q spec 5/25
INSERT INTO mso_catalog.temp_network_heat_template_lookup ( -- 3sc                 * b4 heat_template network_resource <<<1
	network_resource_model_name,
	heat_template_artifact_uuid,
	aic_version_min,
	aic_version_max
)
	SELECT 
		a.network_type,
		b.asdc_uuid,
		a.aic_version_min,
		a.aic_version_max
	FROM
		mso_catalog.network_resource a,
		mso_catalog.heat_template b
	WHERE
		a.template_id = b.id
		AND a.network_type NOT IN ('CONTRAIL_EXTERNAL', 'CONTRAIL_SHARED');

ALTER TABLE `mso_catalog`.`heat_template`  --                                      ^ <<<1
MODIFY `id` INT,
DROP COLUMN `ASDC_LABEL`,
DROP COLUMN `CHILD_TEMPLATE_ID`,
DROP COLUMN `TEMPLATE_PATH`,
DROP COLUMN `ASDC_RESOURCE_NAME`,
CHANGE COLUMN `ASDC_UUID` `ARTIFACT_UUID` VARCHAR(200) NOT NULL FIRST,
CHANGE COLUMN `DESCRIPTION` `DESCRIPTION` VARCHAR(1200) NULL DEFAULT NULL AFTER `VERSION`,
CHANGE COLUMN `ARTIFACT_CHECKSUM` `ARTIFACT_CHECKSUM` VARCHAR(200) NOT NULL DEFAULT 'MANUAL RECORD' AFTER `TIMEOUT_MINUTES`,
CHANGE COLUMN `TEMPLATE_NAME` `NAME` VARCHAR(200) NOT NULL ,
CHANGE COLUMN `TEMPLATE_BODY` `BODY` LONGTEXT NOT NULL ,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`ARTIFACT_UUID`),
DROP INDEX `FK_ek5sot1q07taorbdmkvnveu98` ,
DROP INDEX `UK_k1tq7vblss8ykiwhiltnkg6no` ;

ALTER TABLE `mso_catalog`.`temp_network_heat_template_lookup`  -- after alter heat_template            ^ <<<1
  ADD CONSTRAINT `fk_temp_network_heat_template_lookup__heat_template1`
    FOREIGN KEY (`HEAT_TEMPLATE_ARTIFACT_UUID`)
    REFERENCES `mso_catalog`.`heat_template` (`ARTIFACT_UUID`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE;
-- >>>1

-- heat_nested_template AFTER heat_template                                        * <<<1
CREATE TABLE `mso_catalog`.`hnttemp` ( -- <<<2
  `PARENT_HEAT_TEMPLATE_UUID` VARCHAR(200) NOT NULL ,
  `CHILD_HEAT_TEMPLATE_UUID` VARCHAR(200) NOT NULL,
  `PROVIDER_RESOURCE_FILE` varchar(100) DEFAULT NULL
   )    
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;
        
INSERT INTO mso_catalog.hnttemp ( -- <<<2
    PARENT_HEAT_TEMPLATE_UUID,
    CHILD_HEAT_TEMPLATE_UUID,
    PROVIDER_RESOURCE_FILE
)       
    SELECT
        ht1.artifact_uuid PARENT_HEAT_TEMPLATE_UUID,
        ht2.artifact_uuid CHILD_HEAT_TEMPLATE_UUID,
        a.PROVIDER_RESOURCE_FILE
        FROM 
        (SELECT * FROM mso_catalog.heat_nested_template) AS a
        JOIN (SELECT * FROM mso_catalog.heat_template) AS ht1 ON a.parent_template_id = ht1.id
        JOIN (SELECT * FROM mso_catalog.heat_template) AS ht2 ON a.child_template_id = ht2.id;

DELETE FROM mso_catalog.heat_nested_template; -- <<<2

ALTER TABLE `mso_catalog`.`heat_nested_template`  -- <<<2
CHANGE COLUMN `PARENT_TEMPLATE_ID` `PARENT_HEAT_TEMPLATE_UUID` VARCHAR(200) NOT NULL ,
CHANGE COLUMN `CHILD_TEMPLATE_ID` `CHILD_HEAT_TEMPLATE_UUID` VARCHAR(200) NOT NULL ,
ADD INDEX `fk_heat_nested_template__heat_template2_idx` (`CHILD_HEAT_TEMPLATE_UUID` ASC);

INSERT INTO mso_catalog.heat_nested_template SELECT * FROM mso_catalog.hnttemp; -- <<<2

DROP TABLE IF EXISTS mso_catalog.hnttemp; -- <<<2

-- heat_template_params  AFTER heat_template                                       ^ <<<1
CREATE TABLE IF NOT EXISTS `mso_catalog`.`htptemp` ( -- <<<2
  `PARAM_NAME` varchar(100) NOT NULL,
  `IS_REQUIRED` bit(1) NOT NULL,
  `PARAM_TYPE` varchar(20) DEFAULT NULL,
  `PARAM_ALIAS` varchar(45) DEFAULT NULL,
  `HEAT_TEMPLATE_ARTIFACT_UUID` VARCHAR(200) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO mso_catalog.htptemp ( -- <<<2
  PARAM_NAME,
  IS_REQUIRED,
  PARAM_TYPE,
  PARAM_ALIAS,
  HEAT_TEMPLATE_ARTIFACT_UUID
)
	SELECT
		a.PARAM_NAME,
		a.IS_REQUIRED,
		a.PARAM_TYPE,
		a.PARAM_ALIAS,
		ht1.artifact_uuid HEAT_TEMPLATE_ARTIFACT_UUID
		FROM
		(SELECT * FROM mso_catalog.heat_template_params) AS a
		JOIN (SELECT * FROM mso_catalog.heat_template) AS ht1 ON a.heat_template_id = ht1.id;

DELETE FROM mso_catalog.heat_template_params; -- <<<2

ALTER TABLE `mso_catalog`.`heat_template_params`  -- <<<2
DROP COLUMN `id`,
CHANGE COLUMN `HEAT_TEMPLATE_ID` `HEAT_TEMPLATE_ARTIFACT_UUID` VARCHAR(200) NOT NULL ,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`HEAT_TEMPLATE_ARTIFACT_UUID`, `PARAM_NAME`),
DROP INDEX `UK_pj3cwbmewecf0joqv2mvmbvw3` ;

INSERT INTO mso_catalog.heat_template_params ( -- <<<2
  PARAM_NAME,
  IS_REQUIRED,
  PARAM_TYPE,
  PARAM_ALIAS,
  HEAT_TEMPLATE_ARTIFACT_UUID
)
	SELECT
		a.PARAM_NAME,
		a.IS_REQUIRED,
		a.PARAM_TYPE,
		a.PARAM_ALIAS,
		a.HEAT_TEMPLATE_ARTIFACT_UUID
		FROM mso_catalog.htptemp a;

DROP TABLE IF EXISTS mso_catalog.htptemp; -- <<<2

-- >>>1

ALTER TABLE `mso_catalog`.`network_recipe`  -- <<<1
CHANGE COLUMN `NETWORK_TYPE` `MODEL_NAME` VARCHAR(20) NOT NULL ;

-- 1, 2 UPDATE SERVICE Before SERVICE                                              * <<<1
UPDATE `mso_catalog`.`service_recipe`
JOIN (
	SELECT 
		MAX(CAST((COALESCE(NULLIF(version_str, ''), '1.0')) AS DECIMAL(5,2))),
		id,
		service_name
	FROM mso_catalog.service
	WHERE service_name = "WAN Bonding" 
) a 
ON a.service_name = "WAN Bonding"
SET
	`service_id` = a.id, 
	`action` = CASE
		WHEN action = 'Layer3AddBonding' then 'createInstance'
		WHEN action = 'Layer3DeleteBonding' then 'deleteInstance'
	END
WHERE
	`action` IN ('Layer3AddBonding', 'Layer3DeleteBonding');

UPDATE mso_catalog.service -- 2 <<<2
SET
	service_name_version_id = (SELECT UUID()),
	description = CONCAT(description, '1707MIGRATED')
WHERE
	service_name_version_id LIKE "MAN%" OR service_name_version_id is NULL OR service_name_version_id = '';

UPDATE mso_catalog.service
SET
	model_invariant_uuid = (SELECT UUID()),
	description = CONCAT(description, '1707MIGRATED')
WHERE
	model_invariant_uuid LIKE 'MAN%' OR model_invariant_uuid is NULL OR model_invariant_uuid = '';

-- service - from temporary table servtemp                                         ^ <<<1
CREATE TABLE `mso_catalog`.`servtemp` ( -- <<<2
	`id` int(11),
	`MODEL_NAME` varchar(40) DEFAULT NULL,
	`MODEL_VERSION` varchar(20) NOT NULL,
	`DESCRIPTION` varchar(1200) DEFAULT NULL,
	`CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`MODEL_UUID` varchar(50) NOT NULL DEFAULT 'MANUAL_RECORD',
	`MODEL_INVARIANT_UUID` varchar(200) NOT NULL DEFAULT 'MANUAL_RECORD'
   ) ENGINE = InnoDB DEFAULT CHARACTER SET = latin1;

INSERT INTO mso_catalog.servtemp ( -- <<<2
  id,
  MODEL_NAME,
  MODEL_VERSION,
  DESCRIPTION,
  CREATION_TIMESTAMP,
  MODEL_UUID,
  MODEL_INVARIANT_UUID
)
	SELECT
	  id,
	  SERVICE_NAME,
	  VERSION_STR,
	  DESCRIPTION,
	  CREATION_TIMESTAMP,
	  SERVICE_NAME_VERSION_ID,
	  MODEL_INVARIANT_UUID
	FROM mso_catalog.service 
	WHERE SERVICE_NAME NOT IN ('Layer3AddBonding', 'Layer3DeleteBonding');
	
DELETE FROM mso_catalog.service; -- <<<2

ALTER  TABLE `mso_catalog`.`service_to_allotted_resources` -- <<<2
	DROP FOREIGN KEY `fk_service_to_allotted_resources__service__service_name_ver_id`;

ALTER  TABLE `mso_catalog`.`service_to_networks` -- <<<2
	DROP FOREIGN KEY `fk_service_to_networks__service__service_name_version_id`;

ALTER TABLE `mso_catalog`.`service`  --                                            ^ <<<2
MODIFY `id` INT,
DROP COLUMN `SERVICE_ID`,
DROP COLUMN `HTTP_METHOD`,
DROP COLUMN `SERVICE_NAME_VERSION_ID`,
ADD COLUMN `MODEL_UUID` VARCHAR(200) NOT NULL FIRST,
CHANGE COLUMN `MODEL_INVARIANT_UUID` `MODEL_INVARIANT_UUID` VARCHAR(200) NOT NULL AFTER `MODEL_NAME`,
CHANGE COLUMN `SERVICE_NAME` `MODEL_NAME` VARCHAR(200) NOT NULL ,
CHANGE COLUMN `VERSION_STR` `MODEL_VERSION` VARCHAR(20) NOT NULL ,
ADD COLUMN `TOSCA_CSAR_ARTIFACT_UUID` VARCHAR(200) NULL DEFAULT NULL AFTER `CREATION_TIMESTAMP`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`MODEL_UUID`),
ADD INDEX `fk_service__tosca_csar1_idx` (`TOSCA_CSAR_ARTIFACT_UUID` ASC),
DROP INDEX `UK_service_name__service_name_version_id` ;

INSERT INTO mso_catalog.service (
	id, CREATION_TIMESTAMP, DESCRIPTION, MODEL_INVARIANT_UUID, MODEL_NAME, MODEL_UUID, MODEL_VERSION
)
SELECT 
	id, CREATION_TIMESTAMP, DESCRIPTION, MODEL_INVARIANT_UUID, MODEL_NAME, MODEL_UUID, MODEL_VERSION
FROM mso_catalog.servtemp; -- >>>2 

DROP TABLE IF EXISTS mso_catalog.servtemp; -- <<<2

-- service_recipe - from temporary table srtemp - AFTER service                    ^ <<<1
CREATE TABLE `mso_catalog`.`srtemp` ( -- <<<2
  `id` int(11) NOT NULL ,
  `SERVICE_MODEL_UUID` VARCHAR(200) NOT NULL,
  `ACTION` varchar(40) NOT NULL,
  `VERSION_STR` varchar(20) DEFAULT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `ORCHESTRATION_URI` varchar(256) NOT NULL,
  `SERVICE_PARAM_XSD` varchar(2048) DEFAULT NULL,
  `RECIPE_TIMEOUT` int(11) DEFAULT NULL,
  `SERVICE_TIMEOUT_INTERIM` int(11) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
   )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

-- ST-CreationTimestamp <<<2
-- ERROR 1292 (22007) at line 331: Incorrect datetime value: '0000-00-00 00:00:00' for column 'CREATION_TIMESTAMP' at row 1
UPDATE `mso_catalog`.`service_recipe` set CREATION_TIMESTAMP = now() where cast(`CREATION_TIMESTAMP` as char(20)) = '0000-00-00 00:00:00';
-- >>>2

INSERT INTO mso_catalog.srtemp ( -- <<<2
  id,
  SERVICE_MODEL_UUID,
  ACTION,
  VERSION_STR,
  DESCRIPTION,
  ORCHESTRATION_URI,
  SERVICE_PARAM_XSD,
  RECIPE_TIMEOUT,
  SERVICE_TIMEOUT_INTERIM,
  CREATION_TIMESTAMP
)
	SELECT
		a.id,
		ht1.MODEL_UUID SERVICE_MODEL_UUID,
		a.ACTION,
		a.VERSION_STR,
		a.DESCRIPTION,
		a.ORCHESTRATION_URI,
		a.SERVICE_PARAM_XSD,
		a.RECIPE_TIMEOUT,
		a.SERVICE_TIMEOUT_INTERIM,
		a.CREATION_TIMESTAMP
		FROM mso_catalog.service_recipe a
		JOIN mso_catalog.service AS ht1 ON a.service_id = ht1.id;

DELETE FROM mso_catalog.service_recipe; -- <<<2

ALTER TABLE `mso_catalog`.`service_recipe`  -- <<<2
CHANGE COLUMN `SERVICE_ID` `SERVICE_MODEL_UUID` VARCHAR(200) NOT NULL AFTER `CREATION_TIMESTAMP`,
ADD INDEX `fk_service_recipe__service1_idx` (`SERVICE_MODEL_UUID` ASC);

INSERT INTO mso_catalog.service_recipe ( -- <<<2
  id,
  SERVICE_MODEL_UUID,
  ACTION,
  VERSION_STR,
  DESCRIPTION,
  ORCHESTRATION_URI,
  SERVICE_PARAM_XSD,
  RECIPE_TIMEOUT,
  SERVICE_TIMEOUT_INTERIM,
  CREATION_TIMESTAMP
)
SELECT 
  id,
  SERVICE_MODEL_UUID,
  ACTION,
  VERSION_STR,
  DESCRIPTION,
  ORCHESTRATION_URI,
  SERVICE_PARAM_XSD,
  RECIPE_TIMEOUT,
  SERVICE_TIMEOUT_INTERIM,
  CREATION_TIMESTAMP
 FROM mso_catalog.srtemp;

DROP TABLE IF EXISTS mso_catalog.srtemp; -- <<<2

-- >>>1

DELETE FROM mso_catalog.vnf_components_recipe WHERE vnf_component_type = 'VOLUME_GROUP' and vnf_type != '*'; -- Q spec 5/25 <<<1
-- >>>1

DELETE FROM mso_catalog.vnf_resource WHERE id IN (2,3,4); -- 3                     * <<<1

UPDATE mso_catalog.vnf_resource -- 4                                               * <<<1
SET
	model_name = model_customization_name,
	asdc_uuid = '09cb25b0-f2f6-40ed-96bc-71ad43e42fc8',
	model_invariant_uuid = '9fdda511-ffe3-4117-b3cc-cff9c1fc3fff'
WHERE
	id=5;

UPDATE mso_catalog.vnf_resource -- 6 set model_name                                * <<<1
SET
	model_name = vnf_type
WHERE
	service_model_invariant_uuid IS NULL OR model_invariant_uuid = '';

UPDATE mso_catalog.vnf_resource -- 7 UUID() asdc_uuid                              * <<<1
SET
	asdc_uuid = (SELECT UUID()),
	description = CONCAT(description, '1707MIGRATED')
WHERE
	asdc_uuid LIKE "MAN%" OR asdc_uuid is NULL OR asdc_uuid = '';

UPDATE mso_catalog.vnf_resource -- 8 UUID() model_customization_uuid               * <<<1
SET
	description = CONCAT(description, '1707MIGRATED'),
	model_customization_uuid = (SELECT UUID())
WHERE
	model_customization_uuid LIKE "MAN%" OR model_customization_uuid is NULL OR model_customization_uuid = ''; 

-- >>>1
UPDATE mso_catalog.vnf_resource -- NOT IN SPEC                                     * <<<1
SET
	model_customization_name = CONCAT('1707MIGRATED_', model_name)
WHERE
	model_customization_name is NULL OR model_customization_name = ''; 

-- 5 aka 8d delete each asdc_uuid except highest ASDC_SERVICE_MODEL_VERSION vnf_resource and cascade vf_module * <<<1
CREATE TABLE mso_catalog.req5temp (`vnfs` INT(11) NOT NULL, `vfs` INT(11));

-- delete VR and cascade VMs what have null/empty VR.service_model_invariant_uuid where vnf_name is NOT "BrocadeVce"
INSERT INTO mso_catalog.req5temp (vnfs, vfs) -- <<<2
	SELECT a.id, m.id
		FROM    mso_catalog.vnf_resource a
		LEFT JOIN mso_catalog.vf_module m ON a.id = m.vnf_resource_id
		WHERE (a.vnf_name != "BrocadeVce" OR a.vnf_name IS NULL) 
			AND (a.service_model_invariant_uuid is NULL OR a.service_model_invariant_uuid = '');

DELETE FROM mso_catalog.vnf_resource WHERE id = ANY(SELECT vnfs FROM mso_catalog.req5temp);
DELETE FROM mso_catalog.vf_module WHERE id = ANY(SELECT vfs FROM mso_catalog.req5temp);

DELETE FROM mso_catalog.req5temp; -- <<<2

INSERT INTO mso_catalog.req5temp (vnfs, vfs) -- <<<2
	SELECT a.id, m.id
		FROM mso_catalog.vnf_resource a
		LEFT JOIN mso_catalog.vf_module m ON a.id = m.vnf_resource_id
		JOIN (
			SELECT
				MAX(CAST((COALESCE(NULLIF(asdc_service_model_version, ''), '1.0')) AS DECIMAL(5,2))) AS v,
				asdc_uuid
			FROM mso_catalog.vnf_resource
			GROUP BY asdc_uuid
			) b
		ON
			a.asdc_uuid = b.asdc_uuid AND
			CAST((COALESCE(NULLIF(a.asdc_service_model_version, ''), '1.0')) AS DECIMAL(5,2)) != b.v;
-- >>>1 

UPDATE mso_catalog.vf_module --    7 UUID() asdc_uuid                              * <<<1
SET
	asdc_uuid = (SELECT UUID()),
	description = CONCAT(description, '1707MIGRATED')
WHERE
	asdc_uuid LIKE "MAN%" OR asdc_uuid is NULL OR asdc_uuid = '';

UPDATE mso_catalog.vf_module --    8 UUID() model_customization_uuid               * <<<1
SET
	description = CONCAT(description, '1707MIGRATED'),
	model_customization_uuid = (SELECT UUID())
WHERE
	model_customization_uuid LIKE "MAN%" OR model_customization_uuid is NULL OR model_customization_uuid = '';

--  VMC vf_module_customization                                                                           * <<<1
CREATE TABLE IF NOT EXISTS `mso_catalog`.`vf_module_customization` ( --            V <<<2
  `MODEL_CUSTOMIZATION_UUID` VARCHAR(200) NOT NULL,
  `LABEL` VARCHAR(200) NULL DEFAULT NULL,
  `INITIAL_COUNT` INT(11) NULL DEFAULT 0,
  `MIN_INSTANCES` INT(11) NULL DEFAULT 0,
  `MAX_INSTANCES` INT(11) NULL DEFAULT NULL,
  `AVAILABILITY_ZONE_COUNT` INT(11) NULL DEFAULT NULL,
  `HEAT_ENVIRONMENT_ARTIFACT_UUID` VARCHAR(200) NULL DEFAULT NULL,
  `VOL_ENVIRONMENT_ARTIFACT_UUID` VARCHAR(200) NULL DEFAULT NULL,
  `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `VF_MODULE_MODEL_UUID` VARCHAR(200) NOT NULL,
  PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`),
  INDEX `fk_vf_module_customization__vf_module1_idx` (`VF_MODULE_MODEL_UUID` ASC),
  INDEX `fk_vf_module_customization__heat_env__heat_environment1_idx` (`HEAT_ENVIRONMENT_ARTIFACT_UUID` ASC),
  INDEX `fk_vf_module_customization__vol_env__heat_environment2_idx` (`VOL_ENVIRONMENT_ARTIFACT_UUID` ASC),
  CONSTRAINT `fk_vf_module_customization__heat_env__heat_environment1`
    FOREIGN KEY (`HEAT_ENVIRONMENT_ARTIFACT_UUID`)
    REFERENCES `mso_catalog`.`heat_environment` (`ARTIFACT_UUID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_vf_module_customization__vol_env__heat_environment2`
    FOREIGN KEY (`VOL_ENVIRONMENT_ARTIFACT_UUID`)
    REFERENCES `mso_catalog`.`heat_environment` (`ARTIFACT_UUID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = latin1;

CREATE TABLE mso_catalog.vfduptemp (`id` INT(11) NOT NULL); -- <<<2

INSERT INTO mso_catalog.vfduptemp (id) -- <<<2
SELECT a.id
FROM mso_catalog.vf_module a
JOIN (
		SELECT
			MAX(CAST((COALESCE(NULLIF(asdc_service_model_version, ''), '1.0')) AS DECIMAL(5,2))) AS ver,
			model_customization_uuid mcu,
			id vid
		FROM mso_catalog.vf_module
		GROUP BY model_customization_uuid
		) b
ON
		a.model_customization_uuid = mcu
		AND CAST((COALESCE(NULLIF(a.asdc_service_model_version, ''), '1.0')) AS DECIMAL(5,2)) != b.ver
ORDER BY a.model_customization_uuid;

INSERT INTO mso_catalog.vf_module_customization ( -- <<<2
		model_customization_uuid, -- <<<3
		label,
		initial_count,
		min_instances,
		max_instances,
		heat_environment_artifact_uuid,
		vol_environment_artifact_uuid,
		vf_module_model_uuid -- >>>3
)
SELECT 
		a.model_customization_uuid,
		a.label,
		a.initial_count,
		a.min_instances,
		a.max_instances,
		ht1.artifact_uuid,
		ht2.artifact_uuid,
		a.asdc_uuid
FROM mso_catalog.vf_module a
LEFT JOIN mso_catalog.hetemp AS ht1 ON a.environment_id = ht1.id
LEFT JOIN mso_catalog.hetemp AS ht2 ON a.vol_environment_id = ht2.id
WHERE NOT EXISTS (
		SELECT 1 FROM mso_catalog.vfduptemp vdt
		WHERE
		a.id = vdt.id
);

DROP TABLE IF EXISTS mso_catalog.vfduptemp; -- <<<2

DROP TABLE IF EXISTS mso_catalog.hetemp; -- <<<2

-- >>>1

-- AR ALLOTTED_RESOURCE <<<1
CREATE TABLE IF NOT EXISTS `mso_catalog`.`allotted_resource` ( --                  V <<<2
  `MODEL_UUID` VARCHAR(200) NOT NULL,
  `MODEL_INVARIANT_UUID` VARCHAR(200) NOT NULL,
  `MODEL_VERSION` VARCHAR(20) NOT NULL,
  `MODEL_NAME` VARCHAR(200) NOT NULL,
  `TOSCA_NODE_TYPE` VARCHAR(200) NULL DEFAULT NULL,
  `SUBCATEGORY` VARCHAR(200) NULL DEFAULT NULL,
  `DESCRIPTION` VARCHAR(1200) NULL DEFAULT NULL,
  `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`MODEL_UUID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

 INSERT INTO `mso_catalog`.`allotted_resource`  ( -- 2sc                            * <<<2
 		model_uuid,
 		model_invariant_uuid,
 		model_version,
 		model_name,
 		description
 )
	SELECT DISTINCT
		model_uuid,
		model_invariant_uuid,
		model_version,
		model_name,
		description
 	FROM
 		mso_catalog.allotted_resource_customization;
-- >>>1

ALTER TABLE `mso_catalog`.`allotted_resource_customization`  --                    ^ <<<1
DROP COLUMN `DESCRIPTION`,
DROP COLUMN `MODEL_NAME`,
DROP COLUMN `MODEL_VERSION`,
DROP COLUMN `MODEL_INVARIANT_UUID`,
CHANGE COLUMN `MODEL_UUID` `AR_MODEL_UUID` VARCHAR(200) NOT NULL, -- ARC
CHANGE COLUMN `MODEL_INSTANCE_NAME` `MODEL_INSTANCE_NAME` VARCHAR(200) NOT NULL AFTER `MODEL_CUSTOMIZATION_UUID`,
ADD COLUMN `PROVIDING_SERVICE_MODEL_INVARIANT_UUID` VARCHAR(200) NULL DEFAULT NULL AFTER `MODEL_INSTANCE_NAME`,
ADD COLUMN `TARGET_NETWORK_ROLE` VARCHAR(200) NULL DEFAULT NULL AFTER `PROVIDING_SERVICE_MODEL_INVARIANT_UUID`,
ADD COLUMN `NF_TYPE` VARCHAR(200) NULL DEFAULT NULL AFTER `TARGET_NETWORK_ROLE`,
ADD COLUMN `NF_ROLE` VARCHAR(200) NULL DEFAULT NULL AFTER `NF_TYPE`,
ADD COLUMN `NF_FUNCTION` VARCHAR(200) NULL DEFAULT NULL AFTER `NF_ROLE`,
ADD COLUMN `NF_NAMING_CODE` VARCHAR(200) NULL DEFAULT NULL AFTER `NF_FUNCTION`,
ADD COLUMN `MIN_INSTANCES` INT(11) NULL DEFAULT NULL AFTER `NF_NAMING_CODE`,
ADD COLUMN `MAX_INSTANCES` INT(11) NULL DEFAULT NULL AFTER `MIN_INSTANCES`,
ADD INDEX `fk_allotted_resource_customization__allotted_resource1_idx` (`AR_MODEL_UUID` ASC);
-- >>>1

-- VRC  vnf_resource_customization <<<1
-- vnftemp table <<<2
CREATE TABLE `mso_catalog`.`vnftemp` AS
	SELECT model_customization_uuid, service_model_invariant_uuid, asdc_service_model_version
	FROM `mso_catalog`.`vnf_resource`;

DROP TABLE IF EXISTS `mso_catalog`.`vnf_resource_customization`; -- <<<2

CREATE TABLE `mso_catalog`.`vnf_resource_customization` ( -- <<<2
  `MODEL_CUSTOMIZATION_UUID` VARCHAR(200) NOT NULL,
  `MODEL_INSTANCE_NAME` VARCHAR(200) NOT NULL,
  `MIN_INSTANCES` INT(11) NULL DEFAULT NULL,
  `MAX_INSTANCES` INT(11) NULL DEFAULT NULL,
  `AVAILABILITY_ZONE_MAX_COUNT` INT(11) NULL DEFAULT NULL,
  `NF_TYPE` VARCHAR(200) NULL DEFAULT NULL,
  `NF_ROLE` VARCHAR(200) NULL DEFAULT NULL,
  `NF_FUNCTION` VARCHAR(200) NULL DEFAULT NULL,
  `NF_NAMING_CODE` VARCHAR(200) NULL DEFAULT NULL,
  `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `VNF_RESOURCE_MODEL_UUID` VARCHAR(200) NOT NULL,
  PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`),
  INDEX `fk_vnf_resource_customization__vnf_resource1_idx` (`VNF_RESOURCE_MODEL_UUID` ASC)
) ENGINE = InnoDB DEFAULT CHARACTER SET = latin1;

INSERT INTO mso_catalog.vnf_resource_customization ( -- <<<2
	model_customization_uuid,
	model_instance_name,
	vnf_resource_model_uuid
)
	SELECT DISTINCT
		a.model_customization_uuid,
		ht1.model_customization_name,
		ht1.asdc_uuid
	FROM mso_catalog.vnftemp a
	JOIN mso_catalog.vnf_resource AS ht1 ON
		a.model_customization_uuid = ht1.model_customization_uuid AND
		a.asdc_service_model_version = ht1.asdc_service_model_version;
-- >>>1

-- network_resource_customization                                                  * <<<1
CREATE TABLE `mso_catalog`.`nrctemp` ( -- <<<2
  `MODEL_UUID` varchar(200) NOT NULL,
  `MODEL_NAME` varchar(200) NOT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) NOT NULL,
  `NETWORK_RESOURCE_ID` int(11) NOT NULL,
  `MODEL_CUSTOMIZATION_UUID` VARCHAR(200) NOT NULL,
  `MODEL_INSTANCE_NAME` VARCHAR(200) NOT NULL,
  `NETWORK_TECHNOLOGY` VARCHAR(45) NULL,
  `NETWORK_TYPE` VARCHAR(45) NULL,
  `NETWORK_ROLE` VARCHAR(200) NULL,
  `NETWORK_SCOPE` VARCHAR(45) NULL,
  `MODEL_VERSION` VARCHAR(20) NULL,
  `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `NETWORK_RESOURCE_MODEL_UUID` VARCHAR(200) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO mso_catalog.nrctemp ( -- <<<2
	model_customization_uuid,
	model_uuid,
	model_invariant_uuid,
	model_instance_name,
	model_name,
	network_resource_id,
	model_version,
	creation_timestamp,
	network_resource_model_uuid
)
	SELECT 
		model_customization_uuid,
		model_uuid,
		model_invariant_uuid,
		model_instance_name,
		model_name,
		network_resource_id,
		model_version,
		creation_timestamp,
		model_uuid
	FROM mso_catalog.network_resource_customization;

DELETE FROM mso_catalog.network_resource_customization; -- <<<2

ALTER TABLE `mso_catalog`.`network_resource_customization`  -- <<<2
DROP COLUMN `NETWORK_RESOURCE_ID`,
DROP COLUMN `MODEL_VERSION`,
DROP COLUMN `MODEL_INVARIANT_UUID`,
DROP COLUMN `MODEL_NAME`,
DROP COLUMN `MODEL_UUID`,
ADD COLUMN `NETWORK_TECHNOLOGY` VARCHAR(45) NULL DEFAULT NULL AFTER `MODEL_INSTANCE_NAME`,
ADD COLUMN `NETWORK_TYPE` VARCHAR(45) NULL DEFAULT NULL AFTER `NETWORK_TECHNOLOGY`,
ADD COLUMN `NETWORK_ROLE` VARCHAR(200) NULL DEFAULT NULL AFTER `NETWORK_TYPE`,
ADD COLUMN `NETWORK_SCOPE` VARCHAR(45) NULL DEFAULT NULL AFTER `NETWORK_ROLE`,
ADD COLUMN `NETWORK_RESOURCE_MODEL_UUID` VARCHAR(200) NOT NULL AFTER `CREATION_TIMESTAMP`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`),
ADD INDEX `fk_network_resource_customization__network_resource1_idx` (`NETWORK_RESOURCE_MODEL_UUID` ASC),
DROP INDEX `fk_network_resource_customization__network_resource_id_idx`;
-- >>>2

INSERT INTO mso_catalog.network_resource_customization ( -- <<<2
	model_customization_uuid,
	model_instance_name,
	creation_timestamp,
	network_resource_model_uuid,
	network_type
)
	SELECT 
		a.model_customization_uuid,
		a.model_instance_name,
		a.creation_timestamp,
		a.model_uuid,
		a.network_type
	FROM mso_catalog.nrctemp a;

-- DROP temp table later, after network_resource uses it <<<2

-- >>>1

-- network_resource                                                                * <<<1
CREATE TABLE `mso_catalog`.`nrtemp` ( -- <<<2
  `MODEL_NAME` VARCHAR(200) NOT NULL,
  `ORCHESTRATION_MODE` varchar(20) DEFAULT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `NEUTRON_NETWORK_TYPE` varchar(20) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `MODEL_VERSION` VARCHAR(20) NULL DEFAULT NULL,
  `AIC_VERSION_MIN` varchar(20) NOT NULL,
  `AIC_VERSION_MAX` varchar(20) DEFAULT NULL,
  `MODEL_INVARIANT_UUID` VARCHAR(200) NULL DEFAULT NULL,
  `TOSCA_NODE_TYPE` VARCHAR(200) NULL DEFAULT NULL,
  `TEMPLATE_ID` VARCHAR(200)
   )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

-- E2E-CreationTimestamp <<<2
-- ERROR 1292 (22007) at line 675: Incorrect datetime value: '0000-00-00 00:00:00' for column 'CREATION_TIMESTAMP' at row 1
UPDATE `mso_catalog`.`network_resource` set CREATION_TIMESTAMP = now() where cast(`CREATION_TIMESTAMP` as char(20)) = '0000-00-00 00:00:00';
-- >>>2

INSERT INTO mso_catalog.nrtemp ( -- <<<2
  MODEL_NAME,
  ORCHESTRATION_MODE,
  DESCRIPTION,
  NEUTRON_NETWORK_TYPE,
  CREATION_TIMESTAMP,
  MODEL_VERSION,
  AIC_VERSION_MIN,
  AIC_VERSION_MAX,
  TEMPLATE_ID
)
	SELECT
		NETWORK_TYPE,
		ORCHESTRATION_MODE,
		DESCRIPTION,
		NEUTRON_NETWORK_TYPE,
		CREATION_TIMESTAMP,
		VERSION_STR,
		AIC_VERSION_MIN,
		AIC_VERSION_MAX,
		TEMPLATE_ID
	FROM mso_catalog.network_resource;

DELETE FROM mso_catalog.network_resource; -- <<<2

ALTER TABLE `mso_catalog`.`network_resource`  -- <<<2
DROP COLUMN `id`,
CHANGE COLUMN `VERSION_STR` `MODEL_VERSION` VARCHAR(20) NULL DEFAULT NULL,
CHANGE COLUMN `TEMPLATE_ID` `HEAT_TEMPLATE_ARTIFACT_UUID` VARCHAR(200) NOT NULL,
CHANGE COLUMN `NETWORK_TYPE` `MODEL_NAME` VARCHAR(200) NOT NULL,
CHANGE COLUMN `NEUTRON_NETWORK_TYPE` `NEUTRON_NETWORK_TYPE` VARCHAR(20) NULL DEFAULT NULL,
CHANGE COLUMN `ORCHESTRATION_MODE` `ORCHESTRATION_MODE` VARCHAR(20) NULL DEFAULT 'HEAT' AFTER `AIC_VERSION_MAX`,
CHANGE COLUMN `CREATION_TIMESTAMP` `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `ORCHESTRATION_MODE`,
ADD COLUMN `MODEL_UUID` VARCHAR(200) NOT NULL FIRST,
ADD COLUMN `MODEL_INVARIANT_UUID` VARCHAR(200) NULL DEFAULT NULL AFTER `MODEL_NAME`,
ADD COLUMN `TOSCA_NODE_TYPE` VARCHAR(200) NULL DEFAULT NULL AFTER `MODEL_VERSION`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`MODEL_UUID`),
ADD INDEX `fk_network_resource__temp_network_heat_template_lookup1_idx` (`MODEL_NAME` ASC),
ADD INDEX `fk_network_resource__heat_template1_idx` (`HEAT_TEMPLATE_ARTIFACT_UUID` ASC),
DROP INDEX `UK_e5vlpk2xorqk7ogtg6wgw2eo6` ;

INSERT INTO mso_catalog.network_resource ( -- <<<2
  model_name,
  orchestration_mode,
  description,
  heat_template_artifact_uuid,
  neutron_network_type,
  creation_timestamp,
  model_version,
  aic_version_min,
  aic_version_max,
  model_uuid,
  model_invariant_uuid
)
	SELECT DISTINCT
		ht2.model_name,
		a.ORCHESTRATION_MODE,
		a.DESCRIPTION,
		ht1.ARTIFACT_UUID,
		a.NEUTRON_NETWORK_TYPE,
		a.CREATION_TIMESTAMP,
		ht2.model_version,
		a.AIC_VERSION_MIN,
		a.AIC_VERSION_MAX,
		ht2.model_uuid,
		ht2.model_invariant_uuid
	FROM mso_catalog.nrtemp a
		JOIN mso_catalog.heat_template ht1 ON a.template_id = ht1.id
		JOIN mso_catalog.nrctemp ht2 ON a.model_name = ht2.model_name
		GROUP BY a.model_name;

DROP TABLE IF EXISTS mso_catalog.nrtemp; -- <<<2

DROP TABLE IF EXISTS mso_catalog.nrctemp; -- <<<2

-- >>>1

-- VRC2VMC vnf_res_custom_to_vf_module_custom <<<1
CREATE TABLE IF NOT EXISTS `mso_catalog`.`vnf_res_custom_to_vf_module_custom` ( -- <<<2
  `VNF_RESOURCE_CUST_MODEL_CUSTOMIZATION_UUID` VARCHAR(200) NOT NULL,
  `VF_MODULE_CUST_MODEL_CUSTOMIZATION_UUID` VARCHAR(200) NOT NULL,
  `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`VNF_RESOURCE_CUST_MODEL_CUSTOMIZATION_UUID`, `VF_MODULE_CUST_MODEL_CUSTOMIZATION_UUID`),
  INDEX `fk_vnf_res_custom_to_vf_module_custom__vf_module_customizat_idx` (`VF_MODULE_CUST_MODEL_CUSTOMIZATION_UUID` ASC),
  CONSTRAINT `fk_vnf_res_custom_to_vf_module_custom__vf_module_customization1`
    FOREIGN KEY (`VF_MODULE_CUST_MODEL_CUSTOMIZATION_UUID`)
    REFERENCES `mso_catalog`.`vf_module_customization` (`MODEL_CUSTOMIZATION_UUID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_vnf_res_custom_to_vf_module_custom__vnf_resource_customiza1`
    FOREIGN KEY (`VNF_RESOURCE_CUST_MODEL_CUSTOMIZATION_UUID`)
    REFERENCES `mso_catalog`.`vnf_resource_customization` (`MODEL_CUSTOMIZATION_UUID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

INSERT INTO mso_catalog.vnf_res_custom_to_vf_module_custom ( -- 6sc aka 8c         <<<2
	vnf_resource_cust_model_customization_uuid,
	vf_module_cust_model_customization_uuid,
	creation_timestamp
)
	SELECT DISTINCT
		a.model_customization_uuid,
		b.model_customization_uuid,
		now()
	FROM
		mso_catalog.vnf_resource a,
		mso_catalog.vf_module b
	WHERE a.id = b.vnf_resource_id;
-- >>>1

-- VR vnf_resource After vrc2vmc and vrc                                              ^ <<<1
-- ERROR 1292 (22007) : Incorrect datetime value: '0000-00-00 00:00:00' for column 'CREATION_TIMESTAMP' <<<2
UPDATE `mso_catalog`.`vnf_resource` set CREATION_TIMESTAMP = now() where cast(`CREATION_TIMESTAMP` as char(20)) = '0000-00-00 00:00:00';

ALTER TABLE `mso_catalog`.`vnf_resource`  -- after vrc2vmc and vrc                 ^ <<<2
MODIFY `id` INT,
DROP COLUMN `MODEL_CUSTOMIZATION_UUID`,
DROP COLUMN `SERVICE_MODEL_INVARIANT_UUID`,
DROP COLUMN `MODEL_CUSTOMIZATION_NAME`,
DROP COLUMN `VNF_TYPE`,
DROP COLUMN `ASDC_SERVICE_MODEL_VERSION`,
DROP COLUMN `ENVIRONMENT_ID`,
DROP COLUMN `VERSION`,
DROP COLUMN `VNF_NAME`,
CHANGE COLUMN `DESCRIPTION` `DESCRIPTION` VARCHAR(1200) NULL DEFAULT NULL,
CHANGE COLUMN `ORCHESTRATION_MODE` `ORCHESTRATION_MODE` VARCHAR(20) NOT NULL DEFAULT 'HEAT',
CHANGE COLUMN `AIC_VERSION_MIN` `AIC_VERSION_MIN` VARCHAR(20) NULL DEFAULT NULL,
CHANGE COLUMN `AIC_VERSION_MAX` `AIC_VERSION_MAX` VARCHAR(20) NULL DEFAULT NULL,
CHANGE COLUMN `CREATION_TIMESTAMP` `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
CHANGE COLUMN `ASDC_UUID` `MODEL_UUID` VARCHAR(200) NOT NULL ,
ADD COLUMN `TOSCA_NODE_TYPE` VARCHAR(200) NULL DEFAULT NULL,
ADD COLUMN `HEAT_TEMPLATE_ARTIFACT_UUID` VARCHAR(200) NULL DEFAULT NULL,
DROP PRIMARY KEY,
ADD INDEX `fk_vnf_resource__heat_template1_idx` (`HEAT_TEMPLATE_ARTIFACT_UUID` ASC),
DROP INDEX `UK_model_customization_uuid__asdc_service_model_version`,
DROP INDEX `UK_k10a0w7h4t0lnbynd3inkg67k`;

UPDATE mso_catalog.vnf_resource a --                                              * <<<2
	LEFT JOIN mso_catalog.heat_template ht1 ON a.template_id = ht1.id
SET
	heat_template_artifact_uuid = ht1.artifact_uuid;

-- Eliminate duplicates <<<2
CREATE TABLE `mso_catalog`.`vrtemp` AS

SELECT vr.* FROM `mso_catalog`.`vnf_resource` vr
WHERE vr.id NOT IN (SELECT vnfs FROM mso_catalog.req5temp)
GROUP BY MODEL_UUID;

DROP TABLE `mso_catalog`.`vnf_resource`;  
RENAME TABLE `mso_catalog`.`vrtemp` TO `mso_catalog`.`vnf_resource`;  
-- >>>1

-- VF vf_module  after VRC2VMC and VMC                                                ^ <<<1
CREATE TABLE IF NOT EXISTS `mso_catalog`.`vftemp` ( -- <<<2
  `id` int(11) NOT NULL,
  `MODEL_UUID` VARCHAR(200) NOT NULL,
  `MODEL_INVARIANT_UUID` VARCHAR(200) NULL DEFAULT NULL,
  `MODEL_VERSION` VARCHAR(20) NOT NULL,
  `MODEL_NAME` VARCHAR(200) NOT NULL,
  `DESCRIPTION` VARCHAR(1200) NULL DEFAULT NULL,
  `IS_BASE` INT(11) NOT NULL,
  `HEAT_TEMPLATE_ARTIFACT_UUID` VARCHAR(200),
  `VOL_HEAT_TEMPLATE_ARTIFACT_UUID` VARCHAR(200) NULL DEFAULT NULL,
  `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `VNF_RESOURCE_MODEL_UUID` VARCHAR(200)
   ) ENGINE = InnoDB DEFAULT CHARACTER SET = latin1;

INSERT INTO mso_catalog.vftemp ( -- <<<2
	id, -- <<<3
	model_uuid,
	is_base,
	model_name,
	model_version,
	creation_timestamp,
	description,
	heat_template_artifact_uuid,
	vol_heat_template_artifact_uuid,
	vnf_resource_model_uuid,
	model_invariant_uuid -- >>>3
)
	SELECT
		a.id, -- <<<3
		a.asdc_uuid,
		a.is_base,
		a.model_name,
		a.model_version,
		a.creation_timestamp,
		a.description,
		ht1.artifact_uuid heat_template_artifact_uuid,
		ht2.artifact_uuid vol_heat_template_artifact_uuid,
		vr1.model_uuid vnf_resource_model_uuid,
		a.model_invariant_uuid -- >>>3
		FROM
		(SELECT * FROM mso_catalog.vf_module) AS a
		LEFT JOIN (SELECT * FROM mso_catalog.heat_template) AS ht1 ON a.template_id = ht1.id
		LEFT JOIN (SELECT * FROM mso_catalog.heat_template) AS ht2 ON a.vol_template_id = ht2.id
		JOIN (SELECT * FROM mso_catalog.vnf_resource) AS vr1 ON a.vnf_resource_id = vr1.id;

DELETE FROM mso_catalog.vf_module; -- <<<2

ALTER TABLE `mso_catalog`.`vf_module`  -- after vftemp vrc2vmc and vmc <<<2
DROP COLUMN `LABEL`,
DROP COLUMN `INITIAL_COUNT`,
DROP COLUMN `MAX_INSTANCES`,
DROP COLUMN `MIN_INSTANCES`,
DROP COLUMN `MODEL_CUSTOMIZATION_UUID`,
DROP COLUMN `TYPE`,
DROP COLUMN `ASDC_SERVICE_MODEL_VERSION`,
DROP COLUMN `ENVIRONMENT_ID`,
DROP COLUMN `VNF_RESOURCE_ID`,
DROP COLUMN `VOL_ENVIRONMENT_ID`,
CHANGE COLUMN `id` `id` INT(11),
CHANGE COLUMN `MODEL_INVARIANT_UUID` `MODEL_INVARIANT_UUID` VARCHAR(200) NULL DEFAULT NULL AFTER `MODEL_UUID`,
CHANGE COLUMN `MODEL_VERSION` `MODEL_VERSION` VARCHAR(20) NOT NULL AFTER `MODEL_INVARIANT_UUID`,
CHANGE COLUMN `IS_BASE` `IS_BASE` INT(11) NOT NULL AFTER `DESCRIPTION`,
CHANGE COLUMN `TEMPLATE_ID` `HEAT_TEMPLATE_ARTIFACT_UUID` VARCHAR(200) NULL DEFAULT NULL AFTER `IS_BASE`,
CHANGE COLUMN `CREATION_TIMESTAMP` `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `VOL_HEAT_TEMPLATE_ARTIFACT_UUID`,
CHANGE COLUMN `ASDC_UUID` `MODEL_UUID` VARCHAR(200) NOT NULL ,
CHANGE COLUMN `DESCRIPTION` `DESCRIPTION` VARCHAR(1200) NULL DEFAULT NULL ,
CHANGE COLUMN `VOL_TEMPLATE_ID` `VOL_HEAT_TEMPLATE_ARTIFACT_UUID` VARCHAR(200) NULL DEFAULT NULL ,
ADD COLUMN `VNF_RESOURCE_MODEL_UUID` VARCHAR(200) NOT NULL AFTER `CREATION_TIMESTAMP`,
DROP PRIMARY KEY,
ADD INDEX `fk_vf_module__vnf_resource1_idx` (`VNF_RESOURCE_MODEL_UUID` ASC),
ADD INDEX `fk_vf_module__heat_template_art_uuid__heat_template1_idx` (`HEAT_TEMPLATE_ARTIFACT_UUID` ASC),
ADD INDEX `fk_vf_module__vol_heat_template_art_uuid__heat_template2_idx` (`VOL_HEAT_TEMPLATE_ARTIFACT_UUID` ASC),
DROP INDEX `UK_model_customization_uuid__asdc_service_model_version` ,
DROP INDEX `UK_o3bvdqspginaxlp4gxqohd44l` ;

INSERT INTO mso_catalog.vf_module ( -- <<<2
	id, -- <<<3
	model_uuid,
	is_base,
	model_name,
	model_version,
	creation_timestamp,
	description,
	heat_template_artifact_uuid,
	vol_heat_template_artifact_uuid,
	vnf_resource_model_uuid,
	model_invariant_uuid -- >>>3
)
	SELECT
		id, -- <<<3
		model_uuid,
		is_base,
		model_name,
		model_version,
		creation_timestamp,
		description,
		heat_template_artifact_uuid,
		vol_heat_template_artifact_uuid,
		vnf_resource_model_uuid,
		model_invariant_uuid -- >>>3
	FROM
		mso_catalog.vftemp;

-- DROP vftemp later <<<2

-- >>>1

-- vnf_components_recipe   AFTER vf_module                                         ^ <<<1
CREATE TABLE `mso_catalog`.`vcrtemp` ( -- <<<2
  `id` int(11) NOT NULL,
  `VNF_TYPE` varchar(200) DEFAULT NULL,
  `VNF_COMPONENT_TYPE` varchar(45) NOT NULL,
  `ACTION` varchar(20) NOT NULL,
  `SERVICE_TYPE` varchar(45) DEFAULT NULL,
  `VERSION` varchar(20) DEFAULT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `ORCHESTRATION_URI` varchar(256) NOT NULL,
  `VNF_COMPONENT_PARAM_XSD` varchar(2048) DEFAULT NULL,
  `RECIPE_TIMEOUT` int(11) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime DEFAULT CURRENT_TIMESTAMP,
  `VF_MODULE_MODEL_UUID` VARCHAR(200) NULL DEFAULT NULL
   ) ENGINE = InnoDB DEFAULT CHARACTER SET = latin1;

INSERT INTO mso_catalog.vcrtemp ( -- <<<2
  id, -- <<<3
  VNF_TYPE,
  VNF_COMPONENT_TYPE,
  ACTION,
  SERVICE_TYPE,
  VERSION,
  DESCRIPTION,
  ORCHESTRATION_URI,
  VNF_COMPONENT_PARAM_XSD,
  RECIPE_TIMEOUT,
  CREATION_TIMESTAMP,
  VF_MODULE_MODEL_UUID -- >>>3
)
	SELECT
		a.id, -- <<<3
		a.VNF_TYPE,
		a.VNF_COMPONENT_TYPE,
		a.ACTION,
		a.SERVICE_TYPE,
		a.VERSION,
		a.DESCRIPTION,
		a.ORCHESTRATION_URI,
		a.VNF_COMPONENT_PARAM_XSD,
		a.RECIPE_TIMEOUT,
		a.CREATION_TIMESTAMP,
		COALESCE(ht1.model_uuid, a.vf_module_id)  VF_MODULE_MODEL_UUID -- >>>3
		FROM mso_catalog.vnf_components_recipe a
		LEFT JOIN mso_catalog.vftemp ht1 ON a.vf_module_id = CONVERT(ht1.id, CHAR(100));

-- DROP vftemp later <<<2

DELETE FROM mso_catalog.vnf_components_recipe; -- <<<2

ALTER TABLE `mso_catalog`.`vnf_components_recipe`  -- <<<2
CHANGE COLUMN `VF_MODULE_ID` `VF_MODULE_MODEL_UUID` VARCHAR(200) NULL DEFAULT NULL;

INSERT INTO mso_catalog.vnf_components_recipe SELECT * FROM mso_catalog.vcrtemp; -- <<<2

DROP TABLE IF EXISTS mso_catalog.vcrtemp; -- <<<2

-- >>>1

-- vf_module_to_heat_files  AFTER vf_module heat_files                             ^ <<<1
CREATE TABLE `mso_catalog`.`vmthftemp` ( -- <<<2
	VF_MODULE_MODEL_UUID VARCHAR(200) NOT NULL,
	HEAT_FILES_ARTIFACT_UUID VARCHAR(200) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO mso_catalog.vmthftemp ( -- <<<2
    VF_MODULE_MODEL_UUID,
    HEAT_FILES_ARTIFACT_UUID
)
	SELECT DISTINCT
		ht1.model_uuid,
		ht2.artifact_uuid
		FROM mso_catalog.vf_module_to_heat_files a
		JOIN mso_catalog.vftemp ht1 ON a.vf_module_id = CONVERT(ht1.id, CHAR(100))
		JOIN  mso_catalog.heat_files ht2 ON a.HEAT_FILES_ID = ht2.id;

DROP TABLE IF EXISTS mso_catalog.vftemp; -- <<<2

DELETE FROM mso_catalog.vf_module_to_heat_files; -- <<<2

ALTER TABLE `mso_catalog`.`vf_module_to_heat_files`  -- <<<2
CHANGE COLUMN `VF_MODULE_ID` `VF_MODULE_MODEL_UUID` VARCHAR(200) NOT NULL ,
CHANGE COLUMN `HEAT_FILES_ID` `HEAT_FILES_ARTIFACT_UUID` VARCHAR(200) NOT NULL ,
ADD INDEX `fk_vf_module_to_heat_files__heat_files__artifact_uuid1_idx` (`HEAT_FILES_ARTIFACT_UUID` ASC);

INSERT INTO mso_catalog.vf_module_to_heat_files SELECT * FROM mso_catalog.vmthftemp; -- <<<2

DROP TABLE IF EXISTS mso_catalog.vmthftemp; -- <<<2

-- >>>1

-- S2RC service_to_resource_customizations` <<<1
CREATE TABLE IF NOT EXISTS `mso_catalog`.`service_to_resource_customizations` ( -- V <<<2
  `SERVICE_MODEL_UUID` VARCHAR(200) NOT NULL,
  `RESOURCE_MODEL_CUSTOMIZATION_UUID` VARCHAR(200) NOT NULL,
  `MODEL_TYPE` VARCHAR(20) NOT NULL,
  `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `fk_service_to_resource_cust__service_model_uuid_idx` (`SERVICE_MODEL_UUID` ASC),
  PRIMARY KEY (`SERVICE_MODEL_UUID`, `RESOURCE_MODEL_CUSTOMIZATION_UUID`, `MODEL_TYPE`),
  INDEX `fk_service_to_resource_cust__resource_model_customiz_uuid_idx` (`RESOURCE_MODEL_CUSTOMIZATION_UUID` ASC),
  CONSTRAINT `fk_service_to_resource_cust__service__model_uuid0`
    FOREIGN KEY (`SERVICE_MODEL_UUID`)
    REFERENCES `mso_catalog`.`service` (`MODEL_UUID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
	) ENGINE = InnoDB DEFAULT CHARACTER SET = latin1;

INSERT INTO mso_catalog.service_to_resource_customizations ( -- 4sc                * <<<2
	service_model_uuid,
	resource_model_customization_uuid,
	model_type
)
	SELECT 
		a.service_model_uuid,
		a.network_model_customization_uuid,
		"network"
	FROM
		mso_catalog.service_to_networks a;

INSERT INTO mso_catalog.service_to_resource_customizations ( -- 5sc                * <<<2
	service_model_uuid,
	resource_model_customization_uuid,
	model_type
)
	SELECT 
		a.service_model_uuid,
		a.ar_model_customization_uuid,
		"allottedResource"
	FROM
		mso_catalog.service_to_allotted_resources a;

INSERT INTO mso_catalog.service_to_resource_customizations ( -- 8a                 * <<<2
    service_model_uuid,
    resource_model_customization_uuid,
    model_type
)
    SELECT  
        ht1.model_uuid,
        a.model_customization_uuid,
        "vnf"
    FROM mso_catalog.vnftemp a
	JOIN mso_catalog.service AS ht1 ON
		a.service_model_invariant_uuid = ht1.model_invariant_uuid AND
		a.asdc_service_model_version = ht1.model_version;

ALTER TABLE `mso_catalog`.`service`  --                                            * <<<2
DROP COLUMN `SERVICE_VERSION`;

DROP TABLE IF EXISTS mso_catalog.vnftemp; -- <<<2

-- >>>1

CREATE TABLE IF NOT EXISTS `mso_catalog`.`tosca_csar` ( --                         C <<<1
  `ARTIFACT_UUID` VARCHAR(200) NOT NULL,
  `NAME` VARCHAR(200) NOT NULL,
  `VERSION` VARCHAR(20) NOT NULL,
  `DESCRIPTION` VARCHAR(1200) NULL DEFAULT NULL,
  `ARTIFACT_CHECKSUM` VARCHAR(200) NOT NULL,
  `URL` VARCHAR(200) NOT NULL,
  `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ARTIFACT_UUID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;
-- >>>1

-- 5 aka 8d delete each asdc_uuid except highest ASDC_SERVICE_MODEL_VERSION vnf_resource and cascade vf_module * <<<1
-- DELETE FROM mso_catalog.vnf_resource WHERE id = ANY(SELECT vnfs FROM mso_catalog.req5temp);
DELETE FROM mso_catalog.vf_module WHERE id = ANY(SELECT vfs FROM mso_catalog.req5temp);
DROP TABLE mso_catalog.req5temp;
-- >>>1

DROP TABLE IF EXISTS `mso_catalog`.`service_to_networks` ; --                      D <<<1

DROP TABLE IF EXISTS `mso_catalog`.`service_to_allotted_resources` ; --            D <<<1

-- >>>1

-- Drop ID's <<<1
ALTER TABLE `mso_catalog`.`heat_template` DROP COLUMN `id`;
ALTER TABLE `mso_catalog`.`heat_files`    DROP COLUMN `id`;
ALTER TABLE `mso_catalog`.`service`       DROP COLUMN `id`;
ALTER TABLE `mso_catalog`.`vnf_resource`  DROP COLUMN `id`;
ALTER TABLE `mso_catalog`.`vf_module`     DROP COLUMN `id`;
-- >>>1

-- FOREIGN KEYS <<<1
ALTER TABLE `mso_catalog`.`heat_nested_template`  --                               K <<<2
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

ALTER TABLE `mso_catalog`.`heat_template_params`  --                               K <<<2
ADD CONSTRAINT `fk_heat_template_params__heat_template1`
  FOREIGN KEY (`HEAT_TEMPLATE_ARTIFACT_UUID`)
  REFERENCES `mso_catalog`.`heat_template` (`ARTIFACT_UUID`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

ALTER TABLE `mso_catalog`.`service`  --                                            K <<<2
ADD CONSTRAINT `fk_service__tosca_csar1`
  FOREIGN KEY (`TOSCA_CSAR_ARTIFACT_UUID`)
  REFERENCES `mso_catalog`.`tosca_csar` (`ARTIFACT_UUID`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

ALTER TABLE `mso_catalog`.`service_recipe`  --                                     K <<<2
ADD CONSTRAINT `fk_service_recipe__service1`
  FOREIGN KEY (`SERVICE_MODEL_UUID`)
  REFERENCES `mso_catalog`.`service` (`MODEL_UUID`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

ALTER TABLE `mso_catalog`.`vnf_resource` --                                        K <<<2
	ADD PRIMARY KEY (`MODEL_UUID`),
	DROP COLUMN `TEMPLATE_ID`,
ADD CONSTRAINT `fk_vnf_resource__heat_template1`
  FOREIGN KEY (`HEAT_TEMPLATE_ARTIFACT_UUID`)
  REFERENCES `mso_catalog`.`heat_template` (`ARTIFACT_UUID`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

ALTER TABLE `mso_catalog`.`vf_module`  --                                          K <<<2
ADD PRIMARY KEY (`MODEL_UUID`, `VNF_RESOURCE_MODEL_UUID`),
ADD CONSTRAINT `fk_vf_module__vnf_resource1`
  FOREIGN KEY (`VNF_RESOURCE_MODEL_UUID`)
  REFERENCES `mso_catalog`.`vnf_resource` (`MODEL_UUID`)
  ON DELETE CASCADE
  ON UPDATE CASCADE,
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

ALTER TABLE `mso_catalog`.`vf_module_customization` -- after vf_module             K <<<2
  ADD CONSTRAINT `fk_vf_module_customization__vf_module1`
    FOREIGN KEY (`VF_MODULE_MODEL_UUID`)
    REFERENCES `mso_catalog`.`vf_module` (`MODEL_UUID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

ALTER TABLE `mso_catalog`.`vf_module_to_heat_files`  --                            K <<<2
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

ALTER TABLE `mso_catalog`.`allotted_resource_customization`  --                    K <<<2
ADD CONSTRAINT `fk_allotted_resource_customization__allotted_resource1`
  FOREIGN KEY (`AR_MODEL_UUID`)
  REFERENCES `mso_catalog`.`allotted_resource` (`MODEL_UUID`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

ALTER TABLE `mso_catalog`.`network_resource`  --                                   K <<<2
ADD CONSTRAINT `fk_network_resource__temp_network_heat_template_lookup__mod_nm1`
  FOREIGN KEY (`MODEL_NAME`)
  REFERENCES `mso_catalog`.`temp_network_heat_template_lookup` (`NETWORK_RESOURCE_MODEL_NAME`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION,
ADD CONSTRAINT `fk_network_resource__heat_template1`
  FOREIGN KEY (`HEAT_TEMPLATE_ARTIFACT_UUID`)
  REFERENCES `mso_catalog`.`heat_template` (`ARTIFACT_UUID`)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;

ALTER TABLE `mso_catalog`.`network_resource_customization`  --                     K <<<2
ADD CONSTRAINT `fk_network_resource_customization__network_resource1`
  FOREIGN KEY (`NETWORK_RESOURCE_MODEL_UUID`)
  REFERENCES `mso_catalog`.`network_resource` (`MODEL_UUID`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

ALTER TABLE `mso_catalog`.`vnf_resource_customization`  --                         K <<<2
ADD CONSTRAINT `fk_vnf_resource_customization__vnf_resource1`
    FOREIGN KEY (`VNF_RESOURCE_MODEL_UUID`)
    REFERENCES `mso_catalog`.`vnf_resource` (`MODEL_UUID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;
-- >>>1

-- turn validation back on <<<1
SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

COMMIT;
-- >>>1

/*
This file uses folds, set by last line.

While reading this file, lines with the + are folded.
	To unfold all:     zR
	To fold all:       zM

Move cursor to folded line: type in commands...
	Toggle folding:    za
    Recursively:       zA

Vim help about folding
:help fold
*/
--  vim:foldmarker=<<<,>>>:foldenable:foldmethod=marker
