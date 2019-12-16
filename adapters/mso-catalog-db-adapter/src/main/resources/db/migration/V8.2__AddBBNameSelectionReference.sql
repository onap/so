use catalogdb;

CREATE TABLE IF NOT EXISTS `BBNameSelectionReference` (
  `CONTROLLER_ACTOR` varchar(200) NOT NULL ,
  `SCOPE` varchar(200) NOT NULL,
  `ACTION` varchar(200) NOT NULL,
  `BB_NAME` varchar(200) NOT NULL,
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO BBNameSelectionReference (CONTROLLER_ACTOR,SCOPE,ACTION,BB_NAME)
VALUES
('APPC', 'vfModule', 'healthCheck','GenericVnfHealthCheckBB'),
('APPC', 'vfModule', 'configScaleOut','ConfigurationScaleOutBB'),
('APPC', 'vnf', 'healthCheck','GenericVnfHealthCheckBB');
