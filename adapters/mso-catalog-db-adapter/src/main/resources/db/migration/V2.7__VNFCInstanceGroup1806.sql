use catalogdb;

CREATE TABLE IF NOT EXISTS `catalogdb`.`collection_resource_instance_group_customization` (
  `COLLECTION_RESOURCE_CUSTOMIZATION_MODEL_UUID` VARCHAR(200) NOT NULL,
  `INSTANCE_GROUP_MODEL_UUID` VARCHAR(200) NOT NULL,
  `FUNCTION` VARCHAR(200) NULL,
  `DESCRIPTION` VARCHAR(1200) NULL,
  `SUBINTERFACE_NETWORK_QUANTITY` INT(11) NULL,
  `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`COLLECTION_RESOURCE_CUSTOMIZATION_MODEL_UUID`, `INSTANCE_GROUP_MODEL_UUID`),
  INDEX `fk_collection_resource_instance_group_customization__instan_idx` (`INSTANCE_GROUP_MODEL_UUID` ASC),
  CONSTRAINT `fk_collection_resource_instance_group_customization__collecti1`
    FOREIGN KEY (`COLLECTION_RESOURCE_CUSTOMIZATION_MODEL_UUID`)
    REFERENCES `catalogdb`.`collection_resource_customization` (`MODEL_CUSTOMIZATION_UUID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_collection_resource_instance_group_customization__instance1`
    FOREIGN KEY (`INSTANCE_GROUP_MODEL_UUID`)
    REFERENCES `catalogdb`.`instance_group` (`MODEL_UUID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `catalogdb`.`vnfc_instance_group_customization` (
  `VNF_RESOURCE_CUSTOMIZATION_MODEL_UUID` VARCHAR(200) NOT NULL,
  `INSTANCE_GROUP_MODEL_UUID` VARCHAR(200) NOT NULL,
  `FUNCTION` VARCHAR(200) NULL,
  `DESCRIPTION` VARCHAR(1200) NULL,
  `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`VNF_RESOURCE_CUSTOMIZATION_MODEL_UUID`, `INSTANCE_GROUP_MODEL_UUID`),
  INDEX `fk_vnfc_instance_group_customization__instance_group1_idx` (`INSTANCE_GROUP_MODEL_UUID` ASC),
  CONSTRAINT `fk_vnfc_instance_group_customization__vnf_resource_customizat1`
    FOREIGN KEY (`VNF_RESOURCE_CUSTOMIZATION_MODEL_UUID`)
    REFERENCES `catalogdb`.`vnf_resource_customization` (`MODEL_CUSTOMIZATION_UUID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_vnfc_instance_group_customization__instance_group1`
    FOREIGN KEY (`INSTANCE_GROUP_MODEL_UUID`)
    REFERENCES `catalogdb`.`instance_group` (`MODEL_UUID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

Alter TABLE `instance_group`
DROP COLUMN  `function`,
DROP COLUMN  `description`,
CHANGE `primary_type` `object_type` varchar(200) NOT NULL,
MODIFY  `tosca_node_type` varchar(200) NULL;
  
Alter TABLE `collection_resource_customization`
DROP COLUMN  `subinterface_network_quantity`,
CHANGE `primary_type` `object_type` varchar(200) NOT NULL,
MODIFY  role varchar(200) NULL,
MODIFY function varchar(200)  NULL,
MODIFY collection_resource_type varchar(200) NULL;
