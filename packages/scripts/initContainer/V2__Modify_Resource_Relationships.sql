
USE catalogdb;

DELETE from service where model_uuid in ( SELECT SERVICE_MODEL_UUID
FROM service_to_resource_customizations
GROUP BY
SERVICE_MODEL_UUID, resource_model_customization_uuid
HAVING COUNT(*) > 1);
 

CREATE TABLE IF NOT EXISTS external_service_to_internal_model_mapping (
id INT(11) NOT NULL, 
SERVICE_NAME VARCHAR(200) NOT NULL,
PRODUCT_FLAVOR VARCHAR(200) NULL,
SUBSCRIPTION_SERVICE_TYPE VARCHAR(200) NOT NULL,
SERVICE_MODEL_UUID VARCHAR(200) NOT NULL, 
PRIMARY KEY (id), 
UNIQUE INDEX UK_external_service_to_internal_model_mapping
(SERVICE_NAME ASC, PRODUCT_FLAVOR ASC, SERVICE_MODEL_UUID ASC));

CREATE TABLE IF NOT EXISTS `COLLECTION_RESOURCE` (
 MODEL_UUID varchar(200) NOT NULL,
 MODEL_NAME varchar(200) NOT NULL, 
 MODEL_INVARIANT_UUID varchar(200) NOT NULL,
 MODEL_VERSION varchar(20) NOT NULL, 
 TOSCA_NODE_TYPE varchar(200) NOT NULL,
 DESCRIPTION varchar(200),  
 CREATION_TIMESTAMP datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (`MODEL_UUID`)
)ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `COLLECTION_RESOURCE_CUSTOMIZATION` (
 MODEL_CUSTOMIZATION_UUID varchar(200) NOT NULL,
 MODEL_INSTANCE_NAME varchar(200) NOT NULL,
 ROLE varchar(200) NOT NULL,
 PRIMARY_TYPE varchar(200) NOT NULL, 
 FUNCTION varchar(200) NOT NULL, 
 SUBINTERFACE_NETWORK_QUANTITY INT, 
 COLLECTION_RESOURCE_TYPE varchar(200) NOT NULL,
 CREATION_TIMESTAMP datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CR_MODEL_UUID varchar(200) NOT NULL,
 PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`)
)ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `INSTANCE_GROUP` (
 MODEL_UUID varchar(200) NOT NULL,
 MODEL_NAME varchar(200) NOT NULL,
 MODEL_INVARIANT_UUID varchar(200) NOT NULL,
 MODEL_VERSION varchar(20) NOT NULL,
 TOSCA_NODE_TYPE varchar(200) DEFAULT NULL,
 ROLE varchar(200) NOT NULL,
 PRIMARY_TYPE varchar(200) NOT NULL, 
 FUNCTION varchar(200) NOT NULL, 
 DESCRIPTION varchar(200),  
 CREATION_TIMESTAMP datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CR_MODEL_UUID varchar(200) NOT NULL,
 INSTANCE_GROUP_TYPE varchar(200) NOT NULL,
  PRIMARY KEY (`MODEL_UUID`)
)ENGINE=InnoDB DEFAULT CHARSET=latin1;

 CREATE TABLE IF NOT EXISTS `mso_catalog`.`configuration` 
 ( `MODEL_UUID` VARCHAR(200) NOT NULL, 
 `MODEL_INVARIANT_UUID` VARCHAR(200) NOT NULL, 
 `MODEL_VERSION` VARCHAR(20) NOT NULL, 
 `MODEL_NAME` VARCHAR(200) NOT NULL, 
 `TOSCA_NODE_TYPE` VARCHAR(200) NOT NULL, 
 `DESCRIPTION` VARCHAR(1200) NULL, 
 `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (`MODEL_UUID`)) 
 ENGINE = InnoDB AUTO_INCREMENT = 20654 
 DEFAULT CHARACTER SET = latin1;
 
 CREATE TABLE IF NOT EXISTS `mso_catalog`.`service_proxy` (
 `MODEL_UUID` VARCHAR(200) NOT NULL,
 `MODEL_INVARIANT_UUID` VARCHAR(200) NOT NULL,
 `MODEL_VERSION` VARCHAR(20) NOT NULL,
 `MODEL_NAME` VARCHAR(200) NOT NULL,
 `DESCRIPTION` VARCHAR(1200) NULL,
 `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (`MODEL_UUID`)) 
 ENGINE = InnoDB AUTO_INCREMENT = 20654
 DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `service_proxy_customization` (
`MODEL_CUSTOMIZATION_UUID` VARCHAR(200) NOT NULL,
`MODEL_INSTANCE_NAME` VARCHAR(200) NOT NULL,
`TOSCA_NODE_TYPE` VARCHAR(200) NOT NULL,
`SOURCE_SERVICE_MODEL_UUID` VARCHAR(200) NOT NULL,
`CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
`SERVICE_PROXY_MODEL_UUID` VARCHAR(200) NOT NULL,
PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`),
INDEX `fk_service_proxy_customization__service_proxy1_idx` (`SERVICE_PROXY_MODEL_UUID` ASC),
INDEX `fk_service_proxy_customization__service1_idx` (`SOURCE_SERVICE_MODEL_UUID` ASC), 
CONSTRAINT`fk_spr_customization__service_proxy_resource1` 
FOREIGN KEY (`SERVICE_PROXY_MODEL_UUID`) REFERENCES `mso_catalog`.`service_proxy` (`MODEL_UUID`)
ON DELETE CASCADE ON UPDATE CASCADE,
CONSTRAINT `fk_service_proxy_resource_customization__service1` 
FOREIGN KEY (`SOURCE_SERVICE_MODEL_UUID`) REFERENCES `mso_catalog`.`service`
(`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE) 
ENGINE = InnoDB
AUTO_INCREMENT = 20654 
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `mso_catalog`.`configuration_customization` (
`MODEL_CUSTOMIZATION_UUID` VARCHAR(200) NOT NULL, 
`MODEL_INSTANCE_NAME` VARCHAR(200) NOT NULL,
`CONFIGURATION_TYPE` VARCHAR(200) NULL,
`CONFIGURATION_ROLE` VARCHAR(200) NULL,
`CONFIGURATION_FUNCTION` VARCHAR(200) NULL,
`CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
`CONFIGURATION_MODEL_UUID` VARCHAR(200) NOT NULL,
`SERVICE_PROXY_CUSTOMIZATION_MODEL_CUSTOMIZATION_UUID` VARCHAR(200) NULL, 
`CONFIGURATION_CUSTOMIZATION_MODEL_CUSTOMIZATION_UUID` VARCHAR(200) NULL, 
PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`), 
INDEX `fk_configuration_customization__configuration_idx` (`CONFIGURATION_MODEL_UUID` ASC), 
INDEX `fk_configuration_customization__service_proxy_customization_idx`
(`SERVICE_PROXY_CUSTOMIZATION_MODEL_CUSTOMIZATION_UUID` ASC), 
INDEX `fk_configuration_customization__configuration_customization_idx`
(`CONFIGURATION_CUSTOMIZATION_MODEL_CUSTOMIZATION_UUID` ASC), 
CONSTRAINT `fk_configuration_resource_customization__configuration_resour1`
FOREIGN KEY (`CONFIGURATION_MODEL_UUID`) REFERENCES `mso_catalog`.`configuration` (`MODEL_UUID`)
ON DELETE CASCADE ON UPDATE CASCADE, 
CONSTRAINT `fk_configuration_customization__service_proxy_customization1` FOREIGN
KEY (`SERVICE_PROXY_CUSTOMIZATION_MODEL_CUSTOMIZATION_UUID`) REFERENCES
`mso_catalog`.`service_proxy_customization` (`MODEL_CUSTOMIZATION_UUID`)
ON DELETE CASCADE ON UPDATE CASCADE, CONSTRAINT
`fk_configuration_customization__configuration_customization1` FOREIGN
KEY (`CONFIGURATION_CUSTOMIZATION_MODEL_CUSTOMIZATION_UUID`) REFERENCES
`mso_catalog`.`configuration_customization` (`MODEL_CUSTOMIZATION_UUID`)
ON DELETE CASCADE ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT =20654 
DEFAULT CHARACTER SET = latin1;


CREATE TABLE `service_proxy_customization_to_service` (
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  `RESOURCE_MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`SERVICE_MODEL_UUID`,`RESOURCE_MODEL_CUSTOMIZATION_UUID`)
)ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE `configuration_customization_to_service` (
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  `RESOURCE_MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`SERVICE_MODEL_UUID`,`RESOURCE_MODEL_CUSTOMIZATION_UUID`)
)ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `collection_resource_customization_to_service` (
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  `RESOURCE_MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`SERVICE_MODEL_UUID`,`RESOURCE_MODEL_CUSTOMIZATION_UUID`)
)ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE `network_resource_customization_to_service` (
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  `RESOURCE_MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`SERVICE_MODEL_UUID`,`RESOURCE_MODEL_CUSTOMIZATION_UUID`)
)ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `vnf_resource_customization_to_service` (
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  `RESOURCE_MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`SERVICE_MODEL_UUID`,`RESOURCE_MODEL_CUSTOMIZATION_UUID`)
)ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `allotted_resource_customization_to_service` (
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  `RESOURCE_MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`SERVICE_MODEL_UUID`,`RESOURCE_MODEL_CUSTOMIZATION_UUID`)
)ENGINE=InnoDB DEFAULT CHARSET=latin1;


ALTER TABLE COLLECTION_RESOURCE_CUSTOMIZATION
ADD FOREIGN KEY ( CR_MODEL_UUID) 
REFERENCES collection_resource(MODEL_UUID)
ON DELETE CASCADE;

ALTER TABLE vnf_resource_customization 
ADD COLUMN 
INSTANCE_GROUP_MODEL_UUID varchar(200);


ALTER TABLE INSTANCE_GROUP
ADD FOREIGN KEY ( CR_MODEL_UUID) 
REFERENCES collection_resource(MODEL_UUID)
ON DELETE CASCADE;


ALTER TABLE collection_resource_customization_to_service 
ADD FOREIGN KEY (service_model_uuid) 
REFERENCES service(MODEL_UUID)
ON DELETE CASCADE;

ALTER TABLE allotted_resource_customization_to_service 
ADD FOREIGN KEY (service_model_uuid) 
REFERENCES service(MODEL_UUID)
ON DELETE CASCADE;


ALTER TABLE vnf_resource_customization_to_service 
ADD FOREIGN KEY (service_model_uuid) 
REFERENCES service(MODEL_UUID)
ON DELETE CASCADE;


ALTER TABLE network_resource_customization_to_service 
ADD FOREIGN KEY (service_model_uuid) 
REFERENCES service(MODEL_UUID)
ON DELETE CASCADE;


ALTER TABLE network_resource_customization_to_service 
ADD FOREIGN KEY (resource_model_customization_uuid) 
REFERENCES network_resource_customization(model_customization_uuid)
ON DELETE CASCADE;

ALTER TABLE vnf_resource_customization_to_service 
ADD FOREIGN KEY (resource_model_customization_uuid) 
REFERENCES vnf_resource_customization(model_customization_uuid)
ON DELETE CASCADE;

ALTER TABLE allotted_resource_customization_to_service 
ADD FOREIGN KEY (resource_model_customization_uuid) 
REFERENCES allotted_resource_customization(model_customization_uuid)
ON DELETE CASCADE;  

ALTER TABLE collection_resource_customization_to_service 
ADD FOREIGN KEY (resource_model_customization_uuid) 
REFERENCES collection_resource_customization(model_customization_uuid)
ON DELETE CASCADE;

INSERT INTO network_resource_customization_to_service SELECT service_model_uuid,resource_model_customization_uuid 
FROM service_to_resource_customizations WHERE model_type = 'network' and service_model_uuid in(select model_uuid from service)
AND resource_model_customization_uuid in ( SELECT MODEL_CUSTOMIZATION_UUID from network_resource_customization);

INSERT INTO allotted_resource_customization_to_service SELECT service_model_uuid,resource_model_customization_uuid 
FROM service_to_resource_customizations WHERE model_type = 'allottedResource' and service_model_uuid in(select model_uuid from service)
AND resource_model_customization_uuid in ( SELECT MODEL_CUSTOMIZATION_UUID from allotted_resource_customization);

INSERT INTO vnf_resource_customization_to_service SELECT service_model_uuid,resource_model_customization_uuid 
FROM service_to_resource_customizations WHERE model_type = 'vnf' and service_model_uuid in(select model_uuid from service)
AND resource_model_customization_uuid in ( SELECT MODEL_CUSTOMIZATION_UUID from vnf_resource_customization);

DROP TABLE service_to_resource_customizations;

CREATE TABLE IF NOT EXISTS `collection_network_resource_customization` (
`MODEL_CUSTOMIZATION_UUID` VARCHAR(200) NOT NULL,
`MODEL_INSTANCE_NAME` VARCHAR(200) NOT NULL,
`NETWORK_TECHNOLOGY` VARCHAR(45) NULL,
`NETWORK_TYPE` VARCHAR(45) NULL,
`NETWORK_ROLE` VARCHAR(200) NULL,
`NETWORK_SCOPE` VARCHAR(45) NULL,
`CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
`NETWORK_RESOURCE_MODEL_UUID` VARCHAR(200) NOT NULL, `INSTANCE_GROUP_MODEL_UUID` VARCHAR(200) NULL,
`CRC_MODEL_CUSTOMIZATION_UUID` VARCHAR(200) NOT NULL, PRIMARY KEY
(`MODEL_CUSTOMIZATION_UUID`, `CRC_MODEL_CUSTOMIZATION_UUID`),
INDEX `fk_collection_net_resource_customization__network_resource1_idx`
(`NETWORK_RESOURCE_MODEL_UUID` ASC), INDEX
`fk_collection_net_resource_customization__instance_group1_idx`
(`INSTANCE_GROUP_MODEL_UUID` ASC), INDEX
`fk_col_net_res_customization__collection_res_customization_idx`
(`CRC_MODEL_CUSTOMIZATION_UUID` ASC), CONSTRAINT
`fk_collection_net_resource_customization__network_resource10` FOREIGN
KEY (`NETWORK_RESOURCE_MODEL_UUID`) REFERENCES
`mso_catalog`.`network_resource` (`MODEL_UUID`) ON DELETE CASCADE ON
UPDATE CASCADE, CONSTRAINT
`fk_collection_net_resource_customization__instance_group10` FOREIGN KEY
(`INSTANCE_GROUP_MODEL_UUID`) REFERENCES `instance_group`
(`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE, CONSTRAINT
`fk_collection_network_resource_customization__collection_reso1` FOREIGN
KEY (`CRC_MODEL_CUSTOMIZATION_UUID`) REFERENCES
`collection_resource_customization`
(`MODEL_CUSTOMIZATION_UUID`) ON DELETE CASCADE ON UPDATE CASCADE) ENGINE
= InnoDB DEFAULT CHARACTER SET = latin1;

INSERT INTO vnf_recipe (VNF_TYPE, ACTION, VERSION_STR, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT)
VALUES
('GR-API-DEFAULT', 'createInstance', '1', 'Gr api recipe to create vnf', '/mso/async/services/WorkflowActionBB', 180),
('GR-API-DEFAULT', 'deleteInstance', '1', 'Gr api recipe to delete vnf', '/mso/async/services/WorkflowActionBB', 180),
('GR-API-DEFAULT', 'updateInstance', '1', 'Gr api recipe to update vnf', '/mso/async/services/WorkflowActionBB', 180),
('GR-API-DEFAULT', 'replaceInstance', '1', 'Gr api recipe to replace vnf', '/mso/async/services/WorkflowActionBB', 180),
('GR-API-DEFAULT', 'inPlaceSoftwareUpdate', '1', 'Gr api recipe to do an in place software update', '/mso/async/services/WorkflowActionBB', 180),
('GR-API-DEFAULT', 'applyUpdatedConfig', '1', 'Gr api recipe to apply updated config', '/mso/async/services/WorkflowActionBB', 180);

UPDATE vnf_recipe
SET vnf_type = 'VNF-API-DEFAULT'
WHERE vnf_type = 'VID_DEFAULT';

UPDATE vnf_recipe
SET description = 'Vnf api recipe to create vnf'
WHERE description = 'VID_DEFAULT recipe to create VNF if no custom BPMN flow is found';

UPDATE vnf_recipe
SET description = 'Vnf api recipe to delete vnf'
WHERE description = 'VID_DEFAULT recipe to delete VNF if no custom BPMN flow is found';

UPDATE vnf_recipe
SET description = 'Vnf api recipe to update vnf'
WHERE description = 'VID_DEFAULT update';

UPDATE vnf_recipe
SET description = 'Vnf api recipe to replace vnf'
WHERE description = 'VID_DEFAULT replace';

UPDATE vnf_recipe
SET description = 'Vnf api recipe to do an in place software update'
WHERE description = 'VID_DEFAULT inPlaceSoftwareUpdate';

UPDATE vnf_recipe
SET description = 'Vnf api recipe to apply updated config'
WHERE description = 'VID_DEFAULT applyUpdatedConfig';

INSERT INTO service (MODEL_UUID, MODEL_NAME, MODEL_INVARIANT_UUID, MODEL_VERSION, DESCRIPTION)
VALUES
('DummyGRApiDefaultModelUUID?', 'GR-API-DEFAULT', 'DummyGRApiDefaultModelInvariantUUID?', '1.0', 'Gr api service for VID to use for infra APIH orchestration');

UPDATE service
SET model_name = 'VNF-API-DEFAULT',
	description = 'Vnf api service for VID to use for infra APIH orchestration'
WHERE model_name = 'VID_DEFAULT';

INSERT INTO service_recipe (ACTION, VERSION_STR, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT, SERVICE_MODEL_UUID)
VALUES
('activateInstance', '1.0', 'Gr api recipe to activate service-instance', '/mso/async/services/WorkflowActionBB', 180, 'DummyGRApiDefaultModelUUID?'),
('createInstance', '1.0', 'Gr api recipe to create service-instance', '/mso/async/services/WorkflowActionBB', 180, 'DummyGRApiDefaultModelUUID?'),
('deactivateInstance', '1.0', 'Gr api recipe to deactivate service-instance', '/mso/async/services/WorkflowActionBB', 180, 'DummyGRApiDefaultModelUUID?'),
('deleteInstance', '1.0', 'Gr api recipe to delete service-instance', '/mso/async/services/WorkflowActionBB', 180, 'DummyGRApiDefaultModelUUID?');

UPDATE service_recipe
SET description = 'Vnf api recipe to activate service-instance'
WHERE description = 'VID_DEFAULT activate';

UPDATE service_recipe
SET description = 'Vnf api recipe to create service-instance'
WHERE description = 'VID_DEFAULT recipe to create service-instance if no custom BPMN flow is found';

UPDATE service_recipe
SET description = 'Vnf api recipe to deactivate service-instance'
WHERE description = 'VID_DEFAULT deactivate';

UPDATE service_recipe
SET description = 'Vnf api recipe to delete service-instance'
WHERE description = 'VID_DEFAULT recipe to delete service-instance if no custom BPMN flow is found';

INSERT INTO vnf_components_recipe (VNF_COMPONENT_TYPE, ACTION, VERSION, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT, VF_MODULE_MODEL_UUID)
VALUES
('volumeGroup', 'createInstance', '1', 'Gr api recipe to create volume-group', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT'),
('volumeGroup', 'deleteInstance', '1', 'Gr api recipe to delete volume-group', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT'),
('volumeGroup', 'updateInstance', '1', 'Gr api recipe to update volume-group', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT'),
('vfModule', 'createInstance', '1', 'Gr api recipe to create vf-module', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT'),
('vfModule', 'deleteInstance', '1', 'Gr api recipe to delete vf-module', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT'),
('vfModule', 'updateInstance', '1', 'Gr api recipe to update vf-module', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT'),
('vfModule', 'replaceInstance', '1', 'Gr api recipe to replace vf-module', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT');

UPDATE vnf_components_recipe
SET vf_module_model_uuid = 'VNF-API-DEFAULT'
WHERE vf_module_model_uuid = 'VID_DEFAULT';

UPDATE vnf_components_recipe
SET description = 'Vnf api recipe to create volume-group'
WHERE description = 'VID_DEFAULT recipe to create volume-group if no custom BPMN flow is found';

UPDATE vnf_components_recipe
SET description = 'Vnf api recipe to delete volume-group'
WHERE description = 'VID_DEFAULT recipe to delete volume-group if no custom BPMN flow is found';

UPDATE vnf_components_recipe
SET description = 'Vnf api recipe to update volume-group'
WHERE description = 'VID_DEFAULT recipe to update volume-group if no custom BPMN flow is found';

UPDATE vnf_components_recipe
SET description = 'Vnf api recipe to create vf-module'
WHERE description = 'VID_DEFAULT recipe to create vf-module if no custom BPMN flow is found';

UPDATE vnf_components_recipe
SET description = 'Vnf api recipe to delete vf-module'
WHERE description = 'VID_DEFAULT recipe to delete vf-module if no custom BPMN flow is found';

UPDATE vnf_components_recipe
SET description = 'Vnf api recipe to update vf-module'
WHERE description = 'VID_DEFAULT recipe to update vf-module if no custom BPMN flow is found';

UPDATE vnf_components_recipe
SET description = 'Vnf api recipe to replace vf-module'
WHERE description = 'VID_DEFAULT vfModule replace';

INSERT INTO network_recipe (MODEL_NAME, ACTION, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT, VERSION_STR)
VALUES
('GR-API-DEFAULT', 'createInstance', 'Gr api recipe to create network', '/mso/async/services/WorkflowActionBB', 180, '1.0'),
('GR-API-DEFAULT', 'updateInstance', 'Gr api recipe to update network', '/mso/async/services/WorkflowActionBB', 180, '1.0'),
('GR-API-DEFAULT', 'deleteInstance', 'Gr api recipe to delete network', '/mso/async/services/WorkflowActionBB', 180, '1.0');
	
UPDATE network_recipe
SET model_name = 'VNF-API-DEFAULT'
WHERE model_name = 'VID_DEFAULT';

UPDATE network_recipe
SET description = 'Vnf api recipe to create network'
WHERE description = 'VID_DEFAULT recipe to create network if no custom BPMN flow is found';

UPDATE network_recipe
SET description = 'Vnf api recipe to update network'
WHERE description = 'VID_DEFAULT recipe to update network if no custom BPMN flow is found';

UPDATE network_recipe
SET description = 'Vnf api recipe to delete network'
WHERE description = 'VID_DEFAULT recipe to delete network if no custom BPMN flow is found';

CREATE TABLE IF NOT EXISTS `northbound_request_ref_lookup` (
`id` INT(11) NOT NULL AUTO_INCREMENT,
`REQUEST_SCOPE` VARCHAR(200) NOT NULL,
`MACRO_ACTION` VARCHAR(200) NOT NULL,
`ACTION` VARCHAR(200) NOT NULL,
`IS_ALACARTE` TINYINT(1) NOT NULL DEFAULT 0,
`MIN_API_VERSION` DOUBLE NOT NULL,
`MAX_API_VERSION` DOUBLE NULL,
PRIMARY KEY (`id`),
UNIQUE INDEX `UK_northbound_request_ref_lookup` (`MIN_API_VERSION` ASC, `REQUEST_SCOPE` ASC, `ACTION` ASC, `IS_ALACARTE` ASC, `MACRO_ACTION` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `orchestration_flow_reference` (
`id` INT(11) NOT NULL AUTO_INCREMENT,
`COMPOSITE_ACTION` VARCHAR(200) NOT NULL,
`SEQ_NO` INT(11) NOT NULL,
`FLOW_NAME` VARCHAR(200) NOT NULL,
`FLOW_VERSION` DOUBLE NOT NULL,
`NB_REQ_REF_LOOKUP_ID` INT(11) NOT NULL,
PRIMARY KEY (`id`),
INDEX `fk_orchestration_flow_reference__northbound_req_ref_look_idx` (`NB_REQ_REF_LOOKUP_ID` ASC),
UNIQUE INDEX `UK_orchestration_flow_reference` (`COMPOSITE_ACTION` ASC, `FLOW_NAME` ASC, `SEQ_NO` ASC, `NB_REQ_REF_LOOKUP_ID` ASC),
CONSTRAINT `fk_orchestration_flow_reference__northbound_request_ref_look1` 
FOREIGN KEY (`NB_REQ_REF_LOOKUP_ID`) REFERENCES `northbound_request_ref_lookup` (`id`) 
ON DELETE CASCADE ON UPDATE CASCADE)
ENGINE = InnoDB DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `rainy_day_handler_macro` (
`id` INT(11) NOT NULL AUTO_INCREMENT,
`FLOW_NAME` VARCHAR(200) NOT NULL,
`SERVICE_TYPE` VARCHAR(200) NOT NULL,
`VNF_TYPE` VARCHAR(200) NOT NULL,
`ERROR_CODE` VARCHAR(200) NOT NULL,
`WORK_STEP` VARCHAR(200) NOT NULL,
`POLICY` VARCHAR(200) NOT NULL,
PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;