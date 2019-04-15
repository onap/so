USE catalogdb;

CREATE TABLE IF NOT EXISTS `catalogdb`.`workflow` (
  `ID` INT(11) NOT NULL AUTO_INCREMENT,
  `ARTIFACT_UUID` VARCHAR(200) NOT NULL,
  `ARTIFACT_NAME` VARCHAR(200) NOT NULL,
  `NAME` VARCHAR(200) NOT NULL,
  `OPERATION_NAME` VARCHAR(200) NULL,
  `VERSION` DOUBLE NOT NULL,
  `DESCRIPTION` VARCHAR(1200) NULL,
  `BODY` LONGTEXT NULL,
  `RESOURCE_TARGET` VARCHAR(200) NOT NULL,
  `SOURCE` VARCHAR(200) NOT NULL,
  `TIMEOUT_MINUTES` INT(11) NULL DEFAULT NULL,
  `ARTIFACT_CHECKSUM` VARCHAR(200) NULL DEFAULT 'MANUAL RECORD',
  `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `UK_workflow` (`ARTIFACT_UUID` ASC, `NAME` ASC, `VERSION` ASC, `SOURCE` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `catalogdb`.`vnf_resource_to_workflow` (
  `ID` INT(11) NOT NULL AUTO_INCREMENT,
  `VNF_RESOURCE_MODEL_UUID` VARCHAR(200) NOT NULL,
  `WORKFLOW_ID` INT(11) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `UK_vnf_resource_to_workflow` (`VNF_RESOURCE_MODEL_UUID` ASC, `WORKFLOW_ID` ASC),
  INDEX `fk_vnf_resource_to_workflow__workflow1_idx` (`WORKFLOW_ID` ASC),
  INDEX `fk_vnf_resource_to_workflow__vnf_res_mod_uuid_idx` (`VNF_RESOURCE_MODEL_UUID` ASC),
  CONSTRAINT `fk_vnf_resource_to_workflow__vnf_resource1`
    FOREIGN KEY (`VNF_RESOURCE_MODEL_UUID`)
    REFERENCES `catalogdb`.`vnf_resource` (`MODEL_UUID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_vnf_resource_to_workflow__workflow1`
    FOREIGN KEY (`WORKFLOW_ID`)
    REFERENCES `catalogdb`.`workflow` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `catalogdb`.`activity_spec` (
  `ID` INT(11) NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(200) NOT NULL,
  `DESCRIPTION` VARCHAR(1200) NOT NULL,
  `VERSION` DOUBLE NOT NULL,
  `CREATION_TIMESTAMP` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `UK_activity_spec` (`NAME` ASC, `VERSION` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `catalogdb`.`user_parameters` (
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

CREATE TABLE IF NOT EXISTS `catalogdb`.`workflow_activity_spec_sequence` (
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
    REFERENCES `catalogdb`.`activity_spec` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_workflow_activity_spec_sequence__workflow1`
    FOREIGN KEY (`WORKFLOW_ID`)
    REFERENCES `catalogdb`.`workflow` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `catalogdb`.`activity_spec_parameters` (
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

CREATE TABLE IF NOT EXISTS `catalogdb`.`activity_spec_categories` (
  `ID` INT(11) NOT NULL,
  `NAME` VARCHAR(200) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `UK_activity_spec_categories` (`NAME` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `catalogdb`.`activity_spec_to_activity_spec_categories` (
  `ID` INT(11) NOT NULL,
  `ACTIVITY_SPEC_ID` INT(11) NOT NULL,
  `ACTIVITY_SPEC_CATEGORIES_ID` INT(11) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `UK_activity_spec_to_activity_spec_categories` (`ACTIVITY_SPEC_ID` ASC, `ACTIVITY_SPEC_CATEGORIES_ID` ASC),
  INDEX `fk_activity_spec_to_activity_spec_categories__activity_spec_idx` (`ACTIVITY_SPEC_CATEGORIES_ID` ASC),
  INDEX `fk_activity_spec_to_activity_spec_categories__activity_spec_idx1` (`ACTIVITY_SPEC_ID` ASC),
  CONSTRAINT `fk_activity_spec_to_activity_spec_categories__activity_spec1`
    FOREIGN KEY (`ACTIVITY_SPEC_ID`)
    REFERENCES `catalogdb`.`activity_spec` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_activity_spec_to_activity_spec_categories__activity_spec_c1`
    FOREIGN KEY (`ACTIVITY_SPEC_CATEGORIES_ID`)
    REFERENCES `catalogdb`.`activity_spec_categories` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `catalogdb`.`activity_spec_to_activity_spec_parameters` (
  `ID` INT(11) NOT NULL AUTO_INCREMENT,
  `ACTIVITY_SPEC_ID` INT(11) NOT NULL,
  `ACTIVITY_SPEC_PARAMETERS_ID` INT(11) NOT NULL,
  PRIMARY KEY (`ID`),
  INDEX `fk_activity_spec_to_activity_spec_params__act_sp_param_id_idx` (`ACTIVITY_SPEC_PARAMETERS_ID` ASC),
  UNIQUE INDEX `UK_activity_spec_to_activity_spec_parameters` (`ACTIVITY_SPEC_ID` ASC, `ACTIVITY_SPEC_PARAMETERS_ID` ASC),
  INDEX `fk_activity_spec_to_activity_spec_parameters__act_spec_id_idx` (`ACTIVITY_SPEC_ID` ASC),
  CONSTRAINT `fk_activity_spec_to_activity_spec_parameters__activity_spec_1`
    FOREIGN KEY (`ACTIVITY_SPEC_ID`)
    REFERENCES `catalogdb`.`activity_spec` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_activity_spec_to_activity_spec_parameters__activ_spec_param1`
    FOREIGN KEY (`ACTIVITY_SPEC_PARAMETERS_ID`)
    REFERENCES `catalogdb`.`activity_spec_parameters` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE TABLE IF NOT EXISTS `catalogdb`.`activity_spec_to_user_parameters` (
  `ID` INT(11) NOT NULL,
  `ACTIVITY_SPEC_ID` INT(11) NOT NULL,
  `USER_PARAMETERS_ID` INT(11) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `UK_activity_spec_to_user_parameters` (`ACTIVITY_SPEC_ID` ASC, `USER_PARAMETERS_ID` ASC),
  INDEX `fk_activity_spec_to_user_parameters__user_parameters1_idx` (`USER_PARAMETERS_ID` ASC),
  INDEX `fk_activity_spec_to_user_parameters__activity_spec1_idx` (`ACTIVITY_SPEC_ID` ASC),
  CONSTRAINT `fk_activity_spec_to_user_parameters__activity_spec1`
    FOREIGN KEY (`ACTIVITY_SPEC_ID`)
    REFERENCES `catalogdb`.`activity_spec` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_activity_spec_to_user_parameters__user_parameters1`
    FOREIGN KEY (`USER_PARAMETERS_ID`)
    REFERENCES `catalogdb`.`user_parameters` (`ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


