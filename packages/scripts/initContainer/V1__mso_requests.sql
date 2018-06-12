-- MySQL dump 10.13  Distrib 5.6.21, for osx10.8 (x86_64)
--
-- Host: mso-ecomp-db.dev.att.com    Database: requestdb
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
-- Table structure for table `activate_operational_env_per_distributionid_status`
--
Use requestdb;

--DROP TABLE IF EXISTS `activate_operational_env_per_distributionid_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `activate_operational_env_per_distributionid_status` (
  `DISTRIBUTION_ID` varchar(45) NOT NULL,
  `DISTRIBUTION_ID_STATUS` varchar(45) DEFAULT NULL,
  `DISTRIBUTION_ID_ERROR_REASON` varchar(250) DEFAULT NULL,
  `CREATE_TIME` datetime NOT NULL DEFAULT current_timestamp(),
  `MODIFY_TIME` datetime DEFAULT NULL ON UPDATE current_timestamp(),
  `OPERATIONAL_ENV_ID` varchar(45) NOT NULL,
  `SERVICE_MODEL_VERSION_ID` varchar(45) NOT NULL,
  `REQUEST_ID` varchar(45) NOT NULL,
  PRIMARY KEY (`DISTRIBUTION_ID`),
  KEY `fk_activate_op_env_per_distributionid_status__aoesmds1_idx` (`OPERATIONAL_ENV_ID`,`SERVICE_MODEL_VERSION_ID`,`REQUEST_ID`),
  CONSTRAINT `fk_activate_op_env_per_distributionid_status__aoesmds1` FOREIGN KEY (`OPERATIONAL_ENV_ID`, `SERVICE_MODEL_VERSION_ID`, `REQUEST_ID`) REFERENCES `activate_operational_env_service_model_distribution_status` (`OPERATIONAL_ENV_ID`, `SERVICE_MODEL_VERSION_ID`, `REQUEST_ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activate_operational_env_service_model_distribution_status`
--

--DROP TABLE IF EXISTS `activate_operational_env_service_model_distribution_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `activate_operational_env_service_model_distribution_status` (
  `OPERATIONAL_ENV_ID` varchar(45) NOT NULL,
  `SERVICE_MODEL_VERSION_ID` varchar(45) NOT NULL,
  `REQUEST_ID` varchar(45) NOT NULL,
  `SERVICE_MOD_VER_FINAL_DISTR_STATUS` varchar(45) DEFAULT NULL,
  `RECOVERY_ACTION` varchar(30) DEFAULT NULL,
  `RETRY_COUNT_LEFT` int(11) DEFAULT NULL,
  `WORKLOAD_CONTEXT` varchar(80) NOT NULL,
  `CREATE_TIME` datetime NOT NULL DEFAULT current_timestamp(),
  `MODIFY_TIME` datetime DEFAULT NULL ON UPDATE current_timestamp(),
  PRIMARY KEY (`OPERATIONAL_ENV_ID`,`SERVICE_MODEL_VERSION_ID`,`REQUEST_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `active_requests`
--

--DROP TABLE IF EXISTS `active_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `active_requests` (
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
  `RESPONSE_BODY` longtext DEFAULT NULL,
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
  `REQUEST_BODY` longtext DEFAULT NULL,
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `infra_active_requests`
--

--DROP TABLE IF EXISTS `infra_active_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `infra_active_requests` (
  `REQUEST_ID` varchar(45) NOT NULL,
  `CLIENT_REQUEST_ID` varchar(45) DEFAULT NULL,
  `ACTION` varchar(45) DEFAULT NULL,
  `REQUEST_STATUS` varchar(20) DEFAULT NULL,
  `STATUS_MESSAGE` varchar(2000) DEFAULT NULL,
  `PROGRESS` bigint(20) DEFAULT NULL,
  `START_TIME` datetime DEFAULT NULL,
  `END_TIME` datetime DEFAULT NULL,
  `SOURCE` varchar(45) DEFAULT NULL,
  `VNF_ID` varchar(45) DEFAULT NULL,
  `VNF_NAME` varchar(80) DEFAULT NULL,
  `VNF_TYPE` varchar(200) DEFAULT NULL,
  `SERVICE_TYPE` varchar(45) DEFAULT NULL,
  `AIC_NODE_CLLI` varchar(11) DEFAULT NULL,
  `TENANT_ID` varchar(45) DEFAULT NULL,
  `PROV_STATUS` varchar(20) DEFAULT NULL,
  `VNF_PARAMS` longtext DEFAULT NULL,
  `VNF_OUTPUTS` longtext DEFAULT NULL,
  `REQUEST_BODY` longtext DEFAULT NULL,
  `RESPONSE_BODY` longtext DEFAULT NULL,
  `LAST_MODIFIED_BY` varchar(100) DEFAULT NULL,
  `MODIFY_TIME` datetime DEFAULT NULL,
  `REQUEST_TYPE` varchar(20) DEFAULT NULL,
  `VOLUME_GROUP_ID` varchar(45) DEFAULT NULL,
  `VOLUME_GROUP_NAME` varchar(45) DEFAULT NULL,
  `VF_MODULE_ID` varchar(45) DEFAULT NULL,
  `VF_MODULE_NAME` varchar(200) DEFAULT NULL,
  `VF_MODULE_MODEL_NAME` varchar(200) DEFAULT NULL,
  `AAI_SERVICE_ID` varchar(50) DEFAULT NULL,
  `AIC_CLOUD_REGION` varchar(11) DEFAULT NULL,
  `CALLBACK_URL` varchar(200) DEFAULT NULL,
  `CORRELATOR` varchar(80) DEFAULT NULL,
  `NETWORK_ID` varchar(45) DEFAULT NULL,
  `NETWORK_NAME` varchar(80) DEFAULT NULL,
  `NETWORK_TYPE` varchar(80) DEFAULT NULL,
  `REQUEST_SCOPE` varchar(50) NOT NULL,
  `REQUEST_ACTION` varchar(45) NOT NULL DEFAULT 'unknown',
  `SERVICE_INSTANCE_ID` varchar(45) DEFAULT NULL,
  `SERVICE_INSTANCE_NAME` varchar(80) DEFAULT NULL,
  `REQUESTOR_ID` varchar(50) DEFAULT NULL,
  `CONFIGURATION_ID` varchar(45) DEFAULT NULL,
  `CONFIGURATION_NAME` varchar(200) DEFAULT NULL,
  `OPERATIONAL_ENV_ID` varchar(45) DEFAULT NULL,
  `OPERATIONAL_ENV_NAME` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`REQUEST_ID`),
  UNIQUE KEY `UK_bhu6w8p7wvur4pin0gjw2d5ak` (`CLIENT_REQUEST_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `site_status`
--

--DROP TABLE IF EXISTS `site_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `site_status` (
  `SITE_NAME` varchar(255) NOT NULL,
  `STATUS` bit(1) DEFAULT NULL,
  `CREATION_TIMESTAMP` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`SITE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `watchdog_distributionid_status`
--

--DROP TABLE IF EXISTS `watchdog_distributionid_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `watchdog_distributionid_status` (
  `DISTRIBUTION_ID` varchar(45) NOT NULL,
  `DISTRIBUTION_ID_STATUS` varchar(45) DEFAULT NULL,
  `CREATE_TIME` datetime NOT NULL DEFAULT current_timestamp(),
  `MODIFY_TIME` datetime DEFAULT NULL ON UPDATE current_timestamp(),
  PRIMARY KEY (`DISTRIBUTION_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `watchdog_per_component_distribution_status`
--

--DROP TABLE IF EXISTS `watchdog_per_component_distribution_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `watchdog_per_component_distribution_status` (
  `DISTRIBUTION_ID` varchar(45) NOT NULL,
  `COMPONENT_NAME` varchar(45) NOT NULL,
  `COMPONENT_DISTRIBUTION_STATUS` varchar(45) DEFAULT NULL,
  `CREATE_TIME` datetime NOT NULL DEFAULT current_timestamp(),
  `MODIFY_TIME` datetime DEFAULT NULL ON UPDATE current_timestamp(),
  PRIMARY KEY (`DISTRIBUTION_ID`,`COMPONENT_NAME`),
  CONSTRAINT `fk_watchdog_component_distribution_status_watchdog_distributi1` FOREIGN KEY (`DISTRIBUTION_ID`) REFERENCES `watchdog_distributionid_status` (`DISTRIBUTION_ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `watchdog_service_mod_ver_id_lookup`
--

--DROP TABLE IF EXISTS `watchdog_service_mod_ver_id_lookup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `watchdog_service_mod_ver_id_lookup` (
  `DISTRIBUTION_ID` varchar(45) NOT NULL,
  `SERVICE_MODEL_VERSION_ID` varchar(45) NOT NULL,
  `CREATE_TIME` datetime NOT NULL DEFAULT current_timestamp(),
  `MODIFY_TIME` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`DISTRIBUTION_ID`)
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

-- Dump completed on 2018-04-09 15:39:40
