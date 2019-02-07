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


--------START Request DB INSERTS --------
insert into requestdb.watchdog_distributionid_status(DISTRIBUTION_ID, DISTRIBUTION_ID_STATUS) values 
('watchdogTestStatusSuccess', 'SUCCESS'),
('watchdogTestStatusFailure', 'FAILURE'),
('watchdogTestStatusTimeout', 'TIMEOUT'),
('watchdogTestStatusIncomplete', 'INCOMPLETE'),
('watchdogTestStatusException', 'EXCEPTION'),
('watchdogTestStatusNull', 'NULL'),
('testStatusSuccessTosca', 'SUCCESS'),
('testStatusFailureTosca', 'FAILURE'),
('testStatusTimeoutTosca', 'TIMEOUT'),
('testStatusIncompleteTosca', 'INCOMPLETE'),
('testStatusExceptionTosca', 'EXCEPTION'),
('testStatusNullTosca', 'NULL');

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
