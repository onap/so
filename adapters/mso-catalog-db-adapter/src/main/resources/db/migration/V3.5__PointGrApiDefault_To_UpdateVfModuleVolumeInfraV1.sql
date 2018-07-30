USE catalogdb;


UPDATE `catalogdb`.`vnf_components_recipe` SET `ORCHESTRATION_URI`='/mso/async/services/UpdateVfModuleVolumeInfraV1' WHERE `VF_MODULE_MODEL_UUID`='GR-API-DEFAULT' and `VNF_COMPONENT_TYPE`='volumeGroup' and `ACTION`='updateInstance';
