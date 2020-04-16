

CREATE TABLE IF NOT EXISTS `homing_instances` (
`SERVICE_INSTANCE_ID` varchar(50) NOT NULL,
`CLOUD_OWNER` VARCHAR(200) NOT NULL,
`CLOUD_REGION_ID` VARCHAR(200) NOT NULL,
`OOF_DIRECTIVES` longtext NULL DEFAULT NULL,
PRIMARY KEY (`SERVICE_INSTANCE_ID`)
) ;

insert into heat_files(artifact_uuid, name, version, description, body, artifact_checksum, creation_timestamp) values
('00535bdd-0878-4478-b95a-c575c742bfb0', 'nimbus-ethernet-gw', '1', 'created from csar', 'DEVICE=$dev\nBOOTPROTO=none\nNM_CONTROLLED=no\nIPADDR=$ip\nNETMASK=$netmask\nGATEWAY=$gateway\n', 'MANUAL RECORD', '2017-01-21 23:56:43');


insert into tosca_csar(artifact_uuid, name, version, description, artifact_checksum, url, creation_timestamp) values
('0513f839-459d-46b6-aa3d-2edfef89a079', 'service-Ciservicee3756aea561a-csar.csar', '1', 'TOSCA definition package of the asset', 'YTk1MmY2MGVlNzVhYTU4YjgzYjliMjNjMmM3NzU1NDc=', '/sdc/v1/catalog/services/Ciservicee3756aea561a/1.0/artifacts/service-Ciservicee3756aea561a-csar.csar', '2017-11-27 11:38:27');


insert into service(model_uuid, model_name, model_invariant_uuid, model_version, description, creation_timestamp, tosca_csar_artifact_uuid, service_type, service_role, environment_context, workload_context) values
('5df8b6de-2083-11e7-93ae-92361f002671', 'MSOTADevInfra_vSAMP10a_Service', '9647dfc4-2083-11e7-93ae-92361f002671', '1.0', 'MSO aLaCarte Vfmodule with addon', '2017-04-14 13:42:39', '0513f839-459d-46b6-aa3d-2edfef89a079', 'NA', 'NA', 'Luna', 'Oxygen');

insert into service(model_uuid, model_name, model_invariant_uuid, model_version, description, creation_timestamp, tosca_csar_artifact_uuid, service_type, service_role, environment_context, workload_context) values
('5df8b6de-2083-11e7-93ae-92361f002679', 'MSOTADevInfra_Test_Service', '9647dfc4-2083-11e7-93ae-92361f002673', '3.0', 'MSO aLaCarte Vfmodule with addon', '2017-04-14 13:42:39', '0513f839-459d-46b6-aa3d-2edfef89a079', 'NA', 'NA', 'Luna', 'Oxygen');

INSERT INTO external_service_to_internal_model_mapping(id,SERVICE_NAME,PRODUCT_FLAVOR,SUBSCRIPTION_SERVICE_TYPE,SERVICE_MODEL_UUID) VALUES
(3,'MySpecialServiceName','Gold','Test','c94b1098-3c71-11e8-b467-0ed5f89f7199');

insert into tosca_csar(ARTIFACT_UUID,NAME,VERSION,DESCRIPTION,ARTIFACT_CHECKSUM,URL,CREATION_TIMESTAMP) values
('266eaf78-af74-45cd-83ec-9c477f189dc1','service-NetworkCollectionSvc0106-csar.csar','1','TOSCA definition package of the asset','OGQ1M2QyYjU0OWMzZTY4MWVlYTNhOWIxNmQ2NjEyZDQ=','/sdc/v1/catalog/services/NetworkCollectionSvc0106/1.0/artifacts/service-NetworkCollectionSvc0106-csar.csar','2018-06-01 16:06:52');
insert into service(model_uuid, model_name, model_invariant_uuid, model_version, description, creation_timestamp, tosca_csar_artifact_uuid, service_type, service_role, environment_context, workload_context) values
('4694a55f-58b3-4f17-92a5-796d6f5ffd0d', 'Network_Collection_SVC_0106', 'afa5911f-87a7-4e99-a6dc-8e6cd7b86573', '1.0','tbd','2018-06-01 16:06:52', '266eaf78-af74-45cd-83ec-9c477f189dc1', 'INFRASTRUCTURE', '', 'General_Revenue-Bearing', 'Production');
insert into collection_resource(MODEL_UUID,MODEL_NAME,MODEL_INVARIANT_UUID,MODEL_VERSION,TOSCA_NODE_TYPE,DESCRIPTION,CREATION_TIMESTAMP) values
('e083bdcd-449d-433d-9699-1bbc2d363f06','Network_Collection_Resource_1806','38273494-f61a-4645-bfe2-d971994bc3e5','1.0','org.openecomp.resource.cr.NetworkCollectionResource1806','tbd','2018-06-20 12:53:34');
insert into collection_resource_customization(MODEL_CUSTOMIZATION_UUID,MODEL_INSTANCE_NAME,role,object_type,function,collection_resource_type,CREATION_TIMESTAMP,CR_MODEL_UUID) values
('a07a5826-3281-485c-8f40-6988011ef3f2','Network_Collection_Resource_1806','','NetworkCollection','','','2018-06-01 16:06:52','e083bdcd-449d-433d-9699-1bbc2d363f06'),
('a07a5826-3281-485c-8f40-6988011ef3f3','Network_Collection_Resource_1806','','NetworkCollection','','','2018-06-01 16:06:52','e083bdcd-449d-433d-9699-1bbc2d363f06');
insert into collection_resource_customization_to_service(SERVICE_MODEL_UUID,RESOURCE_MODEL_CUSTOMIZATION_UUID) values
('4694a55f-58b3-4f17-92a5-796d6f5ffd0d', 'a07a5826-3281-485c-8f40-6988011ef3f2');
insert into instance_group(MODEL_UUID, MODEL_NAME, MODEL_INVARIANT_UUID, MODEL_VERSION, TOSCA_NODE_TYPE, ROLE, OBJECT_TYPE, CREATION_TIMESTAMP, CR_MODEL_UUID, INSTANCE_GROUP_TYPE) values
("0c8692ef-b9c0-435d-a738-edf31e71f38b", "network_collection_resource_1806..NetworkCollection..0", "c675f5f6-c017-40cb-807a-cb806ad24c0f", "1", "org.openecomp.resource.cr.NetworkCollectionResource1806", "SUB_INTERFACE", "L3_NETWORK", "2018-05-25 14:33:28", "e083bdcd-449d-433d-9699-1bbc2d363f06", "L3_NETWORK");
insert into collection_resource_instance_group_customization(COLLECTION_RESOURCE_CUSTOMIZATION_MODEL_UUID,INSTANCE_GROUP_MODEL_UUID,FUNCTION,DESCRIPTION,SUBINTERFACE_NETWORK_QUANTITY,CREATION_TIMESTAMP) values
('a07a5826-3281-485c-8f40-6988011ef3f2','0c8692ef-b9c0-435d-a738-edf31e71f38b','ncfunction','ncdescription',5,'2018-06-01 16:06:52'),
('a07a5826-3281-485c-8f40-6988011ef3f3','0c8692ef-b9c0-435d-a738-edf31e71f38b','ncfunction','ncdescription',5,'2018-06-01 16:06:52');
insert into service(model_uuid, model_name, model_invariant_uuid, model_version, description, creation_timestamp, tosca_csar_artifact_uuid, service_type, service_role, environment_context, workload_context) values
('5df8b6de-2083-11e7-93ae-92361f002672', 'MSOTADevInfra_vSAMP10a_Service', '9647dfc4-2083-11e7-93ae-92361f002671', '2.0', 'MSO aLaCarte Vfmodule with addon', '2017-04-14 13:42:39', null, 'NA', 'NA', 'Luna', 'Oxygen');

insert into heat_template(artifact_uuid, name, version, description, body, timeout_minutes, artifact_checksum, creation_timestamp) values
('ff874603-4222-11e7-9252-005056850d2e', 'module_mns_zrdm3frwl01exn_01_rgvm_1.yml', '1', 'created from csar', 'heat_template_version: 2013-05-23 description: heat template that creates TEST VNF parameters: TEST_server_name: type: string label: TEST server name description: TEST server name TEST_image_name: type: string label: image name description: TEST image name TEST_flavor_name: type: string label: TEST flavor name description: flavor name of TEST instance TEST_Role_net_name: type: string label: TEST network name description: TEST network name TEST_vnf_id: type: string label: TEST VNF Id description: TEST VNF Id resources:TEST: type: OS::Nova::Server properties: name: { get_param: TEST_server_name } image: { get_param: TEST_image_name } flavor: { get_param: TEST_flavor_name } networks: - port: { get_resource: TEST_port_0} metadata: vnf_id: {get_param: TEST_vnf_id} TEST_port_0: type: OS::Neutron::Port properties: network: { get_param: TEST_Role_net_name }', '60', 'MANUAL RECORD', '2017-01-21 23:26:56'), 
('ff87482f-4222-11e7-9252-005056850d2e', 'module_mns_zrdm3frwl01exn_01_rgvm_1.yml', '1', 'created from csar', 'heat_template_version: 2013-05-23 description: heat template that creates TEST VNF parameters: TEST_server_name: type: string label: TEST server name description: TEST server name TEST_image_name: type: string label: image name description: TEST image name TEST_flavor_name: type: string label: TEST flavor name description: flavor name of TEST instance TEST_Role_net_name: type: string label: TEST network name description: TEST network name TEST_vnf_id: type: string label: TEST VNF Id description: TEST VNF Id resources:TEST: type: OS::Nova::Server properties: name: { get_param: TEST_server_name } image: { get_param: TEST_image_name } flavor: { get_param: TEST_flavor_name } networks: - port: { get_resource: TEST_port_0} metadata: vnf_id: {get_param: TEST_vnf_id} TEST_port_0: type: OS::Neutron::Port properties: network: { get_param: TEST_Role_net_name }', '60', 'MANUAL RECORD', '2017-01-21 23:26:56'), 
('aa874603-4222-11e7-9252-005056850d2e', 'module_mns_zrdm3frwl01exn_01_rgvm_1.yml', '1', 'created from csar', 'heat_template_version: 2013-05-23 description: heat template that creates TEST VNF parameters: TEST_server_name: type: string label: TEST server name description: TEST server name TEST_image_name: type: string label: image name description: TEST image name TEST_flavor_name: type: string label: TEST flavor name description: flavor name of TEST instance TEST_Role_net_name: type: string label: TEST network name description: TEST network name TEST_vnf_id: type: string label: TEST VNF Id description: TEST VNF Id resources:TEST: type: OS::Nova::Server properties: name: { get_param: TEST_server_name } image: { get_param: TEST_image_name } flavor: { get_param: TEST_flavor_name } networks: - port: { get_resource: TEST_port_0} metadata: vnf_id: {get_param: TEST_vnf_id} TEST_port_0: type: OS::Neutron::Port properties: network: { get_param: TEST_Role_net_name }', '60', 'MANUAL RECORD', '2017-01-21 23:26:56'); 


insert into heat_template_params(heat_template_artifact_uuid, param_name, is_required, param_type, param_alias) values
('ff874603-4222-11e7-9252-005056850d2e', 'availability_zone_0', 1, 'string', ''),
('ff874603-4222-11e7-9252-005056850d2e', 'exn_direct_net_fqdn',1, 'string', ''),
('ff874603-4222-11e7-9252-005056850d2e', 'exn_hsl_net_fqdn', 1, 'string', '');

insert into heat_environment(artifact_uuid, name, version, description, body, artifact_checksum, creation_timestamp) values
('fefb1601-4222-11e7-9252-005056850d2e', 'module_nso.env', '2', 'Auto-generated HEAT Environment deployment artifact', 'parameters:\n  availability_zone_0: \"alln-zone-1\"\n  nso_flavor_name: \"citeis.1vCPUx2GB\"\n  nso_image_name: \"RHEL-6.8-BASE-20160912\"\n  nso_name_0: \"zrdm3vamp01nso001\"\n  nso_oam_ip_0: \"172.18.25.175\"\n  nso_oam_net_gw: \"172.18.25.1\"\n  nso_oam_net_mask: \"255.255.255.0\"\n  nso_sec_grp_id: \"36f48d82-f099-4437-bfbc-70d9e5d420d1\"\n  nso_srv_grp_id: \"e431c477-5bd1-476a-bfa9-e4ce16b8356b\"\n  oam_net_id: \"nso_oam\"\n  vf_module_id: \"145cd730797234b4a40aa99335abc143\"\n  vnf_id: \"730797234b4a40aa99335157b02871cd\"\n  vnf_name: \"Mobisupport\"\n', 'MWI2ODY0Yjc1NDJjNWU1NjdkMTAyMjVkNzFmZDU0MzA=', '2017-11-27 08:42:58'),
('fefb1751-4333-11e7-9252-005056850d2e', 'module_nso.env', '2', 'Auto-generated HEAT Environment deployment artifact', 'parameters:\n  availability_zone_0: \"alln-zone-1\"\n  nso_flavor_name: \"citeis.1vCPUx2GB\"\n  nso_image_name: \"RHEL-6.8-BASE-20160912\"\n  nso_name_0: \"zrdm3vamp01nso001\"\n  nso_oam_ip_0: \"172.18.25.175\"\n  nso_oam_net_gw: \"172.18.25.1\"\n  nso_oam_net_mask: \"255.255.255.0\"\n  nso_sec_grp_id: \"36f48d82-f099-4437-bfbc-70d9e5d420d1\"\n  nso_srv_grp_id: \"e431c477-5bd1-476a-bfa9-e4ce16b8356b\"\n  oam_net_id: \"nso_oam\"\n  vf_module_id: \"145cd730797234b4a40aa99335abc143\"\n  vnf_id: \"730797234b4a40aa99335157b02871cd\"\n  vnf_name: \"Mobisupport\"\n', 'MWI2ODY0Yjc1NDJjNWU1NjdkMTAyMjVkNzFmZDU0MzA=', '2017-11-27 08:42:58');

insert into vnf_resource(orchestration_mode, description, creation_timestamp, model_uuid, aic_version_min, aic_version_max, model_invariant_uuid, model_version, model_name, tosca_node_type, heat_template_artifact_uuid) values
('HEAT', '1607 vSAMP10a - inherent network', '2017-04-14 21:46:28', 'ff2ae348-214a-11e7-93ae-92361f002671', '', '', '2fff5b20-214b-11e7-93ae-92361f002671', '1.0', 'vSAMP10a', 'VF', 'ff874603-4222-11e7-9252-005056850d2e'),
('HEAT', '1607 vSAMP10a - inherent network', '2017-04-14 21:46:28', 'ff2ae348-214a-11e7-93ae-92361f002672', '', '', '2fff5b20-214b-11e7-93ae-92361f002671', '2.0', 'vSAMP10a', 'VF', 'ff874603-4222-11e7-9252-005056850d2e');


insert into vnf_resource_customization(model_customization_uuid, model_instance_name, min_instances, max_instances, availability_zone_max_count, nf_type, nf_role, nf_function, nf_naming_code, creation_timestamp, vnf_resource_model_uuid, multi_stage_design,service_model_uuid) values
('68dc9a92-214c-11e7-93ae-92361f002671', 'vSAMP10a 1', '0', '0', '0', 'vSAMP', 'vSAMP', 'vSAMP', 'vSAMP', '2017-05-26 15:08:24', 'ff2ae348-214a-11e7-93ae-92361f002671', null,'5df8b6de-2083-11e7-93ae-92361f002671'),
('68dc9a92-214c-11e7-93ae-92361f002672', 'vSAMP10a 2', '0', '0', '0', 'vSAMP', 'vSAMP', 'vSAMP', 'vSAMP', '2017-05-26 15:08:24', 'ff2ae348-214a-11e7-93ae-92361f002672', null,'5df8b6de-2083-11e7-93ae-92361f002672');



insert into vf_module(model_uuid, model_invariant_uuid, model_version, model_name, description, is_base, heat_template_artifact_uuid, vol_heat_template_artifact_uuid, creation_timestamp, vnf_resource_model_uuid) values
('20c4431c-246d-11e7-93ae-92361f002671', '78ca26d0-246d-11e7-93ae-92361f002671', '2', 'vSAMP10aDEV::base::module-0', 'vSAMP10a DEV Base', '1', 'ff874603-4222-11e7-9252-005056850d2e', null, '2016-09-14 18:19:56', 'ff2ae348-214a-11e7-93ae-92361f002671'),
('066de97e-253e-11e7-93ae-92361f002671', '64efd51a-2544-11e7-93ae-92361f002671', '2', 'vSAMP10aDEV::PCM::module-1', 'vSAMP10a DEV PCM', '0', 'ff87482f-4222-11e7-9252-005056850d2e', null, '2016-09-14 18:19:56', 'ff2ae348-214a-11e7-93ae-92361f002671'),
('20c4431c-246d-11e7-93ae-92361f002672', '78ca26d0-246d-11e7-93ae-92361f002671', '2', 'vSAMP10aDEV::base::module-0', 'vSAMP10a DEV Base', '1', 'ff874603-4222-11e7-9252-005056850d2e', null, '2016-09-14 18:19:56', 'ff2ae348-214a-11e7-93ae-92361f002671'),
('066de97e-253e-11e7-93ae-92361f002672', '64efd51a-2544-11e7-93ae-92361f002671', '2', 'vSAMP10aDEV::PCM::module-1', 'vSAMP10a DEV PCM', '0', 'ff87482f-4222-11e7-9252-005056850d2e', null, '2016-09-14 18:19:56', 'ff2ae348-214a-11e7-93ae-92361f002671');


insert into vf_module_customization(model_customization_uuid, label, initial_count, min_instances, max_instances, availability_zone_count, heat_environment_artifact_uuid, vol_environment_artifact_uuid, creation_timestamp, vf_module_model_uuid,VNF_RESOURCE_CUSTOMIZATION_ID) values
('cb82ffd8-252a-11e7-93ae-92361f002671', 'base', '1', '0', '0', '0', 'fefb1601-4222-11e7-9252-005056850d2e', null, '2017-05-26 15:08:23', '20c4431c-246d-11e7-93ae-92361f002671',1),
('b4ea86b4-253f-11e7-93ae-92361f002671', 'PCM', '0', '0', '0', '0', 'fefb1751-4333-11e7-9252-005056850d2e', null, '2017-05-26 15:08:23', '066de97e-253e-11e7-93ae-92361f002671',1),
('cb82ffd8-252a-11e7-93ae-92361f002672', 'base', '1', '0', '0', '0', 'fefb1601-4222-11e7-9252-005056850d2e', null, '2017-05-26 15:08:23', '20c4431c-246d-11e7-93ae-92361f002672',2),
('b4ea86b4-253f-11e7-93ae-92361f002672', 'PCM', '0', '0', '0', '0', 'fefb1751-4333-11e7-9252-005056850d2e', null, '2017-05-26 15:08:23', '066de97e-253e-11e7-93ae-92361f002672',2);


insert into allotted_resource(model_uuid, model_invariant_uuid, model_version, model_name, tosca_node_type, subcategory, description, creation_timestamp) values
('f6b7d4c6-e8a4-46e2-81bc-31cad5072842', 'b7a1b78e-6b6b-4b36-9698-8c9530da14af', '1.0', 'Tunnel_Xconn', '', '', '', '2017-05-26 15:08:24'); 

insert into allotted_resource_customization(model_customization_uuid, model_instance_name, providing_service_model_invariant_uuid, target_network_role, nf_type, nf_role, nf_function, nf_naming_code, min_instances, max_instances, ar_model_uuid, creation_timestamp) values
('367a8ba9-057a-4506-b106-fbae818597c6', 'Sec_Tunnel_Xconn 11', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'f6b7d4c6-e8a4-46e2-81bc-31cad5072842', TIMESTAMP '2017-01-20 16:14:20.0');


insert into temp_network_heat_template_lookup(network_resource_model_name, heat_template_artifact_uuid, aic_version_min, aic_version_max) values
('CONTRAIL30_GNDIRECT', 'ff874603-4222-11e7-9252-005056850d2e', '3', '3');

insert into network_resource(model_uuid, model_name, model_invariant_uuid, description, heat_template_artifact_uuid, neutron_network_type, model_version, tosca_node_type, aic_version_min, aic_version_max, orchestration_mode, creation_timestamp) values
('10b36f65-f4e6-4be6-ae49-9596dc1c47fc', 'CONTRAIL30_GNDIRECT', 'ce4ff476-9641-4e60-b4d5-b4abbec1271d', 'Contrail 30 GNDIRECT NW', 'aa874603-4222-11e7-9252-005056850d2e', 'BASIC', '1.0', '', '3.0', '', 'HEAT', '2017-01-17 20:35:05');

insert into network_resource_customization(model_customization_uuid, model_instance_name, network_technology, network_type, network_role, network_scope, creation_timestamp, network_resource_model_uuid) values
('3bdbb104-476c-483e-9f8b-c095b3d308ac', 'CONTRAIL30_GNDIRECT 9', '', '', '', '', '2017-04-19 14:28:32', '10b36f65-f4e6-4be6-ae49-9596dc1c47fc');
insert into collection_network_resource_customization(model_customization_uuid, model_instance_name, network_technology, network_type, network_role, network_scope, creation_timestamp, network_resource_model_uuid, instance_group_model_uuid, crc_model_customization_uuid) values
('1a61be4b-3378-4c9a-91c8-c919519b2d01', 'CONTRAIL30_GNDIRECT 9', '', '', '', '', '2017-04-19 14:28:32', '10b36f65-f4e6-4be6-ae49-9596dc1c47fc', '0c8692ef-b9c0-435d-a738-edf31e71f38b', 'a07a5826-3281-485c-8f40-6988011ef3f2');


insert into vf_module_to_heat_files(vf_module_model_uuid, heat_files_artifact_uuid) values
('20c4431c-246d-11e7-93ae-92361f002671', '00535bdd-0878-4478-b95a-c575c742bfb0'),
('066de97e-253e-11e7-93ae-92361f002671', '00535bdd-0878-4478-b95a-c575c742bfb0');


insert into network_resource_customization_to_service(service_model_uuid, resource_model_customization_uuid) values
('5df8b6de-2083-11e7-93ae-92361f002671', '3bdbb104-476c-483e-9f8b-c095b3d308ac'),
('5df8b6de-2083-11e7-93ae-92361f002672', '3bdbb104-476c-483e-9f8b-c095b3d308ac');




insert into allotted_resource_customization_to_service(service_model_uuid, resource_model_customization_uuid) values
('5df8b6de-2083-11e7-93ae-92361f002671', '367a8ba9-057a-4506-b106-fbae818597c6' ),
('5df8b6de-2083-11e7-93ae-92361f002672', '367a8ba9-057a-4506-b106-fbae818597c6');

insert into vnf_components(vnf_id, component_type, heat_template_id, heat_environment_id, creation_timestamp) values
('13961', 'VOLUME', '13843', '13961', '2016-05-19 20:22:02');   


INSERT INTO `cloudify_managers` (`ID`, `CLOUDIFY_URL`, `USERNAME`, `PASSWORD`, `VERSION`, `LAST_UPDATED_BY`, `CREATION_TIMESTAMP`, `UPDATE_TIMESTAMP`) VALUES ('mtn13', 'http://localhost:28090/v2.0', 'm93945', '93937EA01B94A10A49279D4572B48369', NULL, 'MSO_USER', '2018-07-17 14:05:08', '2018-07-17 14:05:08');

INSERT INTO `identity_services` (`ID`, `IDENTITY_URL`, `MSO_ID`, `MSO_PASS`, `PROJECT_DOMAIN_NAME`, `USER_DOMAIN_NAME`, `ADMIN_TENANT`, `MEMBER_ROLE`, `TENANT_METADATA`, `IDENTITY_SERVER_TYPE`, `IDENTITY_AUTHENTICATION_TYPE`, `LAST_UPDATED_BY`, `CREATION_TIMESTAMP`, `UPDATE_TIMESTAMP`) VALUES ('MTN13', 'http://localhost:28090/v2.0', 'm93945', '93937EA01B94A10A49279D4572B48369', NULL, NULL, 'admin', 'admin', 1, 'KEYSTONE', 'USERNAME_PASSWORD', 'MSO_USER', '2018-07-17 14:02:33', '2018-07-17 14:02:33');

INSERT INTO `cloud_sites` (`ID`, `REGION_ID`, `IDENTITY_SERVICE_ID`, `CLOUD_VERSION`, `CLLI`, `CLOUDIFY_ID`, `PLATFORM`, `ORCHESTRATOR`, `LAST_UPDATED_BY`, `CREATION_TIMESTAMP`, `UPDATE_TIMESTAMP`) VALUES ('mtn13', 'mtn13', 'MTN13', '2.5', 'MDT13', 'mtn13', NULL, 'orchestrator', 'MSO_USER', '2018-07-17 14:06:28', '2018-07-17 14:06:28');

INSERT INTO service_recipe(ID, ACTION, VERSION_STR, DESCRIPTION, ORCHESTRATION_URI, SERVICE_PARAM_XSD, RECIPE_TIMEOUT, SERVICE_TIMEOUT_INTERIM, CREATION_TIMESTAMP, SERVICE_MODEL_UUID) VALUES
('8', 'createInstance', '8', 'MSOTADevInfra aLaCarte', '/mso/async/services/CreateGenericALaCarteServiceInstance', null, '180', '0', '2017-04-14 19:18:20', '4694a55f-58b3-4f17-92a5-796d6f5ffd0d');

INSERT INTO vnf_recipe (nf_role, ACTION, VERSION_STR, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT)
VALUES
('GR-API-DEFAULT', 'createInstance', '1', 'Gr api recipe to create vnf', '/mso/async/services/WorkflowActionBB', 180);

INSERT INTO vnf_components_recipe (VNF_COMPONENT_TYPE, ACTION, VERSION, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT, VF_MODULE_MODEL_UUID)
VALUES
('volumeGroup', 'createInstance', '1', 'Gr api recipe to create volume-group', '/mso/async/services/WorkflowActionBB', 180, '20c4431c-246d-11e7-93ae-92361f002671');

insert into homing_instances (service_instance_id, cloud_owner, cloud_region_id, oof_directives) values
('5df8b6de-2083-11e7-93ae-92361f232671', 'CloudOwner', 'CloudRegionId', '{"directives": [{"directives": [{"attributes": [{"attribute_value": "onap.hpa.flavor32","attribute_name": "firewall_flavor_name"}],"type": "flavor_directives"}],"type": "vnfc","id": "vfw"},{"directives": [{"attributes": [{"attribute_value": "onap.hpa.flavor33","attribute_name": "packetgen_flavor_name"}],"type": "flavor_directives"}],"type": "vnfc","id": "vgenerator"},{"directives": [{"attributes": [{"attribute_value": "onap.hpa.flavor32","attribute_name": "sink_flavor_name"}],"type": "flavor_directives"}],"type": "vnfc","id": "vsink"}]}'),
('5df8b6de-2083-11e7-93ae-92361f562672', 'CloudOwner', 'CloudRegionId', '{"directives": [{"directives": [{"attributes": [{"attribute_value": "onap.hpa.flavor32","attribute_name": "firewall_flavor_name"}],"type": "flavor_directives"}],"type": "vnfc","id": "vfw"},{"directives": [{"attributes": [{"attribute_value": "onap.hpa.flavor33","attribute_name": "packetgen_flavor_name"}],"type": "flavor_directives"}],"type": "vnfc","id": "vgenerator"},{"directives": [{"attributes": [{"attribute_value": "onap.hpa.flavor32","attribute_name": "sink_flavor_name"}],"type": "flavor_directives"}],"type": "vnfc","id": "vsink"}]}');

-- for query resource receipe
INSERT INTO allotted_resource
(MODEL_UUID, MODEL_INVARIANT_UUID, MODEL_VERSION, MODEL_NAME, TOSCA_NODE_TYPE, SUBCATEGORY, DESCRIPTION, CREATION_TIMESTAMP)
VALUES('25e2d69b-3b22-47b8-b4c9-7b14fd4a80df', '8f5fe343-9a3a-4d31-a829-49b27bbfc1c4', '2.0', 'sotnvpnattachmentvF', 'org.openecomp.resource.vf.Sdwanvpnattachmentvf', 'Allotted Resource', 'sdwanvpnattachmentVF', '2019-01-24 09:59:16.000');

INSERT INTO ar_recipe(ID, MODEL_NAME, ACTION, VERSION_STR, SERVICE_TYPE, DESCRIPTION, ORCHESTRATION_URI, AR_PARAM_XSD, RECIPE_TIMEOUT, CREATION_TIMESTAMP)
VALUES(1, 'sotnvpnattachmentvF', 'createInstance', '2.0', 'VF', 'sotnvpnattachmentvF', '/mso/async/services/CreateSDNCNetworkResource', '', 180, '2019-01-24 09:59:16.000');
INSERT INTO northbound_request_ref_lookup(MACRO_ACTION, ACTION, REQUEST_SCOPE, IS_ALACARTE, MIN_API_VERSION, MAX_API_VERSION, CLOUD_OWNER, SERVICE_TYPE) VALUES
('Service-Create', 'createInstance', 'Service', true, '7','7', 'my-custom-cloud-owner','*');


INSERT INTO orchestration_flow_reference(COMPOSITE_ACTION, SEQ_NO, FLOW_NAME, FLOW_VERSION, NB_REQ_REF_LOOKUP_ID) VALUES
('Service-Create', '1', 'AssignServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Create' and CLOUD_OWNER = 'my-custom-cloud-owner'));

INSERT INTO northbound_request_ref_lookup(MACRO_ACTION, ACTION, REQUEST_SCOPE, IS_ALACARTE, MIN_API_VERSION, MAX_API_VERSION, CLOUD_OWNER, SERVICE_TYPE) VALUES
('Service-Create', 'createInstance', 'Service', true, '7','7', 'my-custom-cloud-owner','TRANSPORT');

INSERT INTO orchestration_flow_reference(COMPOSITE_ACTION, SEQ_NO, FLOW_NAME, FLOW_VERSION, NB_REQ_REF_LOOKUP_ID) VALUES
('Service-Create', '1', 'AssignServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Create' and CLOUD_OWNER = 'my-custom-cloud-owner' and SERVICE_TYPE = 'TRANSPORT'));

INSERT INTO `vnfc_customization`
            (`model_customization_uuid`,
             `model_instance_name`,
             `model_uuid`,
             `model_invariant_uuid`,
             `model_version`,
             `model_name`,
             `tosca_node_type`,
             `description`,
             `creation_timestamp`)
VALUES      ( '9bcce658-9b37-11e8-98d0-529269fb1459',
              'testModelInstanceName',
              'b25735fe-9b37-11e8-98d0-529269fb1459',
              'ba7e6ef0-9b37-11e8-98d0-529269fb1459',
              'testModelVersion',
              'testModelName',
              'toscaNodeType',
              'testVnfcCustomizationDescription',
              '2018-07-17 14:05:08');

INSERT INTO `rainy_day_handler_macro` (`FLOW_NAME`,`SERVICE_TYPE`,`VNF_TYPE`,`ERROR_CODE`,`WORK_STEP`,`POLICY`,`SECONDARY_POLICY`,`REG_EX_ERROR_MESSAGE`, `SERVICE_ROLE`) 
VALUES ('AssignServiceInstanceBB','*','*','*','*','Rollback','Rollback','The Flavor ID.*could not be found.','*');

INSERT INTO `cvnfc_customization`
            (`id`,
             `model_customization_uuid`,
             `model_instance_name`,
             `model_uuid`,
             `model_invariant_uuid`,
             `model_version`,
             `model_name`,
             `tosca_node_type`,
             `description`,
             `nfc_function`,
             `nfc_naming_code`,
             `creation_timestamp`,
             `vnfc_cust_model_customization_uuid`,VF_MODULE_CUSTOMIZATION_ID)
VALUES      ( '1',
              'dadc2c8c-2bab-11e9-b210-d663bd873d93',
              'testModelInstanceName',
              'b25735fe-9b37-11e8-98d0-529269fb1459',
              'ba7e6ef0-9b37-11e8-98d0-529269fb1459',
              'testModelVersion',
              'testModelName',
              'testToscaNodeType',
              'testCvnfcCustomzationDescription',
              'testNfcFunction',
              'testNfcNamingCode',
              '2018-07-17 14:05:08',
              '9bcce658-9b37-11e8-98d0-529269fb1459',1),
              ( '2',
              'dadc2c8c-2bab-11e9-b210-d663bd873d95',
              'testModelInstanceName',
              'b25735fe-9b37-11e8-98d0-529269fb1459',
              'ba7e6ef0-9b37-11e8-98d0-529269fb1459',
              'testModelVersion',
              'testModelName',
              'testToscaNodeType',
              'testCvnfcCustomzationDescription',
              'testNfcFunction',
              'testNfcNamingCode',
              '2018-07-17 14:05:08',
              '9bcce658-9b37-11e8-98d0-529269fb1459',1);
              
              
INSERT IGNORE INTO `configuration` (`MODEL_UUID`, `MODEL_INVARIANT_UUID`, `MODEL_VERSION`, `MODEL_NAME`, `TOSCA_NODE_TYPE`, `DESCRIPTION`, `CREATION_TIMESTAMP`) VALUES
	('d2195b0e-307a-4d30-b82f-9c82001d965e', '2f0a4b7a-dfdb-4f82-a2ab-b65d1ddd5e8e', '13.0', 'Fabric Configuration', 'org.openecomp.nodes.FabricConfiguration', 'A fabric Configuration object', '2019-06-04 20:12:20');
	
INSERT IGNORE INTO `cvnfc_configuration_customization` (`MODEL_CUSTOMIZATION_UUID`, `MODEL_INSTANCE_NAME`, `CONFIGURATION_TYPE`, `CONFIGURATION_ROLE`, `CONFIGURATION_FUNCTION`, `POLICY_NAME`, `CREATION_TIMESTAMP`, `CONFIGURATION_MODEL_UUID`, `CVNFC_CUSTOMIZATION_ID`) VALUES
	('386c9aa7-9318-48ee-a6d1-1bf0f85de385', 'Fabric Configuration 0', '5G', 'Fabric Config', 'Network Cloud', 'Config_MS_fabric_configuration_FRWL.1.xml', '2019-06-04 20:12:20', 'd2195b0e-307a-4d30-b82f-9c82001d965e', '2');


	
insert into service(model_uuid, model_name, model_invariant_uuid, model_version, description, creation_timestamp, tosca_csar_artifact_uuid, service_type, service_role, environment_context, workload_context) values
('5df8b6de-2083-11e7-93ae-92361f002676', 'PNF_routing_service', '9647dfc4-2083-11e7-93ae-92361f002676', '1.0', 'PNF service', '2019-03-08 12:00:29', null, 'NA', 'NA', 'Luna', 'Oxygen');

insert into pnf_resource(orchestration_mode, description, creation_timestamp, model_uuid, model_invariant_uuid, model_version, model_name, tosca_node_type) values
('', 'PNF routing', '2019-03-08 12:00:28', 'ff2ae348-214a-11e7-93ae-92361f002680', '2fff5b20-214b-11e7-93ae-92361f002680', '1.0', 'PNF resource', null);

insert into pnf_resource_customization(model_customization_uuid, model_instance_name, nf_type, nf_role, nf_function, nf_naming_code, creation_timestamp, pnf_resource_model_uuid, multi_stage_design, cds_blueprint_name, cds_blueprint_version) values
('68dc9a92-214c-11e7-93ae-92361f002680', 'PNF routing', 'routing', 'routing', 'routing', 'routing', '2019-03-08 12:00:29', 'ff2ae348-214a-11e7-93ae-92361f002680', null, "test_configuration_restconf", "1.0.0");

insert into pnf_resource_customization_to_service(service_model_uuid, resource_model_customization_uuid) values
('5df8b6de-2083-11e7-93ae-92361f002676', '68dc9a92-214c-11e7-93ae-92361f002680');

insert into workflow(artifact_uuid, artifact_name, name, operation_name, version, description, body, resource_target, source) values
('5b0c4322-643d-4c9f-b184-4516049e99b1', 'testingWorkflow.bpmn', 'testingWorkflow', 'create', 1, 'Test Workflow', null, 'vnf', 'sdc');

insert into vnf_resource_to_workflow(vnf_resource_model_uuid, workflow_id) values
('ff2ae348-214a-11e7-93ae-92361f002671', '1');

insert into processing_flags (flag,value,endpoint,description) values
('TESTFLAG', 'NO', 'TESTENDPOINT', 'TEST FLAG');

