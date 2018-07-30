-- MSO Catalog DB: table 'service-recipe' ----
-- should update a row for create instance
UPDATE mso_catalog.service_recipe
SET ORCHESTRATION_URI = "/mso/async/services/CreateGenericALaCarteServiceInstance"
WHERE SERVICE_ID = 4
  AND ACTION = 'createInstance';
  
-- should update a row for delete instance
UPDATE mso_catalog.service_recipe
SET ORCHESTRATION_URI = "/mso/async/services/DeleteGenericALaCarteServiceInstance"
WHERE SERVICE_ID = 4
  AND ACTION = 'deleteInstance';  
  
SET SQL_SAFE_UPDATES = 0;

-- 1 coordinate this change with Dmitry when updating labs 
UPDATE mso_catalog.service_recipe
SET orchestration_uri = "/mso/async/services/CreateGenericMacroServiceNetworkVnf"
WHERE orchestration_uri = "/mso/async/services/CreateViprAtmService";

UPDATE mso_catalog.service_recipe
SET orchestration_uri = "/mso/async/services/DeleteGenericMacroServiceNetworkVnf"
WHERE orchestration_uri = "/mso/async/services/DeleteViprAtmService";

-- 2 network_recipe
UPDATE mso_catalog.network_recipe
SET orchestration_uri = '/mso/async/services/UpdateNetworkInstance'
WHERE network_type = 'VID_DEFAULT' AND action = 'updateInstance';

SET SQL_SAFE_UPDATES = 1;