
--------START Catalog DB SCHEMA --------
use catalogdb;
set foreign_key_checks=0;
DROP TABLE IF EXISTS `allotted_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `allotted_resource` (
  `MODEL_UUID` varchar(200) NOT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) NOT NULL,
  `MODEL_VERSION` varchar(20) NOT NULL,
  `MODEL_NAME` varchar(200) NOT NULL,
  `TOSCA_NODE_TYPE` varchar(200) DEFAULT NULL,
  `SUBCATEGORY` varchar(200) DEFAULT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`MODEL_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `allotted_resource_customization`
--

DROP TABLE IF EXISTS `allotted_resource_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `allotted_resource_customization` (
  `MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `MODEL_INSTANCE_NAME` varchar(200) NOT NULL,
  `PROVIDING_SERVICE_MODEL_UUID` varchar(200) DEFAULT NULL,
  `PROVIDING_SERVICE_MODEL_INVARIANT_UUID` varchar(200) DEFAULT NULL,
  `PROVIDING_SERVICE_MODEL_NAME` varchar(200) DEFAULT NULL,
  `TARGET_NETWORK_ROLE` varchar(200) DEFAULT NULL,
  `NF_TYPE` varchar(200) DEFAULT NULL,
  `NF_ROLE` varchar(200) DEFAULT NULL,
  `NF_FUNCTION` varchar(200) DEFAULT NULL,
  `NF_NAMING_CODE` varchar(200) DEFAULT NULL,
  `MIN_INSTANCES` int(11) DEFAULT NULL,
  `MAX_INSTANCES` int(11) DEFAULT NULL,
  `RESOURCE_INPUT` varchar(20000) DEFAULT NULL,
  `AR_MODEL_UUID` varchar(200) NOT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`),
  KEY `fk_allotted_resource_customization__allotted_resource1_idx` (`AR_MODEL_UUID`),
  CONSTRAINT `fk_allotted_resource_customization__allotted_resource1` FOREIGN KEY (`AR_MODEL_UUID`) REFERENCES `allotted_resource` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `allotted_resource_customization_to_service`
--

DROP TABLE IF EXISTS `allotted_resource_customization_to_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `allotted_resource_customization_to_service` (
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  `RESOURCE_MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`SERVICE_MODEL_UUID`,`RESOURCE_MODEL_CUSTOMIZATION_UUID`),
  KEY `RESOURCE_MODEL_CUSTOMIZATION_UUID` (`RESOURCE_MODEL_CUSTOMIZATION_UUID`),
  CONSTRAINT `allotted_resource_customization_to_service_ibfk_1` FOREIGN KEY (`SERVICE_MODEL_UUID`) REFERENCES `service` (`MODEL_UUID`) ON DELETE CASCADE,
  CONSTRAINT `allotted_resource_customization_to_service_ibfk_2` FOREIGN KEY (`RESOURCE_MODEL_CUSTOMIZATION_UUID`) REFERENCES `allotted_resource_customization` (`MODEL_CUSTOMIZATION_UUID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ar_recipe`
--

DROP TABLE IF EXISTS `ar_recipe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ar_recipe` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `MODEL_NAME` varchar(200) NOT NULL,
  `ACTION` varchar(200) NOT NULL,
  `VERSION_STR` varchar(200) NOT NULL,
  `SERVICE_TYPE` varchar(200) DEFAULT NULL,
  `DESCRIPTION` varchar(200) DEFAULT NULL,
  `ORCHESTRATION_URI` varchar(200) NOT NULL,
  `AR_PARAM_XSD` varchar(200) DEFAULT NULL,
  `RECIPE_TIMEOUT` int(11) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `uk_ar_recipe` (`MODEL_NAME`,`ACTION`,`VERSION_STR`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `building_block_detail`
--

DROP TABLE IF EXISTS `building_block_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `building_block_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `BUILDING_BLOCK_NAME` varchar(200) NOT NULL,
  `RESOURCE_TYPE` varchar(25) NOT NULL,
  `TARGET_ACTION` varchar(25) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_building_block_name` (`BUILDING_BLOCK_NAME`)
) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cloud_sites`
--

DROP TABLE IF EXISTS `cloud_sites`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cloud_sites` (
  `ID` varchar(50) NOT NULL,
  `REGION_ID` varchar(11) DEFAULT NULL,
  `IDENTITY_SERVICE_ID` varchar(50) DEFAULT NULL,
  `CLOUD_VERSION` varchar(20) DEFAULT NULL,
  `CLLI` varchar(11) DEFAULT NULL,
  `CLOUDIFY_ID` varchar(50) DEFAULT NULL,
  `PLATFORM` varchar(50) DEFAULT NULL,
  `ORCHESTRATOR` varchar(50) DEFAULT NULL,
  `LAST_UPDATED_BY` varchar(120) DEFAULT NULL,
  `CREATION_TIMESTAMP` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `UPDATE_TIMESTAMP` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  KEY `FK_cloud_sites_identity_services` (`IDENTITY_SERVICE_ID`),
  CONSTRAINT `FK_cloud_sites_identity_services` FOREIGN KEY (`IDENTITY_SERVICE_ID`) REFERENCES `identity_services` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cloudify_managers`
--

DROP TABLE IF EXISTS `cloudify_managers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cloudify_managers` (
  `ID` varchar(50) NOT NULL,
  `CLOUDIFY_URL` varchar(200) DEFAULT NULL,
  `USERNAME` varchar(255) DEFAULT NULL,
  `PASSWORD` varchar(255) DEFAULT NULL,
  `VERSION` varchar(20) DEFAULT NULL,
  `LAST_UPDATED_BY` varchar(120) DEFAULT NULL,
  `CREATION_TIMESTAMP` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `UPDATE_TIMESTAMP` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `collection_network_resource_customization`
--

DROP TABLE IF EXISTS `collection_network_resource_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `collection_network_resource_customization` (
  `MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `MODEL_INSTANCE_NAME` varchar(200) NOT NULL,
  `NETWORK_TECHNOLOGY` varchar(45) DEFAULT NULL,
  `NETWORK_TYPE` varchar(45) DEFAULT NULL,
  `NETWORK_ROLE` varchar(200) DEFAULT NULL,
  `NETWORK_SCOPE` varchar(45) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `NETWORK_RESOURCE_MODEL_UUID` varchar(200) NOT NULL,
  `INSTANCE_GROUP_MODEL_UUID` varchar(200) DEFAULT NULL,
  `CRC_MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`,`CRC_MODEL_CUSTOMIZATION_UUID`),
  KEY `fk_collection_net_resource_customization__network_resource1_idx` (`NETWORK_RESOURCE_MODEL_UUID`),
  KEY `fk_collection_net_resource_customization__instance_group1_idx` (`INSTANCE_GROUP_MODEL_UUID`),
  KEY `fk_col_net_res_customization__collection_res_customization_idx` (`CRC_MODEL_CUSTOMIZATION_UUID`),
  CONSTRAINT `fk_collection_net_resource_customization__instance_group10` FOREIGN KEY (`INSTANCE_GROUP_MODEL_UUID`) REFERENCES `instance_group` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_collection_net_resource_customization__network_resource10` FOREIGN KEY (`NETWORK_RESOURCE_MODEL_UUID`) REFERENCES `network_resource` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_collection_network_resource_customization__collection_reso1` FOREIGN KEY (`CRC_MODEL_CUSTOMIZATION_UUID`) REFERENCES `collection_resource_customization` (`MODEL_CUSTOMIZATION_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `collection_resource`
--

DROP TABLE IF EXISTS `collection_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `collection_resource` (
  `MODEL_UUID` varchar(200) NOT NULL,
  `MODEL_NAME` varchar(200) NOT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) NOT NULL,
  `MODEL_VERSION` varchar(20) NOT NULL,
  `TOSCA_NODE_TYPE` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`MODEL_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `collection_resource_customization`
--

DROP TABLE IF EXISTS `collection_resource_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `collection_resource_customization` (
  `MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `MODEL_INSTANCE_NAME` varchar(200) NOT NULL,
  `role` varchar(200) DEFAULT NULL,
  `object_type` varchar(200) NOT NULL,
  `function` varchar(200) DEFAULT NULL,
  `collection_resource_type` varchar(200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `CR_MODEL_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`),
  KEY `CR_MODEL_UUID` (`CR_MODEL_UUID`),
  CONSTRAINT `collection_resource_customization_ibfk_1` FOREIGN KEY (`CR_MODEL_UUID`) REFERENCES `collection_resource` (`MODEL_UUID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `collection_resource_customization_to_service`
--

DROP TABLE IF EXISTS `collection_resource_customization_to_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `collection_resource_customization_to_service` (
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  `RESOURCE_MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`SERVICE_MODEL_UUID`,`RESOURCE_MODEL_CUSTOMIZATION_UUID`),
  KEY `RESOURCE_MODEL_CUSTOMIZATION_UUID` (`RESOURCE_MODEL_CUSTOMIZATION_UUID`),
  CONSTRAINT `collection_resource_customization_to_service_ibfk_1` FOREIGN KEY (`SERVICE_MODEL_UUID`) REFERENCES `service` (`MODEL_UUID`) ON DELETE CASCADE,
  CONSTRAINT `collection_resource_customization_to_service_ibfk_2` FOREIGN KEY (`RESOURCE_MODEL_CUSTOMIZATION_UUID`) REFERENCES `collection_resource_customization` (`MODEL_CUSTOMIZATION_UUID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `collection_resource_instance_group_customization`
--

DROP TABLE IF EXISTS `collection_resource_instance_group_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `collection_resource_instance_group_customization` (
  `COLLECTION_RESOURCE_CUSTOMIZATION_MODEL_UUID` varchar(200) NOT NULL,
  `INSTANCE_GROUP_MODEL_UUID` varchar(200) NOT NULL,
  `FUNCTION` varchar(200) DEFAULT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `SUBINTERFACE_NETWORK_QUANTITY` int(11) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`COLLECTION_RESOURCE_CUSTOMIZATION_MODEL_UUID`,`INSTANCE_GROUP_MODEL_UUID`),
  KEY `fk_collection_resource_instance_group_customization__instan_idx` (`INSTANCE_GROUP_MODEL_UUID`),
  CONSTRAINT `fk_collection_resource_instance_group_customization__collecti1` FOREIGN KEY (`COLLECTION_RESOURCE_CUSTOMIZATION_MODEL_UUID`) REFERENCES `collection_resource_customization` (`MODEL_CUSTOMIZATION_UUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_collection_resource_instance_group_customization__instance1` FOREIGN KEY (`INSTANCE_GROUP_MODEL_UUID`) REFERENCES `instance_group` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `configuration`
--

DROP TABLE IF EXISTS `configuration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `configuration` (
  `MODEL_UUID` varchar(200) NOT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) NOT NULL,
  `MODEL_VERSION` varchar(20) NOT NULL,
  `MODEL_NAME` varchar(200) NOT NULL,
  `TOSCA_NODE_TYPE` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`MODEL_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `configuration_customization`
--

DROP TABLE IF EXISTS `configuration_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `configuration_customization` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `MODEL_INSTANCE_NAME` varchar(200) NOT NULL,
  `CONFIGURATION_TYPE` varchar(200) DEFAULT NULL,
  `CONFIGURATION_ROLE` varchar(200) DEFAULT NULL,
  `CONFIGURATION_FUNCTION` varchar(200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `CONFIGURATION_MODEL_UUID` varchar(200) NOT NULL,
  `SERVICE_PROXY_CUSTOMIZATION_MODEL_CUSTOMIZATION_UUID` varchar(200) DEFAULT NULL,
  `CONFIGURATION_CUSTOMIZATION_MODEL_CUSTOMIZATION_ID` int(11) DEFAULT NULL,
  `SERVICE_MODEL_UUID` varchar(200),
   PRIMARY KEY (`ID`),
   KEY `fk_configuration_customization__configuration_idx` (`CONFIGURATION_MODEL_UUID`),
   KEY `fk_configuration_customization__service_idx` (`SERVICE_MODEL_UUID`),
   UNIQUE KEY `uk_configuration_customization`  (`MODEL_CUSTOMIZATION_UUID` ASC, `SERVICE_MODEL_UUID` ASC),
   CONSTRAINT `fk_configuration_customization__configuration1` FOREIGN KEY (`CONFIGURATION_MODEL_UUID`)
        REFERENCES `configuration` (`MODEL_UUID`)
        ON DELETE CASCADE ON UPDATE CASCADE,
   CONSTRAINT `fk_configuration_customization__service1` FOREIGN KEY (`SERVICE_MODEL_UUID`)
        REFERENCES `service` (`MODEL_UUID`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `controller_selection_reference`
--

DROP TABLE IF EXISTS `controller_selection_reference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `controller_selection_reference` (
  `VNF_TYPE` varchar(50) NOT NULL,
  `CONTROLLER_NAME` varchar(100) NOT NULL,
  `ACTION_CATEGORY` varchar(15) NOT NULL,
  PRIMARY KEY (`VNF_TYPE`,`CONTROLLER_NAME`,`ACTION_CATEGORY`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cvnfc_configuration_customization`
--

DROP TABLE IF EXISTS `cvnfc_configuration_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cvnfc_configuration_customization` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `MODEL_INSTANCE_NAME` varchar(200) NOT NULL,
  `CONFIGURATION_TYPE` varchar(200) DEFAULT NULL,
  `CONFIGURATION_ROLE` varchar(200) DEFAULT NULL,
  `CONFIGURATION_FUNCTION` varchar(200) DEFAULT NULL,
  `POLICY_NAME` varchar(200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `CONFIGURATION_MODEL_UUID` varchar(200) NOT NULL,
  `CVNFC_CUSTOMIZATION_ID` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `fk_vnf_vfmodule_cvnfc_config_cust__configuration_idx` (`CONFIGURATION_MODEL_UUID`),
  CONSTRAINT `fk_vnf_vfmod_cvnfc_config_cust__configuration_resource` FOREIGN KEY (`CONFIGURATION_MODEL_UUID`) REFERENCES `configuration` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=20655 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cvnfc_customization`
--

DROP TABLE IF EXISTS `cvnfc_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cvnfc_customization` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `MODEL_INSTANCE_NAME` varchar(200) NOT NULL,
  `MODEL_UUID` varchar(200) NOT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) NOT NULL,
  `MODEL_VERSION` varchar(20) NOT NULL,
  `MODEL_NAME` varchar(200) NOT NULL,
  `TOSCA_NODE_TYPE` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `NFC_FUNCTION` varchar(200) DEFAULT NULL,
  `NFC_NAMING_CODE` varchar(200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `VNFC_CUST_MODEL_CUSTOMIZATION_UUID` varchar(200) DEFAULT NULL,
  `VF_MODULE_CUSTOMIZATION_ID` int(13) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `fk_cvnfc_customization__vnfc_customization1_idx` (`VNFC_CUST_MODEL_CUSTOMIZATION_UUID`),
  KEY `fk_cvnfc_customization__vnf_vfmod_cvnfc_config_cust1_idx` (`MODEL_CUSTOMIZATION_UUID`),
  KEY `fk_cvnfc_customization_to_vf_module_resource_customization` (`VF_MODULE_CUSTOMIZATION_ID`),
  CONSTRAINT `fk_cvnfc_customization__vnfc_customization1` FOREIGN KEY (`VNFC_CUST_MODEL_CUSTOMIZATION_UUID`) REFERENCES `vnfc_customization` (`MODEL_CUSTOMIZATION_UUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_cvnfc_customization_to_vf_module_resource_customization` FOREIGN KEY (`VF_MODULE_CUSTOMIZATION_ID`) REFERENCES `vf_module_customization` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=20655 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `external_service_to_internal_model_mapping`
--

DROP TABLE IF EXISTS `external_service_to_internal_model_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `external_service_to_internal_model_mapping` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `SERVICE_NAME` varchar(200) NOT NULL,
  `PRODUCT_FLAVOR` varchar(200) DEFAULT NULL,
  `SUBSCRIPTION_SERVICE_TYPE` varchar(200) NOT NULL,
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_external_service_to_internal_model_mapping` (`SERVICE_NAME`,`PRODUCT_FLAVOR`,`SERVICE_MODEL_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for Liquibase tracking tables
--

DROP TABLE IF EXISTS `DATABASECHANGELOGLOCK`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DATABASECHANGELOGLOCK` (
  `ID` int(11) NOT NULL,
  `LOCKED` tinyint(1) NOT NULL,
  `LOCKGRANTED` datetime DEFAULT NULL,
  `LOCKEDBY` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE IF EXISTS `DATABASECHANGELOG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DATABASECHANGELOG` (
  `ID` varchar(255) NOT NULL,
  `AUTHOR` varchar(255) NOT NULL,
  `FILENAME` varchar(255) NOT NULL,
  `DATEEXECUTED` datetime NOT NULL,
  `ORDEREXECUTED` int(11) NOT NULL,
  `EXECTYPE` varchar(10) NOT NULL,
  `MD5SUM` varchar(35) DEFAULT NULL,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `COMMENTS` varchar(255) DEFAULT NULL,
  `TAG` varchar(255) DEFAULT NULL,
  `LIQUIBASE` varchar(20) DEFAULT NULL,
  `CONTEXTS` varchar(255) DEFAULT NULL,
  `LABELS` varchar(255) DEFAULT NULL,
  `DEPLOYMENT_ID` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `heat_environment`
--

DROP TABLE IF EXISTS `heat_environment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `heat_environment` (
  `ARTIFACT_UUID` varchar(200) NOT NULL,
  `NAME` varchar(100) NOT NULL,
  `VERSION` varchar(20) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `BODY` longtext NOT NULL,
  `ARTIFACT_CHECKSUM` varchar(200) NOT NULL DEFAULT 'MANUAL RECORD',
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ARTIFACT_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `heat_files`
--

DROP TABLE IF EXISTS `heat_files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `heat_files` (
  `ARTIFACT_UUID` varchar(200) NOT NULL,
  `NAME` varchar(200) NOT NULL,
  `VERSION` varchar(20) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `BODY` longtext NOT NULL,
  `ARTIFACT_CHECKSUM` varchar(200) NOT NULL DEFAULT 'MANUAL RECORD',
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ARTIFACT_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `heat_nested_template`
--

DROP TABLE IF EXISTS `heat_nested_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `heat_nested_template` (
  `PARENT_HEAT_TEMPLATE_UUID` varchar(200) NOT NULL,
  `CHILD_HEAT_TEMPLATE_UUID` varchar(200) NOT NULL,
  `PROVIDER_RESOURCE_FILE` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`PARENT_HEAT_TEMPLATE_UUID`,`CHILD_HEAT_TEMPLATE_UUID`),
  KEY `fk_heat_nested_template__heat_template2_idx` (`CHILD_HEAT_TEMPLATE_UUID`),
  CONSTRAINT `fk_heat_nested_template__child_heat_temp_uuid__heat_template1` FOREIGN KEY (`CHILD_HEAT_TEMPLATE_UUID`) REFERENCES `heat_template` (`ARTIFACT_UUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_heat_nested_template__parent_heat_temp_uuid__heat_template1` FOREIGN KEY (`PARENT_HEAT_TEMPLATE_UUID`) REFERENCES `heat_template` (`ARTIFACT_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `heat_template`
--

DROP TABLE IF EXISTS `heat_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `heat_template` (
  `ARTIFACT_UUID` varchar(200) NOT NULL,
  `NAME` varchar(200) NOT NULL,
  `VERSION` varchar(20) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `BODY` longtext NOT NULL,
  `TIMEOUT_MINUTES` int(11) DEFAULT NULL,
  `ARTIFACT_CHECKSUM` varchar(200) NOT NULL DEFAULT 'MANUAL RECORD',
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ARTIFACT_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `heat_template_params`
--

DROP TABLE IF EXISTS `heat_template_params`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `heat_template_params` (
  `HEAT_TEMPLATE_ARTIFACT_UUID` varchar(200) NOT NULL,
  `PARAM_NAME` varchar(100) NOT NULL,
  `IS_REQUIRED` bit(1) NOT NULL,
  `PARAM_TYPE` varchar(20) DEFAULT NULL,
  `PARAM_ALIAS` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`HEAT_TEMPLATE_ARTIFACT_UUID`,`PARAM_NAME`),
  CONSTRAINT `fk_heat_template_params__heat_template1` FOREIGN KEY (`HEAT_TEMPLATE_ARTIFACT_UUID`) REFERENCES `heat_template` (`ARTIFACT_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `identity_services`
--

DROP TABLE IF EXISTS `identity_services`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `identity_services` (
  `ID` varchar(50) NOT NULL,
  `IDENTITY_URL` varchar(200) DEFAULT NULL,
  `MSO_ID` varchar(255) DEFAULT NULL,
  `MSO_PASS` varchar(255) DEFAULT NULL,
  `ADMIN_TENANT` varchar(50) DEFAULT NULL,
  `MEMBER_ROLE` varchar(50) DEFAULT NULL,
  `TENANT_METADATA` tinyint(1) DEFAULT '0',
  `IDENTITY_SERVER_TYPE` varchar(50) DEFAULT NULL,
  `IDENTITY_AUTHENTICATION_TYPE` varchar(50) DEFAULT NULL,
  `LAST_UPDATED_BY` varchar(120) DEFAULT NULL,
  `CREATION_TIMESTAMP` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `UPDATE_TIMESTAMP` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `PROJECT_DOMAIN_NAME` varchar(255) DEFAULT NULL,
  `USER_DOMAIN_NAME` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instance_group`
--

DROP TABLE IF EXISTS `instance_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `instance_group` (
  `MODEL_UUID` varchar(200) NOT NULL,
  `MODEL_NAME` varchar(200) NOT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) NOT NULL,
  `MODEL_VERSION` varchar(20) NOT NULL,
  `TOSCA_NODE_TYPE` varchar(200) DEFAULT NULL,
  `ROLE` varchar(200) NOT NULL,
  `OBJECT_TYPE` varchar(200) NOT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `CR_MODEL_UUID` varchar(200) DEFAULT NULL,
  `INSTANCE_GROUP_TYPE` varchar(200) NOT NULL,
  PRIMARY KEY (`MODEL_UUID`),
  KEY `CR_MODEL_UUID` (`CR_MODEL_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `model`
--

DROP TABLE IF EXISTS `model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `model` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `MODEL_CUSTOMIZATION_ID` varchar(40) DEFAULT NULL,
  `MODEL_CUSTOMIZATION_NAME` varchar(40) DEFAULT NULL,
  `MODEL_INVARIANT_ID` varchar(40) DEFAULT NULL,
  `MODEL_NAME` varchar(40) DEFAULT NULL,
  `MODEL_TYPE` varchar(20) DEFAULT NULL,
  `MODEL_VERSION` varchar(20) DEFAULT NULL,
  `MODEL_VERSION_ID` varchar(40) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `RECIPE` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `uk1_model` (`MODEL_TYPE`,`MODEL_VERSION_ID`),
  KEY `RECIPE` (`RECIPE`),
  CONSTRAINT `model_ibfk_1` FOREIGN KEY (`RECIPE`) REFERENCES `model_recipe` (`MODEL_ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `model_recipe`
--

DROP TABLE IF EXISTS `model_recipe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `model_recipe` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `MODEL_ID` int(11) DEFAULT NULL,
  `ACTION` varchar(40) DEFAULT NULL,
  `SCHEMA_VERSION` varchar(40) DEFAULT NULL,
  `DESCRIPTION` varchar(40) DEFAULT NULL,
  `ORCHESTRATION_URI` varchar(20) DEFAULT NULL,
  `MODEL_PARAM_XSD` varchar(20) DEFAULT NULL,
  `RECIPE_TIMEOUT` int(11) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `uk1_model_recipe` (`MODEL_ID`,`ACTION`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `network_recipe`
--

DROP TABLE IF EXISTS `network_recipe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `network_recipe` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `MODEL_NAME` varchar(20) NOT NULL,
  `ACTION` varchar(50) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `ORCHESTRATION_URI` varchar(256) NOT NULL,
  `NETWORK_PARAM_XSD` varchar(2048) DEFAULT NULL,
  `RECIPE_TIMEOUT` int(11) DEFAULT NULL,
  `SERVICE_TYPE` varchar(45) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `VERSION_STR` varchar(20) NOT NULL,
  `RESOURCE_CATEGORY` varchar(200) DEFAULT NULL,
  `RESOURCE_SUB_CATEGORY` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_rl4f296i0p8lyokxveaiwkayi` (`MODEL_NAME`,`ACTION`,`VERSION_STR`)
) ENGINE=InnoDB AUTO_INCREMENT=181 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `network_resource`
--

DROP TABLE IF EXISTS `network_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `network_resource` (
  `MODEL_UUID` varchar(200) NOT NULL,
  `MODEL_NAME` varchar(200) NOT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) DEFAULT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `HEAT_TEMPLATE_ARTIFACT_UUID` varchar(200) NULL,
  `NEUTRON_NETWORK_TYPE` varchar(20) DEFAULT NULL,
  `MODEL_VERSION` varchar(20) DEFAULT NULL,
  `TOSCA_NODE_TYPE` varchar(200) DEFAULT NULL,
  `AIC_VERSION_MIN` varchar(20) NULL,
  `AIC_VERSION_MAX` varchar(20) DEFAULT NULL,
  `ORCHESTRATION_MODE` varchar(20) NOT NULL DEFAULT 'HEAT',
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `RESOURCE_CATEGORY` varchar(200) DEFAULT NULL,
  `RESOURCE_SUB_CATEGORY` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`MODEL_UUID`),
  KEY `fk_network_resource__temp_network_heat_template_lookup1_idx` (`MODEL_NAME`),
  KEY `fk_network_resource__heat_template1_idx` (`HEAT_TEMPLATE_ARTIFACT_UUID`),
  CONSTRAINT `fk_network_resource__heat_template1` FOREIGN KEY (`HEAT_TEMPLATE_ARTIFACT_UUID`) REFERENCES `heat_template` (`ARTIFACT_UUID`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_network_resource__temp_network_heat_template_lookup__mod_nm1` FOREIGN KEY (`MODEL_NAME`) REFERENCES `temp_network_heat_template_lookup` (`NETWORK_RESOURCE_MODEL_NAME`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `network_resource_customization`
--

DROP TABLE IF EXISTS `network_resource_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `network_resource_customization` (
  `MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `MODEL_INSTANCE_NAME` varchar(200) NOT NULL,
  `NETWORK_TECHNOLOGY` varchar(45) DEFAULT NULL,
  `NETWORK_TYPE` varchar(45) DEFAULT NULL,
  `NETWORK_ROLE` varchar(200) DEFAULT NULL,
  `NETWORK_SCOPE` varchar(45) DEFAULT NULL,
  `RESOURCE_INPUT` varchar(20000) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `NETWORK_RESOURCE_MODEL_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`),
  KEY `fk_network_resource_customization__network_resource1_idx` (`NETWORK_RESOURCE_MODEL_UUID`),
  CONSTRAINT `fk_network_resource_customization__network_resource1` FOREIGN KEY (`NETWORK_RESOURCE_MODEL_UUID`) REFERENCES `network_resource` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `network_resource_customization_to_service`
--

DROP TABLE IF EXISTS `network_resource_customization_to_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `network_resource_customization_to_service` (
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  `RESOURCE_MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`SERVICE_MODEL_UUID`,`RESOURCE_MODEL_CUSTOMIZATION_UUID`),
  KEY `RESOURCE_MODEL_CUSTOMIZATION_UUID` (`RESOURCE_MODEL_CUSTOMIZATION_UUID`),
  CONSTRAINT `network_resource_customization_to_service_ibfk_1` FOREIGN KEY (`SERVICE_MODEL_UUID`) REFERENCES `service` (`MODEL_UUID`) ON DELETE CASCADE,
  CONSTRAINT `network_resource_customization_to_service_ibfk_2` FOREIGN KEY (`RESOURCE_MODEL_CUSTOMIZATION_UUID`) REFERENCES `network_resource_customization` (`MODEL_CUSTOMIZATION_UUID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `northbound_request_ref_lookup`
--

DROP TABLE IF EXISTS `northbound_request_ref_lookup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `northbound_request_ref_lookup` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `REQUEST_SCOPE` varchar(200) NOT NULL,
  `MACRO_ACTION` varchar(200) NOT NULL,
  `ACTION` varchar(200) NOT NULL,
  `IS_ALACARTE` tinyint(1) NOT NULL DEFAULT '0',
  `MIN_API_VERSION` double NOT NULL,
  `MAX_API_VERSION` double DEFAULT NULL,
  `IS_TOPLEVELFLOW` tinyint(1) DEFAULT NULL,
  `CLOUD_OWNER` varchar(200) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_northbound_request_ref_lookup` (`MIN_API_VERSION`,`REQUEST_SCOPE`,`ACTION`,`IS_ALACARTE`,`MACRO_ACTION`,`CLOUD_OWNER`)
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `orchestration_flow_reference`
--

DROP TABLE IF EXISTS `orchestration_flow_reference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `orchestration_flow_reference` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `COMPOSITE_ACTION` varchar(200) NOT NULL,
  `SEQ_NO` int(11) NOT NULL,
  `FLOW_NAME` varchar(200) NOT NULL,
  `FLOW_VERSION` double NOT NULL,
  `SCOPE` varchar(200) DEFAULT NULL,
  `ACTION` varchar(200) DEFAULT NULL,
  `NB_REQ_REF_LOOKUP_ID` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_orchestration_flow_reference` (`COMPOSITE_ACTION`,`FLOW_NAME`,`SEQ_NO`,`NB_REQ_REF_LOOKUP_ID`),
  KEY `fk_orchestration_flow_reference__northbound_req_ref_look_idx` (`NB_REQ_REF_LOOKUP_ID`),
  KEY `fk_orchestration_flow_reference__building_block_detail` (`FLOW_NAME`),
  CONSTRAINT `fk_orchestration_flow_reference__northbound_request_ref_look1` FOREIGN KEY (`NB_REQ_REF_LOOKUP_ID`) REFERENCES `northbound_request_ref_lookup` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=398 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `orchestration_status_state_transition_directive`
--

DROP TABLE IF EXISTS `orchestration_status_state_transition_directive`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `orchestration_status_state_transition_directive` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `RESOURCE_TYPE` varchar(25) NOT NULL,
  `ORCHESTRATION_STATUS` varchar(25) NOT NULL,
  `TARGET_ACTION` varchar(25) NOT NULL,
  `FLOW_DIRECTIVE` varchar(25) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_orchestration_status_state_transition_directive` (`RESOURCE_TYPE`,`ORCHESTRATION_STATUS`,`TARGET_ACTION`)
) ENGINE=InnoDB AUTO_INCREMENT=686 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rainy_day_handler_macro`
--

DROP TABLE IF EXISTS `rainy_day_handler_macro`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rainy_day_handler_macro` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `FLOW_NAME` varchar(200) NOT NULL,
  `SERVICE_TYPE` varchar(200) NOT NULL,
  `VNF_TYPE` varchar(200) NOT NULL,
  `ERROR_CODE` varchar(200) NOT NULL,
  `WORK_STEP` varchar(200) NOT NULL,
  `POLICY` varchar(200) NOT NULL,
  `SECONDARY_POLICY` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=93 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service`
--

DROP TABLE IF EXISTS `service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service` (
  `MODEL_UUID` varchar(200) NOT NULL,
  `MODEL_NAME` varchar(200) NOT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) NOT NULL,
  `MODEL_VERSION` varchar(20) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `TOSCA_CSAR_ARTIFACT_UUID` varchar(200) DEFAULT NULL,
  `SERVICE_TYPE` varchar(200) DEFAULT NULL,
  `SERVICE_ROLE` varchar(200) DEFAULT NULL,
  `SERVICE_FUNCTION` varchar(200) DEFAULT NULL,
  `ENVIRONMENT_CONTEXT` varchar(200) DEFAULT NULL,
  `WORKLOAD_CONTEXT` varchar(200) DEFAULT NULL,
  `SERVICE_CATEGORY` varchar(200) DEFAULT NULL,
  `RESOURCE_ORDER` varchar(200) default NULL,
  `OVERALL_DISTRIBUTION_STATUS` varchar(45),
  `ONAP_GENERATED_NAMING` TINYINT(1) DEFAULT NULL,
  `NAMING_POLICY` varchar(200) DEFAULT NULL,
  `CDS_BLUEPRINT_NAME` varchar(200) DEFAULT NULL,
  `CDS_BLUEPRINT_VERSION` varchar(20) DEFAULT NULL,
  `CONTROLLER_ACTOR` varchar(200) DEFAULT NULL,
  `SKIP_POST_INSTANTIATION_CONFIGURATION` boolean default true,
  PRIMARY KEY (`MODEL_UUID`),
  KEY `fk_service__tosca_csar1_idx` (`TOSCA_CSAR_ARTIFACT_UUID`),
  CONSTRAINT `fk_service__tosca_csar1` FOREIGN KEY (`TOSCA_CSAR_ARTIFACT_UUID`) REFERENCES `tosca_csar` (`ARTIFACT_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service_proxy_customization`
--

DROP TABLE IF EXISTS `service_proxy_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service_proxy_customization` (
  `MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `MODEL_INSTANCE_NAME` varchar(200) NOT NULL,
  `MODEL_UUID` varchar(200) NOT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) NOT NULL,
  `MODEL_VERSION` varchar(20) NOT NULL,
  `MODEL_NAME` varchar(200) NOT NULL,
  `TOSCA_NODE_TYPE` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `SOURCE_SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`),
  KEY `fk_service_proxy_customization__service1_idx` (`SOURCE_SERVICE_MODEL_UUID`),
  CONSTRAINT `fk_service_proxy_resource_customization__service1` FOREIGN KEY (`SOURCE_SERVICE_MODEL_UUID`) REFERENCES `service` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service_proxy_customization_to_service`
--

DROP TABLE IF EXISTS `service_proxy_customization_to_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service_proxy_customization_to_service` (
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  `RESOURCE_MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`SERVICE_MODEL_UUID`,`RESOURCE_MODEL_CUSTOMIZATION_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service_recipe`
--

DROP TABLE IF EXISTS `service_recipe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service_recipe` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ACTION` varchar(50) NOT NULL,
  `VERSION_STR` varchar(20) DEFAULT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `ORCHESTRATION_URI` varchar(256) NOT NULL,
  `SERVICE_PARAM_XSD` varchar(2048) DEFAULT NULL,
  `RECIPE_TIMEOUT` int(11) DEFAULT NULL,
  `SERVICE_TIMEOUT_INTERIM` int(11) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_7fav5dkux2v8g9d2i5ymudlgc` (`SERVICE_MODEL_UUID`,`ACTION`),
  KEY `fk_service_recipe__service1_idx` (`SERVICE_MODEL_UUID`),
  CONSTRAINT `fk_service_recipe__service1` FOREIGN KEY (`SERVICE_MODEL_UUID`) REFERENCES `service` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=93 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `temp_network_heat_template_lookup`
--

DROP TABLE IF EXISTS `temp_network_heat_template_lookup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `temp_network_heat_template_lookup` (
  `NETWORK_RESOURCE_MODEL_NAME` varchar(200) NOT NULL,
  `HEAT_TEMPLATE_ARTIFACT_UUID` varchar(200) NULL,
  `AIC_VERSION_MIN` varchar(20) NULL,
  `AIC_VERSION_MAX` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`NETWORK_RESOURCE_MODEL_NAME`),
  KEY `fk_temp_network_heat_template_lookup__heat_template1_idx` (`HEAT_TEMPLATE_ARTIFACT_UUID`),
  CONSTRAINT `fk_temp_network_heat_template_lookup__heat_template1` FOREIGN KEY (`HEAT_TEMPLATE_ARTIFACT_UUID`) REFERENCES `heat_template` (`ARTIFACT_UUID`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tosca_csar`
--

DROP TABLE IF EXISTS `tosca_csar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tosca_csar` (
  `ARTIFACT_UUID` varchar(200) NOT NULL,
  `NAME` varchar(200) NOT NULL,
  `VERSION` varchar(20) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `ARTIFACT_CHECKSUM` varchar(200) NOT NULL,
  `URL` varchar(200) NOT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ARTIFACT_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vf_module`
--

DROP TABLE IF EXISTS `vf_module`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vf_module` (
  `MODEL_UUID` varchar(200) NOT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) DEFAULT NULL,
  `MODEL_VERSION` varchar(20) NOT NULL,
  `MODEL_NAME` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `IS_BASE` tinyint(1) NOT NULL,
  `HEAT_TEMPLATE_ARTIFACT_UUID` varchar(200) DEFAULT NULL,
  `VOL_HEAT_TEMPLATE_ARTIFACT_UUID` varchar(200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `VNF_RESOURCE_MODEL_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`MODEL_UUID`,`VNF_RESOURCE_MODEL_UUID`),
  KEY `fk_vf_module__vnf_resource1_idx` (`VNF_RESOURCE_MODEL_UUID`),
  KEY `fk_vf_module__heat_template_art_uuid__heat_template1_idx` (`HEAT_TEMPLATE_ARTIFACT_UUID`),
  KEY `fk_vf_module__vol_heat_template_art_uuid__heat_template2_idx` (`VOL_HEAT_TEMPLATE_ARTIFACT_UUID`),
  CONSTRAINT `fk_vf_module__heat_template_art_uuid__heat_template1` FOREIGN KEY (`HEAT_TEMPLATE_ARTIFACT_UUID`) REFERENCES `heat_template` (`ARTIFACT_UUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_vf_module__vnf_resource1` FOREIGN KEY (`VNF_RESOURCE_MODEL_UUID`) REFERENCES `vnf_resource` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_vf_module__vol_heat_template_art_uuid__heat_template2` FOREIGN KEY (`VOL_HEAT_TEMPLATE_ARTIFACT_UUID`) REFERENCES `heat_template` (`ARTIFACT_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vf_module_customization`
--

DROP TABLE IF EXISTS `vf_module_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vf_module_customization` (
  `ID` int(13) NOT NULL AUTO_INCREMENT,
  `MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `LABEL` varchar(200) DEFAULT NULL,
  `INITIAL_COUNT` int(11) NOT NULL DEFAULT '0',
  `MIN_INSTANCES` int(11) NOT NULL DEFAULT '0',
  `MAX_INSTANCES` int(11) DEFAULT NULL,
  `AVAILABILITY_ZONE_COUNT` int(11) DEFAULT NULL,
  `HEAT_ENVIRONMENT_ARTIFACT_UUID` varchar(200) DEFAULT NULL,
  `VOL_ENVIRONMENT_ARTIFACT_UUID` varchar(200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `VF_MODULE_MODEL_UUID` varchar(200) NOT NULL,
  `VNF_RESOURCE_CUSTOMIZATION_ID` int(13) DEFAULT NULL,
  `SKIP_POST_INSTANTIATION_CONFIGURATION` boolean default true,
  PRIMARY KEY (`ID`),
  KEY `fk_vf_module_customization__vf_module1_idx` (`VF_MODULE_MODEL_UUID`),
  KEY `fk_vf_module_customization__heat_env__heat_environment1_idx` (`HEAT_ENVIRONMENT_ARTIFACT_UUID`),
  KEY `fk_vf_module_customization__vol_env__heat_environment2_idx` (`VOL_ENVIRONMENT_ARTIFACT_UUID`),
  KEY `fk_vf_module_customization_to_vnf_resource_customization` (`VNF_RESOURCE_CUSTOMIZATION_ID`),
  KEY `vf_module_customization_model_cust_uuid_idx` (`MODEL_CUSTOMIZATION_UUID`),
  CONSTRAINT `fk_vf_module_customization__heat_env__heat_environment1` FOREIGN KEY (`HEAT_ENVIRONMENT_ARTIFACT_UUID`) REFERENCES `heat_environment` (`ARTIFACT_UUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_vf_module_customization__vf_module1` FOREIGN KEY (`VF_MODULE_MODEL_UUID`) REFERENCES `vf_module` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_vf_module_customization__vol_env__heat_environment2` FOREIGN KEY (`VOL_ENVIRONMENT_ARTIFACT_UUID`) REFERENCES `heat_environment` (`ARTIFACT_UUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_vf_module_customization_to_vnf_resource_customization` FOREIGN KEY (`VNF_RESOURCE_CUSTOMIZATION_ID`) REFERENCES `vnf_resource_customization` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vf_module_to_heat_files`
--

DROP TABLE IF EXISTS `vf_module_to_heat_files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vf_module_to_heat_files` (
  `VF_MODULE_MODEL_UUID` varchar(200) NOT NULL,
  `HEAT_FILES_ARTIFACT_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`VF_MODULE_MODEL_UUID`,`HEAT_FILES_ARTIFACT_UUID`),
  KEY `fk_vf_module_to_heat_files__heat_files__artifact_uuid1_idx` (`HEAT_FILES_ARTIFACT_UUID`),
  CONSTRAINT `fk_vf_module_to_heat_files__heat_files__artifact_uuid1` FOREIGN KEY (`HEAT_FILES_ARTIFACT_UUID`) REFERENCES `heat_files` (`ARTIFACT_UUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_vf_module_to_heat_files__vf_module__model_uuid1` FOREIGN KEY (`VF_MODULE_MODEL_UUID`) REFERENCES `vf_module` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='il fait ce qu''il dit';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vnf_components`
--

DROP TABLE IF EXISTS `vnf_components`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vnf_components` (
  `VNF_ID` int(11) NOT NULL,
  `COMPONENT_TYPE` varchar(20) NOT NULL,
  `HEAT_TEMPLATE_ID` int(11) DEFAULT NULL,
  `HEAT_ENVIRONMENT_ID` int(11) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`VNF_ID`,`COMPONENT_TYPE`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vnf_components_recipe`
--

DROP TABLE IF EXISTS `vnf_components_recipe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vnf_components_recipe` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `VNF_TYPE` varchar(200) DEFAULT NULL,
  `VNF_COMPONENT_TYPE` varchar(45) NOT NULL,
  `ACTION` varchar(50) NOT NULL,
  `SERVICE_TYPE` varchar(45) DEFAULT NULL,
  `VERSION` varchar(20) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `ORCHESTRATION_URI` varchar(256) NOT NULL,
  `VNF_COMPONENT_PARAM_XSD` varchar(2048) DEFAULT NULL,
  `RECIPE_TIMEOUT` int(11) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime DEFAULT CURRENT_TIMESTAMP,
  `VF_MODULE_MODEL_UUID` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_4dpdwddaaclhc11wxsb7h59ma` (`VF_MODULE_MODEL_UUID`,`VNF_COMPONENT_TYPE`,`ACTION`,`VERSION`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vnf_recipe`
--

DROP TABLE IF EXISTS `vnf_recipe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vnf_recipe` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `NF_ROLE` varchar(200) DEFAULT NULL,
  `ACTION` varchar(50) NOT NULL,
  `SERVICE_TYPE` varchar(45) DEFAULT NULL,
  `VERSION_STR` varchar(20) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `ORCHESTRATION_URI` varchar(256) NOT NULL,
  `VNF_PARAM_XSD` varchar(2048) DEFAULT NULL,
  `RECIPE_TIMEOUT` int(11) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime DEFAULT CURRENT_TIMESTAMP,
  `VF_MODULE_ID` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_f3tvqau498vrifq3cr8qnigkr` (`VF_MODULE_ID`,`ACTION`,`VERSION_STR`)
) ENGINE=InnoDB AUTO_INCREMENT=10015 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vnf_resource`
--

DROP TABLE IF EXISTS `vnf_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vnf_resource` (
  `ORCHESTRATION_MODE` varchar(20) NOT NULL DEFAULT 'HEAT',
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `MODEL_UUID` varchar(200) NOT NULL,
  `AIC_VERSION_MIN` varchar(20) DEFAULT NULL,
  `AIC_VERSION_MAX` varchar(20) DEFAULT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) DEFAULT NULL,
  `MODEL_VERSION` varchar(20) NOT NULL,
  `MODEL_NAME` varchar(200) DEFAULT NULL,
  `TOSCA_NODE_TYPE` varchar(200) DEFAULT NULL,
  `HEAT_TEMPLATE_ARTIFACT_UUID` varchar(200) DEFAULT NULL,
  `RESOURCE_CATEGORY` varchar(200) DEFAULT NULL,
  `RESOURCE_SUB_CATEGORY` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`MODEL_UUID`),
  KEY `fk_vnf_resource__heat_template1` (`HEAT_TEMPLATE_ARTIFACT_UUID`),
  CONSTRAINT `fk_vnf_resource__heat_template1` FOREIGN KEY (`HEAT_TEMPLATE_ARTIFACT_UUID`) REFERENCES `heat_template` (`ARTIFACT_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vnf_resource_customization`
--

DROP TABLE IF EXISTS `vnf_resource_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vnf_resource_customization` (
  `ID` int(13) NOT NULL AUTO_INCREMENT,
  `MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `MODEL_INSTANCE_NAME` varchar(200) NOT NULL,
  `MIN_INSTANCES` int(11) DEFAULT NULL,
  `MAX_INSTANCES` int(11) DEFAULT NULL,
  `AVAILABILITY_ZONE_MAX_COUNT` int(11) DEFAULT NULL,
  `NF_TYPE` varchar(200) DEFAULT NULL,
  `NF_ROLE` varchar(200) DEFAULT NULL,
  `NF_FUNCTION` varchar(200) DEFAULT NULL,
  `NF_NAMING_CODE` varchar(200) DEFAULT NULL,
  `MULTI_STAGE_DESIGN` varchar(20) DEFAULT NULL,
  `RESOURCE_INPUT` varchar(20000) DEFAULT NULL,
  `CDS_BLUEPRINT_NAME` varchar(200) default null,
  `CDS_BLUEPRINT_VERSION` varchar(20) default null,
  `SKIP_POST_INSTANTIATION_CONFIGURATION` boolean default true,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `VNF_RESOURCE_MODEL_UUID` varchar(200) NOT NULL,
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  `VNFCINSTANCEGROUP_ORDER` varchar(200) default NULL,
  `NF_DATA_VALID` tinyint(1) DEFAULT '0',
  `CONTROLLER_ACTOR` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_vnf_resource_customization` (`MODEL_CUSTOMIZATION_UUID`,`SERVICE_MODEL_UUID`),
  KEY `fk_vnf_resource_customization__vnf_resource1_idx` (`VNF_RESOURCE_MODEL_UUID`),
  KEY `fk_vnf_resource_customization_to_service` (`SERVICE_MODEL_UUID`),
  KEY `vnf_resource_customization_mod_cust_uuid_idx` (`MODEL_CUSTOMIZATION_UUID`),
  CONSTRAINT `fk_vnf_resource_customization__vnf_resource1` FOREIGN KEY (`VNF_RESOURCE_MODEL_UUID`) REFERENCES `vnf_resource` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_vnf_resource_customization_to_service` FOREIGN KEY (`SERVICE_MODEL_UUID`) REFERENCES `service` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vnfc_customization`
--

DROP TABLE IF EXISTS `vnfc_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vnfc_customization` (
  `MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `MODEL_INSTANCE_NAME` varchar(200) NOT NULL,
  `MODEL_UUID` varchar(200) NOT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) NOT NULL,
  `MODEL_VERSION` varchar(20) NOT NULL,
  `MODEL_NAME` varchar(200) NOT NULL,
  `TOSCA_NODE_TYPE` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `RESOURCE_INPUT` varchar(20000) DEFAULT NULL,
  `VNFC_INSTANCE_GROUP_CUSTOMIZATION_ID` integer DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vnfc_instance_group_customization`
--

DROP TABLE IF EXISTS `vnfc_instance_group_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vnfc_instance_group_customization` (
  `ID` int(13) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `VNF_RESOURCE_CUSTOMIZATION_ID` int(13) NOT NULL,
  `INSTANCE_GROUP_MODEL_UUID` varchar(200) NOT NULL,
  `FUNCTION` varchar(200) DEFAULT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `fk_vnfc_instance_group_customization__instance_group1_idx` (`INSTANCE_GROUP_MODEL_UUID`),
  CONSTRAINT `fk_vnfc_instance_group_customization__instance_group1` FOREIGN KEY (`INSTANCE_GROUP_MODEL_UUID`) REFERENCES `instance_group` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_vnfc_instance_group_customization_vnf_customization` FOREIGN KEY (`VNF_RESOURCE_CUSTOMIZATION_ID`) REFERENCES `vnf_resource_customization` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
set foreign_key_checks=1;

CREATE TABLE IF NOT EXISTS `pnf_resource` (
  `ORCHESTRATION_MODE` varchar(20) DEFAULT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `MODEL_UUID` varchar(200) NOT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) DEFAULT NULL,
  `MODEL_VERSION` varchar(20) NOT NULL,
  `MODEL_NAME` varchar(200) DEFAULT NULL,
  `TOSCA_NODE_TYPE` varchar(200) DEFAULT NULL,
  `RESOURCE_CATEGORY` varchar(200) DEFAULT NULL,
  `RESOURCE_SUB_CATEGORY` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`MODEL_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `pnf_resource_customization` (
  `MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `MODEL_INSTANCE_NAME` varchar(200) NOT NULL,
  `NF_TYPE` varchar(200) DEFAULT NULL,
  `NF_ROLE` varchar(200) DEFAULT NULL,
  `NF_FUNCTION` varchar(200) DEFAULT NULL,
  `NF_NAMING_CODE` varchar(200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `PNF_RESOURCE_MODEL_UUID` varchar(200) NOT NULL,
  `MULTI_STAGE_DESIGN` varchar(20) DEFAULT NULL,
  `RESOURCE_INPUT` varchar(2000) DEFAULT NULL,
  `CDS_BLUEPRINT_NAME` varchar(200) DEFAULT NULL,
  `CDS_BLUEPRINT_VERSION` varchar(20) DEFAULT NULL,
  `SKIP_POST_INSTANTIATION_CONFIGURATION` boolean default true,
  `CONTROLLER_ACTOR` varchar(200) DEFAULT NULL,
  `DEFAULT_SOFTWARE_VERSION` varchar(4000) DEFAULT NULL,
  PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`),
  KEY `fk_pnf_resource_customization__pnf_resource1_idx` (`PNF_RESOURCE_MODEL_UUID`),
  CONSTRAINT `fk_pnf_resource_customization__pnf_resource1` FOREIGN KEY (`PNF_RESOURCE_MODEL_UUID`) REFERENCES `pnf_resource` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `pnf_resource_customization_to_service` (
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  `RESOURCE_MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`SERVICE_MODEL_UUID`,`RESOURCE_MODEL_CUSTOMIZATION_UUID`)
)ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `workflow` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `ARTIFACT_UUID` varchar(200) NOT NULL,
  `ARTIFACT_NAME` varchar(200) NOT NULL,
  `NAME` varchar(200) NOT NULL,
  `OPERATION_NAME` varchar(200) DEFAULT NULL,
  `VERSION` double NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `BODY` longtext DEFAULT NULL,
  `RESOURCE_TARGET` varchar(200) NOT NULL,
  `SOURCE` varchar(200) NOT NULL,
  `TIMEOUT_MINUTES` int(11) DEFAULT NULL,
  `ARTIFACT_CHECKSUM` varchar(200) NULL DEFAULT 'MANUAL RECORD',
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_workflow` (`ARTIFACT_UUID`,`NAME`,`VERSION`,`SOURCE`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `vnf_resource_to_workflow` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `VNF_RESOURCE_MODEL_UUID` varchar(200) NOT NULL,
  `WORKFLOW_ID` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_vnf_resource_to_workflow` (`VNF_RESOURCE_MODEL_UUID`,`WORKFLOW_ID`),
  KEY `fk_vnf_resource_to_workflow__workflow1_idx` (`WORKFLOW_ID`),
  KEY `fk_vnf_resource_to_workflow__vnf_res_mod_uuid_idx` (`VNF_RESOURCE_MODEL_UUID`),
  CONSTRAINT `fk_vnf_resource_to_workflow__vnf_resource1` FOREIGN KEY (`VNF_RESOURCE_MODEL_UUID`) REFERENCES `vnf_resource` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_vnf_resource_to_workflow__workflow1` FOREIGN KEY (`WORKFLOW_ID`) REFERENCES `workflow` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `activity_spec` (
  `ID` INT(11) NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(200) NOT NULL,
  `DESCRIPTION` VARCHAR(1200) NOT NULL,
  `VERSION` DOUBLE NOT NULL,
  `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `UK_activity_spec` (`NAME` ASC, `VERSION` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `user_parameters` (
  `ID` INT(11) NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(200) NOT NULL,
  `PAYLOAD_LOCATION` VARCHAR(500) NULL,
  `LABEL` VARCHAR(200) NOT NULL,
  `TYPE` VARCHAR(200) NOT NULL,
  `DESCRIPTION` VARCHAR(1200) NULL,
  `IS_REQUIRED` TINYINT(1) NOT NULL,
  `MAX_LENGTH` INT(11) NULL,
  `ALLOWABLE_CHARS` VARCHAR(200) NULL,
  `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `UK_user_parameters` (`NAME` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `workflow_activity_spec_sequence` (
  `ID` INT(11) NOT NULL AUTO_INCREMENT,
  `WORKFLOW_ID` INT(11) NOT NULL,
  `ACTIVITY_SPEC_ID` INT(11) NOT NULL,
  `SEQ_NO` INT(11) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `UK_workflow_activity_spec_sequence` (`WORKFLOW_ID` ASC, `ACTIVITY_SPEC_ID` ASC, `SEQ_NO` ASC),
  INDEX `fk_workflow_activity_spec_sequence__activity_spec_idx` (`ACTIVITY_SPEC_ID` ASC),
  INDEX `fk_workflow_activity_spec_sequence__workflow_actifact_uuid_idx` (`WORKFLOW_ID` ASC),
  CONSTRAINT `fk_workflow_activity_spec_sequence__activity_spec1`
    FOREIGN KEY (`ACTIVITY_SPEC_ID`)
    REFERENCES `activity_spec` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_workflow_activity_spec_sequence__workflow1`
    FOREIGN KEY (`WORKFLOW_ID`)
    REFERENCES `workflow` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `activity_spec_parameters` (
  `ID` INT(11) NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(200) NOT NULL,
  `TYPE` VARCHAR(200) NOT NULL,
  `DIRECTION` VARCHAR(200) NULL,
  `DESCRIPTION` VARCHAR(1200) NULL,
  `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `UK_activity_spec_parameters` (`NAME` ASC, `DIRECTION` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `activity_spec_categories` (
  `ID` INT(11) NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(200) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `UK_activity_spec_categories` (`NAME` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `activity_spec_to_activity_spec_categories` (
  `ID` INT(11) NOT NULL AUTO_INCREMENT,
  `ACTIVITY_SPEC_ID` INT(11) NOT NULL,
  `ACTIVITY_SPEC_CATEGORIES_ID` INT(11) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `UK_activity_spec_to_activity_spec_categories` (`ACTIVITY_SPEC_ID` ASC, `ACTIVITY_SPEC_CATEGORIES_ID` ASC),
  INDEX `fk_activity_spec_to_activity_spec_categories__activity_spec_idx` (`ACTIVITY_SPEC_CATEGORIES_ID` ASC),
  INDEX `fk_activity_spec_to_activity_spec_categories__activity_spec_idx1` (`ACTIVITY_SPEC_ID` ASC),
  CONSTRAINT `fk_activity_spec_to_activity_spec_categories__activity_spec1`
    FOREIGN KEY (`ACTIVITY_SPEC_ID`)
    REFERENCES `activity_spec` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_activity_spec_to_activity_spec_categories__activity_spec_c1`
    FOREIGN KEY (`ACTIVITY_SPEC_CATEGORIES_ID`)
    REFERENCES `activity_spec_categories` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `activity_spec_to_activity_spec_parameters` (
  `ID` INT(11) NOT NULL AUTO_INCREMENT,
  `ACTIVITY_SPEC_ID` INT(11) NOT NULL,
  `ACTIVITY_SPEC_PARAMETERS_ID` INT(11) NOT NULL,
  PRIMARY KEY (`ID`),
  INDEX `fk_activity_spec_to_activity_spec_params__act_sp_param_id_idx` (`ACTIVITY_SPEC_PARAMETERS_ID` ASC),
  UNIQUE INDEX `UK_activity_spec_to_activity_spec_parameters` (`ACTIVITY_SPEC_ID` ASC, `ACTIVITY_SPEC_PARAMETERS_ID` ASC),
  INDEX `fk_activity_spec_to_activity_spec_parameters__act_spec_id_idx` (`ACTIVITY_SPEC_ID` ASC),
  CONSTRAINT `fk_activity_spec_to_activity_spec_parameters__activity_spec_1`
    FOREIGN KEY (`ACTIVITY_SPEC_ID`)
    REFERENCES `activity_spec` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_activity_spec_to_activity_spec_parameters__activ_spec_param1`
    FOREIGN KEY (`ACTIVITY_SPEC_PARAMETERS_ID`)
    REFERENCES `activity_spec_parameters` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `activity_spec_to_user_parameters` (
  `ID` INT(11) NOT NULL AUTO_INCREMENT,
  `ACTIVITY_SPEC_ID` INT(11) NOT NULL,
  `USER_PARAMETERS_ID` INT(11) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `UK_activity_spec_to_user_parameters` (`ACTIVITY_SPEC_ID` ASC, `USER_PARAMETERS_ID` ASC),
  INDEX `fk_activity_spec_to_user_parameters__user_parameters1_idx` (`USER_PARAMETERS_ID` ASC),
  INDEX `fk_activity_spec_to_user_parameters__activity_spec1_idx` (`ACTIVITY_SPEC_ID` ASC),
  CONSTRAINT `fk_activity_spec_to_user_parameters__activity_spec1`
    FOREIGN KEY (`ACTIVITY_SPEC_ID`)
    REFERENCES `activity_spec` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_activity_spec_to_user_parameters__user_parameters1`
    FOREIGN KEY (`USER_PARAMETERS_ID`)
    REFERENCES `user_parameters` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `service_info` (
  `ID` int (11) AUTO_INCREMENT,
  `SERVICE_INPUT` varchar (5000),
  `SERVICE_PROPERTIES` varchar (5000),
  `SERVICE_MODEL_UUID` varchar (200) NOT NULL,
  PRIMARY KEY (`ID`),
  CONSTRAINT `fk_service_info_service1` FOREIGN KEY (`SERVICE_MODEL_UUID`) REFERENCES `service` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `service_artifact`(
  `ARTIFACT_UUID` varchar (200) NOT NULL,
  `TYPE` varchar (200) NOT NULL,
  `NAME` varchar (200) NOT NULL,
  `VERSION` varchar (200) NOT NULL,
  `DESCRIPTION` varchar (200) DEFAULT NULL,
  `CONTENT` LONGTEXT DEFAULT NULL,
  `CHECKSUM` varchar (200) DEFAULT NULL,
  `CREATION_TIMESTAMP` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `SERVICE_MODEL_UUID` varchar (200) NOT NULL,
  PRIMARY KEY (`ARTIFACT_UUID`),
  CONSTRAINT `fk_service_artifact_service_info1` FOREIGN KEY (`SERVICE_MODEL_UUID`) REFERENCES `service` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=latin1;

--------START Request DB SCHEMA --------
CREATE DATABASE requestdb;
USE requestdb;


CREATE TABLE `active_requests` (
  `REQUEST_ID` varchar(45) NOT NULL,
  `CLIENT_REQUEST_ID` varchar(45) DEFAULT NULL,
  `SERVICE_INSTANCE_ID` varchar(50) NOT NULL,
  `SUBSCRIBER_NAME` varchar(200) DEFAULT NULL,
  `REQUEST_URI` varchar(255) DEFAULT NULL,
  `SERVICE_TYPE` varchar(65) NOT NULL,
  `REQUEST_ACTION` varchar(45) NOT NULL,
  `NOTIFICATION_URL` varchar(255) DEFAULT NULL,
  `REQUEST_ID_IN_PROGRESS` varchar(45) DEFAULT NULL,
  `START_TIME` datetime DEFAULT NULL,
  `MODIFY_TIME` datetime DEFAULT NULL,
  `COMPLETION_TIME` datetime DEFAULT NULL,
  `RESPONSE_CODE` varchar(20) DEFAULT NULL,
  `RESPONSE_BODY` longtext,
  `STATUS` varchar(25) DEFAULT NULL,
  `SERVICE_REQUEST_TIMEOUT` datetime DEFAULT NULL,
  `FINAL_ERROR_CODE` varchar(20) DEFAULT NULL,
  `FINAL_ERROR_MESSAGE` varchar(2000) DEFAULT NULL,
  `ORDER_NUMBER` varchar(45) DEFAULT NULL,
  `SOURCE` varchar(20) DEFAULT NULL,
  `RESPONSE_STATUS` varchar(25) DEFAULT NULL,
  `ORDER_VERSION` varchar(20) DEFAULT NULL,
  `LAST_MODIFIED_BY` varchar(20) DEFAULT NULL,
  `MOCARS_TICKET_NUM` varchar(200) DEFAULT NULL,
  `REQUEST_BODY` longtext,
  `REQUEST_SUB_ACTION` varchar(45) DEFAULT NULL,
  `SDNC_CALLBACK_BPEL_URL` varchar(255) DEFAULT NULL,
  `FEATURE_TYPE` varchar(255) DEFAULT NULL,
  `FEATURE_INSTANCE_ID` varchar(255) DEFAULT NULL,
  `REQUEST_TYPE` varchar(255) DEFAULT NULL,
  `INTERIM_COMPLETION_TIME` datetime DEFAULT NULL,
  `INTERIM_STAGE_COMPLETION` int(11) DEFAULT NULL,
  `SERVICE_NAME_VERSION_ID` varchar(50) DEFAULT NULL,
  `GLOBAL_SUBSCRIBER_ID` varchar(255) DEFAULT NULL,
  `SERVICE_ID` varchar(50) DEFAULT NULL,
  `SERVICE_VERSION` varchar(10) DEFAULT NULL,
  `CORRELATOR` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`REQUEST_ID`),
  UNIQUE KEY `UK_f0hdk7xbw5mb2trnxx0fvlh3x` (`CLIENT_REQUEST_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `infra_active_requests` (
  `REQUEST_ID` varchar(45) NOT NULL,
  `REQUEST_STATUS` varchar(20) DEFAULT NULL,
  `STATUS_MESSAGE` longtext DEFAULT NULL,
  `PROGRESS` bigint(20) DEFAULT NULL,
  `START_TIME` datetime DEFAULT NULL,
  `END_TIME` datetime DEFAULT NULL,
  `SOURCE` varchar(45) DEFAULT NULL,
  `VNF_ID` varchar(45) DEFAULT NULL,
  `VNF_NAME` varchar(80) DEFAULT NULL,
  `VNF_TYPE` varchar(200) DEFAULT NULL,
  `SERVICE_TYPE` varchar(45) DEFAULT NULL,
  `TENANT_ID` varchar(45) DEFAULT NULL,
  `VNF_PARAMS` longtext,
  `VNF_OUTPUTS` longtext,
  `REQUEST_BODY` longtext,
  `RESPONSE_BODY` longtext,
  `LAST_MODIFIED_BY` varchar(100) DEFAULT NULL,
  `MODIFY_TIME` datetime DEFAULT NULL,
  `VOLUME_GROUP_ID` varchar(45) DEFAULT NULL,
  `VOLUME_GROUP_NAME` varchar(45) DEFAULT NULL,
  `VF_MODULE_ID` varchar(45) DEFAULT NULL,
  `VF_MODULE_NAME` varchar(200) DEFAULT NULL,
  `VF_MODULE_MODEL_NAME` varchar(200) DEFAULT NULL,
  `CLOUD_REGION` varchar(11) DEFAULT NULL,
  `CALLBACK_URL` varchar(200) DEFAULT NULL,
  `CORRELATOR` varchar(80) DEFAULT NULL,
  `NETWORK_ID` varchar(45) DEFAULT NULL,
  `NETWORK_NAME` varchar(80) DEFAULT NULL,
  `NETWORK_TYPE` varchar(80) DEFAULT NULL,
  `REQUEST_SCOPE` varchar(20) NOT NULL DEFAULT 'unknown',
  `REQUEST_ACTION` varchar(45) NOT NULL DEFAULT 'unknown',
  `SERVICE_INSTANCE_ID` varchar(45) DEFAULT NULL,
  `SERVICE_INSTANCE_NAME` varchar(80) DEFAULT NULL,
  `REQUESTOR_ID` varchar(50) DEFAULT NULL,
  `CONFIGURATION_ID` varchar(45) DEFAULT NULL,
  `CONFIGURATION_NAME` varchar(200) DEFAULT NULL,
  `OPERATIONAL_ENV_ID` varchar(45) DEFAULT NULL,
  `OPERATIONAL_ENV_NAME` varchar(200) DEFAULT NULL,
  `REQUEST_URL` varchar(500) DEFAULT NULL,
  `ORIGINAL_REQUEST_ID` varchar(45) DEFAULT NULL,
  `EXT_SYSTEM_ERROR_SOURCE` varchar(80) DEFAULT NULL,
  `ROLLBACK_EXT_SYSTEM_ERROR_SOURCE` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`REQUEST_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `archived_infra_requests` (
  `REQUEST_ID` varchar(45) NOT NULL,
  `REQUEST_STATUS` varchar(20) DEFAULT NULL,
  `STATUS_MESSAGE` longtext DEFAULT NULL,
  `PROGRESS` bigint(20) DEFAULT NULL,
  `START_TIME` datetime DEFAULT NULL,
  `END_TIME` datetime DEFAULT NULL,
  `SOURCE` varchar(45) DEFAULT NULL,
  `VNF_ID` varchar(45) DEFAULT NULL,
  `VNF_NAME` varchar(80) DEFAULT NULL,
  `VNF_TYPE` varchar(200) DEFAULT NULL,
  `SERVICE_TYPE` varchar(45) DEFAULT NULL,
  `TENANT_ID` varchar(45) DEFAULT NULL,
  `VNF_PARAMS` longtext,
  `VNF_OUTPUTS` longtext,
  `REQUEST_BODY` longtext,
  `RESPONSE_BODY` longtext,
  `LAST_MODIFIED_BY` varchar(100) DEFAULT NULL,
  `MODIFY_TIME` datetime DEFAULT NULL,
  `VOLUME_GROUP_ID` varchar(45) DEFAULT NULL,
  `VOLUME_GROUP_NAME` varchar(45) DEFAULT NULL,
  `VF_MODULE_ID` varchar(45) DEFAULT NULL,
  `VF_MODULE_NAME` varchar(200) DEFAULT NULL,
  `VF_MODULE_MODEL_NAME` varchar(200) DEFAULT NULL,
  `CLOUD_REGION` varchar(11) DEFAULT NULL,
  `CALLBACK_URL` varchar(200) DEFAULT NULL,
  `CORRELATOR` varchar(80) DEFAULT NULL,
  `NETWORK_ID` varchar(45) DEFAULT NULL,
  `NETWORK_NAME` varchar(80) DEFAULT NULL,
  `NETWORK_TYPE` varchar(80) DEFAULT NULL,
  `REQUEST_SCOPE` varchar(20) NOT NULL DEFAULT 'unknown',
  `REQUEST_ACTION` varchar(45) NOT NULL DEFAULT 'unknown',
  `SERVICE_INSTANCE_ID` varchar(45) DEFAULT NULL,
  `SERVICE_INSTANCE_NAME` varchar(80) DEFAULT NULL,
  `REQUESTOR_ID` varchar(50) DEFAULT NULL,
  `CONFIGURATION_ID` varchar(45) DEFAULT NULL,
  `CONFIGURATION_NAME` varchar(200) DEFAULT NULL,
  `OPERATIONAL_ENV_ID` varchar(45) DEFAULT NULL,
  `OPERATIONAL_ENV_NAME` varchar(200) DEFAULT NULL,
  `REQUEST_URL` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`REQUEST_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `site_status` (
  `SITE_NAME` varchar(255) NOT NULL,
  `STATUS` bit(1) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`SITE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `watchdog_distributionid_status` (
  `DISTRIBUTION_ID` varchar(45) NOT NULL,
  `DISTRIBUTION_ID_STATUS` varchar(45) DEFAULT NULL,
  `LOCK_VERSION` int NOT NULL,
  `CREATE_TIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `MODIFY_TIME` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`DISTRIBUTION_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `watchdog_per_component_distribution_status` (
  `DISTRIBUTION_ID` varchar(45) NOT NULL,
  `COMPONENT_NAME` varchar(45) NOT NULL,
  `COMPONENT_DISTRIBUTION_STATUS` varchar(45) DEFAULT NULL,
  `CREATE_TIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `MODIFY_TIME` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`DISTRIBUTION_ID`,`COMPONENT_NAME`),
  CONSTRAINT `fk_watchdog_component_distribution_status_watchdog_distributi1` FOREIGN KEY (`DISTRIBUTION_ID`) REFERENCES `watchdog_distributionid_status` (`DISTRIBUTION_ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `watchdog_service_mod_ver_id_lookup` (
  `DISTRIBUTION_ID` varchar(45) NOT NULL,
  `SERVICE_MODEL_VERSION_ID` varchar(45) NOT NULL,
  `DISTRIBUTION_NOTIFICATION` LONGTEXT NULL,
  `CONSUMER_ID` varchar(200) NULL,
  `CREATE_TIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `MODIFY_TIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`DISTRIBUTION_ID`,`SERVICE_MODEL_VERSION_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `activate_operational_env_service_model_distribution_status` (
  `OPERATIONAL_ENV_ID` varchar(45) NOT NULL,
  `SERVICE_MODEL_VERSION_ID` varchar(45) NOT NULL,
  `REQUEST_ID` varchar(45) NOT NULL,
  `SERVICE_MOD_VER_FINAL_DISTR_STATUS` varchar(45) DEFAULT NULL,
  `RECOVERY_ACTION` varchar(30) DEFAULT NULL,
  `RETRY_COUNT_LEFT` int(11) DEFAULT NULL,
  `WORKLOAD_CONTEXT` varchar(80) NOT NULL,
  `CREATE_TIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `MODIFY_TIME` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `VNF_OPERATIONAL_ENV_ID` varchar(45) NOT NULL,
  PRIMARY KEY (`OPERATIONAL_ENV_ID`,`SERVICE_MODEL_VERSION_ID`,`REQUEST_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `activate_operational_env_per_distributionid_status` (
  `DISTRIBUTION_ID` varchar(45) NOT NULL,
  `DISTRIBUTION_ID_STATUS` varchar(45) DEFAULT NULL,
  `DISTRIBUTION_ID_ERROR_REASON` varchar(250) DEFAULT NULL,
  `CREATE_TIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `MODIFY_TIME` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `OPERATIONAL_ENV_ID` varchar(45) NOT NULL,
  `SERVICE_MODEL_VERSION_ID` varchar(45) NOT NULL,
  `REQUEST_ID` varchar(45) NOT NULL,
  PRIMARY KEY (`DISTRIBUTION_ID`),
  KEY `fk_activate_op_env_per_distributionid_status__aoesmds1_idx` (`OPERATIONAL_ENV_ID`,`SERVICE_MODEL_VERSION_ID`,`REQUEST_ID`),
  CONSTRAINT `fk_activate_op_env_per_distributionid_status__aoesmds1` FOREIGN KEY (`OPERATIONAL_ENV_ID`, `SERVICE_MODEL_VERSION_ID`, `REQUEST_ID`) REFERENCES `activate_operational_env_service_model_distribution_status` (`OPERATIONAL_ENV_ID`, `SERVICE_MODEL_VERSION_ID`, `REQUEST_ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

create table operation_status (
  SERVICE_ID varchar(255) not null,
  OPERATION_ID varchar(255) not null,
  SERVICE_NAME varchar(255),
  OPERATION_TYPE varchar(255),
  USER_ID varchar(255),
  RESULT varchar(255),
  OPERATION_CONTENT varchar(255),
  PROGRESS varchar(255),
  REASON varchar(255),
  OPERATE_AT datetime NOT NULL,
  FINISHED_AT datetime NOT NULL,
  primary key (SERVICE_ID,OPERATION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

create table resource_operation_status (
  SERVICE_ID varchar(255) not null,
  OPERATION_ID varchar(255) not null,
  RESOURCE_TEMPLATE_UUID varchar(255) not null,
  OPER_TYPE varchar(255),
  RESOURCE_INSTANCE_ID varchar(255),
  JOB_ID varchar(255),
  STATUS varchar(255),
  PROGRESS varchar(255),
  ERROR_CODE varchar(255) ,
  STATUS_DESCRIPOTION varchar(255) ,
  primary key (SERVICE_ID,OPERATION_ID,RESOURCE_TEMPLATE_UUID)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

create table if not exists model_recipe (
	`ID` INT(11) NOT NULL AUTO_INCREMENT,
	`MODEL_ID` INT(11),
	`ACTION` VARCHAR(40),
	`SCHEMA_VERSION` VARCHAR(40),
	`DESCRIPTION` VARCHAR(40),
	`ORCHESTRATION_URI` VARCHAR(20),
	`MODEL_PARAM_XSD` VARCHAR(20),
	`RECIPE_TIMEOUT` INT(11),
	`CREATION_TIMESTAMP` datetime not null default current_timestamp,
	PRIMARY KEY (`ID`),
	CONSTRAINT uk1_model_recipe UNIQUE (`MODEL_ID`, `ACTION`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

create table if not exists model (
	`ID` INT(11) NOT NULL AUTO_INCREMENT,
	`MODEL_CUSTOMIZATION_ID` VARCHAR(40),
	`MODEL_CUSTOMIZATION_NAME` VARCHAR(40),
	`MODEL_INVARIANT_ID` VARCHAR(40),
	`MODEL_NAME` VARCHAR(40),
	`MODEL_TYPE` VARCHAR(20),
	`MODEL_VERSION` VARCHAR(20),
	`MODEL_VERSION_ID` VARCHAR(40),
	`CREATION_TIMESTAMP` datetime not null default current_timestamp,
	`RECIPE` INT(11),
	PRIMARY KEY (`ID`),
	CONSTRAINT uk1_model UNIQUE (`MODEL_TYPE`, `MODEL_VERSION_ID`),
	FOREIGN KEY (`RECIPE`) REFERENCES `model_recipe` (`MODEL_ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
