use catalogdb;

CREATE TABLE IF NOT EXISTS `building_block_rollback` (
    `ID` INT NOT NULL AUTO_INCREMENT,
    `BUILDING_BLOCK_NAME` varchar(200) NOT NULL,
    `ACTION` varchar(200) null,
    `ROLLBACK_BUILDING_BLOCK_NAME` varchar(200) NOT NULL,
    `ROLLBACK_ACTION` varchar(200) NULL,
    PRIMARY KEY (`ID`)
)  ENGINE=InnoDB DEFAULT CHARSET=latin1;
