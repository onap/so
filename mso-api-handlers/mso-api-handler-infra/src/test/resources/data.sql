
--Changes here should also be made in InfraActiveRequestsReset.sql to be re-inserted after tests
INSERT INTO requestdb.infra_active_requests(REQUEST_ID, CLIENT_REQUEST_ID, ACTION, REQUEST_STATUS, STATUS_MESSAGE, PROGRESS, START_TIME, END_TIME, SOURCE, VNF_ID, VNF_NAME, VNF_TYPE, SERVICE_TYPE, AIC_NODE_CLLI, TENANT_ID, PROV_STATUS, VNF_PARAMS, VNF_OUTPUTS, REQUEST_BODY, RESPONSE_BODY, LAST_MODIFIED_BY, MODIFY_TIME, REQUEST_TYPE, VOLUME_GROUP_ID, VOLUME_GROUP_NAME, VF_MODULE_ID, VF_MODULE_NAME, VF_MODULE_MODEL_NAME, AAI_SERVICE_ID, AIC_CLOUD_REGION, CALLBACK_URL, CORRELATOR, NETWORK_ID, NETWORK_NAME, NETWORK_TYPE, REQUEST_SCOPE, REQUEST_ACTION, SERVICE_INSTANCE_ID, SERVICE_INSTANCE_NAME, REQUESTOR_ID, CONFIGURATION_ID, CONFIGURATION_NAME, OPERATIONAL_ENV_ID, OPERATIONAL_ENV_NAME, REQUEST_URL) VALUES
('00032ab7-3fb3-42e5-965d-8ea592502017', '00032ab7-3fb3-42e5-965d-8ea592502016', 'deleteInstance', 'COMPLETE', 'Vf Module has been deleted successfully.', '100', '2016-12-22 18:59:54', '2016-12-22 19:00:28', 'VID', 'b92f60c8-8de3-46c1-8dc1-e4390ac2b005', null, null, null, null, '6accefef3cb442ff9e644d589fb04107', null, null, null, '{"modelInfo":{"modelType":"vfModule","modelName":"test::base::module-0"},"requestInfo":{"source":"VID"},"cloudConfiguration":{"tenantId":"6accefef3cb442ff9e644d589fb04107","lcpCloudRegionId":"n6"}}', null, 'BPMN', '2016-12-22 19:00:28', null, null, null, 'c7d527b1-7a91-49fd-b97d-1c8c0f4a7992', null, 'test::base::module-0', null, 'n6', null, null, null, null, null, 'vfModule', 'deleteInstance', 'e3b5744d-2ad1-4cdd-8390-c999a38829bc', null, null, null, null, null, null, 'http://localhost:8080/onap/so/infra/serviceInstantiation/v7/serviceInstances'),
('00032ab7-na18-42e5-965d-8ea592502018', '00032ab7-fake-42e5-965d-8ea592502018', 'deleteInstance', 'PENDING', 'Vf Module deletion pending.', '0', '2016-12-22 18:59:54', '2016-12-22 19:00:28', 'VID', 'b92f60c8-8de3-46c1-8dc1-e4390ac2b005', null, null, null, null, '6accefef3cb442ff9e644d589fb04107', null, null, null, '{"requestDetails": {"modelInfo":{"modelType":"vfModule","modelName":"test::base::module-0"},"requestInfo":{"source":"VID"},"cloudConfiguration":{"tenantId":"6accefef3cb442ff9e644d589fb04107","lcpCloudRegionId":"n6"}}}', null, 'BPMN', '2016-12-22 19:00:28', null, null, null, 'c7d527b1-7a91-49fd-b97d-1c8c0f4a7992', null, 'test::base::module-0', null, 'n6', null, null, null, null, null, 'vfModule', 'deleteInstance', 'e3b5744d-2ad1-4cdd-8390-c999a38829bc', null, null, null, null, null, null, null),
('00093944-bf16-4373-ab9a-3adfe730ff2d', null, 'createInstance', 'FAILED', 'Error: Locked instance - This service (MSODEV_1707_SI_v10_011-4) already has a request being worked with a status of IN_PROGRESS (RequestId - 278e83b1-4f9f-450e-9e7d-3700a6ed22f4). The existing request must finish or be cleaned up before proceeding.', '100', '2017-07-11 18:33:26', '2017-07-11 18:33:26', 'VID', null, null, null, null, null, '19123c2924c648eb8e42a3c1f14b7682', null, null, null, '{"modelInfo":{"modelInvariantId":"9647dfc4-2083-11e7-93ae-92361f002671","modelType":"service","modelName":"Infra_v10_Service","modelVersion":"1.0","modelVersionId":"5df8b6de-2083-11e7-93ae-92361f002671"},"requestInfo":{"source":"VID","instanceName":"MSODEV_1707_SI_v10_011-4","suppressRollback":false,"requestorId":"xxxxxx"},"subscriberInfo":{"globalSubscriberId":"MSO_1610_dev","subscriberName":"MSO_1610_dev"},"cloudConfiguration":{"tenantId":"19123c2924c648eb8e42a3c1f14b7682","lcpCloudRegionId":"n6"},"requestParameters":{"subscriptionServiceType":"MSO-dev-service-type","userParams":[{"name":"someUserParam","value":"someValue"}],"aLaCarte":true,"autoBuildVfModules":false,"cascadeDelete":false,"usePreload":true}}', null, 'APIH', null, null, null, null, null, null, null, null, 'n6', null, null, null, null, null, 'service', 'createInstance', null, 'MSODEV_1707_SI_v10_011-4', 'xxxxxx', null, null, null, null, 'http://localhost:8080/onap/so/infra/serviceInstantiation/v7/serviceInstances'),
('001619d2-a297-4a4b-a9f5-e2823c88458f', '001619d2-a297-4a4b-a9f5-e2823c88458f', 'CREATE_VF_MODULE', 'COMPLETE', 'COMPLETED', '100', '2016-07-01 14:11:42', '2017-05-02 16:03:34', 'PORTAL', null, 'test-vscp', 'elena_test21', null, null, '381b9ff6c75e4625b7a4182f90fc68d3', null, null, null, '{"requestDetails": {"modelInfo":{"modelType":"vfModule","modelName":"test::base::module-0"},"requestInfo":{"source":"VID"},"cloudConfiguration":{"tenantId":"6accefef3cb442ff9e644d589fb04107","lcpCloudRegionId":"n6"}}}', 'NONE', 'RDBTEST', '2016-07-01 14:11:42', 'VNF', null, null, null, 'MODULENAME1', 'moduleModelName', 'a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb', 'mtn9', null, null, null, null, null, 'vfModule', 'createInstance', null, null, null, null, null, null, null, 'http://localhost:8080/onap/so/infra/serviceInstantiation/v7/serviceInstances'),
('5ffbabd6-b793-4377-a1ab-082670fbc7ac', '5ffbabd6-b793-4377-a1ab-082670fbc7ac', 'deleteInstance', 'PENDING', 'Vf Module deletion pending.', '0', '2016-12-22 18:59:54', '2016-12-22 19:00:28', 'VID', 'b92f60c8-8de3-46c1-8dc1-e4390ac2b005', null, null, null, null, '6accefef3cb442ff9e644d589fb04107', null, null, null, '{"requestDetails": {"modelInfo": {"modelType": "vfModule","modelName": "test::base::module-0","modelVersionId": "20c4431c-246d-11e7-93ae-92361f002671","modelInvariantId": "78ca26d0-246d-11e7-93ae-92361f002671","modelVersion": "2","modelCustomizationId": "cb82ffd8-252a-11e7-93ae-92361f002671"},"cloudConfiguration": {"lcpCloudRegionId": "n6","tenantId": "0422ffb57ba042c0800a29dc85ca70f8"},"requestInfo": {"instanceName": "MSO-DEV-VF-1806BB-v10-base-it2-1","source": "VID","suppressRollback": false,"requestorId": "xxxxxx"},"relatedInstanceList": [{"relatedInstance": {"instanceId": "76fa8849-4c98-473f-b431-2590b192a653","modelInfo": {"modelType": "service","modelName": "Infra_v10_Service","modelVersionId": "5df8b6de-2083-11e7-93ae-92361f002671","modelInvariantId": "9647dfc4-2083-11e7-93ae-92361f002671","modelVersion": "1.0"}}},{"relatedInstance": {"instanceId": "d57970e1-5075-48a5-ac5e-75f2d6e10f4c","modelInfo": {"modelType": "vnf","modelName": "v10","modelVersionId": "ff2ae348-214a-11e7-93ae-92361f002671","modelInvariantId": "2fff5b20-214b-11e7-93ae-92361f002671","modelVersion": "1.0","modelCustomizationId": "68dc9a92-214c-11e7-93ae-92361f002671","modelCustomizationName": "v10 1"}}}],"requestParameters": {"usePreload": true,"userParams": []}}}', null, 'BPMN', '2016-12-22 19:00:28', null, null, null, 'c7d527b1-7a91-49fd-b97d-1c8c0f4a7992', null, 'test::base::module-0', null, 'n6', null, null, null, null, null, 'vfModule', 'deleteInstance', 'e3b5744d-2ad1-4cdd-8390-c999a38829bc', null, null, null, null, null, null, 'http://localhost:8080/onap/so/infra/serviceInstantiation/v7/serviceInstances');
INSERT INTO requestdb.infra_active_requests(REQUEST_ID, CLIENT_REQUEST_ID, ACTION, REQUEST_STATUS, STATUS_MESSAGE, PROGRESS, START_TIME, END_TIME, SOURCE, VNF_ID, VNF_NAME, VNF_TYPE, SERVICE_TYPE, AIC_NODE_CLLI, TENANT_ID, PROV_STATUS, VNF_PARAMS, VNF_OUTPUTS, REQUEST_BODY, RESPONSE_BODY, LAST_MODIFIED_BY, MODIFY_TIME, REQUEST_TYPE, VOLUME_GROUP_ID, VOLUME_GROUP_NAME, VF_MODULE_ID, VF_MODULE_NAME, VF_MODULE_MODEL_NAME, AAI_SERVICE_ID, AIC_CLOUD_REGION, CALLBACK_URL, CORRELATOR, NETWORK_ID, NETWORK_NAME, NETWORK_TYPE, REQUEST_SCOPE, REQUEST_ACTION, SERVICE_INSTANCE_ID, SERVICE_INSTANCE_NAME, REQUESTOR_ID, CONFIGURATION_ID, CONFIGURATION_NAME, OPERATIONAL_ENV_ID, OPERATIONAL_ENV_NAME, REQUEST_URL) VALUES
('00164b9e-784d-48a8-8973-bbad6ef818ed', null, 'createInstance', 'COMPLETE', 'Service Instance was created successfully.', '100', '2017-09-28 12:45:51', '2017-09-28 12:45:53', 'VID', null, null, null, null, null, '19123c2924c648eb8e42a3c1f14b7682', null, null, null, '{"modelInfo":{"modelCustomizationName":null,"modelInvariantId":"52b49b5d-3086-4ffd-b5e6-1b1e5e7e062f","modelType":"service","modelNameVersionId":null,"modelName":"MSO Test Network","modelVersion":"1.0","modelCustomizationUuid":null,"modelVersionId":"aed5a5b7-20d3-44f7-90a3-ddbd16f14d1e","modelCustomizationId":null,"modelUuid":null,"modelInvariantUuid":null,"modelInstanceName":null},"requestInfo":{"billingAccountNumber":null,"callbackUrl":null,"correlator":null,"orderNumber":null,"productFamilyId":null,"orderVersion":null,"source":"VID","instanceName":"DEV-n6-3100-0927-1","suppressRollback":false,"requestorId":"xxxxxx"},"relatedInstanceList":null,"subscriberInfo":{"globalSubscriberId":"MSO_1610_dev","subscriberName":"MSO_1610_dev"},"cloudConfiguration":{"aicNodeClli":null,"tenantId":"19123c2924c648eb8e42a3c1f14b7682","lcpCloudRegionId":"n6"},"requestParameters":{"subscriptionServiceType":"MSO-dev-service-type","userParams":[{"name":"someUserParam","value":"someValue"}],"aLaCarte":true,"autoBuildVfModules":false,"cascadeDelete":false,"usePreload":true},"project":null,"owningEntity":null,"platform":null,"lineOfBusiness":null}', null, 'CreateGenericALaCarteServiceInstance', '2017-09-28 12:45:52', null, null, null, null, null, null, null, 'n6', null, null, null, null, null, 'service', 'createInstance', 'b2f59173-b7e5-4e0f-8440-232fd601b865', 'DEV-n6-3100-0927-1', 'xxxxxx', null, null, null, null, 'http://localhost:8080/onap/so/infra/serviceInstantiation/v7/serviceInstances'),
('00173cc9-5ce2-4673-a810-f87fefb2829e', null, 'createInstance', 'FAILED', 'Error parsing request.  No valid instanceName is specified', '100', '2017-04-14 21:08:46', '2017-04-14 21:08:46', 'VID', null, null, null, null, null, 'a259ae7b7c3f493cb3d91f95a7c18149', null, null, null, '{"modelInfo":{"modelInvariantId":"ff6163d4-7214-459e-9f76-507b4eb00f51","modelType":"service","modelName":"ConstraintsSrvcVID","modelVersion":"2.0","modelVersionId":"722d256c-a374-4fba-a14f-a59b76bb7656"},"requestInfo":{"productFamilyId":"LRSI-OSPF","source":"VID","requestorId":"xxxxxx"},"subscriberInfo":{"globalSubscriberId":"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb"},"cloudConfiguration":{"tenantId":"a259ae7b7c3f493cb3d91f95a7c18149","lcpCloudRegionId":"mtn16"},"requestParameters":{"subscriptionServiceType":"Mobility","userParams":[{"name":"neutronport6_name","value":"8"},{"name":"neutronnet5_network_name","value":"8"},{"name":"contrailv2vlansubinterface3_name","value":"false"}]}}', null, 'APIH', null, null, null, null, null, null, null, null, 'mtn16', null, null, null, null, null, 'service', 'createInstance', null, null, null, null, null, null, null, null),
('0017f68c-eb2d-45bb-b7c7-ec31b37dc349', null, 'activateInstance', 'UNLOCKED', null, '20', '2017-09-26 16:09:29', null, 'VID', null, null, null, null, null, null, null, null, null, '{"modelInfo":{"modelCustomizationName":null,"modelInvariantId":"1587cf0e-f12f-478d-8530-5c55ac578c39","modelType":"configuration","modelNameVersionId":null,"modelName":null,"modelVersion":null,"modelCustomizationUuid":null,"modelVersionId":"36a3a8ea-49a6-4ac8-b06c-89a545444455","modelCustomizationId":"68dc9a92-214c-11e7-93ae-92361f002671","modelUuid":null,"modelInvariantUuid":null,"modelInstanceName":null},"requestInfo":{"billingAccountNumber":null,"callbackUrl":null,"correlator":null,"orderNumber":null,"productFamilyId":null,"orderVersion":null,"source":"VID","instanceName":null,"suppressRollback":false,"requestorId":"xxxxxx"},"relatedInstanceList":[{"relatedInstance":{"instanceName":null,"instanceId":"9e15a443-af65-4f05-9000-47ae495e937d","modelInfo":{"modelCustomizationName":null,"modelInvariantId":"de19ae10-9a25-11e7-abc4-cec278b6b50a","modelType":"service","modelNameVersionId":null,"modelName":"Infra_Configuration_Service","modelVersion":"1.0","modelCustomizationUuid":null,"modelVersionId":"ee938612-9a25-11e7-abc4-cec278b6b50a","modelCustomizationId":null,"modelUuid":null,"modelInvariantUuid":null,"modelInstanceName":null},"instanceDirection":null}}],"subscriberInfo":null,"cloudConfiguration":{"aicNodeClli":null,"tenantId":null,"lcpCloudRegionId":"n6"},"requestParameters":{"subscriptionServiceType":null,"userParams":[],"aLaCarte":false,"autoBuildVfModules":false,"cascadeDelete":false,"usePreload":true},"project":null,"owningEntity":null,"platform":null,"lineOfBusiness":null}', null, 'APIH', '2017-09-26 16:09:29', null, null, null, null, null, null, null, 'n6', null, null, null, null, null, 'configuration', 'activateInstance', '9e15a443-af65-4f05-9000-47ae495e937d', null, 'xxxxxx', '26ef7f15-57bb-48df-8170-e59edc26234c', null, null, null, 'http://localhost:8080/onap/so/infra/serviceInstantiation/v7/serviceInstances'),
('0017f68c-eb2d-45bb-b7c7-ec31b37dc350', null, 'createInstance', 'IN_PROGRESS', 'Error parsing request.
    No valid instanceName is specified', null, '2017-04-14 21:08:46', '2017-04-14 21:08:46', 'VID',
    '1882938', null, null, null, null, 'a259ae7b7c3f493cb3d91f95a7c18149', null, null, null,
    '{"serviceInstanceId":"1882939","vnfInstanceId":"1882938","networkInstanceId":"1882937","volumeGroupInstanceId":"1882935","vfModuleInstanceId":"1882934","requestDetails":{"requestInfo":{"source":"VID","requestorId":"xxxxxx","instanceName":"testService9"},"requestParameters":{"aLaCarte":true,"autoBuildVfModules":false,"subscriptionServiceType":"test"},"modelInfo":{"modelInvariantId":"f7ce78bb-423b-11e7-93f8-0050569a7965","modelVersion":"1","modelVersionId":"10","modelType":"service","modelName":"serviceModel","modelInstanceName":"modelInstanceName","modelCustomizationId":"f7ce78bb-423b-11e7-93f8-0050569a796"},"subscriberInfo":{"globalSubscriberId":"MSO_1610_dev","subscriberName":"MSO_1610_dev"}}}',
    null, 'APIH', null, null, '1882935', null, '1882934', null, null, null, 'mtn16', null, null, null, null, null, 'service', 'createInstance', '1882939', 'testService10', 'xxxxxx', 'test', null, null, null, 'http://localhost:8080/onap/so/infra/serviceInstantiation/v7/serviceInstances');


INSERT INTO requestdb.site_status(SITE_NAME, STATUS, CREATION_TIMESTAMP) VALUES
('testSite', 0, '2017-11-30 15:48:09');
    

    
INSERT INTO requestdb.activate_operational_env_service_model_distribution_status(OPERATIONAL_ENV_ID, SERVICE_MODEL_VERSION_ID, REQUEST_ID, SERVICE_MOD_VER_FINAL_DISTR_STATUS, RECOVERY_ACTION, RETRY_COUNT_LEFT, WORKLOAD_CONTEXT, CREATE_TIME, MODIFY_TIME) VALUES
    ('1dfe7154-eae0-44f2-8e7a-8e5e7882e55d', '37305814-4949-45ce-ae24-c378c7ed07d1', '483646fe-36cc-4e90-895a-c472f6da3f74', 'DISTRIBUTION_COMPLETE_OK', 'RETRY', 0, 'VNF_D2D', '2018-02-02 18:44:27', '2018-02-02 18:45:06'),
    ('1dfe7154-eae0-44f2-8e7a-8e5e7882e55d', '7bfa2197-8cbb-4080-b369-56c4d53c4573', '483646fe-36cc-4e90-895a-c472f6da3f74', 'DISTRIBUTION_COMPLETE_OK', 'SKIP', 0, 'VNF_D2D', '2018-02-02 18:44:27', '2018-02-02 18:44:56');

INSERT INTO requestdb.activate_operational_env_per_distributionid_status(DISTRIBUTION_ID, DISTRIBUTION_ID_STATUS, DISTRIBUTION_ID_ERROR_REASON, CREATE_TIME, MODIFY_TIME, OPERATIONAL_ENV_ID, SERVICE_MODEL_VERSION_ID, REQUEST_ID) VALUES
    ('TEST_distributionId', 'DISTRIBUTION_COMPLETE_OK', NULL, '2018-02-02 18:44:27', '2018-02-02 18:44:56', '1dfe7154-eae0-44f2-8e7a-8e5e7882e55d', '7bfa2197-8cbb-4080-b369-56c4d53c4573', '483646fe-36cc-4e90-895a-c472f6da3f74'),
    ('TEST_distributionId_2', 'DISTRIBUTION_COMPLETE_OK', NULL, '2018-02-02 18:44:27', '2018-02-02 18:45:06', '1dfe7154-eae0-44f2-8e7a-8e5e7882e55d', '37305814-4949-45ce-ae24-c378c7ed07d1', '483646fe-36cc-4e90-895a-c472f6da3f74');
    
    
INSERT INTO requestdb.watchdog_distributionid_status(DISTRIBUTION_ID, DISTRIBUTION_ID_STATUS, LOCK_VERSION, CREATE_TIME, MODIFY_TIME) VALUES
('1533c4bd-a3e3-493f-a16d-28c20614415e', '', 0, '2017-11-30 15:48:09', '2017-11-30 15:48:09'),
('55429711-809b-4a3b-9ee5-5120d46d9de0', '', 0, '2017-11-30 16:35:36', '2017-11-30 16:35:36'),
('67f0b2d1-9013-4b2b-9914-bbe2288284fb', '', 0, '2017-11-30 15:54:39', '2017-11-30 15:54:39');   


           
INSERT INTO requestdb.watchdog_per_component_distribution_status(DISTRIBUTION_ID, COMPONENT_NAME, COMPONENT_DISTRIBUTION_STATUS, CREATE_TIME, MODIFY_TIME) VALUES
('1533c4bd-a3e3-493f-a16d-28c20614415e', 'MSO', 'COMPONENT_DONE_OK', '2017-11-30 15:48:09', '2017-11-30 15:48:09'),
('55429711-809b-4a3b-9ee5-5120d46d9de0', 'MSO', 'COMPONENT_DONE_ERROR', '2017-11-30 16:35:36', '2017-11-30 16:35:36'),
('67f0b2d1-9013-4b2b-9914-bbe2288284fb', 'MSO', 'COMPONENT_DONE_OK', '2017-11-30 15:54:39', '2017-11-30 15:54:39');        


INSERT INTO requestdb.watchdog_service_mod_ver_id_lookup(DISTRIBUTION_ID, SERVICE_MODEL_VERSION_ID, CREATE_TIME, MODIFY_TIME) VALUES
('1533c4bd-a3e3-493f-a16d-28c20614415e', '7e813ab5-88d3-4fcb-86c0-498c5d7eef9a', '2017-11-30 15:48:08', '2017-11-30 15:48:08'),
('55429711-809b-4a3b-9ee5-5120d46d9de0', 'cc031e75-4442-4d1a-b774-8a2b434e0a50', '2017-11-30 16:35:36', '2017-11-30 16:35:36'),
('67f0b2d1-9013-4b2b-9914-bbe2288284fb', 'eade1e9d-c1ec-4ef3-bc31-60570fba1573', '2017-11-30 15:54:39', '2017-11-30 15:54:39');    

use catalogdb;
SET FOREIGN_KEY_CHECKS=0;

INSERT INTO heat_files(ARTIFACT_UUID, NAME, VERSION, DESCRIPTION, BODY, ARTIFACT_CHECKSUM, CREATION_TIMESTAMP) VALUES
('00535bdd-0878-4478-b95a-c575c742bfb0', 'nimbus-ethernet-gw', '1', 'created from csar', 'DEVICE=$dev\nBOOTPROTO=none\nNM_CONTROLLED=no\nIPADDR=$ip\nNETMASK=$netmask\nGATEWAY=$gateway\n', 'MANUAL RECORD', '2017-01-21 23:56:43');


INSERT INTO tosca_csar(ARTIFACT_UUID, NAME, VERSION, DESCRIPTION, ARTIFACT_CHECKSUM, URL, CREATION_TIMESTAMP) VALUES
('0513f839-459d-46b6-aa3d-2edfef89a079', 'service-Ciservicee3756aea561a-csar.csar', '1', 'TOSCA definition package of the asset', 'YTk1MmY2MGVlNzVhYTU4YjgzYjliMjNjMmM3NzU1NDc=', '/sdc/v1/catalog/services/Ciservicee3756aea561a/1.0/artifacts/service-Ciservicee3756aea561a-csar.csar', '2017-11-27 11:38:27');


INSERT INTO service(MODEL_UUID, MODEL_NAME, MODEL_INVARIANT_UUID, MODEL_VERSION, DESCRIPTION, CREATION_TIMESTAMP, TOSCA_CSAR_ARTIFACT_UUID, SERVICE_TYPE, SERVICE_ROLE, ENVIRONMENT_CONTEXT, WORKLOAD_CONTEXT) VALUES
('5df8b6de-2083-11e7-93ae-92361f002671', 'Infra_v10_Service', '9647dfc4-2083-11e7-93ae-92361f002671', '1.0', 'MSO aLaCarte Vfmodule with addon', '2017-04-14 13:42:39', '0513f839-459d-46b6-aa3d-2edfef89a079', 'NA', 'NA', 'Luna', 'Oxygen'),
('5df8b6de-2083-11e7-93ae-92361f002672', 'Infra_v10_Service', '9647dfc4-2083-11e7-93ae-92361f002671', '2.0', 'MSO aLaCarte Vfmodule with addon', '2017-04-14 13:42:39', null, 'NA', 'NA', 'Luna', 'Oxygen'),
('f78914d9-423b-11e7-93f8-0050569a7965', 'VID_DEFAULT', 'f7ce78bb-423b-11e7-93f8-0050569a7965', '2.0', 'Default service for VID to use for infra APIH orchestration1707MIGRATED1707MIGRATED', '2017-04-14 13:42:39', NULL, NULL, NULL, '2016-09-13 15:05:05', NULL),
('f78914d9-423b-11e7-93f8-0050569a7966', 'ServiceInstanceTest', 'f7ce78bb-423b-11e7-93f8-0050569a7965', '1.0', 'ServiceInstance', '2017-04-14 13:42:39', NULL, NULL, NULL, '2016-09-13 15:06:05', NULL),
('f78914d9-423b-11e7-93f8-0050569a7967', 'VnfInstanceTest', 'f7ce78bb-423b-11e7-93f8-0050569a7967', '1', 'VnfInstance', '2017-04-14 13:42:39', NULL, NULL, NULL, '2016-09-13 15:06:05', NULL),
('f78914d9-423b-11e7-93f8-0050569a7968', 'VfModuleTest', 'f7ce78bb-423b-11e7-93f8-0050569a7968', '1', 'VfModule', '2017-04-14 13:42:39', NULL, NULL, NULL, '2016-09-13 15:06:05', NULL),
('f78914d9-423b-11e7-93f8-0050569a7969', 'NetworkInstanceTest', 'f7ce78bb-423b-11e7-93f8-0050569a7969', '1', 'NetworkInstance', '2017-04-14 13:42:39', NULL, NULL, NULL, '2016-09-13 15:06:05', NULL),
('f78914d9-423b-11e7-93f8-0050569a7970', 'SDN-ETHERNET-INTERNET', 'f7ce78bb-423b-11e7-93f8-0050569a7969', '1', 'NetworkInstance', '2017-04-14 13:42:39', NULL, NULL, NULL, '2016-09-13 15:06:05', NULL),
('5d48acb5-097d-4982-aeb2-f4a3bd87d31b', 'AssignInstance', '5d48acb5-097d-4982-aeb2-f4a3bd87d31b', '10.0', 'AssignInstance', '2017-04-14 13:42:39', NULL, NULL, NULL, '2016-09-13 15:06:05', NULL),
('ff3514e3-5a33-55df-13ab-12abad84e7ff', 'UnassignInstance', 'ff3514e3-5a33-55df-13ab-12abad84e7ff', '1.0', 'UnassignInstance', '2017-04-14 13:42:39', NULL, NULL, NULL, '2016-09-13 15:06:05', NULL);


INSERT INTO service_recipe(ID, ACTION, VERSION_STR, DESCRIPTION, ORCHESTRATION_URI, SERVICE_PARAM_XSD, RECIPE_TIMEOUT, SERVICE_TIMEOUT_INTERIM, CREATION_TIMESTAMP, SERVICE_MODEL_UUID) VALUES
('1', 'createInstance', '1', 'Infra aLaCarte', '/mso/async/services/CreateGenericALaCarteServiceInstance', null, '180', '0', '2017-04-14 19:18:20', '5df8b6de-2083-11e7-93ae-92361f002671'),
('10', 'createInstance', '1', 'VID_DEFAULT recipe to create service-instance if no custom BPMN flow is found', '/mso/async/services/CreateGenericALaCarteServiceInstance', NULL, '180', NULL, '2016-09-13 15:05:18', 'f78914d9-423b-11e7-93f8-0050569a7965'),
('11', 'activateInstance', '1', 'Activate ServiceInstance', '/mso/async/services/ActivateInstance', NULL, '120', NULL, '2016-09-13 15:06:18', 'f78914d9-423b-11e7-93f8-0050569a7966'),
('12', 'deactivateInstance', '1', 'Deactivate ServiceInstance', '/mso/async/services/DeactivateInstance', NULL, '180', NULL, '2016-09-13 15:06:18', 'f78914d9-423b-11e7-93f8-0050569a7966'),
('13', 'deleteInstance', '1', 'Delete ServiceInstance', '/mso/async/services/DeleteInstance', NULL, '180', NULL, '2016-09-13 15:06:18', 'f78914d9-423b-11e7-93f8-0050569a7966'),
('14', 'createInstance', '1', 'Create VnfInstance', '/mso/async/services/CreateVnfInstance', NULL, '180', NULL, '2016-09-13 15:06:18', 'f78914d9-423b-11e7-93f8-0050569a7967'),
('15', 'replaceInstance', '1', 'Replace VnfInstance', '/mso/async/services/ReplaceVnfInstance', NULL, '180', NULL, '2016-09-13 15:06:18', 'f78914d9-423b-11e7-93f8-0050569a7967'),
('16', 'updateInstance', '1', 'Update VnfInstance', '/mso/async/services/UpdateVnfInstance', NULL, '180', NULL, '2016-09-13 15:06:18', 'f78914d9-423b-11e7-93f8-0050569a7967'),
('17', 'applyUpdatedConfig', '1', 'ApplyUpdatedConfig VnfInstance', '/mso/async/services/ApplyUpdatedConfig', NULL, '180', NULL, '2016-09-13 15:06:18', 'f78914d9-423b-11e7-93f8-0050569a7967'),
('18', 'deleteInstance', '1', 'Delete VnfInstance', '/mso/async/services/DeleteVnfInstance', NULL, '180', NULL, '2016-09-13 15:06:18', 'f78914d9-423b-11e7-93f8-0050569a7967'),
('19', 'createInstance', '1', 'Create VfModule', '/mso/async/services/CreateVfModule', NULL, '180', NULL, '2016-09-13 15:06:18', 'f78914d9-423b-11e7-93f8-0050569a7968'),
('20', 'createInstance', '1', 'Create NetworkInstance', '/mso/async/services/CreateNetworkInstance', NULL, '180', NULL, '2016-09-13 15:06:18', 'f78914d9-423b-11e7-93f8-0050569a7969'),
('36', 'assignInstance', '10.0', 'Assign ServiceInstance', '/mso/async/services/AssignServiceInstance', NULL, '180', NULL, '2016-09-13 15:06:18', '5d48acb5-097d-4982-aeb2-f4a3bd87d31b'),
('37', 'unassignInstance', '1.0', 'Unassign ServiceInstance', '/mso/async/services/UnassignServiceInstance', NULL, '180', NULL, '2016-09-13 15:06:18', 'ff3514e3-5a33-55df-13ab-12abad84e7ff'),
('41', 'createInstance', '1', 'Create ServiceRequest', '/mso/async/services/CreateMacroServiceNetworkVnf', null, '180', '0', '2017-04-14 19:18:20', '109a153e-325f-4df5-8161-edd91314daee');

INSERT INTO heat_template(ARTIFACT_UUID, NAME, VERSION, DESCRIPTION, BODY, TIMEOUT_MINUTES, ARTIFACT_CHECKSUM, CREATION_TIMESTAMP) VALUES
('ff874603-4222-11e7-9252-005056850d2e', 'module_mns_zrdm3frwl01exn_01_rgvm_1.yml', '1', 'created from csar', 'heat_template_version: 2013-05-23 description: heat template that creates TEST VNF parameters: TEST_server_name: type: string label: TEST server name description: TEST server name TEST_image_name: type: string label: image name description: TEST image name TEST_flavor_name: type: string label: TEST flavor name description: flavor name of TEST instance TEST_Role_net_name: type: string label: TEST network name description: TEST network name TEST_vnf_id: type: string label: TEST VNF Id description: TEST VNF Id resources:TEST: type: OS::Nova::Server properties: name: { get_param: TEST_server_name } image: { get_param: TEST_image_name } flavor: { get_param: TEST_flavor_name } networks: - port: { get_resource: TEST_port_0} metadata: vnf_id: {get_param: TEST_vnf_id} TEST_port_0: type: OS::Neutron::Port properties: network: { get_param: TEST_Role_net_name }', '60', 'MANUAL RECORD', '2017-01-21 23:26:56'), 
('ff87482f-4222-11e7-9252-005056850d2e', 'module_mns_zrdm3frwl01exn_01_rgvm_1.yml', '1', 'created from csar', 'heat_template_version: 2013-05-23 description: heat template that creates TEST VNF parameters: TEST_server_name: type: string label: TEST server name description: TEST server name TEST_image_name: type: string label: image name description: TEST image name TEST_flavor_name: type: string label: TEST flavor name description: flavor name of TEST instance TEST_Role_net_name: type: string label: TEST network name description: TEST network name TEST_vnf_id: type: string label: TEST VNF Id description: TEST VNF Id resources:TEST: type: OS::Nova::Server properties: name: { get_param: TEST_server_name } image: { get_param: TEST_image_name } flavor: { get_param: TEST_flavor_name } networks: - port: { get_resource: TEST_port_0} metadata: vnf_id: {get_param: TEST_vnf_id} TEST_port_0: type: OS::Neutron::Port properties: network: { get_param: TEST_Role_net_name }', '60', 'MANUAL RECORD', '2017-01-21 23:26:56'), 
('aa874603-4222-11e7-9252-005056850d2e', 'module_mns_zrdm3frwl01exn_01_rgvm_1.yml', '1', 'created from csar', 'heat_template_version: 2013-05-23 description: heat template that creates TEST VNF parameters: TEST_server_name: type: string label: TEST server name description: TEST server name TEST_image_name: type: string label: image name description: TEST image name TEST_flavor_name: type: string label: TEST flavor name description: flavor name of TEST instance TEST_Role_net_name: type: string label: TEST network name description: TEST network name TEST_vnf_id: type: string label: TEST VNF Id description: TEST VNF Id resources:TEST: type: OS::Nova::Server properties: name: { get_param: TEST_server_name } image: { get_param: TEST_image_name } flavor: { get_param: TEST_flavor_name } networks: - port: { get_resource: TEST_port_0} metadata: vnf_id: {get_param: TEST_vnf_id} TEST_port_0: type: OS::Neutron::Port properties: network: { get_param: TEST_Role_net_name }', '60', 'MANUAL RECORD', '2017-01-21 23:26:56'); 


INSERT INTO heat_template_params(HEAT_TEMPLATE_ARTIFACT_UUID, PARAM_NAME, IS_REQUIRED, PARAM_TYPE, PARAM_ALIAS) VALUES
('ff874603-4222-11e7-9252-005056850d2e', 'availability_zone_0', 1, 'string', ''),
('ff874603-4222-11e7-9252-005056850d2e', 'exn_direct_net_fqdn', 1, 'string', ''),
('ff874603-4222-11e7-9252-005056850d2e', 'exn_hsl_net_fqdn', 1, 'string', '');


INSERT INTO heat_environment(ARTIFACT_UUID, NAME, VERSION, DESCRIPTION, BODY, ARTIFACT_CHECKSUM, CREATION_TIMESTAMP) VALUES
('fefb1601-4222-11e7-9252-005056850d2e', 'module_nso.env', '2', 'Auto-generated HEAT Environment deployment artifact', 'parameters:\n  availability_zone_0: \"alln-zone-1\"\n  nso_flavor_name: \"citeis.1vCPUx2GB\"\n  nso_image_name: \"RHEL-6.8-BASE-20160912\"\n  nso_name_0: \"zrdm3vamp01nso001\"\n  nso_oam_ip_0: \"172.18.25.175\"\n  nso_oam_net_gw: \"172.18.25.1\"\n  nso_oam_net_mask: \"255.255.255.0\"\n  nso_sec_grp_id: \"36f48d82-f099-4437-bfbc-70d9e5d420d1\"\n  nso_srv_grp_id: \"e431c477-5bd1-476a-bfa9-e4ce16b8356b\"\n  oam_net_id: \"nso_oam\"\n  vf_module_id: \"145cd730797234b4a40aa99335abc143\"\n  vnf_id: \"730797234b4a40aa99335157b02871cd\"\n  vnf_name: \"Mobisupport\"\n', 'MWI2ODY0Yjc1NDJjNWU1NjdkMTAyMjVkNzFmZDU0MzA=', '2017-11-27 08:42:58'),
('fefb1751-4333-11e7-9252-005056850d2e', 'module_nso.env', '2', 'Auto-generated HEAT Environment deployment artifact', 'parameters:\n  availability_zone_0: \"alln-zone-1\"\n  nso_flavor_name: \"citeis.1vCPUx2GB\"\n  nso_image_name: \"RHEL-6.8-BASE-20160912\"\n  nso_name_0: \"zrdm3vamp01nso001\"\n  nso_oam_ip_0: \"172.18.25.175\"\n  nso_oam_net_gw: \"172.18.25.1\"\n  nso_oam_net_mask: \"255.255.255.0\"\n  nso_sec_grp_id: \"36f48d82-f099-4437-bfbc-70d9e5d420d1\"\n  nso_srv_grp_id: \"e431c477-5bd1-476a-bfa9-e4ce16b8356b\"\n  oam_net_id: \"nso_oam\"\n  vf_module_id: \"145cd730797234b4a40aa99335abc143\"\n  vnf_id: \"730797234b4a40aa99335157b02871cd\"\n  vnf_name: \"Mobisupport\"\n', 'MWI2ODY0Yjc1NDJjNWU1NjdkMTAyMjVkNzFmZDU0MzA=', '2017-11-27 08:42:58');

INSERT INTO vnf_resource(ORCHESTRATION_MODE, DESCRIPTION, CREATION_TIMESTAMP, MODEL_UUID, AIC_VERSION_MIN, AIC_VERSION_MAX, MODEL_INVARIANT_UUID, MODEL_VERSION, MODEL_NAME, TOSCA_NODE_TYPE, HEAT_TEMPLATE_ARTIFACT_UUID) VALUES
('HEAT', '1607 v10 - inherent network', '2017-04-14 21:46:28', 'ff2ae348-214a-11e7-93ae-92361f002672', '', '', '2fff5b20-214b-11e7-93ae-92361f002671', '2.0', 'v10', 'VF', null),
('HEAT', '1607 v10 - inherent network', '2017-04-14 21:46:28', 'fe6478e4-ea33-3346-ac12-ab121484a3fe', '', '', 'ff5256d1-5a33-55df-13ab-12abad84e7ff', '2.0', 'v12', 'VF', null),
('HEAT', '1607 v10 - inherent network', '2017-04-14 21:46:28', 'ff2ae348-214a-11e7-93ae-92361f002671', '', '', '2fff5b20-214b-11e7-93ae-92361f002671', '1.0', 'v10', 'VF', null),
('HEAT', '1607 vSAMP10a - inherent network', '2017-04-14 21:46:28', 'fe6478e4-ea33-3346-ac12-ab121484a3fw', '', '', '2fff5b20-214b-11e7-93ae-92361f002671', '1.0', 'vSAMP10a', 'VF', null);

INSERT INTO vnf_resource_customization(MODEL_CUSTOMIZATION_UUID, MODEL_INSTANCE_NAME, MIN_INSTANCES, MAX_INSTANCES, AVAILABILITY_ZONE_MAX_COUNT, NF_TYPE, NF_ROLE, NF_FUNCTION, NF_NAMING_CODE, CREATION_TIMESTAMP, VNF_RESOURCE_MODEL_UUID, MULTI_STAGE_DESIGN) VALUES
('68dc9a92-214c-11e7-93ae-92361f002671', 'v10 1', '0', '0', '0', 'vSAMP', 'vSAMP', 'vSAMP', 'vSAMP', '2017-05-26 15:08:24', 'ff2ae348-214a-11e7-93ae-92361f002671', null),
('f78914d9-423b-11e7-93f8-0050569a7967', 'test', '0', '0', '0', 'vSAMP', 'vSAMP', 'vSAMP', 'vSAMP', '2017-05-26 15:08:24', 'f78914d9-423b-11e7-93f8-0050569a7967', null),
('68dc9a92-214c-11e7-93ae-92361f002672', 'test', '0', '0', '0', 'vSAMP', 'vSAMP', 'vSAMP', 'vSAMP', '2017-05-26 15:08:24', 'ff2ae348-214a-11e7-93ae-92361f002672', null),
('68dc9a92-214c-11e7-93ae-92361f002673', 'test', '0', '0', '0', 'vSAMP', 'vSAMP', 'vSAMP', 'vSAMP', '2017-05-26 15:08:24', 'fe6478e4-ea33-3346-ac12-ab121484a3fe', null),
('68dc9a92-214c-11e7-93ae-92361f002674', 'test', '0', '0', '0', '', 'TEST', '', '', '2017-05-26 15:08:24', 'fe6478e4-ea33-3346-ac12-ab121484a3fw', null);



INSERT INTO vf_module(MODEL_UUID, MODEL_INVARIANT_UUID, MODEL_VERSION, MODEL_NAME, DESCRIPTION, IS_BASE, HEAT_TEMPLATE_ARTIFACT_UUID, VOL_HEAT_TEMPLATE_ARTIFACT_UUID, CREATION_TIMESTAMP, VNF_RESOURCE_MODEL_UUID) VALUES

('20c4431c-246d-11e7-93ae-92361f002671', '78ca26d0-246d-11e7-93ae-92361f002671', '2', 'test::base::module-0', 'v10 DEV Base', '1', 'ff874603-4222-11e7-9252-005056850d2e', null, '2016-09-14 18:19:56', 'ff2ae348-214a-11e7-93ae-92361f002671'),
('066de97e-253e-11e7-93ae-92361f002671', '64efd51a-2544-11e7-93ae-92361f002671', '2', 'test::PCM::module-1', 'v10 DEV PCM', '0', 'ff87482f-4222-11e7-9252-005056850d2e', null, '2016-09-14 18:19:56', 'ff2ae348-214a-11e7-93ae-92361f002671');


INSERT INTO vf_module_customization(MODEL_CUSTOMIZATION_UUID, LABEL, INITIAL_COUNT, MIN_INSTANCES, MAX_INSTANCES, AVAILABILITY_ZONE_COUNT, HEAT_ENVIRONMENT_ARTIFACT_UUID, VOL_ENVIRONMENT_ARTIFACT_UUID, CREATION_TIMESTAMP, VF_MODULE_MODEL_UUID) VALUES
('cb82ffd8-252a-11e7-93ae-92361f002671', 'base', '1', '0', '0', '0', 'fefb1601-4222-11e7-9252-005056850d2e', null, '2017-05-26 15:08:23', '20c4431c-246d-11e7-93ae-92361f002671'),
('b4ea86b4-253f-11e7-93ae-92361f002671', 'PCM', '0', '0', '0', '0', 'fefb1751-4333-11e7-9252-005056850d2e', null, '2017-05-26 15:08:23', '066de97e-253e-11e7-93ae-92361f002671');


INSERT INTO vnf_res_custom_to_vf_module_custom(VNF_RESOURCE_CUST_MODEL_CUSTOMIZATION_UUID, VF_MODULE_CUST_MODEL_CUSTOMIZATION_UUID, CREATION_TIMESTAMP) VALUES
('68dc9a92-214c-11e7-93ae-92361f002671', 'cb82ffd8-252a-11e7-93ae-92361f002671', '2017-05-26 15:08:24'),
('68dc9a92-214c-11e7-93ae-92361f002671', 'b4ea86b4-253f-11e7-93ae-92361f002671', '2017-05-26 15:08:24');

INSERT INTO allotted_resource(MODEL_UUID, MODEL_INVARIANT_UUID, MODEL_VERSION, MODEL_NAME, TOSCA_NODE_TYPE, SUBCATEGORY, DESCRIPTION, CREATION_TIMESTAMP) VALUES
('f6b7d4c6-e8a4-46e2-81bc-31cad5072842', 'b7a1b78e-6b6b-4b36-9698-8c9530da14af', '1.0', 'Tunnel_Xconn', '', '', '', '2017-05-26 15:08:24'); 

INSERT INTO allotted_resource_customization(MODEL_CUSTOMIZATION_UUID, MODEL_INSTANCE_NAME, PROVIDING_SERVICE_MODEL_INVARIANT_UUID, TARGET_NETWORK_ROLE, NF_TYPE, NF_ROLE, NF_FUNCTION, NF_NAMING_CODE, MIN_INSTANCES, MAX_INSTANCES, AR_MODEL_UUID, CREATION_TIMESTAMP) VALUES
('367a8ba9-057a-4506-b106-fbae818597c6', 'Sec_Tunnel_Xconn 11', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'f6b7d4c6-e8a4-46e2-81bc-31cad5072842', TIMESTAMP '2017-01-20 16:14:20.0');


INSERT INTO network_resource(MODEL_UUID, MODEL_NAME, MODEL_INVARIANT_UUID, DESCRIPTION, HEAT_TEMPLATE_ARTIFACT_UUID, NEUTRON_NETWORK_TYPE, MODEL_VERSION, TOSCA_NODE_TYPE, AIC_VERSION_MIN, AIC_VERSION_MAX, ORCHESTRATION_MODE, CREATION_TIMESTAMP) VALUES
('10b36f65-f4e6-4be6-ae49-9596dc1c47fc', 'CONTRAIL30_GNDIRECT', 'ce4ff476-9641-4e60-b4d5-b4abbec1271d', 'Contrail 30 GNDIRECT NW', 'aa874603-4222-11e7-9252-005056850d2e', 'BASIC', '1.0', '', '3.0', '', 'HEAT', '2017-01-17 20:35:05');

INSERT INTO network_resource_customization(MODEL_CUSTOMIZATION_UUID, MODEL_INSTANCE_NAME, NETWORK_TECHNOLOGY, NETWORK_TYPE, NETWORK_ROLE, NETWORK_SCOPE, CREATION_TIMESTAMP, NETWORK_RESOURCE_MODEL_UUID) VALUES
('3bdbb104-476c-483e-9f8b-c095b3d308ac', 'CONTRAIL30_GNDIRECT 9', '', '', '', '', '2017-04-19 14:28:32', '10b36f65-f4e6-4be6-ae49-9596dc1c47fc');

--second service insert





INSERT INTO vf_module(MODEL_UUID, MODEL_INVARIANT_UUID, MODEL_VERSION, MODEL_NAME, DESCRIPTION, IS_BASE, HEAT_TEMPLATE_ARTIFACT_UUID, VOL_HEAT_TEMPLATE_ARTIFACT_UUID, CREATION_TIMESTAMP, VNF_RESOURCE_MODEL_UUID) VALUES

('20c4431c-246d-11e7-93ae-92361f002672', '78ca26d0-246d-11e7-93ae-92361f002671', '2', 'test::base::module-0', 'v10 DEV Base', '1', 'ff874603-4222-11e7-9252-005056850d2e', null, '2016-09-14 18:19:56', 'ff2ae348-214a-11e7-93ae-92361f002671'),
('066de97e-253e-11e7-93ae-92361f002672', '64efd51a-2544-11e7-93ae-92361f002671', '2', 'test::PCM::module-1', 'v10 DEV PCM', '0', 'ff87482f-4222-11e7-9252-005056850d2e', null, '2016-09-14 18:19:56', 'ff2ae348-214a-11e7-93ae-92361f002671'),
('test', '64efd51a-2544-11e7-93ae-92361f002671', '2', 'test::PCM::module-1', 'v10 DEV PCM', '1', 'ff87482f-4222-11e7-9252-005056850d2e', null, '2016-09-14 18:19:56', 'ff2ae348-214a-11e7-93ae-92361f002672'),
('test1', 'b4ea86b4-253f-11e7-93ae-92361f002672', '2', 'test::PCM::module-1', 'v10 DEV PCM', '1', 'ff87482f-4222-11e7-9252-005056850d2e', null, '2016-09-14 18:19:56', 'ff2ae348-214a-11e7-93ae-92361f002672');

INSERT INTO vf_module_customization(MODEL_CUSTOMIZATION_UUID, LABEL, INITIAL_COUNT, MIN_INSTANCES, MAX_INSTANCES, AVAILABILITY_ZONE_COUNT, HEAT_ENVIRONMENT_ARTIFACT_UUID, VOL_ENVIRONMENT_ARTIFACT_UUID, CREATION_TIMESTAMP, VF_MODULE_MODEL_UUID) VALUES
('cb82ffd8-252a-11e7-93ae-92361f002672', 'base', '1', '0', '0', '0', 'fefb1601-4222-11e7-9252-005056850d2e', null, '2017-05-26 15:08:23', '20c4431c-246d-11e7-93ae-92361f002672'),
('b4ea86b4-253f-11e7-93ae-92361f002672', 'PCM', '0', '0', '0', '0', 'fefb1751-4333-11e7-9252-005056850d2e', null, '2017-05-26 15:08:23', '066de97e-253e-11e7-93ae-92361f002672');


INSERT INTO vnf_res_custom_to_vf_module_custom(VNF_RESOURCE_CUST_MODEL_CUSTOMIZATION_UUID, VF_MODULE_CUST_MODEL_CUSTOMIZATION_UUID, CREATION_TIMESTAMP) VALUES
('68dc9a92-214c-11e7-93ae-92361f002672', 'cb82ffd8-252a-11e7-93ae-92361f002672', '2017-05-26 15:08:24'),
('68dc9a92-214c-11e7-93ae-92361f002672', 'b4ea86b4-253f-11e7-93ae-92361f002672', '2017-05-26 15:08:24'),
('68dc9a92-214c-11e7-93ae-92361f002673', 'b4ea86b4-253f-11e7-93ae-92361f002672', '2017-05-26 15:08:24');


INSERT INTO vf_module_to_heat_files(VF_MODULE_MODEL_UUID, HEAT_FILES_ARTIFACT_UUID) VALUES
('20c4431c-246d-11e7-93ae-92361f002671', '00535bdd-0878-4478-b95a-c575c742bfb0'),
('066de97e-253e-11e7-93ae-92361f002671', '00535bdd-0878-4478-b95a-c575c742bfb0');


insert into network_resource_customization_to_service(service_model_uuid, resource_model_customization_uuid) values
('5df8b6de-2083-11e7-93ae-92361f002671', '3bdbb104-476c-483e-9f8b-c095b3d308ac'),
('5df8b6de-2083-11e7-93ae-92361f002672', '3bdbb104-476c-483e-9f8b-c095b3d308ac');


insert into vnf_resource_customization_to_service(service_model_uuid, resource_model_customization_uuid) values
('5df8b6de-2083-11e7-93ae-92361f002671', '68dc9a92-214c-11e7-93ae-92361f002671'),
('f78914d9-423b-11e7-93f8-0050569a7965', 'f78914d9-423b-11e7-93f8-0050569a7967'),
('5df8b6de-2083-11e7-93ae-92361f002672', '68dc9a92-214c-11e7-93ae-92361f002672');

insert into allotted_resource_customization_to_service(service_model_uuid, resource_model_customization_uuid) values
('5df8b6de-2083-11e7-93ae-92361f002671', '367a8ba9-057a-4506-b106-fbae818597c6'),
('5df8b6de-2083-11e7-93ae-92361f002672', '367a8ba9-057a-4506-b106-fbae818597c6');


INSERT INTO vnf_recipe(ID, nf_role, ACTION, SERVICE_TYPE, VERSION_STR, DESCRIPTION, ORCHESTRATION_URI, VNF_PARAM_XSD, RECIPE_TIMEOUT, CREATION_TIMESTAMP, VF_MODULE_ID) VALUES
('77', 'VID_DEFAULT', 'createInstance', '', '1', 'VID_DEFAULT recipe to create VNF if no custom BPMN flow is found', '/mso/async/services/CreateVnfInfra', '', '180', '2016-09-14 19:18:20', ''),
('78', 'VID_DEFAULT', 'deleteInstance', '', '1', 'VID_DEFAULT recipe to delete VNF if no custom BPMN flow is found', '/mso/async/services/DeleteVnfInfra', '', '180', '2016-09-14 19:18:20', ''),
('81', 'VID_DEFAULT', 'updateInstance', '', '1', 'VID_DEFAULT update', '/mso/async/services/UpdateVnfInfra', '', '180', '2017-07-28 18:19:39', ''),
('85', 'VID_DEFAULT', 'replaceInstance', '', '1', 'VID_DEFAULT replace', '/mso/async/services/ReplaceVnfInfra', 'VID_DEFAULT', '180', '2017-07-28 18:19:45', ''),
('10000', 'VID_DEFAULT', 'inPlaceSoftwareUpdate', '', '1', 'VID_DEFAULT inPlaceSoftwareUpdate', '/mso/async/services/VnfInPlaceUpdate', '', '180', '2017-10-25 18:19:45', ''),
('10001', 'VID_DEFAULT', 'applyUpdatedConfig', '', '1', 'VID_DEFAULT applyUpdatedConfig', '/mso/async/services/VnfConfigUpdate', '', '180', '2017-10-25 18:19:45', ''),


('13', 'CREATE', 'createInstance', 'VID_DEFAULT', '1', 'Create VnfInstance', '/mso/async/services/CreateVnf', '', '180', '2016-06-03 10:14:10', '1882934'),
('14', 'REPLACE', 'replaceInstance', 'VID_DEFAULT', '1', 'Replace VnfInstance', '/mso/async/services/ReplaceVnf', '', '180', '2016-06-03 10:14:10', '1882934'),
('15', 'UPDATE', 'updateInstance', 'VID_DEFAULT', '1', 'Update VnfInstance', '/mso/async/services/UpdateVnf', '', '180', '2016-06-03 10:14:10', '1882934'),
('16', 'DEFAULT', 'applyUpdatedConfig', 'VID_DEFAULT', '1', 'Default VnfInstance', '/mso/async/services/DefaultVnfUri', '', '180', '2016-06-03 10:14:10', '1882934'),
('17', 'DEFAULT', 'inPlaceSoftwareUpdate', 'VID_DEFAULT', '1', 'Default VnfInstance', '/mso/async/services/DefaultVnfUri', '', '180', '2016-06-03 10:14:10', '1882934'),
('20', 'DELETE', 'deleteInstance', 'VID_DEFAULT', '1', 'Delete VnfInstance', '/mso/async/services/DeleteVnf', '', '180', '2016-06-03 10:14:10', '1882934'),
('21', 'TEST', 'replaceInstance', 'VID_DEFAULT', '2', 'custom bpmn for vnf recreate', '/mso/async/services/RecreateInfraVce', '', '180', '2016-06-03 10:14:10', '1882934');

INSERT INTO vnf_components(VNF_ID, COMPONENT_TYPE, HEAT_TEMPLATE_ID, HEAT_ENVIRONMENT_ID, CREATION_TIMESTAMP) VALUES
('13961', 'VOLUME', '13843', '13961', '2016-05-19 20:22:02');   

INSERT INTO vnf_components_recipe(ID, VNF_TYPE, VNF_COMPONENT_TYPE, ACTION, SERVICE_TYPE, VERSION, DESCRIPTION, ORCHESTRATION_URI, VNF_COMPONENT_PARAM_XSD, RECIPE_TIMEOUT, CREATION_TIMESTAMP, VF_MODULE_MODEL_UUID) VALUES
('5', '*', 'VOLUME_GROUP', 'CREATE', '', '1', 'Recipe Match All for VF Modules if no custom flow exists', '/mso/async/services/createCinderVolumeV1', '', '180', '2016-06-03 10:15:11', ''),
('7', '*', 'VOLUME_GROUP', 'DELETE', '', '1', 'Recipe Match All for VF Modules if no custom flow exists', '/mso/async/services/deleteCinderVolumeV1', '', '180', '2016-06-03 10:15:11', ''),
('9', '*', 'VOLUME_GROUP', 'UPDATE', '', '1', 'Recipe Match All for VF Modules if no custom flow exists', '/mso/async/services/updateCinderVolumeV1', '', '180', '2016-06-03 10:15:11', ''),
('13', '', 'VOLUME_GROUP', 'DELETE_VF_MODULE_VOL', '', '1', 'Recipe Match All for VF Modules if no custom flow exists', '/mso/async/services/DeleteVfModuleVolume', '', '180', '2016-06-03 10:15:11', '*'),
('15', '', 'VOLUME_GROUP', 'UPDATE_VF_MODULE_VOL', '', '1', 'Recipe Match All for VF Modules if no custom flow exists', '/mso/async/services/UpdateVfModuleVolume', '', '180', '2016-06-03 10:15:11', '*'),
('16', 'volumeGroup', 'volumeGroup', 'createInstance', '', '1', 'VID_DEFAULT recipe to create volume-group if no custom BPMN flow is found', '/mso/async/services/CreateVfModuleVolumeInfraV1', '', '180', '2016-09-14 19:18:20', 'VID_DEFAULT'),
('17', 'volumeGroup', 'volumeGroup', 'deleteInstance', '', '1', 'VID_DEFAULT recipe to delete volume-group if no custom BPMN flow is found', '/mso/async/services/DeleteVfModuleVolumeInfraV1', '', '180', '2016-09-14 19:18:20', 'VID_DEFAULT'),
('18', 'volumeGroup', 'volumeGroup', 'updateInstance', '', '1', 'VID_DEFAULT recipe to update volume-group if no custom BPMN flow is found', '/mso/async/services/UpdateVfModuleVolumeInfraV1', '', '180', '2016-09-14 19:18:20', 'VID_DEFAULT'),
('19', 'vfModule', 'vfModule', 'createInstance', '', '1', 'VID_DEFAULT recipe to create vf-module if no custom BPMN flow is found', '/mso/async/services/CreateVfModuleInfra', '', '180', '2016-09-14 19:18:20', '20c4431c-246d-11e7-93ae-92361f002672'),
('20', 'vfModule', 'vfModule', 'deleteInstance', '', '1', 'VID_DEFAULT recipe to delete vf-module if no custom BPMN flow is found', '/mso/async/services/DeleteVfModuleInfra', '', '180', '2016-09-14 19:18:20', 'VID_DEFAULT'),
('21', 'vfModule', 'vfModule', 'updateInstance', '', '1', 'VID_DEFAULT recipe to update vf-module if no custom BPMN flow is found', '/mso/async/services/UpdateVfModuleInfra', '', '180', '2016-09-14 19:18:20', 'VID_DEFAULT'),
('25', 'vfModule', 'vfModule', 'replaceInstance', '', '1', 'VID_DEFAULT vfModule replace', '/mso/async/services/ReplaceVfModuleInfra', '', '180', '2017-07-28 18:25:06', 'VID_DEFAULT');

INSERT INTO catalogdb.network_recipe(ID, MODEL_NAME, ACTION, DESCRIPTION, ORCHESTRATION_URI, NETWORK_PARAM_XSD, RECIPE_TIMEOUT, SERVICE_TYPE, CREATION_TIMESTAMP, VERSION_STR) VALUES
('17', 'VID_DEFAULT', 'createInstance', 'VID_DEFAULT recipe to create network if no custom BPMN flow is found', '/mso/async/services/CreateNetworkInstance', '', '180', '', '2016-09-14 19:18:20', '1.0'),
('18', 'VID_DEFAULT', 'updateInstance', 'VID_DEFAULT recipe to update network if no custom BPMN flow is found', '/mso/async/services/UpdateNetworkInstance', '', '180', '', '2016-09-14 19:18:20', '1.0'),
('19', 'VID_DEFAULT', 'deleteInstance', 'VID_DEFAULT recipe to delete network if no custom BPMN flow is found', '/mso/async/services/DeleteNetworkInstance', '', '180', '', '2016-09-14 19:18:20', '1.0'),
('14', 'tester', 'createInstance', '', '/mso/async/services/CreateNetwork', '', '180', '', '2017-09-22 18:47:31', '1'),
('15', 'tester', 'updateInstance', '', '/mso/async/services/UpdateNetwork', '', '180', '', '2017-09-22 18:47:31', '1'),
('16', 'tester', 'deleteInstance', '', '/mso/async/services/DeleteNetwork', '', '180', '', '2017-09-22 18:47:31', '1');    

INSERT INTO vnf_recipe (nf_role, ACTION, VERSION_STR, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT)
VALUES
('GR-API-DEFAULT', 'createInstance', '1', 'Gr api recipe to create vnf', '/mso/async/services/WorkflowActionBB', 180),
('GR-API-DEFAULT', 'deleteInstance', '1', 'Gr api recipe to delete vnf', '/mso/async/services/WorkflowActionBB', 180),
('GR-API-DEFAULT', 'updateInstance', '1', 'Gr api recipe to update vnf', '/mso/async/services/WorkflowActionBB', 180),
('GR-API-DEFAULT', 'replaceInstance', '1', 'Gr api recipe to replace vnf', '/mso/async/services/WorkflowActionBB', 180),
('GR-API-DEFAULT', 'inPlaceSoftwareUpdate', '1', 'Gr api recipe to do an in place software update', '/mso/async/services/VnfInPlaceUpdate', 180),
('GR-API-DEFAULT', 'applyUpdatedConfig', '1', 'Gr api recipe to apply updated config', '/mso/async/services/VnfConfigUpdate', 180);

UPDATE vnf_recipe
SET nf_role = 'VNF-API-DEFAULT'
WHERE nf_role = 'VID_DEFAULT';

INSERT INTO service (MODEL_UUID, MODEL_NAME, MODEL_INVARIANT_UUID, MODEL_VERSION, DESCRIPTION)
VALUES
('d88da85c-d9e8-4f73-b837-3a72a431622b', 'GR-API-DEFAULT', '944862ae-bb65-4429-8330-a6c9170d6672', '1.0', 'Gr api service for VID to use for infra APIH orchestration');

UPDATE service
SET model_name = 'VNF-API-DEFAULT'
WHERE model_name = 'VID_DEFAULT';

INSERT INTO service_recipe (ACTION, VERSION_STR, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT, SERVICE_MODEL_UUID)
VALUES
('activateInstance', '1.0', 'Gr api recipe to activate service-instance', '/mso/async/services/WorkflowActionBB', 180, 'd88da85c-d9e8-4f73-b837-3a72a431622b'),
('createInstance', '1.0', 'Gr api recipe to create service-instance', '/mso/async/services/WorkflowActionBB', 180, 'd88da85c-d9e8-4f73-b837-3a72a431622b'),
('deactivateInstance', '1.0', 'Gr api recipe to deactivate service-instance', '/mso/async/services/WorkflowActionBB', 180, 'd88da85c-d9e8-4f73-b837-3a72a431622b'),
('deleteInstance', '1.0', 'Gr api recipe to delete service-instance', '/mso/async/services/WorkflowActionBB', 180, 'd88da85c-d9e8-4f73-b837-3a72a431622b'),
('assignInstance', '1.0', 'Gr api recipe to assign service-instance', '/mso/async/services/WorkflowActionBB', 180, 'd88da85c-d9e8-4f73-b837-3a72a431622b'),
('unassignInstance', '1.0', 'Gr api recipe to unassign service-instance', '/mso/async/services/WorkflowActionBB', 180, 'd88da85c-d9e8-4f73-b837-3a72a431622b');

INSERT INTO vnf_components_recipe (VNF_COMPONENT_TYPE, ACTION, VERSION, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT, VF_MODULE_MODEL_UUID)
VALUES
('volumeGroup', 'createInstance', '1', 'Gr api recipe to create volume-group', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT'),
('volumeGroup', 'deleteInstance', '1', 'Gr api recipe to delete volume-group', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT'),
('volumeGroup', 'updateInstance', '1', 'Gr api recipe to update volume-group', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT'),
('vfModule', 'createInstance', '1', 'Gr api recipe to create vf-module', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT'),
('vfModule', 'deleteInstance', '1', 'Gr api recipe to delete vf-module', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT'),
('vfModule', 'updateInstance', '1', 'Gr api recipe to update vf-module', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT'),
('vfModule', 'replaceInstance', '1', 'Gr api recipe to replace vf-module', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT'),
('vfModule', 'deactivateAndCloudDelete', '1', 'Gr api recipe to deactivateAndCloudDelete vf-module', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT'),
('vfModule', 'scaleOut', '1', 'Gr api recipe to scale out vfModule', '/mso/async/services/WorkflowActionBB', '180', 'GR-API-DEFAULT');               
INSERT INTO requestdb.activate_operational_env_service_model_distribution_status (OPERATIONAL_ENV_ID, SERVICE_MODEL_VERSION_ID, REQUEST_ID,SERVICE_MOD_VER_FINAL_DISTR_STATUS,RECOVERY_ACTION,RETRY_COUNT_LEFT,WORKLOAD_CONTEXT, CREATE_TIME, MODIFY_TIME)
VALUES
('1234', 'TEST1234', '00032ab7-3fb3-42e5-965d-8ea592502017', "Test", "Test", 1, 'DEFAULT', '2018-08-14 16:50:59',  '2018-08-14 16:50:59');
INSERT INTO requestdb.activate_operational_env_service_model_distribution_status (OPERATIONAL_ENV_ID, SERVICE_MODEL_VERSION_ID, REQUEST_ID,SERVICE_MOD_VER_FINAL_DISTR_STATUS,RECOVERY_ACTION,RETRY_COUNT_LEFT,WORKLOAD_CONTEXT, CREATE_TIME, MODIFY_TIME)
VALUES
('1234', 'TEST1235', '00032ab7-3fb3-42e5-965d-8ea592502017', "Test", "Test", 2, 'DEFAULT', '2018-08-14 16:50:59',  '2018-08-14 16:50:59');

INSERT INTO requestdb.activate_operational_env_per_distributionid_status (DISTRIBUTION_ID, DISTRIBUTION_ID_STATUS, DISTRIBUTION_ID_ERROR_REASON, CREATE_TIME, MODIFY_TIME, OPERATIONAL_ENV_ID, SERVICE_MODEL_VERSION_ID, REQUEST_ID)
VALUES
('111', 'TEST', 'ERROR', '2018-09-12 19:29:24', '2018-09-12 19:29:25', '1234', 'TEST1234', '00032ab7-3fb3-42e5-965d-8ea592502017');

UPDATE vnf_components_recipe
SET vf_module_model_uuid = 'VNF-API-DEFAULT'
WHERE vf_module_model_uuid = 'VID_DEFAULT';

INSERT INTO network_recipe (MODEL_NAME, ACTION, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT, VERSION_STR)
VALUES
('GR-API-DEFAULT', 'createInstance', 'Gr api recipe to create network', '/mso/async/services/WorkflowActionBB', 180, '1.0'),
('GR-API-DEFAULT', 'updateInstance', 'Gr api recipe to update network', '/mso/async/services/WorkflowActionBB', 180, '1.0'),
('GR-API-DEFAULT', 'deleteInstance', 'Gr api recipe to delete network', '/mso/async/services/WorkflowActionBB', 180, '1.0');

UPDATE network_recipe
SET model_name = 'VNF-API-DEFAULT'
WHERE model_name = 'VID_DEFAULT';