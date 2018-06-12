-- MySQL dump 10.13  Distrib 5.6.21, for osx10.8 (x86_64)
--
-- Host: mso-ecomp-db.dev.att.com    Database: catalogdb
-- ------------------------------------------------------
-- Server version	5.5.5-10.2.12-MariaDB-10.2.12+maria~jessie-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `allotted_resource`
--
Use catalogdb;

--DROP TABLE IF EXISTS `allotted_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `allotted_resource` (
  `MODEL_UUID` varchar(200) NOT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) NOT NULL,
  `MODEL_VERSION` varchar(20) NOT NULL,
  `MODEL_NAME` varchar(200) NOT NULL,
  `TOSCA_NODE_TYPE` varchar(200) DEFAULT NULL,
  `SUBCATEGORY` varchar(200) DEFAULT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`MODEL_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `allotted_resource_customization`
--

--DROP TABLE IF EXISTS `allotted_resource_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `allotted_resource_customization` (
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
  `AR_MODEL_UUID` varchar(200) NOT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`),
  KEY `fk_allotted_resource_customization__allotted_resource1_idx` (`AR_MODEL_UUID`),
  CONSTRAINT `fk_allotted_resource_customization__allotted_resource1` FOREIGN KEY (`AR_MODEL_UUID`) REFERENCES `allotted_resource` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `allotted_resource_customization_to_service`
--

--DROP TABLE IF EXISTS `allotted_resource_customization_to_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `allotted_resource_customization_to_service` (
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  `RESOURCE_MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`SERVICE_MODEL_UUID`,`RESOURCE_MODEL_CUSTOMIZATION_UUID`),
  KEY `RESOURCE_MODEL_CUSTOMIZATION_UUID` (`RESOURCE_MODEL_CUSTOMIZATION_UUID`),
  CONSTRAINT `allotted_resource_customization_to_service_ibfk_1` FOREIGN KEY (`SERVICE_MODEL_UUID`) REFERENCES `service` (`MODEL_UUID`) ON DELETE CASCADE,
  CONSTRAINT `allotted_resource_customization_to_service_ibfk_2` FOREIGN KEY (`RESOURCE_MODEL_CUSTOMIZATION_UUID`) REFERENCES `allotted_resource_customization` (`MODEL_CUSTOMIZATION_UUID`) ON DELETE CASCADE,
  CONSTRAINT `allotted_resource_customization_to_service_ibfk_3` FOREIGN KEY (`SERVICE_MODEL_UUID`) REFERENCES `service` (`MODEL_UUID`) ON DELETE CASCADE,
  CONSTRAINT `allotted_resource_customization_to_service_ibfk_4` FOREIGN KEY (`RESOURCE_MODEL_CUSTOMIZATION_UUID`) REFERENCES `allotted_resource_customization` (`MODEL_CUSTOMIZATION_UUID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `collection_network_resource_customization`
--

--DROP TABLE IF EXISTS `collection_network_resource_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `collection_network_resource_customization` (
  `MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `MODEL_INSTANCE_NAME` varchar(200) NOT NULL,
  `NETWORK_TECHNOLOGY` varchar(45) DEFAULT NULL,
  `NETWORK_TYPE` varchar(45) DEFAULT NULL,
  `NETWORK_ROLE` varchar(200) DEFAULT NULL,
  `NETWORK_SCOPE` varchar(45) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
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

--DROP TABLE IF EXISTS `collection_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `collection_resource` (
  `MODEL_UUID` varchar(200) NOT NULL,
  `MODEL_NAME` varchar(200) NOT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) NOT NULL,
  `MODEL_VERSION` varchar(20) NOT NULL,
  `TOSCA_NODE_TYPE` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`MODEL_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `collection_resource_customization`
--

--DROP TABLE IF EXISTS `collection_resource_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `collection_resource_customization` (
  `MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `MODEL_INSTANCE_NAME` varchar(200) NOT NULL,
  `ROLE` varchar(50) NOT NULL,
  `PRIMARY_TYPE` varchar(50) NOT NULL,
  `FUNCTION` varchar(50) NOT NULL,
  `SUBINTERFACE_NETWORK_QUANTITY` int(11) DEFAULT NULL,
  `COLLECTION_RESOURCE_TYPE` varchar(200) NOT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  `CR_MODEL_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`),
  KEY `CR_MODEL_UUID` (`CR_MODEL_UUID`),
  CONSTRAINT `collection_resource_customization_ibfk_1` FOREIGN KEY (`CR_MODEL_UUID`) REFERENCES `collection_resource` (`MODEL_UUID`) ON DELETE CASCADE,
  CONSTRAINT `collection_resource_customization_ibfk_2` FOREIGN KEY (`CR_MODEL_UUID`) REFERENCES `collection_resource` (`MODEL_UUID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `collection_resource_customization_to_service`
--

--DROP TABLE IF EXISTS `collection_resource_customization_to_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `collection_resource_customization_to_service` (
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  `RESOURCE_MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`SERVICE_MODEL_UUID`,`RESOURCE_MODEL_CUSTOMIZATION_UUID`),
  KEY `RESOURCE_MODEL_CUSTOMIZATION_UUID` (`RESOURCE_MODEL_CUSTOMIZATION_UUID`),
  CONSTRAINT `collection_resource_customization_to_service_ibfk_1` FOREIGN KEY (`RESOURCE_MODEL_CUSTOMIZATION_UUID`) REFERENCES `collection_resource_customization` (`MODEL_CUSTOMIZATION_UUID`) ON DELETE CASCADE,
  CONSTRAINT `collection_resource_customization_to_service_ibfk_2` FOREIGN KEY (`SERVICE_MODEL_UUID`) REFERENCES `service` (`MODEL_UUID`) ON DELETE CASCADE,
  CONSTRAINT `collection_resource_customization_to_service_ibfk_3` FOREIGN KEY (`RESOURCE_MODEL_CUSTOMIZATION_UUID`) REFERENCES `collection_resource_customization` (`MODEL_CUSTOMIZATION_UUID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `configuration`
--

--DROP TABLE IF EXISTS `configuration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `configuration` (
  `MODEL_UUID` varchar(200) NOT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) NOT NULL,
  `MODEL_VERSION` varchar(20) NOT NULL,
  `MODEL_NAME` varchar(200) NOT NULL,
  `TOSCA_NODE_TYPE` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`MODEL_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `configuration_customization`
--

--DROP TABLE IF EXISTS `configuration_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `configuration_customization` (
  `MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `MODEL_INSTANCE_NAME` varchar(200) NOT NULL,
  `CONFIGURATION_TYPE` varchar(200) DEFAULT NULL,
  `CONFIGURATION_ROLE` varchar(200) DEFAULT NULL,
  `CONFIGURATION_FUNCTION` varchar(200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  `CONFIGURATION_MODEL_UUID` varchar(200) NOT NULL,
  `SERVICE_PROXY_CUSTOMIZATION_MODEL_CUSTOMIZATION_UUID` varchar(200) DEFAULT NULL,
  `CONFIGURATION_CUSTOMIZATION_MODEL_CUSTOMIZATION_UUID` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`),
  KEY `fk_configuration_customization__configuration_idx` (`CONFIGURATION_MODEL_UUID`),
  KEY `fk_configuration_customization__service_proxy_customization_idx` (`SERVICE_PROXY_CUSTOMIZATION_MODEL_CUSTOMIZATION_UUID`),
  KEY `fk_configuration_customization__configuration_customization_idx` (`CONFIGURATION_CUSTOMIZATION_MODEL_CUSTOMIZATION_UUID`),
  CONSTRAINT `fk_configuration_customization__configuration_customization1` FOREIGN KEY (`CONFIGURATION_CUSTOMIZATION_MODEL_CUSTOMIZATION_UUID`) REFERENCES `configuration_customization` (`MODEL_CUSTOMIZATION_UUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_configuration_customization__service_proxy_customization1` FOREIGN KEY (`SERVICE_PROXY_CUSTOMIZATION_MODEL_CUSTOMIZATION_UUID`) REFERENCES `service_proxy_customization` (`MODEL_CUSTOMIZATION_UUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_configuration_resource_customization__configuration_resour1` FOREIGN KEY (`CONFIGURATION_MODEL_UUID`) REFERENCES `configuration` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `configuration_customization_to_service`
--

--DROP TABLE IF EXISTS `configuration_customization_to_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `configuration_customization_to_service` (
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  `RESOURCE_MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`SERVICE_MODEL_UUID`,`RESOURCE_MODEL_CUSTOMIZATION_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `external_service_to_internal_model_mapping`
--

--DROP TABLE IF EXISTS `external_service_to_internal_model_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `external_service_to_internal_model_mapping` (
  `id` int(11) NOT NULL,
  `SERVICE_NAME` varchar(200) NOT NULL,
  `PRODUCT_FLAVOR` varchar(200) DEFAULT NULL,
  `SUBSCRIPTION_SERVICE_TYPE` varchar(200) NOT NULL,
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_external_service_to_internal_model_mapping` (`SERVICE_NAME`,`PRODUCT_FLAVOR`,`SERVICE_MODEL_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `flyway_schema_history`
--

--DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `flyway_schema_history` (
  `installed_rank` int(11) NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int(11) DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT current_timestamp(),
  `execution_time` int(11) NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `heat_environment`
--

--DROP TABLE IF EXISTS `heat_environment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `heat_environment` (
  `ARTIFACT_UUID` varchar(200) NOT NULL,
  `NAME` varchar(100) NOT NULL,
  `VERSION` varchar(20) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `BODY` longtext NOT NULL,
  `ARTIFACT_CHECKSUM` varchar(200) NOT NULL DEFAULT 'MANUAL RECORD',
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`ARTIFACT_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `heat_files`
--

--DROP TABLE IF EXISTS `heat_files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `heat_files` (
  `ARTIFACT_UUID` varchar(200) NOT NULL,
  `NAME` varchar(200) NOT NULL,
  `VERSION` varchar(20) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `BODY` longtext NOT NULL,
  `ARTIFACT_CHECKSUM` varchar(200) NOT NULL DEFAULT 'MANUAL RECORD',
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`ARTIFACT_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `heat_nested_template`
--

--DROP TABLE IF EXISTS `heat_nested_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `heat_nested_template` (
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

--DROP TABLE IF EXISTS `heat_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `heat_template` (
  `ARTIFACT_UUID` varchar(200) NOT NULL,
  `NAME` varchar(200) NOT NULL,
  `VERSION` varchar(20) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `BODY` longtext NOT NULL,
  `TIMEOUT_MINUTES` int(11) DEFAULT NULL,
  `ARTIFACT_CHECKSUM` varchar(200) NOT NULL DEFAULT 'MANUAL RECORD',
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`ARTIFACT_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `heat_template_bk160629`
--

--DROP TABLE IF EXISTS `heat_template_bk160629`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `heat_template_bk160629` (
  `id` int(11) NOT NULL DEFAULT 0,
  `TEMPLATE_NAME` varchar(200) DEFAULT NULL,
  `VERSION` varchar(20) DEFAULT NULL,
  `ASDC_RESOURCE_NAME` varchar(100) DEFAULT NULL,
  `TEMPLATE_PATH` varchar(100) DEFAULT NULL,
  `TEMPLATE_BODY` longtext DEFAULT NULL,
  `TIMEOUT_MINUTES` int(11) DEFAULT NULL,
  `ASDC_UUID` varchar(200) DEFAULT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime DEFAULT current_timestamp(),
  `CHILD_TEMPLATE_ID` int(11) DEFAULT NULL,
  `ASDC_LABEL` varchar(200) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `heat_template_params`
--

--DROP TABLE IF EXISTS `heat_template_params`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `heat_template_params` (
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
-- Table structure for table `heat_template_params_bk160629`
--

--DROP TABLE IF EXISTS `heat_template_params_bk160629`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `heat_template_params_bk160629` (
  `id` int(11) NOT NULL DEFAULT 0,
  `HEAT_TEMPLATE_ID` int(11) DEFAULT NULL,
  `PARAM_NAME` varchar(45) DEFAULT NULL,
  `IS_REQUIRED` bit(1) DEFAULT NULL,
  `PARAM_TYPE` varchar(20) DEFAULT NULL,
  `PARAM_ALIAS` varchar(45) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instance_group`
--

--DROP TABLE IF EXISTS `instance_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `instance_group` (
  `MODEL_UUID` varchar(200) NOT NULL,
  `MODEL_NAME` varchar(200) NOT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) NOT NULL,
  `MODEL_VERSION` varchar(20) NOT NULL,
  `TOSCA_NODE_TYPE` varchar(200) DEFAULT NULL,
  `ROLE` varchar(50) NOT NULL,
  `PRIMARY_TYPE` varchar(50) NOT NULL,
  `FUNCTION` varchar(50) NOT NULL,
  `DESCRIPTION` varchar(200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  `CR_MODEL_UUID` varchar(200) NOT NULL,
  `INSTANCE_GROUP_TYPE` varchar(50) NOT NULL,
  PRIMARY KEY (`MODEL_UUID`),
  KEY `CR_MODEL_UUID` (`CR_MODEL_UUID`),
  CONSTRAINT `instance_group_ibfk_1` FOREIGN KEY (`CR_MODEL_UUID`) REFERENCES `collection_resource` (`MODEL_UUID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `network_recipe`
--

--DROP TABLE IF EXISTS `network_recipe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `network_recipe` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `MODEL_NAME` varchar(20) NOT NULL,
  `ACTION` varchar(50) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `ORCHESTRATION_URI` varchar(256) NOT NULL,
  `NETWORK_PARAM_XSD` varchar(2048) DEFAULT NULL,
  `RECIPE_TIMEOUT` int(11) DEFAULT NULL,
  `SERVICE_TYPE` varchar(45) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  `VERSION_STR` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_rl4f296i0p8lyokxveaiwkayi` (`MODEL_NAME`,`ACTION`,`VERSION_STR`)
) ENGINE=InnoDB AUTO_INCREMENT=178 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `network_recipe_bk160629`
--

--DROP TABLE IF EXISTS `network_recipe_bk160629`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `network_recipe_bk160629` (
  `id` int(11) NOT NULL DEFAULT 0,
  `NETWORK_TYPE` varchar(20) DEFAULT NULL,
  `ACTION` varchar(20) DEFAULT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `ORCHESTRATION_URI` varchar(256) DEFAULT NULL,
  `NETWORK_PARAM_XSD` varchar(2048) DEFAULT NULL,
  `RECIPE_TIMEOUT` int(11) DEFAULT NULL,
  `SERVICE_TYPE` varchar(45) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime DEFAULT current_timestamp(),
  `VERSION` int(11) DEFAULT NULL,
  `VERSION_STR` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `network_resource`
--

--DROP TABLE IF EXISTS `network_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `network_resource` (
  `MODEL_UUID` varchar(200) NOT NULL,
  `MODEL_NAME` varchar(200) NOT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) DEFAULT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `HEAT_TEMPLATE_ARTIFACT_UUID` varchar(200) NOT NULL,
  `NEUTRON_NETWORK_TYPE` varchar(20) DEFAULT NULL,
  `MODEL_VERSION` varchar(20) DEFAULT NULL,
  `TOSCA_NODE_TYPE` varchar(200) DEFAULT NULL,
  `AIC_VERSION_MIN` varchar(20) NOT NULL,
  `AIC_VERSION_MAX` varchar(20) DEFAULT NULL,
  `ORCHESTRATION_MODE` varchar(20) DEFAULT 'HEAT',
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`MODEL_UUID`),
  KEY `fk_network_resource__temp_network_heat_template_lookup1_idx` (`MODEL_NAME`),
  KEY `fk_network_resource__heat_template1_idx` (`HEAT_TEMPLATE_ARTIFACT_UUID`),
  CONSTRAINT `fk_network_resource__heat_template1` FOREIGN KEY (`HEAT_TEMPLATE_ARTIFACT_UUID`) REFERENCES `heat_template` (`ARTIFACT_UUID`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_network_resource__temp_network_heat_template_lookup__mod_nm1` FOREIGN KEY (`MODEL_NAME`) REFERENCES `temp_network_heat_template_lookup` (`NETWORK_RESOURCE_MODEL_NAME`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `network_resource_bk160629`
--

--DROP TABLE IF EXISTS `network_resource_bk160629`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `network_resource_bk160629` (
  `id` int(11) NOT NULL,
  `NETWORK_TYPE` varchar(45) DEFAULT NULL,
  `VERSION` int(11) DEFAULT NULL,
  `ORCHESTRATION_MODE` varchar(20) DEFAULT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `TEMPLATE_ID` int(11) DEFAULT NULL,
  `NEUTRON_NETWORK_TYPE` varchar(20) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime DEFAULT current_timestamp(),
  `VERSION_STR` varchar(20) DEFAULT NULL,
  `AIC_VERSION_MIN` varchar(20) DEFAULT '2.5',
  `AIC_VERSION_MAX` varchar(20) DEFAULT '2.5'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `network_resource_customization`
--

--DROP TABLE IF EXISTS `network_resource_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `network_resource_customization` (
  `MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `MODEL_INSTANCE_NAME` varchar(200) NOT NULL,
  `NETWORK_TECHNOLOGY` varchar(45) DEFAULT NULL,
  `NETWORK_TYPE` varchar(45) DEFAULT NULL,
  `NETWORK_ROLE` varchar(200) DEFAULT NULL,
  `NETWORK_SCOPE` varchar(45) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  `NETWORK_RESOURCE_MODEL_UUID` varchar(200) NOT NULL,
  `INSTANCE_GROUP_MODEL_UUID` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`),
  KEY `fk_network_resource_customization__network_resource1_idx` (`NETWORK_RESOURCE_MODEL_UUID`),
  CONSTRAINT `fk_network_resource_customization__network_resource1` FOREIGN KEY (`NETWORK_RESOURCE_MODEL_UUID`) REFERENCES `network_resource` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `network_resource_customization_to_service`
--

--DROP TABLE IF EXISTS `network_resource_customization_to_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `network_resource_customization_to_service` (
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  `RESOURCE_MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`SERVICE_MODEL_UUID`,`RESOURCE_MODEL_CUSTOMIZATION_UUID`),
  KEY `RESOURCE_MODEL_CUSTOMIZATION_UUID` (`RESOURCE_MODEL_CUSTOMIZATION_UUID`),
  CONSTRAINT `network_resource_customization_to_service_ibfk_1` FOREIGN KEY (`SERVICE_MODEL_UUID`) REFERENCES `service` (`MODEL_UUID`) ON DELETE CASCADE,
  CONSTRAINT `network_resource_customization_to_service_ibfk_2` FOREIGN KEY (`RESOURCE_MODEL_CUSTOMIZATION_UUID`) REFERENCES `network_resource_customization` (`MODEL_CUSTOMIZATION_UUID`) ON DELETE CASCADE,
  CONSTRAINT `network_resource_customization_to_service_ibfk_3` FOREIGN KEY (`SERVICE_MODEL_UUID`) REFERENCES `service` (`MODEL_UUID`) ON DELETE CASCADE,
  CONSTRAINT `network_resource_customization_to_service_ibfk_4` FOREIGN KEY (`RESOURCE_MODEL_CUSTOMIZATION_UUID`) REFERENCES `network_resource_customization` (`MODEL_CUSTOMIZATION_UUID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service`
--

--DROP TABLE IF EXISTS `service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `service` (
  `MODEL_UUID` varchar(200) NOT NULL,
  `MODEL_NAME` varchar(200) NOT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) NOT NULL,
  `MODEL_VERSION` varchar(20) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  `TOSCA_CSAR_ARTIFACT_UUID` varchar(200) DEFAULT NULL,
  `SERVICE_TYPE` varchar(200) DEFAULT NULL,
  `SERVICE_ROLE` varchar(200) DEFAULT NULL,
  `ENVIRONMENT_CONTEXT` varchar(200) DEFAULT NULL,
  `WORKLOAD_CONTEXT` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`MODEL_UUID`),
  KEY `fk_service__tosca_csar1_idx` (`TOSCA_CSAR_ARTIFACT_UUID`),
  CONSTRAINT `fk_service__tosca_csar1` FOREIGN KEY (`TOSCA_CSAR_ARTIFACT_UUID`) REFERENCES `tosca_csar` (`ARTIFACT_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service_proxy`
--

--DROP TABLE IF EXISTS `service_proxy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `service_proxy` (
  `MODEL_UUID` varchar(200) NOT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) NOT NULL,
  `MODEL_VERSION` varchar(20) NOT NULL,
  `MODEL_NAME` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`MODEL_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service_proxy_customization`
--

--DROP TABLE IF EXISTS `service_proxy_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `service_proxy_customization` (
  `MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `MODEL_INSTANCE_NAME` varchar(200) NOT NULL,
  `TOSCA_NODE_TYPE` varchar(200) NOT NULL,
  `SOURCE_SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  `SERVICE_PROXY_MODEL_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`),
  KEY `fk_service_proxy_customization__service_proxy1_idx` (`SERVICE_PROXY_MODEL_UUID`),
  KEY `fk_service_proxy_customization__service1_idx` (`SOURCE_SERVICE_MODEL_UUID`),
  CONSTRAINT `fk_service_proxy_resource_customization__service1` FOREIGN KEY (`SOURCE_SERVICE_MODEL_UUID`) REFERENCES `service` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_spr_customization__service_proxy_resource1` FOREIGN KEY (`SERVICE_PROXY_MODEL_UUID`) REFERENCES `service_proxy` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service_proxy_customization_to_service`
--

--DROP TABLE IF EXISTS `service_proxy_customization_to_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `service_proxy_customization_to_service` (
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  `RESOURCE_MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`SERVICE_MODEL_UUID`,`RESOURCE_MODEL_CUSTOMIZATION_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service_recipe`
--

--DROP TABLE IF EXISTS `service_recipe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `service_recipe` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ACTION` varchar(50) NOT NULL,
  `VERSION_STR` varchar(20) DEFAULT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `ORCHESTRATION_URI` varchar(256) NOT NULL,
  `SERVICE_PARAM_XSD` varchar(2048) DEFAULT NULL,
  `RECIPE_TIMEOUT` int(11) DEFAULT NULL,
  `SERVICE_TIMEOUT_INTERIM` int(11) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_7fav5dkux2v8g9d2i5ymudlgc` (`SERVICE_MODEL_UUID`,`ACTION`),
  KEY `fk_service_recipe__service1_idx` (`SERVICE_MODEL_UUID`),
  CONSTRAINT `fk_service_recipe__service1` FOREIGN KEY (`SERVICE_MODEL_UUID`) REFERENCES `service` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=522 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `temp_network_heat_template_lookup`
--

--DROP TABLE IF EXISTS `temp_network_heat_template_lookup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `temp_network_heat_template_lookup` (
  `NETWORK_RESOURCE_MODEL_NAME` varchar(200) NOT NULL,
  `HEAT_TEMPLATE_ARTIFACT_UUID` varchar(200) NOT NULL,
  `AIC_VERSION_MIN` varchar(20) NOT NULL,
  `AIC_VERSION_MAX` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`NETWORK_RESOURCE_MODEL_NAME`),
  KEY `fk_temp_network_heat_template_lookup__heat_template1_idx` (`HEAT_TEMPLATE_ARTIFACT_UUID`),
  CONSTRAINT `fk_temp_network_heat_template_lookup__heat_template1` FOREIGN KEY (`HEAT_TEMPLATE_ARTIFACT_UUID`) REFERENCES `heat_template` (`ARTIFACT_UUID`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tosca_csar`
--

--DROP TABLE IF EXISTS `tosca_csar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `tosca_csar` (
  `ARTIFACT_UUID` varchar(200) NOT NULL,
  `NAME` varchar(200) NOT NULL,
  `VERSION` varchar(20) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `ARTIFACT_CHECKSUM` varchar(200) NOT NULL,
  `URL` varchar(200) NOT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`ARTIFACT_UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vf_module`
--

--DROP TABLE IF EXISTS `vf_module`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `vf_module` (
  `MODEL_UUID` varchar(200) NOT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) DEFAULT NULL,
  `MODEL_VERSION` varchar(20) NOT NULL,
  `MODEL_NAME` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `IS_BASE` int(11) NOT NULL,
  `HEAT_TEMPLATE_ARTIFACT_UUID` varchar(200) DEFAULT NULL,
  `VOL_HEAT_TEMPLATE_ARTIFACT_UUID` varchar(200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
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

--DROP TABLE IF EXISTS `vf_module_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `vf_module_customization` (
  `MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `LABEL` varchar(200) DEFAULT NULL,
  `INITIAL_COUNT` int(11) DEFAULT 0,
  `MIN_INSTANCES` int(11) DEFAULT 0,
  `MAX_INSTANCES` int(11) DEFAULT NULL,
  `AVAILABILITY_ZONE_COUNT` int(11) DEFAULT NULL,
  `HEAT_ENVIRONMENT_ARTIFACT_UUID` varchar(200) DEFAULT NULL,
  `VOL_ENVIRONMENT_ARTIFACT_UUID` varchar(200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  `VF_MODULE_MODEL_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`),
  KEY `fk_vf_module_customization__vf_module1_idx` (`VF_MODULE_MODEL_UUID`),
  KEY `fk_vf_module_customization__heat_env__heat_environment1_idx` (`HEAT_ENVIRONMENT_ARTIFACT_UUID`),
  KEY `fk_vf_module_customization__vol_env__heat_environment2_idx` (`VOL_ENVIRONMENT_ARTIFACT_UUID`),
  CONSTRAINT `fk_vf_module_customization__heat_env__heat_environment1` FOREIGN KEY (`HEAT_ENVIRONMENT_ARTIFACT_UUID`) REFERENCES `heat_environment` (`ARTIFACT_UUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_vf_module_customization__vf_module1` FOREIGN KEY (`VF_MODULE_MODEL_UUID`) REFERENCES `vf_module` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_vf_module_customization__vol_env__heat_environment2` FOREIGN KEY (`VOL_ENVIRONMENT_ARTIFACT_UUID`) REFERENCES `heat_environment` (`ARTIFACT_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vf_module_to_heat_files`
--

--DROP TABLE IF EXISTS `vf_module_to_heat_files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `vf_module_to_heat_files` (
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

--DROP TABLE IF EXISTS `vnf_components`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `vnf_components` (
  `VNF_ID` int(11) NOT NULL,
  `COMPONENT_TYPE` varchar(20) NOT NULL,
  `HEAT_TEMPLATE_ID` int(11) DEFAULT NULL,
  `HEAT_ENVIRONMENT_ID` int(11) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`VNF_ID`,`COMPONENT_TYPE`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vnf_components_recipe`
--

--DROP TABLE IF EXISTS `vnf_components_recipe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `vnf_components_recipe` (
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
  `CREATION_TIMESTAMP` datetime DEFAULT current_timestamp(),
  `VF_MODULE_MODEL_UUID` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_4dpdwddaaclhc11wxsb7h59ma` (`VF_MODULE_MODEL_UUID`,`VNF_COMPONENT_TYPE`,`ACTION`,`VERSION`)
) ENGINE=InnoDB AUTO_INCREMENT=259 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vnf_components_recipe_bk`
--

--DROP TABLE IF EXISTS `vnf_components_recipe_bk`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `vnf_components_recipe_bk` (
  `id` int(11) NOT NULL DEFAULT 0,
  `VNF_TYPE` varchar(200) DEFAULT NULL,
  `VNF_COMPONENT_TYPE` varchar(45) NOT NULL,
  `ACTION` varchar(20) NOT NULL,
  `SERVICE_TYPE` varchar(45) DEFAULT NULL,
  `VERSION` varchar(20) DEFAULT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `ORCHESTRATION_URI` varchar(256) NOT NULL,
  `VNF_COMPONENT_PARAM_XSD` varchar(2048) DEFAULT NULL,
  `RECIPE_TIMEOUT` int(11) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime DEFAULT current_timestamp(),
  `VF_MODULE_ID` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vnf_recipe`
--

--DROP TABLE IF EXISTS `vnf_recipe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `vnf_recipe` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `VNF_TYPE` varchar(200) DEFAULT NULL,
  `ACTION` varchar(50) NOT NULL,
  `SERVICE_TYPE` varchar(45) DEFAULT NULL,
  `VERSION_STR` varchar(20) NOT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `ORCHESTRATION_URI` varchar(256) NOT NULL,
  `VNF_PARAM_XSD` varchar(2048) DEFAULT NULL,
  `RECIPE_TIMEOUT` int(11) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime DEFAULT current_timestamp(),
  `VF_MODULE_ID` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_f3tvqau498vrifq3cr8qnigkr` (`VF_MODULE_ID`,`ACTION`,`VERSION_STR`)
) ENGINE=InnoDB AUTO_INCREMENT=10006 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vnf_res_custom_to_vf_module_custom`
--

--DROP TABLE IF EXISTS `vnf_res_custom_to_vf_module_custom`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `vnf_res_custom_to_vf_module_custom` (
  `VNF_RESOURCE_CUST_MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `VF_MODULE_CUST_MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`VNF_RESOURCE_CUST_MODEL_CUSTOMIZATION_UUID`,`VF_MODULE_CUST_MODEL_CUSTOMIZATION_UUID`),
  KEY `fk_vnf_res_custom_to_vf_module_custom__vf_module_customizat_idx` (`VF_MODULE_CUST_MODEL_CUSTOMIZATION_UUID`),
  CONSTRAINT `fk_vnf_res_custom_to_vf_module_custom__vf_module_customization1` FOREIGN KEY (`VF_MODULE_CUST_MODEL_CUSTOMIZATION_UUID`) REFERENCES `vf_module_customization` (`MODEL_CUSTOMIZATION_UUID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_vnf_res_custom_to_vf_module_custom__vnf_resource_customiza1` FOREIGN KEY (`VNF_RESOURCE_CUST_MODEL_CUSTOMIZATION_UUID`) REFERENCES `vnf_resource_customization` (`MODEL_CUSTOMIZATION_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vnf_resource`
--

--DROP TABLE IF EXISTS `vnf_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `vnf_resource` (
  `ORCHESTRATION_MODE` varchar(20) NOT NULL DEFAULT 'HEAT',
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  `MODEL_UUID` varchar(200) NOT NULL,
  `AIC_VERSION_MIN` varchar(20) DEFAULT NULL,
  `AIC_VERSION_MAX` varchar(20) DEFAULT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) DEFAULT NULL,
  `MODEL_VERSION` varchar(20) NOT NULL,
  `MODEL_NAME` varchar(200) DEFAULT NULL,
  `TOSCA_NODE_TYPE` varchar(200) DEFAULT NULL,
  `HEAT_TEMPLATE_ARTIFACT_UUID` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`MODEL_UUID`),
  KEY `fk_vnf_resource__heat_template1` (`HEAT_TEMPLATE_ARTIFACT_UUID`),
  CONSTRAINT `fk_vnf_resource__heat_template1` FOREIGN KEY (`HEAT_TEMPLATE_ARTIFACT_UUID`) REFERENCES `heat_template` (`ARTIFACT_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vnf_resource_bk160621`
--

--DROP TABLE IF EXISTS `vnf_resource_bk160621`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `vnf_resource_bk160621` (
  `id` int(11) NOT NULL DEFAULT 0,
  `VNF_NAME` varchar(200) DEFAULT NULL,
  `VERSION` varchar(20) DEFAULT NULL,
  `ORCHESTRATION_MODE` varchar(20) DEFAULT NULL,
  `DESCRIPTION` varchar(1200) DEFAULT NULL,
  `TEMPLATE_ID` int(11) DEFAULT NULL,
  `ENVIRONMENT_ID` int(11) DEFAULT NULL,
  `ASDC_UUID` varchar(200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime DEFAULT current_timestamp(),
  `AIC_VERSION_MIN` varchar(20) DEFAULT NULL,
  `AIC_VERSION_MAX` varchar(20) DEFAULT NULL,
  `MODEL_INVARIANT_UUID` varchar(200) DEFAULT NULL,
  `ASDC_SERVICE_MODEL_VERSION` varchar(20) DEFAULT NULL,
  `MODEL_VERSION` varchar(20) DEFAULT NULL,
  `VNF_TYPE` varchar(200) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vnf_resource_customization`
--

--DROP TABLE IF EXISTS `vnf_resource_customization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `vnf_resource_customization` (
  `MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  `MODEL_INSTANCE_NAME` varchar(200) NOT NULL,
  `MIN_INSTANCES` int(11) DEFAULT NULL,
  `MAX_INSTANCES` int(11) DEFAULT NULL,
  `AVAILABILITY_ZONE_MAX_COUNT` int(11) DEFAULT NULL,
  `NF_TYPE` varchar(200) DEFAULT NULL,
  `NF_ROLE` varchar(200) DEFAULT NULL,
  `NF_FUNCTION` varchar(200) DEFAULT NULL,
  `NF_NAMING_CODE` varchar(200) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime NOT NULL DEFAULT current_timestamp(),
  `VNF_RESOURCE_MODEL_UUID` varchar(200) NOT NULL,
  `MULTI_STAGE_DESIGN` varchar(20) DEFAULT NULL,
  `INSTANCE_GROUP_MODEL_UUID` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`),
  KEY `fk_vnf_resource_customization__vnf_resource1_idx` (`VNF_RESOURCE_MODEL_UUID`),
  CONSTRAINT `fk_vnf_resource_customization__vnf_resource1` FOREIGN KEY (`VNF_RESOURCE_MODEL_UUID`) REFERENCES `vnf_resource` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vnf_resource_customization_to_service`
--

--DROP TABLE IF EXISTS `vnf_resource_customization_to_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE  IF NOT EXISTS `vnf_resource_customization_to_service` (
  `SERVICE_MODEL_UUID` varchar(200) NOT NULL,
  `RESOURCE_MODEL_CUSTOMIZATION_UUID` varchar(200) NOT NULL,
  PRIMARY KEY (`SERVICE_MODEL_UUID`,`RESOURCE_MODEL_CUSTOMIZATION_UUID`),
  KEY `RESOURCE_MODEL_CUSTOMIZATION_UUID` (`RESOURCE_MODEL_CUSTOMIZATION_UUID`),
  CONSTRAINT `vnf_resource_customization_to_service_ibfk_1` FOREIGN KEY (`SERVICE_MODEL_UUID`) REFERENCES `service` (`MODEL_UUID`) ON DELETE CASCADE,
  CONSTRAINT `vnf_resource_customization_to_service_ibfk_2` FOREIGN KEY (`RESOURCE_MODEL_CUSTOMIZATION_UUID`) REFERENCES `vnf_resource_customization` (`MODEL_CUSTOMIZATION_UUID`) ON DELETE CASCADE,
  CONSTRAINT `vnf_resource_customization_to_service_ibfk_3` FOREIGN KEY (`SERVICE_MODEL_UUID`) REFERENCES `service` (`MODEL_UUID`) ON DELETE CASCADE,
  CONSTRAINT `vnf_resource_customization_to_service_ibfk_4` FOREIGN KEY (`RESOURCE_MODEL_CUSTOMIZATION_UUID`) REFERENCES `vnf_resource_customization` (`MODEL_CUSTOMIZATION_UUID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-04-09 16:16:52
