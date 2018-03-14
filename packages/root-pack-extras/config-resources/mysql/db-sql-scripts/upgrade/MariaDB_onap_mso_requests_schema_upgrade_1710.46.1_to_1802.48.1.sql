-- MSO-816 mso_requests DB changes to support tenant isolation
-- -----------------------------------------------------------
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
--

ALTER TABLE `mso_requests`.`infra_active_requests`
   ADD COLUMN `OPERATIONAL_ENV_ID` VARCHAR(45) NULL DEFAULT NULL AFTER `CONFIGURATION_NAME`,
   ADD COLUMN `OPERATIONAL_ENV_NAME` VARCHAR(200) NULL DEFAULT NULL AFTER `OPERATIONAL_ENV_ID`,
   CHANGE COLUMN `REQUEST_SCOPE` `REQUEST_SCOPE` VARCHAR(50) NOT NULL;

--

DROP TABLE IF EXISTS `mso_requests`.`activate_operational_env_per_distributionid_status`;
DROP TABLE IF EXISTS `mso_requests`.`activate_operational_env_service_model_distribution_status`;
DROP TABLE IF EXISTS `mso_requests`.`watchdog_distributionid_status`;
DROP TABLE IF EXISTS `mso_requests`.`watchdog_per_component_distribution_status`;
DROP TABLE IF EXISTS `mso_requests`.`watchdog_service_mod_ver_id_lookup`;

-- -----------------------------------------------------
-- Table `mso_requests`.`activate_operational_env_service_model_distribution_status`
-- -----------------------------------------------------
CREATE TABLE `mso_requests`.`activate_operational_env_service_model_distribution_status` (
  `OPERATIONAL_ENV_ID` VARCHAR(45) NOT NULL,
  `SERVICE_MODEL_VERSION_ID` VARCHAR(45) NOT NULL,
  `REQUEST_ID` VARCHAR(45) NOT NULL,
  `SERVICE_MOD_VER_FINAL_DISTR_STATUS` VARCHAR(45) NULL,
  `RECOVERY_ACTION` VARCHAR(30) NULL,
  `RETRY_COUNT_LEFT` INT(11) NULL,
  `WORKLOAD_CONTEXT` VARCHAR(80) NOT NULL,
  `CREATE_TIME` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `MODIFY_TIME` DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`OPERATIONAL_ENV_ID`, `SERVICE_MODEL_VERSION_ID`, `REQUEST_ID`))
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `mso_requests`.`activate_operational_env_per_distributionid_status`
-- -----------------------------------------------------
CREATE TABLE `mso_requests`.`activate_operational_env_per_distributionid_status` (
  `DISTRIBUTION_ID` VARCHAR(45) NOT NULL,
  `DISTRIBUTION_ID_STATUS` VARCHAR(45) NULL,
  `DISTRIBUTION_ID_ERROR_REASON` VARCHAR(250) NULL,
  `CREATE_TIME` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `MODIFY_TIME` DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `OPERATIONAL_ENV_ID` VARCHAR(45) NOT NULL,
  `SERVICE_MODEL_VERSION_ID` VARCHAR(45) NOT NULL,
  `REQUEST_ID` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`DISTRIBUTION_ID`),
  INDEX `fk_activate_op_env_per_distributionid_status__aoesmds1_idx` (`OPERATIONAL_ENV_ID` ASC, `SERVICE_MODEL_VERSION_ID` ASC, `REQUEST_ID` ASC),
  CONSTRAINT `fk_activate_op_env_per_distributionid_status__aoesmds1`
    FOREIGN KEY (`OPERATIONAL_ENV_ID` , `SERVICE_MODEL_VERSION_ID` , `REQUEST_ID`)
    REFERENCES `mso_requests`.`activate_operational_env_service_model_distribution_status` (`OPERATIONAL_ENV_ID` , `SERVICE_MODEL_VERSION_ID` , `REQUEST_ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB; 

-- -----------------------------------------------------
-- Table `mso_requests`.`watchdog_distributionid_status`
-- -----------------------------------------------------
CREATE TABLE `mso_requests`.`watchdog_distributionid_status` (
  `DISTRIBUTION_ID` VARCHAR(45) NOT NULL,
  `DISTRIBUTION_ID_STATUS` VARCHAR(45) NULL,
  `CREATE_TIME` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `MODIFY_TIME` DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`DISTRIBUTION_ID`))
ENGINE = InnoDB; 

-- -----------------------------------------------------
-- Table `mso_requests`.`watchdog_per_component_distribution_status`
-- -----------------------------------------------------
CREATE TABLE `mso_requests`.`watchdog_per_component_distribution_status` (
  `DISTRIBUTION_ID` VARCHAR(45) NOT NULL,
  `COMPONENT_NAME` VARCHAR(45) NOT NULL,
  `COMPONENT_DISTRIBUTION_STATUS` VARCHAR(45) NULL,
  `CREATE_TIME` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `MODIFY_TIME` DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`DISTRIBUTION_ID`, `COMPONENT_NAME`),
  CONSTRAINT `fk_watchdog_component_distribution_status_watchdog_distributi1`
    FOREIGN KEY (`DISTRIBUTION_ID`)
    REFERENCES `mso_requests`.`watchdog_distributionid_status` (`DISTRIBUTION_ID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB; 

-- -----------------------------------------------------
-- Table `mso_requests`.`watchdog_service_mod_ver_id_lookup`
-- -----------------------------------------------------
CREATE TABLE `mso_requests`.`watchdog_service_mod_ver_id_lookup` (
  `DISTRIBUTION_ID` VARCHAR(45) NOT NULL,
  `SERVICE_MODEL_VERSION_ID` VARCHAR(45) NOT NULL,
  `CREATE_TIME` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`DISTRIBUTION_ID`))
ENGINE = InnoDB; 

--
SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
--