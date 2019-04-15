use catalogdb;

insert into heat_template(artifact_uuid, name, version, description, body, timeout_minutes, artifact_checksum, creation_timestamp) values ('ff874603-4222-11e7-9252-005056850d2e', 'module_mns_zrdm3frwl01exn_01_rgvm_1.yml', '1', 'created from csar', 'heat_template_version: 2013-05-23 description: heat template that creates TEST VNF parameters: TEST_server_name: type: string label: TEST server name description: TEST server name TEST_image_name: type: string label: image name description: TEST image name TEST_flavor_name: type: string label: TEST flavor name description: flavor name of TEST instance TEST_Role_net_name: type: string label: TEST network name description: TEST network name TEST_vnf_id: type: string label: TEST VNF Id description: TEST VNF Id resources:TEST: type: OS::Nova::Server properties: name: { get_param: TEST_server_name } image: { get_param: TEST_image_name } flavor: { get_param: TEST_flavor_name } networks: - port: { get_resource: TEST_port_0} metadata: vnf_id: {get_param: TEST_vnf_id} TEST_port_0: type: OS::Neutron::Port properties: network: { get_param: TEST_Role_net_name }', '60', 'MANUAL RECORD', '2017-01-21 23:26:56');

insert into temp_network_heat_template_lookup(network_resource_model_name, heat_template_artifact_uuid, aic_version_min, aic_version_max) values
('CONTRAIL30_GNDIRECT', 'ff874603-4222-11e7-9252-005056850d2e', '3', '3'),
('MSO_Example', 'ff874603-4222-11e7-9252-005056850d2e', '3', '3'),
('ExtVL', 'ff874603-4222-11e7-9252-005056850d2e', '3', '3'),
('AIC30_CONTRAIL_BASIC', 'ff874603-4222-11e7-9252-005056850d2e', '3', '3'),
('CONTRAIL30_BASIC', 'ff874603-4222-11e7-9252-005056850d2e', '3', '3');

insert into network_resource(model_uuid, model_name, model_invariant_uuid, description, heat_template_artifact_uuid, neutron_network_type, model_version, tosca_node_type, aic_version_min, aic_version_max, orchestration_mode, creation_timestamp) values
('10b36f65-f4e6-4be6-ae49-9596dc1c47fc', 'CONTRAIL30_GNDIRECT', 'ce4ff476-9641-4e60-b4d5-b4abbec1271d', 'Contrail 30 GNDIRECT NW', 'ff874603-4222-11e7-9252-005056850d2e', 'BASIC', '1.0', '', '3.0', '', 'HEAT', '2017-01-17 20:35:05');

insert into network_resource_customization(model_customization_uuid, model_instance_name, network_technology, network_type, network_role, network_scope, creation_timestamp, network_resource_model_uuid) values
('3bdbb104-476c-483e-9f8b-c095b3d308ac', 'CONTRAIL30_GNDIRECT 9', '', '', '', '', '2017-04-19 14:28:32', '10b36f65-f4e6-4be6-ae49-9596dc1c47fc');



INSERT INTO temp_network_heat_template_lookup(NETWORK_RESOURCE_MODEL_NAME, HEAT_TEMPLATE_ARTIFACT_UUID, AIC_VERSION_MIN, AIC_VERSION_MAX) VALUES
('TENANT_OAM_NETWORK', 'ff874603-4222-11e7-9252-005056850d2e', '3.0', NULL);
INSERT INTO temp_network_heat_template_lookup(NETWORK_RESOURCE_MODEL_NAME, HEAT_TEMPLATE_ARTIFACT_UUID, AIC_VERSION_MIN, AIC_VERSION_MAX) VALUES
('SRIOV_PROVIDER_NETWORK', 'ff874603-4222-11e7-9252-005056850d2e', '3.0', NULL);

insert into vnf_resource(orchestration_mode, description, creation_timestamp, model_uuid, aic_version_min, aic_version_max, model_invariant_uuid, model_version, model_name, tosca_node_type, heat_template_artifact_uuid) values
('HEAT', '1607 vSAMP10a - inherent network', '2017-04-14 21:46:28', 'ff2ae348-214a-11e7-93ae-92361f002671', '', '', '2fff5b20-214b-11e7-93ae-92361f002671', '1.0', 'vSAMP10a', 'VF', null);

insert into workflow(artifact_uuid, artifact_name, name, operation_name, version, description, body, resource_target, source) values
('5b0c4322-643d-4c9f-b184-4516049e99b1', 'testingWorkflow', 'testingWorkflow', 'create', 1, 'Test Workflow', null, 'vnf', 'sdc');

insert into vnf_resource_to_workflow(vnf_resource_model_uuid, workflow_id) values
('ff2ae348-214a-11e7-93ae-92361f002671', '1');

insert into activity_spec(name, description, version) values
('testActivity1', 'Test Activity 1', 1.0);

insert into workflow_activity_spec_sequence(workflow_id, activity_spec_id, seq_no) values
(1, 1, 1);

INSERT INTO activity_spec (NAME, DESCRIPTION, VERSION) 
VALUES 
('VNFSetInMaintFlagActivity','Activity to Set InMaint Flag in A&AI',1.0),
('VNFCheckPserversLockedFlagActivity','Activity Check Pservers Locked Flag VNF',1.0),
('VNFCheckInMaintFlagActivity','Activity CheckIn Maint Flag on VNF',1.0),
('VNFCheckClosedLoopDisabledFlagActivity','Activity Check Closed Loop Disabled Flag on VNF',1.0),
('VNFSetClosedLoopDisabledFlagActivity','Activity Set Closed Loop Disabled Flag on VNF',1.0),
('VNFUnsetClosedLoopDisabledFlagActivity','Activity Unset Closed Loop Disabled Flag on VNF',1.0),
('VNFLockActivity','Activity Lock on VNF',1.0),
('VNFUnlockActivity','Activity UnLock on VNF',1.0),
('VNFStopActivity','Activity Stop on VNF',1.0),
('VNFStartActivity','Activity Start on VNF',1.0),
('VNFSnapShotActivity','Activity Snap Shot on VNF',1.0),
('FlowCompleteActivity','Activity Complete on VNF',1.0),
('PauseForManualTaskActivity','Activity Pause For Manual Task on VNF',1.0),
('DistributeTrafficActivity','Activity Distribute Traffic on VNF',1.0),
('DistributeTrafficCheckActivity','Activity Distribute Traffic Check on VNF',1.0),
('VNFHealthCheckActivity','Activity Health Check on VNF',1.0),
('VNFQuiesceTrafficActivity','Activity Quiesce Traffic on VNF',1.0),
('VNFResumeTrafficActivity','Activity Resume Traffic on VNF',1.0),
('VNFUnsetInMaintFlagActivity','Activity Unset InMaint Flag on VNF',1.0),
('VNFUpgradeBackupActivity','Activity Upgrade Backup on VNF',1.0),
('VNFUpgradePostCheckActivity','Activity Upgrade Post Check on VNF',1.0),
('VNFUpgradePreCheckActivity','Activity Upgrade PreCheck on VNF',1.0),
('VNFUpgradeSoftwareActivity','Activity UpgradeS oftware on VNF',1.0),
('VnfInPlaceSoftwareUpdate','Activity InPlace Software Update on VNF',1.0);

INSERT INTO activity_spec_categories (NAME)
VALUES ('VNF');

INSERT INTO activity_spec_to_activity_spec_categories(ACTIVITY_SPEC_ID, ACTIVITY_SPEC_CATEGORIES_ID) 
VALUES
((select ID from activity_spec where NAME='VNFSetInMaintFlagActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFCheckPserversLockedFlagActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFCheckInMaintFlagActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFCheckClosedLoopDisabledFlagActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFSetClosedLoopDisabledFlagActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFUnsetClosedLoopDisabledFlagActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFLockActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFUnlockActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFStopActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFStartActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFSnapShotActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='FlowCompleteActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='PauseForManualTaskActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='DistributeTrafficActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='DistributeTrafficCheckActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFHealthCheckActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFQuiesceTrafficActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFResumeTrafficActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFUnsetInMaintFlagActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFUpgradeBackupActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFUpgradePostCheckActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFUpgradePreCheckActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFUpgradeSoftwareActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VnfInPlaceSoftwareUpdate' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF'));

INSERT INTO activity_spec_parameters (NAME, TYPE, DIRECTION, DESCRIPTION) 
VALUES('WorkflowException','WorkflowException','output','Description');

INSERT INTO activity_spec_to_activity_spec_parameters( ACTIVITY_SPEC_ID, ACTIVITY_SPEC_PARAMETERS_ID) 
VALUES
((select ID from activity_spec where NAME='VNFSetInMaintFlagActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFCheckPserversLockedFlagActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFCheckInMaintFlagActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFCheckClosedLoopDisabledFlagActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFSetClosedLoopDisabledFlagActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFUnsetClosedLoopDisabledFlagActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFLockActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFUnlockActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFStopActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFStartActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFSnapShotActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='FlowCompleteActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='PauseForManualTaskActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='DistributeTrafficActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='DistributeTrafficCheckActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFHealthCheckActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFQuiesceTrafficActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFResumeTrafficActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFUnsetInMaintFlagActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFUpgradeBackupActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFUpgradePostCheckActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFUpgradePreCheckActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFUpgradeSoftwareActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output'));

INSERT INTO `user_parameters`(`NAME`,`PAYLOAD_LOCATION`,`LABEL`,`TYPE`,`DESCRIPTION`,`IS_REQUIRED`,`MAX_LENGTH`,`ALLOWABLE_CHARS`)
VALUES 
('cloudOwner','cloudConfiguration','Cloud Owner','text','',1,7,''), 
('operations_timeout','userParams','Operations Timeout','text','',1,50,''),
('existing_software_version','userParams','Existing Software Version','text','',1,50,''),
('tenantId','cloudConfiguration','Tenant/Project ID','text','',1,36,''),
('new_software_version','userParams','New Software Version','text','',1,50,''),
('lcpCloudRegionId','cloudConfiguration','Cloud Region ID','text','',1,7,'');

INSERT INTO `activity_spec_to_user_parameters`(`ACTIVITY_SPEC_ID`,`USER_PARAMETERS_ID`)
VALUES
((select ID from activity_spec where NAME='VNFStopActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='lcpCloudRegionId')),
((select ID from activity_spec where NAME='VNFStopActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='tenantId')),
((select ID from activity_spec where NAME='VNFStartActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='lcpCloudRegionId')),
((select ID from activity_spec where NAME='VNFStartActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='tenantId')),
((select ID from activity_spec where NAME='VNFSnapShotActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='lcpCloudRegionId')),
((select ID from activity_spec where NAME='VNFSnapShotActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='tenantId')),
((select ID from activity_spec where NAME='VNFQuiesceTrafficActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='operations_timeout')),
((select ID from activity_spec where NAME='VNFUpgradeBackupActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='existing_software_version')),
((select ID from activity_spec where NAME='VNFUpgradeBackupActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='new_software_version')),
((select ID from activity_spec where NAME='VNFUpgradePostCheckActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='existing_software_version')),
((select ID from activity_spec where NAME='VNFUpgradePostCheckActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='new_software_version')),
((select ID from activity_spec where NAME='VNFUpgradePreCheckActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='existing_software_version')),
((select ID from activity_spec where NAME='VNFUpgradePreCheckActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='new_software_version')),
((select ID from activity_spec where NAME='VNFUpgradeSoftwareActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='existing_software_version')),
((select ID from activity_spec where NAME='VNFUpgradeSoftwareActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='new_software_version'));


--------START Request DB INSERTS --------
insert into requestdb.watchdog_distributionid_status(DISTRIBUTION_ID, DISTRIBUTION_ID_STATUS,LOCK_VERSION) values 
('watchdogTestStatusSuccess', 'SUCCESS',0),
('watchdogTestStatusFailure', 'FAILURE',0),
('watchdogTestStatusTimeout', 'TIMEOUT',0),
('watchdogTestStatusIncomplete', 'INCOMPLETE',0),
('watchdogTestStatusException', 'EXCEPTION',0),
('watchdogTestStatusNull', 'NULL',0),
('testStatusSuccessTosca', 'SUCCESS',0),
('testStatusFailureTosca', 'FAILURE',0),
('testStatusTimeoutTosca', 'TIMEOUT',0),
('testStatusIncompleteTosca', 'INCOMPLETE',0),
('testStatusExceptionTosca', 'EXCEPTION',0),
('testStatusNullTosca', 'NULL',0);

--WatchdogDistrubutionTest
insert into requestdb.watchdog_per_component_distribution_status(DISTRIBUTION_ID, COMPONENT_NAME, COMPONENT_DISTRIBUTION_STATUS) values
('watchdogTestStatusSuccess', 'SO', 'COMPONENT_DONE_OK'),
('watchdogTestStatusSuccess', 'AAI', 'COMPONENT_DONE_OK'),
('watchdogTestStatusSuccess', 'SDNC', 'COMPONENT_DONE_OK'),
('watchdogTestStatusFailure', 'SO', 'COMPONENT_DONE_ERROR'),
('watchdogTestStatusFailure', 'AAI', 'COMPONENT_DONE_ERROR'),
('watchdogTestStatusFailure', 'SDNC', 'COMPONENT_DONE_ERROR'),
('watchdogTestStatusException', 'SO', 'COMPONENT_MALFORMED'),
('watchdogTestStatusException', 'AAI', 'COMPONENT_MALFORMED'),
('watchdogTestStatusException', 'SDNC', 'COMPONENT_MALFORMED'),
('testStatusSuccessTosca', 'SO', 'COMPONENT_DONE_OK'),
('testStatusSuccessTosca', 'AAI', 'COMPONENT_DONE_OK'),
('testStatusSuccessTosca', 'SDNC', 'COMPONENT_DONE_OK'),
('testStatusFailureTosca', 'SO', 'COMPONENT_DONE_ERROR'),
('testStatusFailureTosca', 'AAI', 'COMPONENT_DONE_ERROR'),
('testStatusFailureTosca', 'SDNC', 'COMPONENT_DONE_ERROR'),
('testStatusExceptionTosca', 'SO', 'COMPONENT_MALFORMED'),
('testStatusExceptionTosca', 'AAI', 'COMPONENT_MALFORMED'),
('testStatusExceptionTosca', 'SDNC', 'COMPONENT_MALFORMED');

insert into requestdb.watchdog_service_mod_ver_id_lookup(DISTRIBUTION_ID, SERVICE_MODEL_VERSION_ID, DISTRIBUTION_NOTIFICATION, CONSUMER_ID) values
('watchdogTestStatusSuccess', '5df8b6de-2083-11e7-93ae-92361f002671', NULL, NULL),
('watchdogTestStatusNull', '00000000-0000-0000-0000-000000000000', NULL, NULL);