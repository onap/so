use catalogdb;

CREATE TABLE IF NOT EXISTS `bbname_selection_reference` (
  `ID` INT(11) NOT NULL AUTO_INCREMENT,
  `CONTROLLER_ACTOR` varchar(200) NOT NULL ,
  `SCOPE` varchar(200) NOT NULL,
  `ACTION` varchar(200) NOT NULL,
  `BB_NAME` varchar(200) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO bbname_selection_reference (CONTROLLER_ACTOR,SCOPE,ACTION,BB_NAME)
VALUES
('APPC', 'vfModule', 'healthCheck','GenericVnfHealthCheckBB'),
('APPC', 'vfModule', 'configScaleOut','ConfigurationScaleOutBB'),
('APPC', 'vnf', 'healthCheck','GenericVnfHealthCheckBB');
