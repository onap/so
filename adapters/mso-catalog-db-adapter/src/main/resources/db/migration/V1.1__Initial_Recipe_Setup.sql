SET FOREIGN_KEY_CHECKS=0;
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (1,'CONTRAIL_BASIC','CREATE',NULL,'/mso/async/services/CreateNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (2,'CONTRAIL_BASIC','DELETE',NULL,'/mso/async/services/DeleteNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (3,'CONTRAIL_BASIC','UPDATE',NULL,'/mso/async/services/UpdateNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (4,'CONTRAIL_SHARED','CREATE',NULL,'/mso/async/services/CreateNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (5,'CONTRAIL_SHARED','UPDATE',NULL,'/mso/async/services/UpdateNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (6,'CONTRAIL_SHARED','DELETE',NULL,'/mso/async/services/DeleteNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (7,'CONTRAIL_EXTERNAL','CREATE',NULL,'/mso/async/services/CreateNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (8,'CONTRAIL_EXTERNAL','UPDATE',NULL,'/mso/async/services/UpdateNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (9,'CONTRAIL_EXTERNAL','DELETE',NULL,'/mso/async/services/DeleteNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (10,'CONTRAIL30_BASIC','CREATE',NULL,'/mso/async/services/CreateNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (11,'CONTRAIL30_BASIC','UPDATE',NULL,'/mso/async/services/UpdateNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (12,'CONTRAIL30_BASIC','DELETE',NULL,'/mso/async/services/DeleteNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (13,'CONTRAIL30_MPSCE','CREATE',NULL,'/mso/async/services/CreateNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (14,'CONTRAIL30_MPSCE','UPDATE',NULL,'/mso/async/services/UpdateNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (15,'CONTRAIL30_MPSCE','DELETE',NULL,'/mso/async/services/DeleteNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (16,'VID_DEFAULT','createInstance','VID_DEFAULT recipe to create network if no custom BPMN flow is found','/mso/async/services/CreateNetworkInstance',NULL,180,NULL,'2017-10-05 18:52:03','1.0');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (17,'VID_DEFAULT','updateInstance','VID_DEFAULT recipe to update network if no custom BPMN flow is found','/mso/async/services/UpdateNetworkInstance',NULL,180,NULL,'2017-10-05 18:52:03','1.0');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (18,'VID_DEFAULT','deleteInstance','VID_DEFAULT recipe to delete network if no custom BPMN flow is found','/mso/async/services/DeleteNetworkInstance',NULL,180,NULL,'2017-10-05 18:52:03','1.0');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (19,'CONTRAIL30_L2NODHCP','CREATE',NULL,'/mso/async/services/CreateNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (20,'CONTRAIL30_L2NODHCP','UPDATE',NULL,'/mso/async/services/UpdateNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (21,'CONTRAIL30_L2NODHCP','DELETE',NULL,'/mso/async/services/DeleteNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (22,'CONTRAIL30_GNDIRECT','CREATE',NULL,'/mso/async/services/CreateNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (23,'CONTRAIL30_GNDIRECT','UPDATE',NULL,'/mso/async/services/UpdateNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (24,'CONTRAIL30_GNDIRECT','DELETE',NULL,'/mso/async/services/DeleteNetworkV2',NULL,180,NULL,'2017-10-05 18:52:03','1');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (25,'SDNC_DEFAULT','createInstance','E2E DEFAULT recipe to create network if no custom BPMN flow is found','/mso/async/services/CreateSDNCNetworkResource',NULL,180,NULL,'2017-04-19 18:52:19','1.0');
INSERT INTO `network_recipe` (`id`, `MODEL_NAME`, `ACTION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `NETWORK_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`, `CREATION_TIMESTAMP`, `VERSION_STR`) VALUES (26,'SDNC_DEFAULT','deleteInstance','E2E DEFAULT recipe to delete network if no custom BPMN flow is found','/mso/async/services/DeleteSDNCNetworkResource',NULL,180,NULL,'2017-04-19 18:52:19','1.0');

INSERT INTO `service` (`MODEL_UUID`, `MODEL_NAME`, `MODEL_INVARIANT_UUID`, `MODEL_VERSION`, `DESCRIPTION`, `CREATION_TIMESTAMP`, `TOSCA_CSAR_ARTIFACT_UUID`) VALUES ('48cc36cc-a9fe-11e7-8b4b-0242ac120002','VID_DEFAULT','48cd56c8-a9fe-11e7-8b4b-0242ac120002','1.0','Default service for VID to use for infra APIH orchestration1707MIGRATED1707MIGRATED','2017-10-05 18:52:03',NULL);
INSERT INTO `service` (`MODEL_UUID`, `MODEL_NAME`, `MODEL_INVARIANT_UUID`, `MODEL_VERSION`, `DESCRIPTION`, `CREATION_TIMESTAMP`, `TOSCA_CSAR_ARTIFACT_UUID`) VALUES ('48cc3acd-a9fe-11e7-8b4b-0242ac120002','*','48ce2256-a9fe-11e7-8b4b-0242ac120002','1.0','Default service to use for infra APIH orchestration1707MIGRATED1707MIGRATED','2017-10-05 18:52:03',NULL);

INSERT INTO `service_recipe` (`id`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `SERVICE_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TIMEOUT_INTERIM`, `CREATION_TIMESTAMP`, `SERVICE_MODEL_UUID`) VALUES (1,'createInstance','1','VID_DEFAULT recipe to create service-instance if no custom BPMN flow is found','/mso/async/services/CreateGenericALaCarteServiceInstance',NULL,180,NULL,'2017-10-05 18:52:03','48cc36cc-a9fe-11e7-8b4b-0242ac120002');
INSERT INTO `service_recipe` (`id`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `SERVICE_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TIMEOUT_INTERIM`, `CREATION_TIMESTAMP`, `SERVICE_MODEL_UUID`) VALUES (2,'deleteInstance','1','VID_DEFAULT recipe to delete service-instance if no custom BPMN flow is found','/mso/async/services/DeleteGenericALaCarteServiceInstance',NULL,180,NULL,'2017-10-05 18:52:03','48cc36cc-a9fe-11e7-8b4b-0242ac120002');
INSERT INTO `service_recipe` (`id`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `SERVICE_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TIMEOUT_INTERIM`, `CREATION_TIMESTAMP`, `SERVICE_MODEL_UUID`) VALUES (3,'createInstance','1','DEFAULT recipe to create service-instance if no custom BPMN flow is found','/mso/async/services/CreateGenericALaCarteServiceInstance',NULL,180,NULL,'2017-10-05 18:52:03','48cc3acd-a9fe-11e7-8b4b-0242ac120002');
INSERT INTO `service_recipe` (`id`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `SERVICE_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TIMEOUT_INTERIM`, `CREATION_TIMESTAMP`, `SERVICE_MODEL_UUID`) VALUES (4,'deleteInstance','1','DEFAULT recipe to delete service-instance if no custom BPMN flow is found','/mso/async/services/DeleteGenericALaCarteServiceInstance',NULL,180,NULL,'2017-10-05 18:52:03','48cc3acd-a9fe-11e7-8b4b-0242ac120002');
insert into `service_recipe` (`id`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `SERVICE_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TIMEOUT_INTERIM`, `CREATION_TIMESTAMP`, `SERVICE_MODEL_UUID`) values (500,'updateInstance','1.0','Gr api recipe to update service-instance', '/mso/async/services/WorkflowActionBB', NULL, 180, NULL, '2017-10-05 18:52:03', 'd88da85c-d9e8-4f73-b837-3a72a431622b');
insert into `service_recipe` (`id`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `SERVICE_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TIMEOUT_INTERIM`, `CREATION_TIMESTAMP`, `SERVICE_MODEL_UUID`) values (500,'healthCheck','1.0','Gr api recipe to do CNF health check', '/mso/async/services/WorkflowActionBB', NULL, 180, NULL, '2021-08-20 18:52:03', 'd88da85c-d9e8-4f73-b837-3a72a431622b');

--
-- Custom Reciepe for the VoLTE service added temporarily
--

INSERT INTO `service` (`MODEL_UUID`, `MODEL_NAME`, `MODEL_INVARIANT_UUID`, `MODEL_VERSION`, `DESCRIPTION`, `CREATION_TIMESTAMP`, `TOSCA_CSAR_ARTIFACT_UUID`) VALUES ('dfcd7471-16c7-444e-8268-d4c50d90593a','UUI_DEFAULT','dfcd7471-16c7-444e-8268-d4c50d90593a','1.0','Default service for UUI to use for infra APIH orchestration1707MIGRATED1707MIGRATED','2017-10-23 18:52:03',NULL);

INSERT INTO `service_recipe` (`id`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `SERVICE_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TIMEOUT_INTERIM`, `CREATION_TIMESTAMP`, `SERVICE_MODEL_UUID`) VALUES (11,'createInstance','1','Custom recipe to create E2E service-instance if no custom BPMN flow is found','/mso/async/services/CreateCustomE2EServiceInstance',NULL,180,NULL,'2017-10-05 18:52:03','dfcd7471-16c7-444e-8268-d4c50d90593a');
INSERT INTO `service_recipe` (`id`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `SERVICE_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TIMEOUT_INTERIM`, `CREATION_TIMESTAMP`, `SERVICE_MODEL_UUID`) VALUES (12,'deleteInstance','1','Custom recipe to delete E2E service-instance if no custom BPMN flow is found','/mso/async/services/DeleteCustomE2EServiceInstance',NULL,180,NULL,'2017-10-05 18:52:03','dfcd7471-16c7-444e-8268-d4c50d90593a');

-- Recipe for onap3gppServiceInstances

INSERT INTO `service` (`MODEL_UUID`, `MODEL_NAME`, `MODEL_INVARIANT_UUID`, `MODEL_VERSION`, `DESCRIPTION`, `CREATION_TIMESTAMP`, `TOSCA_CSAR_ARTIFACT_UUID`) VALUES ('3d30a774-e149-11ea-87d0-0242ac130003','COMMON_SS_DEFAULT','3d30a774-e149-11ea-87d0-0242ac130003','1.0','Default service for common NSSMF','2020-08-18 17:40:03',NULL);

INSERT INTO `service_recipe` (`id`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `SERVICE_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TIMEOUT_INTERIM`, `CREATION_TIMESTAMP`, `SERVICE_MODEL_UUID`) VALUES (17,'createInstance','1','Custom recipe to allocate 3gpp service-instance if no custom BPMN flow is found','/mso/async/services/AllocateSliceSubnet',NULL,180,NULL,'2020-08-18 17:40:03','3d30a774-e149-11ea-87d0-0242ac130003');
INSERT INTO `service_recipe` (`id`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `SERVICE_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TIMEOUT_INTERIM`, `CREATION_TIMESTAMP`, `SERVICE_MODEL_UUID`) VALUES (18,'deleteInstance','1','Custom recipe to deallocate 3gpp service-instance if no custom BPMN flow is found','/mso/async/services/DeAllocateSliceSubnet',NULL,180,NULL,'2020-08-18 18:40:03','3d30a774-e149-11ea-87d0-0242ac130003');
INSERT INTO `service_recipe` (`id`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `SERVICE_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TIMEOUT_INTERIM`, `CREATION_TIMESTAMP`, `SERVICE_MODEL_UUID`) VALUES (19,'updateInstance','1','Custom recipe to modify 3gpp service-instance if no custom BPMN flow is found','/mso/async/services/ModifySliceSubnet',NULL,180,NULL,'2020-08-18 18:40:03','3d30a774-e149-11ea-87d0-0242ac130003');
INSERT INTO `service_recipe` (`id`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `SERVICE_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TIMEOUT_INTERIM`, `CREATION_TIMESTAMP`, `SERVICE_MODEL_UUID`) VALUES (20,'activateInstance','1','Custom recipe to activate/deactivate 3gpp service-instance if no custom BPMN flow is found','/mso/async/services/ActivateSliceSubnet',NULL,180,NULL,'2020-08-18 18:40:03','3d30a774-e149-11ea-87d0-0242ac130003');
INSERT INTO `service_recipe` (`id`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `SERVICE_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TIMEOUT_INTERIM`, `CREATION_TIMESTAMP`, `SERVICE_MODEL_UUID`) VALUES (21,'deactivateInstance','1','Custom recipe to activate/deactivate 3gpp service-instance if no custom BPMN flow is found','/mso/async/services/ActivateSliceSubnet',NULL,180,NULL,'2020-08-18 18:40:03','3d30a774-e149-11ea-87d0-0242ac130003');

-- Recipe for Service Intent

INSERT INTO `service` (`MODEL_UUID`, `MODEL_NAME`, `MODEL_INVARIANT_UUID`, `MODEL_VERSION`, `DESCRIPTION`, `CREATION_TIMESTAMP`, `TOSCA_CSAR_ARTIFACT_UUID`) VALUES ('6790ab0e-034f-11eb-adc1-0242ac120002','COMMON_SI_DEFAULT','6790ab0e-034f-11eb-adc1-0242ac120002','1.0','Default service for Service Intent','2021-08-18 17:40:03',NULL);

INSERT INTO `service_recipe` (`id`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `SERVICE_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TIMEOUT_INTERIM`, `CREATION_TIMESTAMP`, `SERVICE_MODEL_UUID`) VALUES (22,'createInstance','1','Custom recipe to create Service Intent instance if no custom BPMN flow is found','/mso/async/services/CreateServiceIntentInstance',NULL,180,NULL,'2021-08-18 17:40:03','6790ab0e-034f-11eb-adc1-0242ac120002');
INSERT INTO `service_recipe` (`id`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `SERVICE_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TIMEOUT_INTERIM`, `CREATION_TIMESTAMP`, `SERVICE_MODEL_UUID`) VALUES (23,'deleteInstance','1','Custom recipe to delete Service Intent instance if no custom BPMN flow is found','/mso/async/services/DeleteServiceIntentInstance',NULL,180,NULL,'2021-08-18 18:40:03','6790ab0e-034f-11eb-adc1-0242ac120002');
INSERT INTO `service_recipe` (`id`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `SERVICE_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TIMEOUT_INTERIM`, `CREATION_TIMESTAMP`, `SERVICE_MODEL_UUID`) VALUES (24,'updateInstance','1','Custom recipe to modify Service Intent instance if no custom BPMN flow is found','/mso/async/services/ModifyServiceIntentInstance',NULL,180,NULL,'2021-08-18 18:40:03','6790ab0e-034f-11eb-adc1-0242ac120002');

-- Recipe for E2E service update (R2 just support adding/deleting network service)
INSERT INTO `service_recipe` (`id`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `SERVICE_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TIMEOUT_INTERIM`, `CREATION_TIMESTAMP`, `SERVICE_MODEL_UUID`) VALUES (15,'updateInstance','1','Custom recipe to update E2E service-instance if no custom BPMN flow is found','/mso/async/services/UpdateCustomE2EServiceInstance',NULL,180,NULL,'2018-03-05 10:52:03','dfcd7471-16c7-444e-8268-d4c50d90593a');
INSERT INTO `service_recipe` (`id`, `ACTION`, `VERSION_STR`, `DESCRIPTION`, `ORCHESTRATION_URI`, `SERVICE_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TIMEOUT_INTERIM`, `CREATION_TIMESTAMP`, `SERVICE_MODEL_UUID`) VALUES (16,'scaleInstance','1','Custom recipe to scale E2E service-instance if no custom BPMN flow is found','/mso/async/services/ScaleCustomE2EServiceInstance',NULL,180,NULL,'2018-05-15 18:52:03','dfcd7471-16c7-444e-8268-d4c50d90593a');

INSERT INTO `vnf_components_recipe` (`id`, `VNF_TYPE`, `VNF_COMPONENT_TYPE`, `VF_MODULE_MODEL_UUID`, `ACTION`, `SERVICE_TYPE`, `VERSION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_COMPONENT_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (1,'*','VOLUME_GROUP',NULL,'CREATE',NULL,'1','Recipe Match All for','/mso/async/services/createCinderVolumeV1',null,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_components_recipe` (`id`, `VNF_TYPE`, `VNF_COMPONENT_TYPE`, `VF_MODULE_MODEL_UUID`, `ACTION`, `SERVICE_TYPE`, `VERSION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_COMPONENT_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (2,'*','VOLUME_GROUP',NULL,'DELETE',NULL,'1','Recipe Match All for','/mso/async/services/deleteCinderVolumeV1',null,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_components_recipe` (`id`, `VNF_TYPE`, `VNF_COMPONENT_TYPE`, `VF_MODULE_MODEL_UUID`, `ACTION`, `SERVICE_TYPE`, `VERSION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_COMPONENT_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (3,'*','VOLUME_GROUP',NULL,'UPDATE',NULL,'1','Recipe Match All for','/mso/async/services/updateCinderVolumeV1',null,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_components_recipe` (`id`, `VNF_TYPE`, `VNF_COMPONENT_TYPE`, `VF_MODULE_MODEL_UUID`, `ACTION`, `SERVICE_TYPE`, `VERSION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_COMPONENT_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (4,NULL,'VOLUME_GROUP',NULL,'CREATE_VF_MODULE_VOL',NULL,'1','Recipe Match All for','/mso/async/services/CreateVfModuleVolume',null,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_components_recipe` (`id`, `VNF_TYPE`, `VNF_COMPONENT_TYPE`, `VF_MODULE_MODEL_UUID`, `ACTION`, `SERVICE_TYPE`, `VERSION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_COMPONENT_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (5,NULL,'VOLUME_GROUP',NULL,'DELETE_VF_MODULE_VOL',NULL,'1','Recipe Match All for','/mso/async/services/DeleteVfModuleVolume',null,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_components_recipe` (`id`, `VNF_TYPE`, `VNF_COMPONENT_TYPE`, `VF_MODULE_MODEL_UUID`, `ACTION`, `SERVICE_TYPE`, `VERSION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_COMPONENT_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (6,NULL,'VOLUME_GROUP',NULL,'UPDATE_VF_MODULE_VOL',NULL,'1','Recipe Match All for','/mso/async/services/UpdateVfModuleVolume',null,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_components_recipe` (`id`, `VNF_TYPE`, `VNF_COMPONENT_TYPE`, `VF_MODULE_MODEL_UUID`, `ACTION`, `SERVICE_TYPE`, `VERSION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_COMPONENT_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (7,NULL,'volumeGroup','VID_DEFAULT','createInstance',NULL,'1','VID_DEFAULT recipe t','/mso/async/services/CreateVfModuleVolumeInfraV1',null,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_components_recipe` (`id`, `VNF_TYPE`, `VNF_COMPONENT_TYPE`, `VF_MODULE_MODEL_UUID`, `ACTION`, `SERVICE_TYPE`, `VERSION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_COMPONENT_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (8,NULL,'volumeGroup','VID_DEFAULT','deleteInstance',NULL,'1','VID_DEFAULT recipe t','/mso/async/services/DeleteVfModuleVolumeInfraV1',null,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_components_recipe` (`id`, `VNF_TYPE`, `VNF_COMPONENT_TYPE`, `VF_MODULE_MODEL_UUID`, `ACTION`, `SERVICE_TYPE`, `VERSION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_COMPONENT_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (9,NULL,'volumeGroup','VID_DEFAULT','updateInstance',NULL,'1','VID_DEFAULT recipe t','/mso/async/services/UpdateVfModuleVolumeInfraV1',null,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_components_recipe` (`id`, `VNF_TYPE`, `VNF_COMPONENT_TYPE`, `VF_MODULE_MODEL_UUID`, `ACTION`, `SERVICE_TYPE`, `VERSION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_COMPONENT_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (10,NULL,'vfModule','VID_DEFAULT','createInstance',NULL,'1','VID_DEFAULT recipe t','/mso/async/services/CreateVfModuleInfra',null,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_components_recipe` (`id`, `VNF_TYPE`, `VNF_COMPONENT_TYPE`, `VF_MODULE_MODEL_UUID`, `ACTION`, `SERVICE_TYPE`, `VERSION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_COMPONENT_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (11,NULL,'vfModule','VID_DEFAULT','deleteInstance',NULL,'1','VID_DEFAULT recipe t','/mso/async/services/DeleteVfModuleInfra',null,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_components_recipe` (`id`, `VNF_TYPE`, `VNF_COMPONENT_TYPE`, `VF_MODULE_MODEL_UUID`, `ACTION`, `SERVICE_TYPE`, `VERSION`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_COMPONENT_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (12,NULL,'vfModule','VID_DEFAULT','updateInstance',NULL,'1','VID_DEFAULT recipe t','/mso/async/services/UpdateVfModuleInfra',null,180,'2017-10-05 18:52:03');


--
-- Default Reciepe for the VNF componnets added start #SO-334, to unblock the VNF operations
--

INSERT INTO `vnf_components_recipe` (`VNF_TYPE`, `VF_MODULE_MODEL_UUID`, `ACTION`, `VERSION`, `DESCRIPTION`, `ORCHESTRATION_URI`,`VNF_COMPONENT_TYPE`, `VNF_COMPONENT_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`) VALUES  (NULL,'POLICY_DEFAULT','createInstance','1','Recipe Match POLICY_DEFAULT for VF Modules if no custom flow exists','/mso/async/services/CreateVfModuleInfra','vfModule',NULL,180,NULL);
INSERT INTO `vnf_components_recipe` (`VNF_TYPE`, `VF_MODULE_MODEL_UUID`, `ACTION`, `VERSION`, `DESCRIPTION`, `ORCHESTRATION_URI`,`VNF_COMPONENT_TYPE`, `VNF_COMPONENT_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`) VALUES (NULL,'POLICY_DEFAULT','updateInstance','1','Recipe Match POLICY_DEFAULT for VF Modules if no custom flow exists','/mso/async/services/UpdateVfModuleInfra','vfModule',NULL,180,NULL);
INSERT INTO `vnf_components_recipe` (`VNF_TYPE`, `VF_MODULE_MODEL_UUID`, `ACTION`, `VERSION`, `DESCRIPTION`, `ORCHESTRATION_URI`,`VNF_COMPONENT_TYPE`, `VNF_COMPONENT_PARAM_XSD`, `RECIPE_TIMEOUT`, `SERVICE_TYPE`) VALUES (NULL,'POLICY_DEFAULT','deleteInstance','1','Recipe Match POLICY_DEFAULT for VF Modules if no custom flow exists','/mso/async/services/DeleteVfModuleInfra','vfModule',NULL,180,NULL);
--
-- Default Reciepe for the VNF componnets added End
--

INSERT INTO `vnf_recipe` (`id`, `VF_MODULE_ID`, `ACTION`, `SERVICE_TYPE`, `VERSION_STR`, `VNF_TYPE`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (1,NULL,'CREATE',NULL,'1','*','Recipe Match All for VNFs if no custom flow exists','/mso/workflow/services/CreateGenericVNFV1',NULL,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_recipe` (`id`, `VF_MODULE_ID`, `ACTION`, `SERVICE_TYPE`, `VERSION_STR`, `VNF_TYPE`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (2,NULL,'DELETE',NULL,'1','*','Recipe Match All for VNFs if no custom flow exists','/mso/async/services//deleteGenericVNFV1',NULL,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_recipe` (`id`, `VF_MODULE_ID`, `ACTION`, `SERVICE_TYPE`, `VERSION_STR`, `VNF_TYPE`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (3,NULL,'UPDATE',NULL,'1','*','Recipe Match All for VNFs if no custom flow exists','/mso/workflow/services/updateGenericVNFV1',NULL,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_recipe` (`id`, `VF_MODULE_ID`, `ACTION`, `SERVICE_TYPE`, `VERSION_STR`, `VNF_TYPE`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (4,'*','CREATE_VF_MODULE',NULL,'1',NULL,'Recipe Match All for VNFs if no custom flow exists','/mso/async/services/CreateVfModule',NULL,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_recipe` (`id`, `VF_MODULE_ID`, `ACTION`, `SERVICE_TYPE`, `VERSION_STR`, `VNF_TYPE`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (5,'*','DELETE_VF_MODULE',NULL,'1',NULL,'Recipe Match All for VNFs if no custom flow exists','/mso/async/services/DeleteVfModule',NULL,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_recipe` (`id`, `VF_MODULE_ID`, `ACTION`, `SERVICE_TYPE`, `VERSION_STR`, `VNF_TYPE`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (6,'*','UPDATE_VF_MODULE',NULL,'1',NULL,'Recipe Match All for VNFs if no custom flow exists','/mso/async/services/UpdateVfModule',NULL,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_recipe` (`id`, `VF_MODULE_ID`, `ACTION`, `SERVICE_TYPE`, `VERSION_STR`, `VNF_TYPE`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (7,NULL,'createInstance',NULL,'1','VID_DEFAULT','VID_DEFAULT recipe to create VNF if no custom BPMN flow is found','/mso/async/services/CreateVnfInfra',NULL,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_recipe` (`id`, `VF_MODULE_ID`, `ACTION`, `SERVICE_TYPE`, `VERSION_STR`, `VNF_TYPE`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (8,NULL,'deleteInstance',NULL,'1','VID_DEFAULT','VID_DEFAULT recipe to delete VNF if no custom BPMN flow is found','/mso/async/services/DeleteVnfInfra',NULL,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_recipe` (`id`, `VF_MODULE_ID`, `ACTION`, `SERVICE_TYPE`, `VERSION_STR`, `VNF_TYPE`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (9,NULL,'createInstance',NULL,'1','RESOURCE_NS_DEFAULT','NS default recipe to create network service if no custom BPMN flow is found','/mso/async/services/CreateVFCNetworkService',NULL,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_recipe` (`id`, `VF_MODULE_ID`, `ACTION`, `SERVICE_TYPE`, `VERSION_STR`, `VNF_TYPE`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (10,NULL,'deleteInstance',NULL,'1','RESOURCE_NS_DEFAULT','NS default recipe to delete network service if no custom BPMN flow is found','/mso/async/services/DeleteVFCNetworkService',NULL,180,'2017-10-05 18:52:03');
INSERT INTO `vnf_recipe` (`id`, `VF_MODULE_ID`, `ACTION`, `SERVICE_TYPE`, `VERSION_STR`, `VNF_TYPE`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (11,NULL,'createInstance',NULL,'1','NS_DEFAULT','default custom E2E recipe to create NS if no custom BPMN flow is found','/mso/async/services/CreateVFCNSResource',NULL,180,'2018-04-18 18:52:03');
INSERT INTO `vnf_recipe` (`id`, `VF_MODULE_ID`, `ACTION`, `SERVICE_TYPE`, `VERSION_STR`, `VNF_TYPE`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (12,NULL,'deleteInstance',NULL,'1','NS_DEFAULT','default custom E2E recipe to delete NS if no custom BPMN flow is found','/mso/async/services/DeleteVFCNSResource',NULL,180,'2018-04-18 18:52:03');
INSERT INTO `vnf_recipe` (`id`, `VF_MODULE_ID`, `ACTION`, `SERVICE_TYPE`, `VERSION_STR`, `VNF_TYPE`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (13,NULL,'inPlaceSoftwareUpdate',NULL,'1','VID_DEFAULT','VID_DEFAULT recipe to update VNF software if no custom BPMN flow is found','/mso/async/services/VnfInPlaceUpdate',NULL,180,'2018-05-23 11:00:00');
INSERT INTO `vnf_recipe` (`id`, `VF_MODULE_ID`, `ACTION`, `SERVICE_TYPE`, `VERSION_STR`, `VNF_TYPE`, `DESCRIPTION`, `ORCHESTRATION_URI`, `VNF_PARAM_XSD`, `RECIPE_TIMEOUT`, `CREATION_TIMESTAMP`) VALUES (14,NULL,'applyUpdatedConfig',NULL,'1','VID_DEFAULT','VID_DEFAULT recipe to apply updated VNF config if no custom BPMN flow is found','/mso/async/services/VnfConfigUpdate',NULL,180,'2018-05-23 11:00:00');
SET FOREIGN_KEY_CHECKS=1;
