
CREATE TABLE IF NOT EXISTS `identity_services` (
  `ID` varchar(50) NOT NULL,
  `IDENTITY_URL` varchar(200) DEFAULT NULL,
  `MSO_ID` varchar(255) DEFAULT NULL,
  `MSO_PASS` varchar(255) DEFAULT NULL,
  `ADMIN_TENANT` varchar(50) DEFAULT NULL,
  `MEMBER_ROLE` varchar(50) DEFAULT NULL,
  `TENANT_METADATA` tinyint(1) DEFAULT 0,
  `IDENTITY_SERVER_TYPE` varchar(50) DEFAULT NULL,
  `IDENTITY_AUTHENTICATION_TYPE` varchar(50) DEFAULT NULL,
  `LAST_UPDATED_BY` varchar(120) DEFAULT NULL,
  `CREATION_TIMESTAMP` timestamp NULL DEFAULT current_timestamp(),
  `UPDATE_TIMESTAMP` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`ID`)
) ;


CREATE TABLE IF NOT EXISTS `cloudify_managers` (
  `ID` varchar(50) NOT NULL,
  `CLOUDIFY_URL` varchar(200) DEFAULT NULL,
  `USERNAME` varchar(255) DEFAULT NULL,
  `PASSWORD` varchar(255) DEFAULT NULL,
  `VERSION` varchar(20) DEFAULT NULL,
  `LAST_UPDATED_BY` varchar(120) DEFAULT NULL,
  `CREATION_TIMESTAMP` timestamp NULL DEFAULT current_timestamp(),
  `UPDATE_TIMESTAMP` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`ID`)
) ;


CREATE TABLE IF NOT EXISTS `cloud_sites` (
  `ID` varchar(50) NOT NULL,
  `REGION_ID` varchar(11)  DEFAULT NULL,
  `IDENTITY_SERVICE_ID` varchar(50)  DEFAULT NULL,
  `CLOUD_VERSION` varchar(20)  DEFAULT NULL,
  `CLLI` varchar(11)  DEFAULT NULL,
  `CLOUDIFY_ID` varchar(50)  DEFAULT NULL,
  `PLATFORM` varchar(50)  DEFAULT NULL,
  `ORCHESTRATOR` varchar(50)  DEFAULT NULL,
  `LAST_UPDATED_BY` varchar(120) DEFAULT NULL,
  `CREATION_TIMESTAMP` timestamp NULL DEFAULT current_timestamp(),
  `UPDATE_TIMESTAMP` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`ID`),
  KEY `FK_cloud_sites_identity_services` (`IDENTITY_SERVICE_ID`),
  CONSTRAINT `FK_cloud_sites_identity_services` FOREIGN KEY (`IDENTITY_SERVICE_ID`) REFERENCES `identity_services` (`ID`)
) ;