use catalogdb;

CREATE TABLE IF NOT EXISTS `processing_flags` (
  `ID` INT(11) NOT NULL AUTO_INCREMENT,
  `FLAG` varchar(200) NOT NULL,
  `VALUE` varchar(200) NOT NULL,
  `ENDPOINT` varchar(200) NOT NULL,
  `DESCRIPTION` longtext NOT NULL,
  `CREATION_TIMESTAMP` timestamp NULL DEFAULT current_timestamp(),
  `UPDATE_TIMESTAMP` timestamp DEFAULT current_timestamp() ON UPDATE current_timestamp(),  
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_processing_flags` (`FLAG`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;