-- MSO-1224 Add 2 new allottedResource columns in Catalog DB and return in catalog db adapter - AND -
-- MSO-670 To support new ACTION value of "inPlaceSoftwareUpdate"
-- increase ACTION column length to varchar(50) in all *_RECIPE tables in catalog db.
-- ------------------------------------------------------------- 
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
--  
    
ALTER TABLE `mso_catalog`.`network_recipe`          CHANGE COLUMN `ACTION` `ACTION` VARCHAR(50) NOT NULL ;
ALTER TABLE `mso_catalog`.`service_recipe`          CHANGE COLUMN `ACTION` `ACTION` VARCHAR(50) NOT NULL ;
ALTER TABLE `mso_catalog`.`vnf_components_recipe`   CHANGE COLUMN `ACTION` `ACTION` VARCHAR(50) NOT NULL ;
ALTER TABLE `mso_catalog`.`vnf_recipe`              CHANGE COLUMN `ACTION` `ACTION` VARCHAR(50) NOT NULL ;

ALTER TABLE `mso_catalog`.`allotted_resource_customization`
    ADD COLUMN `PROVIDING_SERVICE_MODEL_UUID` VARCHAR(200) NULL DEFAULT NULL AFTER `MODEL_INSTANCE_NAME`,
    ADD COLUMN `PROVIDING_SERVICE_MODEL_NAME` VARCHAR(200) NULL DEFAULT NULL AFTER `PROVIDING_SERVICE_MODEL_INVARIANT_UUID`;

--
SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;