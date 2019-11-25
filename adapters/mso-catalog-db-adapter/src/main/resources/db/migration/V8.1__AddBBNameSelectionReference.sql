use catalogdb;

CREATE TABLE IF NOT EXISTS `BBNameSelectionReference` (
  `ACTOR` varchar(200) NOT NULL ,
  `SCOPE` varchar(200) NOT NULL,
  `ACTION` varchar(200) NOT NULL,
  `BB_NAME` varchar(200) NOT NULL,
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO BBNameSelectionReference (ACTOR,SCOPE,ACTION,BB_NAME)
VALUES
('APPC', 'VfModule', 'HealthCheck','GenericVnfHealthCheckBB'),
('APPC', 'VfModule', 'ScaleOut','ConfigurationScaleOutBB'),
('APPC', 'Vnf', 'HealthCheck','GenericVnfHealthCheckBB');
