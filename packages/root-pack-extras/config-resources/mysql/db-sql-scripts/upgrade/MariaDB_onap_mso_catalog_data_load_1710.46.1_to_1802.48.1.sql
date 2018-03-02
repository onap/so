-- MSO-817 Insert new vnf_recipe records for "inPlaceSoftwareUpdate" and "applyUpdatedConfig" actions for VID_DEFAULT
-- -----------------------------------------------------------
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
--

INSERT INTO mso_catalog.VNF_RECIPE (
    VNF_TYPE, ACTION, VERSION_STR, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT
) VALUES
    ('VID_DEFAULT', 'inPlaceSoftwareUpdate', '1', 'VID_DEFAULT inPlaceSoftwareUpdate', '/mso/async/services/VnfInPlaceUpdate', 180),
    ('VID_DEFAULT', 'applyUpdatedConfig', '1', 'VID_DEFAULT applyUpdatedConfig', '/mso/async/services/VnfConfigUpdate', 180);

--
SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
--