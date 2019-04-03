use catalogdb;

-- Drop Foreign Keys so we can migrate data
ALTER TABLE vnf_res_custom_to_vf_module_custom DROP FOREIGN KEY IF EXISTS fk_vnf_res_custom_to_vf_module_custom__vf_module_customization1;
ALTER TABLE vnf_res_custom_to_vf_module_custom DROP FOREIGN KEY IF EXISTS fk_vnf_res_custom_to_vf_module_custom__vnf_resource_customiza1;
ALTER TABLE cvnfc_customization DROP FOREIGN KEY IF EXISTS fk_cvnfc_customization__vf_module_customization1;
ALTER TABLE vnf_vfmodule_cvnfc_configuration_customization DROP FOREIGN KEY IF EXISTS fk_vnf_configuration_cvnfc_customization__vf_module_customiza1;
ALTER TABLE cvnfc_customization DROP FOREIGN KEY IF EXISTS fk_cvnfc_customization__vnf_resource_customization1;
ALTER TABLE vnf_resource_customization_to_service DROP FOREIGN KEY IF EXISTS vnf_resource_customization_to_service_ibfk_2;
ALTER TABLE vnf_vfmodule_cvnfc_configuration_customization DROP FOREIGN KEY IF EXISTS fk_vfmodule_cvnfc_configuration_customization__vnf_resource_c1;
ALTER TABLE vnfc_instance_group_customization DROP FOREIGN KEY IF EXISTS fk_vnfc_instance_group_customization__vnf_resource_customizat1;
DROP INDEX  UK_cvnfc_customization on cvnfc_customization;
DELETE FROM vf_module_customization WHERE vf_module_model_uuid NOT IN (SELECT model_uuid FROM vf_module);

ALTER TABLE vnf_resource_customization
CHANGE COLUMN MULTI_STAGE_DESIGN MULTI_STAGE_DESIGN VARCHAR(20) NULL DEFAULT NULL AFTER NF_NAMING_CODE;

-- Generate Primary keys for tables
ALTER TABLE vnf_resource_customization DROP PRIMARY KEY, ADD ID INT(13) NOT NULL AUTO_INCREMENT PRIMARY KEY;
ALTER TABLE vf_module_customization DROP PRIMARY KEY, ADD ID INT(13) NOT NULL AUTO_INCREMENT PRIMARY KEY;

-- Add columns for Joins between tables
ALTER TABLE vnf_resource_customization ADD SERVICE_MODEL_UUID VARCHAR(200) DEFAULT NULL;
ALTER TABLE vf_module_customization ADD VNF_RESOURCE_CUSTOMIZATION_ID INT(13) DEFAULT NULL;
ALTER TABLE vnfc_instance_group_customization ADD VNF_RESOURCE_CUSTOMIZATION_ID INT(13) DEFAULT NULL;
ALTER TABLE cvnfc_customization ADD VF_MODULE_CUSTOMIZATION_ID INT(13) DEFAULT NULL;


-- Migrate linkage between VNF/Services, re-add foreign key
INSERT INTO vnf_resource_customization (SELECT vrc.MODEL_CUSTOMIZATION_UUID,vrc.MODEL_INSTANCE_NAME, vrc.MIN_INSTANCES,vrc.MAX_INSTANCES,vrc.AVAILABILITY_ZONE_MAX_COUNT,vrc.NF_TYPE,vrc.NF_ROLE,vrc.NF_FUNCTION,
vrc.NF_NAMING_CODE,vrc.MULTI_STAGE_DESIGN,vrc.CREATION_TIMESTAMP, vrc.VNF_RESOURCE_MODEL_UUID,vrc.INSTANCE_GROUP_MODEL_UUID,NULL,crcs.SERVICE_MODEL_UUID from vnf_resource_customization vrc, vnf_resource_customization_to_service crcs, service srv WHERE crcs.RESOURCE_MODEL_CUSTOMIZATION_UUID = vrc.MODEL_CUSTOMIZATION_UUID AND srv.MODEL_UUID=crcs.SERVICE_MODEL_UUID);

DELETE FROM vnf_resource_customization where SERVICE_MODEL_UUID IS NULL;

ALTER TABLE vnf_resource_customization CHANGE COLUMN SERVICE_MODEL_UUID SERVICE_MODEL_UUID VARCHAR(200) NOT NULL;

ALTER TABLE vnf_resource_customization 
    ADD CONSTRAINT fk_vnf_resource_customization_to_service FOREIGN KEY (SERVICE_MODEL_UUID) 
    REFERENCES service(MODEL_UUID) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE;
    
-- Migrate linkage between VNF/VNFCInstance Groups

ALTER TABLE vnfc_instance_group_customization DROP PRIMARY KEY;

INSERT INTO vnfc_instance_group_customization 
(SELECT vnfcgroup.VNF_RESOURCE_CUSTOMIZATION_MODEL_UUID, vnfcgroup.INSTANCE_GROUP_MODEL_UUID, vnfcgroup.FUNCTION, vnfcgroup.DESCRIPTION, vnfcgroup.CREATION_TIMESTAMP, vnfrc.ID
FROM vnfc_instance_group_customization vnfcgroup, vnf_resource_customization vnfrc
WHERE vnfrc.MODEL_CUSTOMIZATION_UUID = vnfcgroup.VNF_RESOURCE_CUSTOMIZATION_MODEL_UUID);

DELETE FROM vnfc_instance_group_customization where VNF_RESOURCE_CUSTOMIZATION_ID IS NULL;

ALTER TABLE vnfc_instance_group_customization ADD ID INT(13) NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;

ALTER TABLE vnfc_instance_group_customization 
    ADD INDEX fk_vnfc_instance_group_customization__vnf_res_custom1_idx (VNF_RESOURCE_CUSTOMIZATION_ID ASC),
    ADD CONSTRAINT fk_vnfc_instance_group_customization_vnf_customization FOREIGN KEY (VNF_RESOURCE_CUSTOMIZATION_ID) 
    REFERENCES vnf_resource_customization(ID) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE;

ALTER TABLE vnfc_instance_group_customization DROP VNF_RESOURCE_CUSTOMIZATION_MODEL_UUID;
    
-- Migrate linkage between VNF/VFMODULEs, re-add foreign key
INSERT INTO vf_module_customization 
(SELECT vmc.MODEL_CUSTOMIZATION_UUID, vmc.LABEL, vmc.INITIAL_COUNT, vmc.MIN_INSTANCES, vmc.MAX_INSTANCES, vmc.AVAILABILITY_ZONE_COUNT, vmc.HEAT_ENVIRONMENT_ARTIFACT_UUID, vmc.VOL_ENVIRONMENT_ARTIFACT_UUID, vmc.CREATION_TIMESTAMP, vmc.VF_MODULE_MODEL_UUID, NULL,vnfrc.ID
FROM vf_module_customization vmc, vnf_res_custom_to_vf_module_custom vnfcvf, vnf_resource_customization vnfrc
WHERE vnfcvf.VF_MODULE_CUST_MODEL_CUSTOMIZATION_UUID = vmc.MODEL_CUSTOMIZATION_UUID AND vnfrc.MODEL_CUSTOMIZATION_UUID=vnfcvf.VNF_RESOURCE_CUST_MODEL_CUSTOMIZATION_UUID);

DELETE FROM vf_module_customization where VNF_RESOURCE_CUSTOMIZATION_ID IS NULL;

-- Migrate Linkage between CVNFC_Customization and VF_Modules
CREATE VIEW vf_module_vnf AS
SELECT vmc.ID as vf_module_id, vnfrc.MODEL_CUSTOMIZATION_UUID as VNF_MODEL_CUST_UUID, vmc.MODEL_CUSTOMIZATION_UUID
FROM vf_module_customization vmc
JOIN vnf_res_custom_to_vf_module_custom vnfcvf ON vmc.MODEL_CUSTOMIZATION_UUID = vnfcvf.VF_MODULE_CUST_MODEL_CUSTOMIZATION_UUID
JOIN vnf_resource_customization vnfrc ON vnfrc.MODEL_CUSTOMIZATION_UUID=vnfcvf.VNF_RESOURCE_CUST_MODEL_CUSTOMIZATION_UUID;

UPDATE cvnfc_customization as cvnfcc 
JOIN vf_module_vnf AS vfmvnf 
  ON vfmvnf.model_customization_uuid = cvnfcc.VF_MODULE_CUST_MODEL_CUSTOMIZATION_UUID
  	 AND cvnfcc.VNF_RESOURCE_CUST_MODEL_CUSTOMIZATION_UUID=vfmvnf.VNF_MODEL_CUST_UUID
SET cvnfcc.VF_MODULE_CUSTOMIZATION_ID = vfmvnf.vf_module_id;


DROP VIEW vf_module_vnf;

ALTER TABLE cvnfc_customization 
    ADD CONSTRAINT fk_cvnfc_customization_to_vf_module_resource_customization FOREIGN KEY (VF_MODULE_CUSTOMIZATION_ID) 
    REFERENCES vf_module_customization(ID) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE;

-- rename/clean up crazy table
ALTER TABLE vnf_vfmodule_cvnfc_configuration_customization DROP VNF_RESOURCE_CUST_MODEL_CUSTOMIZATION_UUID;
ALTER TABLE vnf_vfmodule_cvnfc_configuration_customization DROP VF_MODULE_MODEL_CUSTOMIZATION_UUID;
RENAME TABLE vnf_vfmodule_cvnfc_configuration_customization TO cvnfc_configuration_customization;

-- DROP Tables/columns no longer used
ALTER TABLE cvnfc_customization DROP VNF_RESOURCE_CUST_MODEL_CUSTOMIZATION_UUID;
ALTER TABLE cvnfc_customization DROP VF_MODULE_CUST_MODEL_CUSTOMIZATION_UUID;
ALTER TABLE vnf_resource_customization DROP instance_group_model_uuid;


-- Remove orphaned records, that hav ebeen migrated

DROP TABLE vnf_res_custom_to_vf_module_custom;
DROP TABLE vnf_resource_customization_to_service;

ALTER TABLE vnf_resource_customization
ADD UNIQUE INDEX UK_vnf_resource_customization (MODEL_CUSTOMIZATION_UUID ASC, SERVICE_MODEL_UUID ASC);

-- Additional Clean Up
ALTER TABLE vnf_resource_customization
CHANGE COLUMN `ID` `ID` INT(13) NOT NULL AUTO_INCREMENT FIRST,
ADD INDEX `vnf_resource_customization_mod_cust_uuid_idx` (`MODEL_CUSTOMIZATION_UUID` ASC);

ALTER TABLE service_proxy_customization
DROP INDEX fk_service_proxy_customization__serv_prox_to_serv,
DROP INDEX UK_service_proxy_customization;

ALTER TABLE vf_module_customization 
    ADD CONSTRAINT fk_vf_module_customization_to_vnf_resource_customization FOREIGN KEY (VNF_RESOURCE_CUSTOMIZATION_ID) 
    REFERENCES vnf_resource_customization(ID) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE;

ALTER TABLE collection_resource
CHANGE COLUMN DESCRIPTION DESCRIPTION VARCHAR(1200) NULL DEFAULT NULL;

ALTER TABLE building_block_detail
CHANGE COLUMN BUILDING_BLOCK_NAME BUILDING_BLOCK_NAME VARCHAR(200) NOT NULL;

ALTER TABLE vnfc_instance_group_customization CHANGE COLUMN VNF_RESOURCE_CUSTOMIZATION_ID VNF_RESOURCE_CUSTOMIZATION_ID INT(13) NOT NULL;

-- clean up before making FK null
DELETE from cvnfc_customization where vf_module_customization_id is NULL;
ALTER TABLE cvnfc_customization CHANGE COLUMN VF_MODULE_CUSTOMIZATION_ID VF_MODULE_CUSTOMIZATION_ID INT(13) NOT NULL;

ALTER TABLE vf_module_customization CHANGE COLUMN VNF_RESOURCE_CUSTOMIZATION_ID VNF_RESOURCE_CUSTOMIZATION_ID INT(13) NOT NULL;

ALTER TABLE cvnfc_configuration_customization CHANGE COLUMN CVNFC_CUSTOMIZATION_ID CVNFC_CUSTOMIZATION_ID INT(11) NOT NULL;

ALTER TABLE cvnfc_configuration_customization 
    ADD CONSTRAINT fk_cvnfc_config_custom__cvnfc_cust1 FOREIGN KEY (CVNFC_CUSTOMIZATION_ID) 
    REFERENCES cvnfc_customization(ID) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE;
    
ALTER TABLE vf_module_customization
CHANGE COLUMN ID ID INT(13) NOT NULL AUTO_INCREMENT FIRST,
ADD INDEX vf_module_customization_model_cust_uuid_idx (MODEL_CUSTOMIZATION_UUID ASC);
