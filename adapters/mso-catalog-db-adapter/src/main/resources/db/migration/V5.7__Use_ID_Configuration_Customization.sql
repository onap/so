USE catalogdb;

/* Drop existing foreign key */
ALTER TABLE `catalogdb`.`configuration_customization` 
DROP FOREIGN KEY IF EXISTS `fk_configuration_customization__configuration_customization1`;

ALTER TABLE `catalogdb`.`configuration_customization` 
DROP FOREIGN KEY IF EXISTS `fk_configuration_resource_customization__configuration_resour1`;
/* Drop existing index */
ALTER TABLE `catalogdb`.`configuration_customization` 
DROP INDEX IF EXISTS `fk_configuration_customization__configuration_customization_idx` ;

/* Create a new table */
CREATE TABLE `tmp_configuration_customization` (
    `ID` INT(11) NOT NULL AUTO_INCREMENT,
    `MODEL_CUSTOMIZATION_UUID` VARCHAR(200) NOT NULL,
    `MODEL_INSTANCE_NAME` VARCHAR(200) NOT NULL,
    `CONFIGURATION_TYPE` VARCHAR(200) DEFAULT NULL,
    `CONFIGURATION_ROLE` VARCHAR(200) DEFAULT NULL,
    `CONFIGURATION_FUNCTION` VARCHAR(200) DEFAULT NULL,
    `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `CONFIGURATION_MODEL_UUID` VARCHAR(200) NOT NULL,
    `SERVICE_PROXY_CUSTOMIZATION_MODEL_CUSTOMIZATION_UUID` VARCHAR(200) DEFAULT NULL,
	`CONFIGURATION_CUSTOMIZATION_MODEL_CUSTOMIZATION_ID` int(11) DEFAULT NULL,
    `SERVICE_MODEL_UUID` VARCHAR(200) NOT NULL,
    PRIMARY KEY (`ID`) ,
    KEY `fk_configuration_customization__configuration_idx` (`CONFIGURATION_MODEL_UUID`),
    KEY `fk_configuration_customization__service_idx` (`SERVICE_MODEL_UUID`),
	UNIQUE KEY `uk_configuration_customization`  (`MODEL_CUSTOMIZATION_UUID` ASC, `SERVICE_MODEL_UUID` ASC),
	CONSTRAINT `fk_configuration_customization__configuration1` FOREIGN KEY (`CONFIGURATION_MODEL_UUID`)
        REFERENCES `configuration` (`MODEL_UUID`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_configuration_customization__service1` FOREIGN KEY (`SERVICE_MODEL_UUID`)
        REFERENCES `service` (`MODEL_UUID`)
        ON DELETE CASCADE ON UPDATE CASCADE
	
)  ENGINE=INNODB DEFAULT CHARSET=LATIN1;

/* Migrate the existing data */
INSERT INTO tmp_configuration_customization 
(`model_customization_uuid` ,
		  `model_instance_name`,
		  `configuration_type` ,
		  `configuration_role` ,
		  `configuration_function` ,
		  `creation_timestamp` ,
		  `configuration_model_uuid` ,
		  `service_proxy_customization_model_customization_uuid` ,
		  `service_model_uuid`)
SELECT `config`.`model_customization_uuid`,
    `config`.`model_instance_name`,
    `config`.`configuration_type`,
    `config`.`configuration_role`,
    `config`.`configuration_function`,
    `config`.`creation_timestamp`,
    `config`.`configuration_model_uuid`,
    `config`.`service_proxy_customization_model_customization_uuid`,
    `svc`.`model_uuid` service_model_uuid FROM
    configuration_customization config,
    service svc,
    configuration_customization_to_service config_svc
WHERE
    config_svc.service_model_uuid = svc.model_uuid
        AND config_svc.resource_model_customization_uuid = config.model_customization_uuid;
      
/* Drop the old tables */

DROP TABLE `catalogdb`.`configuration_customization`;

DROP TABLE `catalogdb`.`configuration_customization_to_service`;

/* Rename the table */
RENAME TABLE tmp_configuration_customization TO configuration_customization;       
        
	