use catalogdb;

/* Consolidate Service_Proxy_Customization and Service_Proxy tables into a new table also called Service_Proxy_Customization */

ALTER TABLE  service_proxy_customization  DROP FOREIGN KEY fk_service_proxy_resource_customization__service1;

ALTER TABLE  service_proxy_customization  DROP FOREIGN KEY fk_spr_customization__service_proxy_resource1;

ALTER TABLE configuration_customization DROP FOREIGN KEY fk_configuration_customization__service_proxy_customization1;

CREATE TABLE IF NOT EXISTS `service_proxy_customization_temp` (
  `MODEL_CUSTOMIZATION_UUID` VARCHAR(200) NOT NULL,
  `MODEL_INSTANCE_NAME` VARCHAR(200) NOT NULL,
  `MODEL_UUID` VARCHAR(200) NOT NULL,
  `MODEL_INVARIANT_UUID` VARCHAR(200) NOT NULL,
  `MODEL_VERSION` VARCHAR(20) NOT NULL,
  `MODEL_NAME` VARCHAR(200) NOT NULL,
  `TOSCA_NODE_TYPE` VARCHAR(200) NOT NULL,
  `DESCRIPTION` VARCHAR(1200) NULL,
  `SOURCE_SERVICE_MODEL_UUID` VARCHAR(200) NOT NULL,
  `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`),
  INDEX `fk_service_proxy_customization__service1_idx` (`SOURCE_SERVICE_MODEL_UUID` ASC),
  UNIQUE INDEX `UK_service_proxy_customization` (`MODEL_CUSTOMIZATION_UUID` ASC),
  INDEX `fk_service_proxy_customization__serv_prox_to_serv` (`MODEL_CUSTOMIZATION_UUID` ASC),
  CONSTRAINT `fk_service_proxy_resource_customization__service1`
    FOREIGN KEY (`SOURCE_SERVICE_MODEL_UUID`)
    REFERENCES `catalogdb`.`service` (`MODEL_UUID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 20654
DEFAULT CHARACTER SET = latin1; 

INSERT INTO catalogdb.service_proxy_customization_temp (model_customization_uuid,model_instance_name,model_uuid,model_invariant_uuid,model_version,model_name,tosca_node_type,description,source_service_model_uuid)
SELECT T1.model_customization_uuid, T1.model_instance_name,T2.model_uuid, T2.model_invariant_uuid, T2.model_version, T2.model_name, T1.tosca_node_type, T2.description, T1.source_service_model_uuid
  FROM catalogdb.service_proxy_customization T1 
  JOIN catalogdb.service_proxy T2 ON T1.service_proxy_model_uuid = T2.model_uuid; 

DROP TABLE service_proxy_customization;

DROP TABLE service_proxy;

RENAME TABLE service_proxy_customization_temp TO service_proxy_customization;
