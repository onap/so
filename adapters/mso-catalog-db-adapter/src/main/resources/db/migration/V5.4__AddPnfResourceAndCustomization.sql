use catalogdb;

CREATE TABLE IF NOT EXISTS `pnf_resource` (
  `ORCHESTRATION_MODE` varchar(20) NOT NULL DEFAULT 'HEAT',
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
  CDS_BLUEPRINT_NAME varchar(200) DEFAULT NULL,
  CDS_BLUEPRINT_VERSION varchar(20) DEFAULT NULL,
  PRIMARY KEY (`MODEL_CUSTOMIZATION_UUID`),
  KEY `fk_pnf_resource_customization__pnf_resource1_idx` (`PNF_RESOURCE_MODEL_UUID`),
  CONSTRAINT `fk_pnf_resource_customization__pnf_resource1` FOREIGN KEY (`PNF_RESOURCE_MODEL_UUID`) REFERENCES `pnf_resource` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

