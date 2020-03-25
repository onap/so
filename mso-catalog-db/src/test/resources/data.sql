
insert into heat_files(artifact_uuid, name, version, description, body, artifact_checksum, creation_timestamp) values
('00535bdd-0878-4478-b95a-c575c742bfb0', 'nimbus-ethernet-gw', '1', 'created from csar', 'DEVICE=$dev\nBOOTPROTO=none\nNM_CONTROLLED=no\nIPADDR=$ip\nNETMASK=$netmask\nGATEWAY=$gateway\n', 'MANUAL RECORD', '2017-01-21 23:56:43');


insert into tosca_csar(artifact_uuid, name, version, description, artifact_checksum, url, creation_timestamp) values
('0513f839-459d-46b6-aa3d-2edfef89a079', 'service-Ciservicee3756aea561a-csar.csar', '1', 'TOSCA definition package of the asset', 'YTk1MmY2MGVlNzVhYTU4YjgzYjliMjNjMmM3NzU1NDc=', '/sdc/v1/catalog/services/Ciservicee3756aea561a/1.0/artifacts/service-Ciservicee3756aea561a-csar.csar', '2017-11-27 11:38:27');


insert into service(model_uuid, model_name, model_invariant_uuid, model_version, description, creation_timestamp, tosca_csar_artifact_uuid, service_type, service_role, environment_context, workload_context) values
('5df8b6de-2083-11e7-93ae-92361f002671', 'MSOTADevInfra_vSAMP10a_Service', '9647dfc4-2083-11e7-93ae-92361f002671', '1.0', 'MSO aLaCarte Vfmodule with addon', '2017-04-14 13:42:39', '0513f839-459d-46b6-aa3d-2edfef89a079', 'NA', 'NA', 'Luna', 'Oxygen');

insert into service(model_uuid, model_name, model_invariant_uuid, model_version, description, creation_timestamp, tosca_csar_artifact_uuid, service_type, service_role, environment_context, workload_context) values
('5df8b6de-2083-11e7-93ae-92361f002672', 'MSOTADevInfra_vSAMP10a_Service', '9647dfc4-2083-11e7-93ae-92361f002671', '2.0', 'MSO aLaCarte Vfmodule with addon', '2017-04-14 13:42:39', null, 'NA', 'NA', 'Luna', 'Oxygen');

insert into service(model_uuid, model_name, model_invariant_uuid, model_version, description, creation_timestamp, tosca_csar_artifact_uuid, service_type, service_role, environment_context, workload_context) values
('5df8b6de-2083-11e7-93ae-92361f002673', 'MSOTADevInfra_vSAMP10a_Service', '9647dfc4-2083-11e7-93ae-92361f002671', '2.1', 'MSO aLaCarte Vfmodule with addon', '2017-04-14 13:42:39', null, 'NA', 'NA', 'Luna', 'Oxygen');


insert into service(model_uuid, model_name, model_invariant_uuid, model_version, description, creation_timestamp, tosca_csar_artifact_uuid, service_type, service_role, environment_context, workload_context) values
('5df8b6de-2083-11e7-93ae-92361f002674', 'MSOTADevInfra_vSAMP10a_Service', '9647dfc4-2083-11e7-93ae-92361f002671', '2.1.1', 'MSO aLaCarte Vfmodule with addon', '2017-04-14 13:42:39', null, 'NA', 'NA', 'Luna', 'Oxygen');
insert into service(model_uuid, model_name, model_invariant_uuid, model_version, description, creation_timestamp, tosca_csar_artifact_uuid, service_type, service_role, environment_context, workload_context) values
('5df8b6de-2083-11e7-93ae-92361f002675', 'MSOTADevInfra_vSAMP10a_Service', '9647dfc4-2083-11e7-93ae-92361f002671', '2.1.2', 'MSO aLaCarte Vfmodule with addon', '2017-04-14 13:42:39', null, 'NA', 'NA', 'Luna', 'Oxygen');


insert into service_recipe(id, action, version_str, description, orchestration_uri, service_param_xsd, recipe_timeout, service_timeout_interim, creation_timestamp, service_model_uuid) values
('1', 'createInstance', '1', 'MSOTADevInfra aLaCarte', '/mso/async/services/CreateGenericALaCarteServiceInstance', null, '180', '0', '2017-04-14 19:18:20', '5df8b6de-2083-11e7-93ae-92361f002671');

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
('HEAT', '1607 vSAMP10a - inherent network', '2017-04-14 21:46:28', 'ff2ae348-214a-11e7-93ae-92361f002671', '', '', '2fff5b20-214b-11e7-93ae-92361f002671', '1.0', 'vSAMP10a', 'VF', null);

insert into vnf_resource_customization(model_customization_uuid, model_instance_name, min_instances, max_instances, availability_zone_max_count, nf_type, nf_role, nf_function, nf_naming_code, creation_timestamp, vnf_resource_model_uuid, multi_stage_design,SERVICE_MODEL_UUID) values
('68dc9a92-214c-11e7-93ae-92361f002671', 'vSAMP10a 1', '0', '0', '0', 'vSAMP', 'vSAMP', 'vSAMP', 'vSAMP', '2017-05-26 15:08:24', 'ff2ae348-214a-11e7-93ae-92361f002671', null,'5df8b6de-2083-11e7-93ae-92361f002671');


insert into vf_module(model_uuid, model_invariant_uuid, model_version, model_name, description, is_base, heat_template_artifact_uuid, vol_heat_template_artifact_uuid, creation_timestamp, vnf_resource_model_uuid) values

('20c4431c-246d-11e7-93ae-92361f002671', '78ca26d0-246d-11e7-93ae-92361f002671', '2', 'vSAMP10aDEV::base::module-0', 'vSAMP10a DEV Base', '1', 'ff874603-4222-11e7-9252-005056850d2e', null, '2016-09-14 18:19:56', 'ff2ae348-214a-11e7-93ae-92361f002671'),
('066de97e-253e-11e7-93ae-92361f002671', '64efd51a-2544-11e7-93ae-92361f002671', '2', 'vSAMP10aDEV::PCM::module-1', 'vSAMP10a DEV PCM', '0', 'ff87482f-4222-11e7-9252-005056850d2e', null, '2016-09-14 18:19:56', 'ff2ae348-214a-11e7-93ae-92361f002671');


insert into vf_module_customization(model_customization_uuid, label, initial_count, min_instances, max_instances, availability_zone_count, heat_environment_artifact_uuid, vol_environment_artifact_uuid, creation_timestamp, vf_module_model_uuid) values
('cb82ffd8-252a-11e7-93ae-92361f002671', 'base', '1', '0', '0', '0', 'fefb1601-4222-11e7-9252-005056850d2e', null, '2017-05-26 15:08:23', '20c4431c-246d-11e7-93ae-92361f002671'),
('b4ea86b4-253f-11e7-93ae-92361f002671', 'PCM', '0', '0', '0', '0', 'fefb1751-4333-11e7-9252-005056850d2e', null, '2017-05-26 15:08:23', '066de97e-253e-11e7-93ae-92361f002671');

insert into allotted_resource(model_uuid, model_invariant_uuid, model_version, model_name, tosca_node_type, subcategory, description, creation_timestamp) values
('f6b7d4c6-e8a4-46e2-81bc-31cad5072842', 'b7a1b78e-6b6b-4b36-9698-8c9530da14af', '1.0', 'Tunnel_Xconn', '', '', '', '2017-05-26 15:08:24');

insert into allotted_resource_customization(model_customization_uuid, model_instance_name, providing_service_model_invariant_uuid, target_network_role, nf_type, nf_role, nf_function, nf_naming_code, min_instances, max_instances, ar_model_uuid, creation_timestamp) values
('367a8ba9-057a-4506-b106-fbae818597c6', 'Sec_Tunnel_Xconn 11', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'f6b7d4c6-e8a4-46e2-81bc-31cad5072842', TIMESTAMP '2017-01-20 16:14:20.0');


insert into temp_network_heat_template_lookup(network_resource_model_name, heat_template_artifact_uuid, aic_version_min, aic_version_max) values
('CONTRAIL30_GNDIRECT', 'ff874603-4222-11e7-9252-005056850d2e', '3', '3');

insert into network_resource(model_uuid, model_name, model_invariant_uuid, description, heat_template_artifact_uuid, neutron_network_type, model_version, tosca_node_type, aic_version_min, aic_version_max, orchestration_mode, creation_timestamp) values
('10b36f65-f4e6-4be6-ae49-9596dc1c47fc', 'CONTRAIL30_GNDIRECT', 'ce4ff476-9641-4e60-b4d5-b4abbec1271d', 'Contrail 30 GNDIRECT NW', 'aa874603-4222-11e7-9252-005056850d2e', 'BASIC', '1.0', '', '3.0', '', 'HEAT', '2017-01-17 20:35:05');
insert into network_resource(model_uuid, model_name, model_invariant_uuid, description, heat_template_artifact_uuid, neutron_network_type, model_version, tosca_node_type, aic_version_min, aic_version_max, orchestration_mode, creation_timestamp) values
('10b36f65-f4e6-4be6-ae49-9596dc1c47fy', 'CONTRAIL30_GNDIRECT', 'ce4ff476-9641-4e60-b4d5-b4abbec1271d', 'Contrail 30 GNDIRECT NW', 'aa874603-4222-11e7-9252-005056850d2e', 'BASIC', '2.0', '', '3.0', '', 'HEAT', '2017-01-17 20:35:05');

insert into network_resource(model_uuid, model_name, model_invariant_uuid, description, heat_template_artifact_uuid, neutron_network_type, model_version, tosca_node_type, aic_version_min, aic_version_max, orchestration_mode, creation_timestamp) values
('10b36f65-f4e6-4be6-ae49-9596dc1c47fx', 'CONTRAIL30_GNDIRECT', 'ce4ff476-9641-4e60-b4d5-b4abbec1271d', 'Contrail 30 GNDIRECT NW', 'aa874603-4222-11e7-9252-005056850d2e', 'BASIC', '3.0', '', '3.0', '', 'HEAT', '2017-01-17 20:35:05');

insert into network_resource(model_uuid, model_name, model_invariant_uuid, description, heat_template_artifact_uuid, neutron_network_type, model_version, tosca_node_type, aic_version_min, aic_version_max, orchestration_mode, creation_timestamp) values
('10b36f65-f4e6-4be6-ae49-9596dc1c47fz', 'CONTRAIL30_GNDIRECT', 'ce4ff476-9641-4e60-b4d5-b4abbec1271d', 'Contrail 30 GNDIRECT NW', 'aa874603-4222-11e7-9252-005056850d2e', 'BASIC', '3.1', '', '3.0', '', 'HEAT', '2017-01-17 20:35:05');


insert into network_resource_customization(model_customization_uuid, model_instance_name, network_technology, network_type, network_role, network_scope, creation_timestamp, network_resource_model_uuid) values
('3bdbb104-476c-483e-9f8b-c095b3d308ac', 'CONTRAIL30_GNDIRECT 9', '', '', '', '', '2017-04-19 14:28:32', '10b36f65-f4e6-4be6-ae49-9596dc1c47fc');

insert into instance_group(model_uuid, model_name, model_invariant_uuid, model_version, tosca_node_type, role, object_type, cr_model_uuid, instance_group_type) values
('21e43a7c-d823-4f5b-a427-5235f63035ff', 'dror_cr_network_resource_1806..NetworkCollection..0', '81c94263-c01e-4046-b0c7-51878d658eab', '1', 'org.openecomp.groups.NetworkCollection', 'SUB_INTERFACE', 'L3_NETWORK', '5e3fca45-e2d8-4987-bef1-016d9bda1a8c', 'L3_NETWORK');

insert into collection_resource(model_uuid, model_name, model_invariant_uuid, model_version, tosca_node_type, description) values
('5e3fca45-e2d8-4987-bef1-016d9bda1a8c', 'Dror_CR_Network_Resource_1806', 'fe243154-ac18-405f-94c2-ef629d26b8bb', '2.0', 'org.openecomp.resource.cr.DrorCrNetworkResource1806', 'Creation date: 07/25/18');

insert into collection_resource_customization(model_customization_uuid, model_instance_name, role, object_type, function, collection_resource_type, cr_model_uuid) values
('c51096a4-6081-41f4-a540-3ed015a8064a', 'Dror_CR_Network_Resource_1806', 'Dror2', 'NetworkCollection', 'Dror1', 'Dror3', '5e3fca45-e2d8-4987-bef1-016d9bda1a8c');

insert into collection_network_resource_customization(model_customization_uuid, model_instance_name, network_technology, network_type, network_role, network_scope, network_resource_model_uuid, instance_group_model_uuid, crc_model_customization_uuid) values
('3bdbb104-ffff-483e-9f8b-c095b3d30844', 'ExtVL 0', 'CONTRAIL', 'L3-NETWORK', '', '', '10b36f65-f4e6-4be6-ae49-9596dc1c47fz', '21e43a7c-d823-4f5b-a427-5235f63035ff', 'c51096a4-6081-41f4-a540-3ed015a8064a'),
('3bdbb104-ffff-483e-9f8b-c095b3d3068c', 'ExtVL 01', 'CONTRAIL', 'L3-NETWORK', '', '', '10b36f65-f4e6-4be6-ae49-9596dc1c47fz', '21e43a7c-d823-4f5b-a427-5235f63035ff', 'c51096a4-6081-41f4-a540-3ed015a8064a');

insert into vnf_resource(orchestration_mode, description, creation_timestamp, model_uuid, aic_version_min, aic_version_max, model_invariant_uuid, model_version, model_name, tosca_node_type, heat_template_artifact_uuid) values
('HEAT', '1607 vSAMP10a - inherent network', '2017-04-14 21:46:28', 'ff2ae348-214a-11e7-93ae-92361f002672', '', '', '2fff5b20-214b-11e7-93ae-92361f002671', '2.0', 'vSAMP10a', 'VF', null);


insert into vnf_resource_customization(id, model_customization_uuid, model_instance_name, min_instances, max_instances, availability_zone_max_count, nf_type, nf_role, nf_function, nf_naming_code, creation_timestamp, vnf_resource_model_uuid, multi_stage_design,SERVICE_MODEL_UUID) values
('1429', '68dc9a92-214c-11e7-93ae-92361f002672', 'vSAMP10a 2', '0', '0', '0', 'vSAMP', 'vSAMP', 'vSAMP', 'vSAMP', '2017-05-26 15:08:24', 'ff2ae348-214a-11e7-93ae-92361f002672', null,'5df8b6de-2083-11e7-93ae-92361f002672');

INSERT INTO vnfc_instance_group_customization (`ID`, `INSTANCE_GROUP_MODEL_UUID`, `FUNCTION`, `VNF_RESOURCE_CUSTOMIZATION_ID`) VALUES 
('1450', '21e43a7c-d823-4f5b-a427-5235f63035ff', 'FUNCTION', '1429');




insert into vf_module(model_uuid, model_invariant_uuid, model_version, model_name, description, is_base, heat_template_artifact_uuid, vol_heat_template_artifact_uuid, creation_timestamp, vnf_resource_model_uuid) values

('20c4431c-246d-11e7-93ae-92361f002672', '78ca26d0-246d-11e7-93ae-92361f002671', '2', 'vSAMP10aDEV::base::module-0', 'vSAMP10a DEV Base', '1', 'ff874603-4222-11e7-9252-005056850d2e', null, '2016-09-14 18:19:56', 'ff2ae348-214a-11e7-93ae-92361f002671'),
('066de97e-253e-11e7-93ae-92361f002672', '64efd51a-2544-11e7-93ae-92361f002671', '2', 'vSAMP10aDEV::PCM::module-1', 'vSAMP10a DEV PCM', '0', 'ff87482f-4222-11e7-9252-005056850d2e', null, '2016-09-14 18:19:56', 'ff2ae348-214a-11e7-93ae-92361f002671'),
('066de97e-253e-11e7-93ae-92361f002673', '64efd51a-2544-11e7-93ae-92361f002671', '3', 'vSAMP10aDEV::PCM::module-1', 'vSAMP10a DEV PCM', '0', 'ff87482f-4222-11e7-9252-005056850d2e', null, '2016-09-14 18:19:54', 'ff2ae348-214a-11e7-93ae-92361f002671'),
('066de97e-253e-11e7-93ae-92361f002674', '64efd51a-2544-11e7-93ae-92361f002671', '3.1', 'vSAMP10aDEV::PCM::module-1', 'vSAMP10a DEV PCM', '0', 'ff87482f-4222-11e7-9252-005056850d2e', null, '2016-09-14 18:19:56', 'ff2ae348-214a-11e7-93ae-92361f002671'),
('066de97e-253e-11e7-93ae-92361f002675', '64efd51a-2544-11e7-93ae-92361f002671', '3.1.1', 'vSAMP10aDEV::PCM::module-1', 'vSAMP10a DEV PCM', '0', 'ff87482f-4222-11e7-9252-005056850d2e', null, '2016-09-14 18:19:56', 'ff2ae348-214a-11e7-93ae-92361f002671');



insert into vf_module_customization(model_customization_uuid, label, initial_count, min_instances, max_instances, availability_zone_count, heat_environment_artifact_uuid, vol_environment_artifact_uuid, creation_timestamp, vf_module_model_uuid) values
('cb82ffd8-252a-11e7-93ae-92361f002672', 'base', '1', '0', '0', '0', 'fefb1601-4222-11e7-9252-005056850d2e', null, '2017-05-26 15:08:23', '20c4431c-246d-11e7-93ae-92361f002672'),
('b4ea86b4-253f-11e7-93ae-92361f002672', 'PCM', '0', '0', '0', '0', 'fefb1751-4333-11e7-9252-005056850d2e', null, '2017-05-26 15:08:23', '066de97e-253e-11e7-93ae-92361f002672');





insert into vf_module_to_heat_files(vf_module_model_uuid, heat_files_artifact_uuid) values
('20c4431c-246d-11e7-93ae-92361f002671', '00535bdd-0878-4478-b95a-c575c742bfb0'),
('066de97e-253e-11e7-93ae-92361f002671', '00535bdd-0878-4478-b95a-c575c742bfb0');


insert into network_resource_customization_to_service(service_model_uuid, resource_model_customization_uuid) values
('5df8b6de-2083-11e7-93ae-92361f002671', '3bdbb104-476c-483e-9f8b-c095b3d308ac'),
('5df8b6de-2083-11e7-93ae-92361f002672', '3bdbb104-476c-483e-9f8b-c095b3d308ac');


insert into allotted_resource_customization_to_service(service_model_uuid, resource_model_customization_uuid) values
('5df8b6de-2083-11e7-93ae-92361f002671', '367a8ba9-057a-4506-b106-fbae818597c6' ),
('5df8b6de-2083-11e7-93ae-92361f002672', '367a8ba9-057a-4506-b106-fbae818597c6');



insert into vnf_recipe(id, nf_role, action, service_type, version_str, description, orchestration_uri, vnf_param_xsd, recipe_timeout, creation_timestamp, vf_module_id) values
('61', '*', 'CREATE', '', '1', 'Recipe Match All for VNFs if no custom flow exists', '/mso/workflow/services/CreateGenericVNFV1', '', '180', '2016-06-03 10:14:10', ''),
('63', '*', 'DELETE', '', '1', 'Recipe Match All for VNFs if no custom flow exists', '/mso/async/services/deleteGenericVNFV1', '', '180', '2016-06-03 10:14:10', ''),
('65', '*', 'UPDATE', '', '1', 'Recipe Match All for VNFs if no custom flow exists', '/mso/workflow/services/updateGenericVNFV1', '', '180', '2016-06-03 10:14:10', ''),
('67', '*', 'CREATE_VF_MODULE', '', '1', 'Recipe Match All for VNFs if no custom flow exists', '/mso/async/services/CreateVfModule', '', '180', '2016-06-03 10:14:10', '*'),
('69', '*', 'DELETE_VF_MODULE', '', '1', 'Recipe Match All for VNFs if no custom flow exists', '/mso/async/services/DeleteVfModule', '', '180', '2016-06-03 10:14:10', '*'),
('71', '*', 'UPDATE_VF_MODULE', '', '1', 'Recipe Match All for VNFs if no custom flow exists', '/mso/async/services/UpdateVfModule', '', '180', '2016-06-03 10:14:10', '*'),
('77', 'VID_DEFAULT', 'createInstance', '', '1', 'VID_DEFAULT recipe to create VNF if no custom BPMN flow is found', '/mso/async/services/CreateVnfInfra', '', '180', '2016-09-14 19:18:20', ''),
('78', 'VID_DEFAULT', 'deleteInstance', '', '1', 'VID_DEFAULT recipe to delete VNF if no custom BPMN flow is found', '/mso/async/services/DeleteVnfInfra', '', '180', '2016-09-14 19:18:20', ''),
('81', 'VID_DEFAULT', 'updateInstance', '', '1', 'VID_DEFAULT update', '/mso/async/services/UpdateVnfInfra', '', '180', '2017-07-28 18:19:39', ''),
('85', 'VID_DEFAULT', 'replaceInstance', '', '1', 'VID_DEFAULT replace', '/mso/async/services/ReplaceVnfInfra', '', '180', '2017-07-28 18:19:45', ''),
('10000', 'VID_DEFAULT', 'inPlaceSoftwareUpdate', '', '1', 'VID_DEFAULT inPlaceSoftwareUpdate', '/mso/async/services/VnfInPlaceUpdate', '', '180', '2017-10-25 18:19:45', ''),
('10001', 'VID_DEFAULT', 'applyUpdatedConfig', '', '1', 'VID_DEFAULT applyUpdatedConfig', '/mso/async/services/VnfConfigUpdate', '', '180', '2017-10-25 18:19:45', '');


insert into vnf_components(vnf_id, component_type, heat_template_id, heat_environment_id, creation_timestamp) values
('13961', 'VOLUME', '13843', '13961', '2016-05-19 20:22:02');

insert into vnf_components_recipe(id, vnf_type, vnf_component_type, action, service_type, version, description, orchestration_uri, vnf_component_param_xsd, recipe_timeout, creation_timestamp, vf_module_model_uuid) values
('5', '*', 'VOLUME_GROUP', 'CREATE', '', '1', 'Recipe Match All for VF Modules if no custom flow exists', '/mso/async/services/createCinderVolumeV1', '', '180', '2016-06-03 10:15:11', ''),
('7', '*', 'VOLUME_GROUP', 'DELETE', '', '1', 'Recipe Match All for VF Modules if no custom flow exists', '/mso/async/services/deleteCinderVolumeV1', '', '180', '2016-06-03 10:15:11', ''),
('9', '*', 'VOLUME_GROUP', 'UPDATE', '', '1', 'Recipe Match All for VF Modules if no custom flow exists', '/mso/async/services/updateCinderVolumeV1', '', '180', '2016-06-03 10:15:11', ''),
('13', '', 'VOLUME_GROUP', 'DELETE_VF_MODULE_VOL', '', '1', 'Recipe Match All for VF Modules if no custom flow exists', '/mso/async/services/DeleteVfModuleVolume', '', '180', '2016-06-03 10:15:11', '*'),
('15', '', 'VOLUME_GROUP', 'UPDATE_VF_MODULE_VOL', '', '1', 'Recipe Match All for VF Modules if no custom flow exists', '/mso/async/services/UpdateVfModuleVolume', '', '180', '2016-06-03 10:15:11', '*'),
('16', '', 'volumeGroup', 'createInstance', '', '1', 'VID_DEFAULT recipe to create volume-group if no custom BPMN flow is found', '/mso/async/services/CreateVfModuleVolumeInfraV1', '', '180', '2016-09-14 19:18:20', 'VID_DEFAULT'),
('17', '', 'volumeGroup', 'deleteInstance', '', '1', 'VID_DEFAULT recipe to delete volume-group if no custom BPMN flow is found', '/mso/async/services/DeleteVfModuleVolumeInfraV1', '', '180', '2016-09-14 19:18:20', 'VID_DEFAULT'),
('18', '', 'volumeGroup', 'updateInstance', '', '1', 'VID_DEFAULT recipe to update volume-group if no custom BPMN flow is found', '/mso/async/services/UpdateVfModuleVolumeInfraV1', '', '180', '2016-09-14 19:18:20', 'VID_DEFAULT'),
('19', '', 'vfModule', 'createInstance', '', '1', 'VID_DEFAULT recipe to create vf-module if no custom BPMN flow is found', '/mso/async/services/CreateVfModuleInfra', '', '180', '2016-09-14 19:18:20', 'VID_DEFAULT'),
('20', '', 'vfModule', 'deleteInstance', '', '1', 'VID_DEFAULT recipe to delete vf-module if no custom BPMN flow is found', '/mso/async/services/DeleteVfModuleInfra', '', '180', '2016-09-14 19:18:20', 'VID_DEFAULT'),
('21', '', 'vfModule', 'updateInstance', '', '1', 'VID_DEFAULT recipe to update vf-module if no custom BPMN flow is found', '/mso/async/services/UpdateVfModuleInfra', '', '180', '2016-09-14 19:18:20', 'VID_DEFAULT'),
('25', '', 'vfModule', 'replaceInstance', '', '1', 'VID_DEFAULT vfModule replace', '/mso/async/services/ReplaceVfModuleInfra', '', '180', '2017-07-28 18:25:06', 'VID_DEFAULT');

insert into network_recipe(id, model_name, action, description, orchestration_uri, network_param_xsd, recipe_timeout, service_type, creation_timestamp, version_str) values
('1', 'CONTRAIL_BASIC', 'CREATE', '', '/mso/async/services/CreateNetworkV2', '', '180', '', '2016-09-14 19:00:57', '1'),
('2', 'CONTRAIL_BASIC', 'DELETE', '', '/mso/async/services/DeleteNetworkV2', '', '180', '', '2016-09-14 19:00:57', '1'),
('3', 'CONTRAIL_BASIC', 'UPDATE', '', '/mso/async/services/UpdateNetworkV2', '', '180', '', '2016-09-14 19:00:57', '1'),
('4', 'CONTRAIL_SHARED', 'CREATE', '', '/mso/async/services/CreateNetworkV2', '', '180', '', '2016-09-14 19:00:57', '1'),
('5', 'CONTRAIL_SHARED', 'UPDATE', '', '/mso/async/services/UpdateNetworkV2', '', '180', '', '2016-09-14 19:00:57', '1'),
('6', 'CONTRAIL_SHARED', 'DELETE', '', '/mso/async/services/DeleteNetworkV2', '', '180', '', '2016-09-14 19:00:57', '1'),
('7', 'CONTRAIL_EXTERNAL', 'CREATE', '', '/mso/async/services/CreateNetworkV2', '', '180', '', '2016-09-14 19:00:57', '1'),
('8', 'CONTRAIL_EXTERNAL', 'UPDATE', '', '/mso/async/services/UpdateNetworkV2', '', '180', '', '2016-09-14 19:00:57', '1'),
('9', 'CONTRAIL_EXTERNAL', 'DELETE', '', '/mso/async/services/DeleteNetworkV2', '', '180', '', '2016-09-14 19:00:57', '1'),
('10', 'CONTRAIL30_BASIC', 'CREATE', '', '/mso/async/services/CreateNetworkV2', '', '180', '', '2016-05-26 14:48:13', '1'),
('11', 'CONTRAIL30_BASIC', 'UPDATE', '', '/mso/async/services/UpdateNetworkV2', '', '180', '', '2016-05-26 14:48:13', '1'),
('12', 'CONTRAIL30_BASIC', 'DELETE', '', '/mso/async/services/DeleteNetworkV2', '', '180', '', '2016-05-26 14:48:13', '1'),
('13', 'NEUTRON_BASIC', 'CREATE', '', '/mso/async/services/CreateNetworkV2', '', '180', '', '2016-06-01 19:54:51', '1'),
('17', 'VID_DEFAULT', 'createInstance', 'VID_DEFAULT recipe to create network if no custom BPMN flow is found', '/mso/async/services/CreateNetworkInstance', '', '180', '', '2016-09-14 19:18:20', '1.0'),
('18', 'VID_DEFAULT', 'updateInstance', 'VID_DEFAULT recipe to update network if no custom BPMN flow is found', '/mso/async/services/UpdateNetworkInstance', '', '180', '', '2016-09-14 19:18:20', '1.0'),
('19', 'VID_DEFAULT', 'deleteInstance', 'VID_DEFAULT recipe to delete network if no custom BPMN flow is found', '/mso/async/services/DeleteNetworkInstance', '', '180', '', '2016-09-14 19:18:20', '1.0'),
('124', 'CONTRAIL30_MPSCE', 'CREATE', '', '/mso/async/services/CreateNetworkV2', '', '180', '', '2016-10-18 18:47:52', '1'),
('126', 'CONTRAIL30_MPSCE', 'UPDATE', '', '/mso/async/services/UpdateNetworkV2', '', '180', '', '2016-10-18 18:47:52', '1'),
('128', 'CONTRAIL30_MPSCE', 'DELETE', '', '/mso/async/services/DeleteNetworkV2', '', '180', '', '2016-10-18 18:47:52', '1'),
('141', 'CONTRAIL30_L2NODHCP', 'CREATE', '', '/mso/async/services/CreateNetworkV2', '', '180', '', '2017-01-03 20:12:46', '1'),
('144', 'CONTRAIL30_L2NODHCP', 'UPDATE', '', '/mso/async/services/UpdateNetworkV2', '', '180', '', '2017-01-03 20:12:46', '1'),
('147', 'CONTRAIL30_L2NODHCP', 'DELETE', '', '/mso/async/services/DeleteNetworkV2', '', '180', '', '2017-01-03 20:12:46', '1'),
('169', 'CONTRAIL30_GNDIRECT', 'CREATE', '', '/mso/async/services/CreateNetworkV2', '', '180', '', '2017-01-17 20:25:34', '1'),
('172', 'CONTRAIL30_GNDIRECT', 'UPDATE', '', '/mso/async/services/UpdateNetworkV2', '', '180', '', '2017-01-17 20:25:34', '1'),
('175', 'CONTRAIL30_GNDIRECT', 'DELETE', '', '/mso/async/services/DeleteNetworkV2', '', '180', '', '2017-01-17 20:25:34', '1'),
('176', 'NEUTRON_BASIC', 'DELETE', '', '/mso/async/services/DeleteNetworkV2', '', '180', '', '2017-09-22 18:47:31', '1'),
('177', 'NEUTRON_BASIC', 'UPDATE', '', '/mso/async/services/UpdateNetworkV2', '', '180', '', '2017-09-22 18:47:31', '1');

INSERT INTO northbound_request_ref_lookup(MACRO_ACTION, ACTION, REQUEST_SCOPE, IS_ALACARTE, IS_TOPLEVELFLOW, MIN_API_VERSION, MAX_API_VERSION,cloud_owner) VALUES
('Service-Create', 'createInstance', 'Service', true, true, '7','7','cloudOwner'),
('Service-Delete', 'deleteInstance', 'Service', true, true, '7','7','cloudOwner'),
('Service-Macro-Assign', 'assignInstance', 'Service', false, true, '7','7','cloudOwner'),
('Service-Macro-Activate', 'activateInstance', 'Service', false, true, '7','7','cloudOwner'),
('Service-Macro-Unassign', 'unassignInstance', 'Service', false, true, '7','7','cloudOwner'),
('Service-Macro-Create', 'createInstance', 'Service', false, true, '7','7','cloudOwner'),
('Service-Macro-Delete', 'deleteInstance', 'Service', false, true, '7','7','cloudOwner'),
('Network-Create', 'createInstance', 'Network', true, true, '7','7','cloudOwner'),
('Network-Delete', 'deleteInstance', 'Network', true, true, '7','7','cloudOwner'),
('VNF-Macro-Recreate', 'replaceInstance', 'Vnf', false, false, '7','7','cloudOwner'),
('VNF-Macro-Replace', 'internalReplace', 'Vnf', false, false, '7','7','cloudOwner'),
('VNF-Create', 'createInstance', 'Vnf', true, true, '7', '7','cloudOwner'),
('VNF-Delete', 'deleteInstance', 'Vnf', true, true, '7', '7','cloudOwner'),
('VolumeGroup-Create', 'createInstance', 'VolumeGroup', true, true, '7','7','cloudOwner'),
('VolumeGroup-Delete', 'deleteInstance', 'VolumeGroup', true, true, '7','7','cloudOwner'),
('VFModule-Create', 'createInstance', 'VfModule', true, true, '7','7','cloudOwner'),
('VFModule-Delete', 'deleteInstance', 'VfModule', true, true, '7','7','cloudOwner'),
('VFModule-DeactivateAndCloudDelete', 'deactivateAndCloudDelete', 'VfModule', true, true, '7','7','cloudOwner'),
('NetworkCollection-Macro-Create', 'createInstance', 'NetworkCollection', false, true, '7','7','cloudOwner'),
('NetworkCollection-Macro-Delete', 'deleteInstance', 'NetworkCollection', false, true, '7','7','cloudOwner'),
('InstanceGroup-Create', 'createInstance', 'InstanceGroup', true, true, '7','7','cloudOwner'),
('InstanceGroup-Delete', 'deleteInstance', 'InstanceGroup', true, true, '7','7','cloudOwner'),
('InstanceGroupMembers-Add', 'addMembers', 'InstanceGroupMembers', true, true, '7','7','cloudOwner'),
('InstanceGroupMembers-Remove', 'removeMembers', 'InstanceGroupMembers', true, true, '7','7','cloudOwner');

INSERT INTO building_block_detail (BUILDING_BLOCK_NAME, RESOURCE_TYPE, TARGET_ACTION)
VALUES
('AssignServiceInstanceBB', 'SERVICE', 'ASSIGN'),
('AssignVnfBB', 'VNF', 'ASSIGN'),
('AssignVolumeGroupBB', 'VOLUME_GROUP', 'ASSIGN'),
('AssignVfModuleBB', 'VF_MODULE', 'ASSIGN'),
('AssignNetworkBB', 'NETWORK', 'ASSIGN'),
('AssignNetwork1802BB', 'NETWORK', 'ASSIGN'),
('UnassignServiceInstanceBB', 'SERVICE', 'UNASSIGN'),
('UnassignVnfBB', 'VNF', 'UNASSIGN'),
('UnassignVolumeGroupBB', 'VOLUME_GROUP', 'UNASSIGN'),
('UnassignVfModuleBB', 'VF_MODULE', 'UNASSIGN'),
('UnassignNetwork1802BB', 'NETWORK', 'UNASSIGN'),
('UnassignNetworkBB', 'NETWORK', 'UNASSIGN'),
('ActivateServiceInstanceBB', 'SERVICE', 'ACTIVATE'),
('ActivateVnfBB', 'VNF', 'ACTIVATE'),
('ActivateVolumeGroupBB', 'VOLUME_GROUP', 'ACTIVATE'),
('ActivateVfModuleBB', 'VF_MODULE', 'ACTIVATE'),
('ActivateNetworkBB', 'NETWORK', 'ACTIVATE'),
('ActivateNetworkCollectionBB', 'NETWORK', 'ACTIVATE'),
('DeactivateServiceInstanceBB', 'SERVICE', 'DEACTIVATE'),
('DeactivateVnfBB', 'VNF', 'DEACTIVATE'),
('DeactivateVolumeGroupBB', 'VOLUME_GROUP', 'DEACTIVATE'),
('DeactivateVfModuleBB', 'VF_MODULE', 'DEACTIVATE'),
('DeactivateNetworkBB', 'NETWORK', 'DEACTIVATE'),
('ChangeModelServiceInstanceBB', 'SERVICE', 'CHANGE_MODEL'),
('ChangeModelVnfBB', 'VNF', 'CHANGE_MODEL'),
('ChangeModelVfModuleBB', 'VF_MODULE', 'CHANGE_MODEL'),
('CreateVolumeGroupBB', 'VOLUME_GROUP', 'CREATE'),
('CreateVfModuleBB', 'VF_MODULE', 'CREATE'),
('CreateNetworkBB', 'NETWORK', 'CREATE'),
('CreateNetworkCollectionBB', 'NETWORK', 'CREATE'),
('DeleteVolumeGroupBB', 'VOLUME_GROUP', 'DELETE'),
('DeleteVfModuleBB', 'VF_MODULE', 'DELETE'),
('DeleteNetworkBB', 'NETWORK', 'DELETE'),
('DeleteNetworkCollectionBB', 'NETWORK', 'DELETE'),
('AssignAndActivateVpnBondingLinksBB', 'NO_VALIDATE', 'CUSTOM'),
('AvpnAssignServiceInstanceBB', 'NO_VALIDATE', 'CUSTOM'),
('CreateCustomerVpnBindingBB', 'NO_VALIDATE', 'CUSTOM'),
('SniroHoming', 'NO_VALIDATE', 'CUSTOM'),
('DeactivateAndUnassignVpnBondingLinksBB', 'NO_VALIDATE', 'CUSTOM'),
('DeactivateNetworkCollectionBB', 'NO_VALIDATE', 'CUSTOM'),
('AAICheckVnfInMaintBB', 'NO_VALIDATE', 'CUSTOM'),
('AAISetVnfInMaintBB', 'NO_VALIDATE', 'CUSTOM'),
('AAIUnsetVnfInMaintBB', 'NO_VALIDATE', 'CUSTOM'),
('SDNOVnfHealthCheckBB', 'NO_VALIDATE', 'CUSTOM'),
('VNF-Macro-Replace', 'NO_VALIDATE', 'CUSTOM'),
('HomingBB', 'NO_VALIDATE', 'CUSTOM'),
('VNFSetInMaintFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFCheckPserversLockedFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFCheckInMaintFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFCheckClosedLoopDisabledFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFSetClosedLoopDisabledFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUnsetClosedLoopDisabledFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFLockActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUnlockActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFStopActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFStartActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFSnapShotActivity', 'NO_VALIDATE', 'CUSTOM'),
('FlowCompleteActivity', 'NO_VALIDATE', 'CUSTOM'),
('PauseForManualTaskActivity', 'NO_VALIDATE', 'CUSTOM'),
('DistributeTrafficActivity', 'NO_VALIDATE', 'CUSTOM'),
('DistributeTrafficCheckActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFHealthCheckActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFQuiesceTrafficActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFResumeTrafficActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUnsetInMaintFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUpgradeBackupActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUpgradePostCheckActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUpgradePreCheckActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUpgradeSoftwareActivity', 'NO_VALIDATE', 'CUSTOM'),
('VnfInPlaceSoftwareUpdate', 'NO_VALIDATE', 'CUSTOM');


INSERT INTO orchestration_flow_reference(COMPOSITE_ACTION, SEQ_NO, FLOW_NAME, FLOW_VERSION, NB_REQ_REF_LOOKUP_ID) VALUES
('Service-Create', '1', 'AssignServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Create')),
('Service-Create', '2', 'ActivateServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Create')),
('Service-Delete', '1', 'DeactivateServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Delete')),
('Service-Delete', '2', 'UnassignServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Delete')),
('Service-Macro-Assign', '1', 'AssignServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Assign')),
('Service-Macro-Assign', '2', 'AssignNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Assign')),
('Service-Macro-Assign', '3', 'AssignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Assign')),
('Service-Macro-Assign', '4', 'AssignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Assign')),
('Service-Macro-Assign', '5', 'AssignVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Assign')),
('Service-Macro-Activate', '1', 'CreateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate')),
('Service-Macro-Activate', '2', 'ActivateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate')),
('Service-Macro-Activate', '3', 'CreateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate')),
('Service-Macro-Activate', '4', 'ActivateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate')),
('Service-Macro-Activate', '5', 'CreateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate')),
('Service-Macro-Activate', '6', 'ActivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate')),
('Service-Macro-Activate', '7', 'ActivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate')),
('Service-Macro-Activate', '8', 'ActivateServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate')),
('Service-Macro-Unassign', '1', 'UnassignVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Unassign')),
('Service-Macro-Unassign', '2', 'UnassignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Unassign')),
('Service-Macro-Unassign', '3', 'UnassignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Unassign')),
('Service-Macro-Unassign', '4', 'UnassignNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Unassign')),
('Service-Macro-Unassign', '5', 'UnassignServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Unassign')),
('Service-Macro-Create', '1', 'AssignServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '2', 'CreateNetworkCollectionBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '3', 'AssignNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '4', 'AssignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '5', 'AssignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '6', 'AssignVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '7', 'CreateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '8', 'ActivateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '9', 'CreateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '10', 'ActivateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '11', 'CreateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '12', 'ActivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '13', 'ActivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '14', 'ActivateNetworkCollectionBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Create', '15', 'ActivateServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create')),
('Service-Macro-Delete', '1', 'DeactivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '2', 'DeleteVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '3', 'DeactivateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '4', 'DeleteVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '5', 'DeactivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '6', 'DeactivateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '7', 'DeleteNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '8', 'DeleteNetworkCollectionBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '9', 'DeactivateServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '10', 'UnassignVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '11', 'UnassignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '12', 'UnassignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '13', 'UnassignNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Service-Macro-Delete', '14', 'UnassignServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete')),
('Network-Create', '1', 'AssignNetwork1802BB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Network-Create')),
('Network-Create', '2', 'CreateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Network-Create')),
('Network-Create', '3', 'ActivateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Network-Create')),
('Network-Delete', '1', 'DeactivateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Network-Delete')),
('Network-Delete', '2', 'DeleteNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Network-Delete')),
('Network-Delete', '3', 'UnassignNetwork1802BB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Network-Delete')),
('VNF-Create', '1', 'AssignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Create')),
('VNF-Create', '2', 'ActivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Create')),
('VNF-Delete', '1', 'DeactivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Delete')),
('VNF-Delete', '2', 'UnassignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Delete')),
('VNF-Macro-Recreate', '1', 'AAICheckVnfInMaintBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Recreate')),
('VNF-Macro-Recreate', '2', 'AAISetVnfInMaintBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Recreate')),
('VNF-Macro-Recreate', '3', 'VNF-Macro-Replace', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Recreate')),
('VNF-Macro-Recreate', '4', 'SDNOVnfHealthCheckBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Recreate')),
('VNF-Macro-Recreate', '5', 'AAIUnsetVnfInMaintBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Recreate')),
('VNF-Macro-Replace', '1', 'DeactivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace')),
('VNF-Macro-Replace', '2', 'DeleteVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace')),
('VNF-Macro-Replace', '3', 'DeactivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace')),
('VNF-Macro-Replace', '4', 'CreateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace')),
('VNF-Macro-Replace', '5', 'ActivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace')),
('VNF-Macro-Replace', '6', 'ActivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace')),
('VolumeGroup-Create', '1', 'AssignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VolumeGroup-Create')),
('VolumeGroup-Create', '2', 'CreateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VolumeGroup-Create')),
('VolumeGroup-Create', '3', 'ActivateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VolumeGroup-Create')),
('VolumeGroup-Delete', '1', 'DeactivateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VolumeGroup-Delete')),
('VolumeGroup-Delete', '2', 'DeleteVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VolumeGroup-Delete')),
('VolumeGroup-Delete', '3', 'UnassignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VolumeGroup-Delete')),
('VFModule-Create', '1', 'AssignVfModuleBB',  1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Create')),
('VFModule-Create', '2', 'CreateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Create')),
('VFModule-Create', '3', 'ActivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Create')),
('VFModule-Delete', '1', 'DeactivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Delete')),
('VFModule-Delete', '2', 'DeleteVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Delete')),
('VFModule-Delete', '3', 'UnassignVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Delete')),
('VFModule-DeactivateAndCloudDelete', '1', 'DeactivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-DeactivateAndCloudDelete')),
('VFModule-DeactivateAndCloudDelete', '2', 'DeleteVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-DeactivateAndCloudDelete')),
('NetworkCollection-Macro-Create', '1', 'CreateNetworkCollectionBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Create')),
('NetworkCollection-Macro-Create', '2', 'AssignNetworkBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Create')),
('NetworkCollection-Macro-Create', '3', 'CreateNetworkBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Create')),
('NetworkCollection-Macro-Create', '4', 'ActivateNetworkBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Create')),
('NetworkCollection-Macro-Create', '5', 'ActivateNetworkCollectionBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Create')),
('NetworkCollection-Macro-Delete', '1', 'DeactivateNetworkBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Delete')),
('NetworkCollection-Macro-Delete', '2', 'DeleteNetworkBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Delete')),
('NetworkCollection-Macro-Delete', '3', 'UnassignNetworkBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Delete')),
('NetworkCollection-Macro-Delete', '4', 'DeleteNetworkCollectionBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Delete')),
('InstanceGroup-Create', '1', 'CreateInstanceGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'InstanceGroup-Create')),
('InstanceGroup-Delete', '1', 'DeleteInstanceGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'InstanceGroup-Delete')),
('InstanceGroupMembers-Add', '1', 'AddInstanceGroupMembersBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'InstanceGroupMembers-Add')),
('InstanceGroupmembers-Remove', '1', 'RemoveInstanceGroupMembersBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'InstanceGroupMembers-Remove'));


INSERT INTO orchestration_status_state_transition_directive (RESOURCE_TYPE, ORCHESTRATION_STATUS, TARGET_ACTION, FLOW_DIRECTIVE)
VALUES
('SERVICE', 'PRECREATED', 'ASSIGN', 'CONTINUE'),
('VNF', 'PRECREATED', 'ASSIGN', 'CONTINUE'),
('VOLUME_GROUP', 'PRECREATED', 'ASSIGN', 'CONTINUE'),
('VF_MODULE', 'PRECREATED', 'ASSIGN', 'CONTINUE'),
('NETWORK', 'PRECREATED', 'ASSIGN', 'CONTINUE'),
('SERVICE', 'INVENTORIED', 'ASSIGN', 'CONTINUE'),
('VNF', 'INVENTORIED', 'ASSIGN', 'CONTINUE'),
('VOLUME_GROUP', 'INVENTORIED', 'ASSIGN', 'FAIL'),
('VF_MODULE', 'INVENTORIED', 'ASSIGN', 'CONTINUE'),
('NETWORK', 'INVENTORIED', 'ASSIGN', 'CONTINUE'),
('SERVICE', 'ASSIGNED', 'ASSIGN', 'SILENT_SUCCESS'),
('VNF', 'ASSIGNED', 'ASSIGN', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'ASSIGNED', 'ASSIGN', 'SILENT_SUCCESS'),
('VF_MODULE', 'ASSIGNED', 'ASSIGN', 'SILENT_SUCCESS'),
('NETWORK', 'ASSIGNED', 'ASSIGN', 'SILENT_SUCCESS'),
('SERVICE', 'CREATED', 'ASSIGN', 'SILENT_SUCCESS'),
('VNF', 'CREATED', 'ASSIGN', 'CONTINUE'),
('VOLUME_GROUP', 'CREATED', 'ASSIGN', 'FAIL'),
('VF_MODULE', 'CREATED', 'ASSIGN', 'SILENT_SUCCESS'),
('NETWORK', 'CREATED', 'ASSIGN', 'SILENT_SUCCESS'),
('SERVICE', 'ACTIVE', 'ASSIGN', 'SILENT_SUCCESS'),
('VNF', 'ACTIVE', 'ASSIGN', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'ACTIVE', 'ASSIGN', 'SILENT_SUCCESS'),
('VF_MODULE', 'ACTIVE', 'ASSIGN', 'SILENT_SUCCESS'),
('NETWORK', 'ACTIVE', 'ASSIGN', 'SILENT_SUCCESS'),
('SERVICE', 'PENDING_CREATE', 'ASSIGN', 'FAIL'),
('VNF', 'PENDING_CREATE', 'ASSIGN', 'FAIL'),
('VOLUME_GROUP', 'PENDING_CREATE', 'ASSIGN', 'FAIL'),
('VF_MODULE', 'PENDING_CREATE', 'ASSIGN', 'CONTINUE'),
('NETWORK', 'PENDING_CREATE', 'ASSIGN', 'SILENT_SUCCESS'),
('SERVICE', 'PENDING_DELETE', 'ASSIGN', 'SILENT_SUCCESS'),
('VNF', 'PENDING_DELETE', 'ASSIGN', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'PENDING_DELETE', 'ASSIGN', 'FAIL'),
('VF_MODULE', 'PENDING_DELETE', 'ASSIGN', 'SILENT_SUCCESS'),
('NETWORK', 'PENDING_DELETE', 'ASSIGN', 'SILENT_SUCCESS'),
('SERVICE', 'PENDING_ACTIVATION', 'ASSIGN', 'FAIL'),
('VNF', 'PENDING_ACTIVATION', 'ASSIGN', 'FAIL'),
('VOLUME_GROUP', 'PENDING_ACTIVATION', 'ASSIGN', 'FAIL'),
('VF_MODULE', 'PENDING_ACTIVATION', 'ASSIGN', 'SILENT_SUCCESS'),
('NETWORK', 'PENDING_ACTIVATION', 'ASSIGN', 'FAIL'),
('SERVICE', 'PENDING', 'ASSIGN', 'FAIL'),
('VNF', 'PENDING', 'ASSIGN', 'FAIL'),
('VOLUME_GROUP', 'PENDING', 'ASSIGN', 'SILENT_SUCCESS'),
('VF_MODULE', 'PENDING', 'ASSIGN', 'FAIL'),
('NETWORK', 'PENDING', 'ASSIGN', 'FAIL'),
('SERVICE', 'PRECREATED', 'UNASSIGN', 'SILENT_SUCCESS'),
('VNF', 'PRECREATED', 'UNASSIGN', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'PRECREATED', 'UNASSIGN', 'SILENT_SUCCESS'),
('VF_MODULE', 'PRECREATED', 'UNASSIGN', 'SILENT_SUCCESS'),
('NETWORK', 'PRECREATED', 'UNASSIGN', 'SILENT_SUCCESS'),
('SERVICE', 'INVENTORIED', 'UNASSIGN', 'CONTINUE'),
('VNF', 'INVENTORIED', 'UNASSIGN', 'CONTINUE'),
('VOLUME_GROUP', 'INVENTORIED', 'UNASSIGN', 'FAIL'),
('VF_MODULE', 'INVENTORIED', 'UNASSIGN', 'CONTINUE'),
('NETWORK', 'INVENTORIED', 'UNASSIGN', 'CONTINUE'),
('SERVICE', 'ASSIGNED', 'UNASSIGN', 'CONTINUE'),
('VNF', 'ASSIGNED', 'UNASSIGN', 'CONTINUE'),
('VOLUME_GROUP', 'ASSIGNED', 'UNASSIGN', 'CONTINUE'),
('VF_MODULE', 'ASSIGNED', 'UNASSIGN', 'CONTINUE'),
('NETWORK', 'ASSIGNED', 'UNASSIGN', 'CONTINUE'),
('SERVICE', 'CREATED', 'UNASSIGN', 'CONTINUE'),
('VNF', 'CREATED', 'UNASSIGN', 'CONTINUE'),
('VOLUME_GROUP', 'CREATED', 'UNASSIGN', 'FAIL'),
('VF_MODULE', 'CREATED', 'UNASSIGN', 'FAIL'),
('NETWORK', 'CREATED', 'UNASSIGN', 'FAIL'),
('SERVICE', 'ACTIVE', 'UNASSIGN', 'FAIL'),
('VNF', 'ACTIVE', 'UNASSIGN', 'FAIL'),
('VOLUME_GROUP', 'ACTIVE', 'UNASSIGN', 'FAIL'),
('VF_MODULE', 'ACTIVE', 'UNASSIGN', 'FAIL'),
('NETWORK', 'ACTIVE', 'UNASSIGN', 'FAIL'),
('SERVICE', 'PENDING_CREATE', 'UNASSIGN', 'FAIL'),
('VNF', 'PENDING_CREATE', 'UNASSIGN', 'FAIL'),
('VOLUME_GROUP', 'PENDING_CREATE', 'UNASSIGN', 'FAIL'),
('VF_MODULE', 'PENDING_CREATE', 'UNASSIGN', 'CONTINUE'),
('NETWORK', 'PENDING_CREATE', 'UNASSIGN', 'CONTINUE'),
('SERVICE', 'PENDING_DELETE', 'UNASSIGN', 'CONTINUE'),
('VNF', 'PENDING_DELETE', 'UNASSIGN', 'CONTINUE'),
('VOLUME_GROUP', 'PENDING_DELETE', 'UNASSIGN', 'FAIL'),
('VF_MODULE', 'PENDING_DELETE', 'UNASSIGN', 'CONTINUE'),
('NETWORK', 'PENDING_DELETE', 'UNASSIGN', 'CONTINUE'),
('SERVICE', 'PENDING_ACTIVATION', 'UNASSIGN', 'FAIL'),
('VNF', 'PENDING_ACTIVATION', 'UNASSIGN', 'FAIL'),
('VOLUME_GROUP', 'PENDING_ACTIVATION', 'UNASSIGN', 'FAIL'),
('VF_MODULE', 'PENDING_ACTIVATION', 'UNASSIGN', 'CONTINUE'),
('NETWORK', 'PENDING_ACTIVATION', 'UNASSIGN', 'FAIL'),
('SERVICE', 'PENDING', 'UNASSIGN', 'FAIL'),
('VNF', 'PENDING', 'UNASSIGN', 'FAIL'),
('VOLUME_GROUP', 'PENDING', 'UNASSIGN', 'CONTINUE'),
('VF_MODULE', 'PENDING', 'UNASSIGN', 'FAIL'),
('NETWORK', 'PENDING', 'UNASSIGN', 'FAIL'),
('SERVICE', 'PRECREATED', 'ACTIVATE', 'FAIL'),
('VNF', 'PRECREATED', 'ACTIVATE', 'FAIL'),
('VOLUME_GROUP', 'PRECREATED', 'ACTIVATE', 'FAIL'),
('VF_MODULE', 'PRECREATED', 'ACTIVATE', 'FAIL'),
('NETWORK', 'PRECREATED', 'ACTIVATE', 'FAIL'),
('SERVICE', 'INVENTORIED', 'ACTIVATE', 'FAIL'),
('VNF', 'INVENTORIED', 'ACTIVATE', 'FAIL'),
('VOLUME_GROUP', 'INVENTORIED', 'ACTIVATE', 'FAIL'),
('VF_MODULE', 'INVENTORIED', 'ACTIVATE', 'FAIL'),
('NETWORK', 'INVENTORIED', 'ACTIVATE', 'FAIL'),
('SERVICE', 'ASSIGNED', 'ACTIVATE', 'CONTINUE'),
('VNF', 'ASSIGNED', 'ACTIVATE', 'CONTINUE'),
('VOLUME_GROUP', 'ASSIGNED', 'ACTIVATE', 'FAIL'),
('VF_MODULE', 'ASSIGNED', 'ACTIVATE', 'FAIL'),
('NETWORK', 'ASSIGNED', 'ACTIVATE', 'FAIL'),
('SERVICE', 'CREATED', 'ACTIVATE', 'CONTINUE'),
('VNF', 'CREATED', 'ACTIVATE', 'FAIL'),
('VOLUME_GROUP', 'CREATED', 'ACTIVATE', 'CONTINUE'),
('VF_MODULE', 'CREATED', 'ACTIVATE', 'CONTINUE'),
('NETWORK', 'CREATED', 'ACTIVATE', 'CONTINUE'),
('SERVICE', 'ACTIVE', 'ACTIVATE', 'SILENT_SUCCESS'),
('VNF', 'ACTIVE', 'ACTIVATE', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'ACTIVE', 'ACTIVATE', 'SILENT_SUCCESS'),
('VF_MODULE', 'ACTIVE', 'ACTIVATE', 'SILENT_SUCCESS'),
('NETWORK', 'ACTIVE', 'ACTIVATE', 'SILENT_SUCCESS'),
('SERVICE', 'PENDING_CREATE', 'ACTIVATE', 'FAIL'),
('VNF', 'PENDING_CREATE', 'ACTIVATE', 'FAIL'),
('VOLUME_GROUP', 'PENDING_CREATE', 'ACTIVATE', 'FAIL'),
('VF_MODULE', 'PENDING_CREATE', 'ACTIVATE', 'FAIL'),
('NETWORK', 'PENDING_CREATE', 'ACTIVATE', 'FAIL'),
('SERVICE', 'PENDING_DELETE', 'ACTIVATE', 'CONTINUE'),
('VNF', 'PENDING_DELETE', 'ACTIVATE', 'CONTINUE'),
('VOLUME_GROUP', 'PENDING_DELETE', 'ACTIVATE', 'FAIL'),
('VF_MODULE', 'PENDING_DELETE', 'ACTIVATE', 'FAIL'),
('NETWORK', 'PENDING_DELETE', 'ACTIVATE', 'FAIL'),
('SERVICE', 'PENDING_ACTIVATION', 'ACTIVATE', 'FAIL'),
('VNF', 'PENDING_ACTIVATION', 'ACTIVATE', 'FAIL'),
('VOLUME_GROUP', 'PENDING_ACTIVATION', 'ACTIVATE', 'FAIL'),
('VF_MODULE', 'PENDING_ACTIVATION', 'ACTIVATE', 'SILENT_SUCCESS'),
('NETWORK', 'PENDING_ACTIVATION', 'ACTIVATE', 'FAIL'),
('SERVICE', 'PENDING', 'ACTIVATE', 'FAIL'),
('VNF', 'PENDING', 'ACTIVATE', 'FAIL'),
('VOLUME_GROUP', 'PENDING', 'ACTIVATE', 'FAIL'),
('VF_MODULE', 'PENDING', 'ACTIVATE', 'FAIL'),
('NETWORK', 'PENDING', 'ACTIVATE', 'FAIL'),
('SERVICE', 'PRECREATED', 'DEACTIVATE', 'FAIL'),
('VNF', 'PRECREATED', 'DEACTIVATE', 'FAIL'),
('VOLUME_GROUP', 'PRECREATED', 'DEACTIVATE', 'FAIL'),
('VF_MODULE', 'PRECREATED', 'DEACTIVATE', 'FAIL'),
('NETWORK', 'PRECREATED', 'DEACTIVATE', 'FAIL'),
('SERVICE', 'INVENTORIED', 'DEACTIVATE', 'FAIL'),
('VNF', 'INVENTORIED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'INVENTORIED', 'DEACTIVATE', 'FAIL'),
('VF_MODULE', 'INVENTORIED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('NETWORK', 'INVENTORIED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('SERVICE', 'ASSIGNED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('VNF', 'ASSIGNED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'ASSIGNED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('VF_MODULE', 'ASSIGNED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('NETWORK', 'ASSIGNED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('SERVICE', 'CREATED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('VNF', 'CREATED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'CREATED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('VF_MODULE', 'CREATED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('NETWORK', 'CREATED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('SERVICE', 'ACTIVE', 'DEACTIVATE', 'CONTINUE'),
('VNF', 'ACTIVE', 'DEACTIVATE', 'CONTINUE'),
('VOLUME_GROUP', 'ACTIVE', 'DEACTIVATE', 'CONTINUE'),
('VF_MODULE', 'ACTIVE', 'DEACTIVATE', 'CONTINUE'),
('NETWORK', 'ACTIVE', 'DEACTIVATE', 'CONTINUE'),
('SERVICE', 'PENDING_CREATE', 'DEACTIVATE', 'FAIL'),
('VNF', 'PENDING_CREATE', 'DEACTIVATE', 'FAIL'),
('VOLUME_GROUP', 'PENDING_CREATE', 'DEACTIVATE', 'FAIL'),
('VF_MODULE', 'PENDING_CREATE', 'DEACTIVATE', 'SILENT_SUCCESS'),
('NETWORK', 'PENDING_CREATE', 'DEACTIVATE', 'SILENT_SUCCESS'),
('SERVICE', 'PENDING_DELETE', 'DEACTIVATE', 'SILENT_SUCCESS'),
('VNF', 'PENDING_DELETE', 'DEACTIVATE', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'PENDING_DELETE', 'DEACTIVATE', 'FAIL'),
('VF_MODULE', 'PENDING_DELETE', 'DEACTIVATE', 'SILENT_SUCCESS'),
('NETWORK', 'PENDING_DELETE', 'DEACTIVATE', 'SILENT_SUCCESS'),
('SERVICE', 'PENDING_ACTIVATION', 'DEACTIVATE', 'FAIL'),
('VNF', 'PENDING_ACTIVATION', 'DEACTIVATE', 'FAIL'),
('VOLUME_GROUP', 'PENDING_ACTIVATION', 'DEACTIVATE', 'FAIL'),
('VF_MODULE', 'PENDING_ACTIVATION', 'DEACTIVATE', 'SILENT_SUCCESS'),
('NETWORK', 'PENDING_ACTIVATION', 'DEACTIVATE', 'FAIL'),
('SERVICE', 'PENDING', 'DEACTIVATE', 'FAIL'),
('VNF', 'PENDING', 'DEACTIVATE', 'FAIL'),
('VOLUME_GROUP', 'PENDING', 'DEACTIVATE', 'SILENT_SUCCESS'),
('VF_MODULE', 'PENDING', 'DEACTIVATE', 'FAIL'),
('NETWORK', 'PENDING', 'DEACTIVATE', 'FAIL'),
('SERVICE', 'PRECREATED', 'CHANGE_MODEL', 'FAIL'),
('VNF', 'PRECREATED', 'CHANGE_MODEL', 'FAIL'),
('VF_MODULE', 'PRECREATED', 'CHANGE_MODEL', 'FAIL'),
('SERVICE', 'INVENTORIED', 'CHANGE_MODEL', 'FAIL'),
('VNF', 'INVENTORIED', 'CHANGE_MODEL', 'FAIL'),
('VF_MODULE', 'INVENTORIED', 'CHANGE_MODEL', 'FAIL'),
('SERVICE', 'ASSIGNED', 'CHANGE_MODEL', 'FAIL'),
('VNF', 'ASSIGNED', 'CHANGE_MODEL', 'CONTINUE'),
('VF_MODULE', 'ASSIGNED', 'CHANGE_MODEL', 'CONTINUE'),
('SERVICE', 'CREATED', 'CHANGE_MODEL', 'FAIL'),
('VNF', 'CREATED', 'CHANGE_MODEL', 'FAIL'),
('VF_MODULE', 'CREATED', 'CHANGE_MODEL', 'FAIL'),
('SERVICE', 'ACTIVE', 'CHANGE_MODEL', 'CONTINUE'),
('VNF', 'ACTIVE', 'CHANGE_MODEL', 'CONTINUE'),
('VF_MODULE', 'ACTIVE', 'CHANGE_MODEL', 'CONTINUE'),
('SERVICE', 'PENDING_CREATE', 'CHANGE_MODEL', 'FAIL'),
('VNF', 'PENDING_CREATE', 'CHANGE_MODEL', 'FAIL'),
('VF_MODULE', 'PENDING_CREATE', 'CHANGE_MODEL', 'FAIL'),
('SERVICE', 'PENDING_DELETE', 'CHANGE_MODEL', 'FAIL'),
('VNF', 'PENDING_DELETE', 'CHANGE_MODEL', 'CONTINUE'),
('VF_MODULE', 'PENDING_DELETE', 'CHANGE_MODEL', 'CONTINUE'),
('SERVICE', 'PENDING_ACTIVATION', 'CHANGE_MODEL', 'FAIL'),
('VNF', 'PENDING_ACTIVATION', 'CHANGE_MODEL', 'CONTINUE'),
('VF_MODULE', 'PENDING_ACTIVATION', 'CHANGE_MODEL', 'CONTINUE'),
('SERVICE', 'PENDING', 'CHANGE_MODEL', 'FAIL'),
('VNF', 'PENDING', 'CHANGE_MODEL', 'FAIL'),
('VF_MODULE', 'PENDING', 'CHANGE_MODEL', 'FAIL'),
('VOLUME_GROUP', 'PRECREATED', 'CREATE', 'FAIL'),
('VF_MODULE', 'PRECREATED', 'CREATE', 'FAIL'),
('NETWORK', 'PRECREATED', 'CREATE', 'FAIL'),
('VOLUME_GROUP', 'INVENTORIED', 'CREATE', 'FAIL'),
('VF_MODULE', 'INVENTORIED', 'CREATE', 'FAIL'),
('NETWORK', 'INVENTORIED', 'CREATE', 'FAIL'),
('VOLUME_GROUP', 'ASSIGNED', 'CREATE', 'CONTINUE'),
('VF_MODULE', 'ASSIGNED', 'CREATE', 'CONTINUE'),
('NETWORK', 'ASSIGNED', 'CREATE', 'CONTINUE'),
('VOLUME_GROUP', 'CREATED', 'CREATE', 'SILENT_SUCCESS'),
('VF_MODULE', 'CREATED', 'CREATE', 'SILENT_SUCCESS'),
('NETWORK', 'CREATED', 'CREATE', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'ACTIVE', 'CREATE', 'SILENT_SUCCESS'),
('VF_MODULE', 'ACTIVE', 'CREATE', 'SILENT_SUCCESS'),
('NETWORK', 'ACTIVE', 'CREATE', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'PENDING_CREATE', 'CREATE', 'FAIL'),
('VF_MODULE', 'PENDING_CREATE', 'CREATE', 'FAIL'),
('NETWORK', 'PENDING_CREATE', 'CREATE', 'CONTINUE'),
('VOLUME_GROUP', 'PENDING_DELETE', 'CREATE', 'FAIL'),
('VF_MODULE', 'PENDING_DELETE', 'CREATE', 'CONTINUE'),
('NETWORK', 'PENDING_DELETE', 'CREATE', 'CONTINUE'),
('VOLUME_GROUP', 'PENDING_ACTIVATION', 'CREATE', 'FAIL'),
('VF_MODULE', 'PENDING_ACTIVATION', 'CREATE', 'SILENT_SUCCESS'),
('NETWORK', 'PENDING_ACTIVATION', 'CREATE', 'FAIL'),
('VOLUME_GROUP', 'PENDING', 'CREATE', 'CONTINUE'),
('VF_MODULE', 'PENDING', 'CREATE', 'FAIL'),
('NETWORK', 'PENDING', 'CREATE', 'FAIL'),
('VOLUME_GROUP', 'PRECREATED', 'DELETE', 'FAIL'),
('VF_MODULE', 'PRECREATED', 'DELETE', 'FAIL'),
('NETWORK', 'PRECREATED', 'DELETE', 'FAIL'),
('VOLUME_GROUP', 'INVENTORIED', 'DELETE', 'FAIL'),
('VF_MODULE', 'INVENTORIED', 'DELETE', 'SILENT_SUCCESS'),
('NETWORK', 'INVENTORIED', 'DELETE', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'ASSIGNED', 'DELETE', 'SILENT_SUCCESS'),
('VF_MODULE', 'ASSIGNED', 'DELETE', 'SILENT_SUCCESS'),
('NETWORK', 'ASSIGNED', 'DELETE', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'CREATED', 'DELETE', 'CONTINUE'),
('VF_MODULE', 'CREATED', 'DELETE', 'CONTINUE'),
('NETWORK', 'CREATED', 'DELETE', 'CONTINUE'),
('VOLUME_GROUP', 'ACTIVE', 'DELETE', 'FAIL'),
('VF_MODULE', 'ACTIVE', 'DELETE', 'FAIL'),
('NETWORK', 'ACTIVE', 'DELETE', 'FAIL'),
('VOLUME_GROUP', 'PENDING_CREATE', 'DELETE', 'FAIL'),
('VF_MODULE', 'PENDING_CREATE', 'DELETE', 'SILENT_SUCCESS'),
('NETWORK', 'PENDING_CREATE', 'DELETE', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'PENDING_DELETE', 'DELETE', 'FAIL'),
('VF_MODULE', 'PENDING_DELETE', 'DELETE', 'SILENT_SUCCESS'),
('NETWORK', 'PENDING_DELETE', 'DELETE', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'PENDING_ACTIVATION', 'DELETE', 'FAIL'),
('VF_MODULE', 'PENDING_ACTIVATION', 'DELETE', 'SILENT_SUCCESS'),
('NETWORK', 'PENDING_ACTIVATION', 'DELETE', 'FAIL'),
('VOLUME_GROUP', 'PENDING', 'DELETE', 'SILENT_SUCCESS'),
('VF_MODULE', 'PENDING', 'DELETE', 'FAIL'),
('NETWORK', 'PENDING', 'DELETE', 'FAIL'),
('NO_VALIDATE', 'ACTIVE', 'CUSTOM', 'CONTINUE'),
('NO_VALIDATE', 'ASSIGNED', 'CUSTOM', 'CONTINUE'),
('NO_VALIDATE', 'CREATED', 'CUSTOM', 'CONTINUE'),
('NO_VALIDATE', 'INVENTORIED', 'CUSTOM', 'CONTINUE'),
('NO_VALIDATE', 'PENDING', 'CUSTOM', 'CONTINUE'),
('NO_VALIDATE', 'PENDING_ACTIVATION', 'CUSTOM', 'CONTINUE'),
('NO_VALIDATE', 'PENDING_CREATE', 'CUSTOM', 'CONTINUE'),
('NO_VALIDATE', 'PENDING_DELETE', 'CUSTOM', 'CONTINUE'),
('NO_VALIDATE', 'PRECREATED', 'CUSTOM', 'CONTINUE');

INSERT INTO `cloudify_managers` (`ID`, `CLOUDIFY_URL`, `USERNAME`, `PASSWORD`, `VERSION`, `LAST_UPDATED_BY`, `CREATION_TIMESTAMP`, `UPDATE_TIMESTAMP`) VALUES ('mtn13', 'http://localhost:28090/v2.0', 'm93945', '93937EA01B94A10A49279D4572B48369', NULL, 'MSO_USER', '2018-07-17 14:05:08', '2018-07-17 14:05:08');

INSERT INTO `identity_services` (`ID`, `IDENTITY_URL`, `MSO_ID`, `MSO_PASS`, `PROJECT_DOMAIN_NAME`, `USER_DOMAIN_NAME`, `ADMIN_TENANT`, `MEMBER_ROLE`, `TENANT_METADATA`, `IDENTITY_SERVER_TYPE`, `IDENTITY_AUTHENTICATION_TYPE`, `LAST_UPDATED_BY`, `CREATION_TIMESTAMP`, `UPDATE_TIMESTAMP`) VALUES ('MTN13', 'http://localhost:28090/v2.0', 'm93945', '93937EA01B94A10A49279D4572B48369', NULL, NULL, 'admin', 'admin', 1, 'KEYSTONE', 'USERNAME_PASSWORD', 'MSO_USER', '2018-07-17 14:02:33', '2018-07-17 14:02:33');

INSERT INTO `cloud_sites` (`ID`, `REGION_ID`, `IDENTITY_SERVICE_ID`, `CLOUD_VERSION`, `CLLI`, `CLOUDIFY_ID`, `PLATFORM`, `ORCHESTRATOR`, `LAST_UPDATED_BY`, `CREATION_TIMESTAMP`, `UPDATE_TIMESTAMP`, `SUPPORT_FABRIC`,`CLOUD_OWNER`) VALUES ('mtn13', 'mtn13', 'MTN13', '2.5', 'MDT13', 'mtn13', NULL, 'orchestrator', 'MSO_USER', '2018-07-17 14:06:28', '2018-07-17 14:06:28', 1,'cloudOwner');

INSERT INTO `controller_selection_reference` (`VNF_TYPE`, `CONTROLLER_NAME`, `ACTION_CATEGORY`) VALUES
('vLoadBalancerMS/vLoadBalancerMS 0', 'APPC', 'ConfigScaleOut'),
('vLoadBalancerMS/vLoadBalancerMS 0', 'APPC', 'HealthCheck');

INSERT INTO `configuration`
            (`model_uuid`,
             `model_invariant_uuid`,
             `model_version`,
             `model_name`,
             `tosca_node_type`,
             `description`,
             `creation_timestamp`)
VALUES      ( 'c59a41ca-9b3b-11e8-98d0-529269fb1459',
              '15881e64-9b3c-11e8-98d0-529269fb1459',
              'testModelVersion',
              'testModelName',
              'testToscaModelType',
              'testConfigurationDescription',
              '2018-07-17 14:05:08' );


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
             `vnfc_cust_model_customization_uuid`)
VALUES      ( '1',
              '9bcce658-9b37-11e8-98d0-529269fb1459',
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
              '9bcce658-9b37-11e8-98d0-529269fb1459');

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
insert into workflow(artifact_uuid, artifact_name, name, operation_name, version, description, body, resource_target, source) values
('b2fd5627-55e4-4f4f-8064-9e6f443e9152','DummyPnfWorkflow','Dummy Pnf Workflow','DummyPnfWorkflow',1.0,'Dummy Pnf Workflow to test custom Pnf workflow',null,'pnf','native');

insert into vnf_resource_to_workflow(vnf_resource_model_uuid, workflow_id) values
('ff2ae348-214a-11e7-93ae-92361f002671', '1');

Insert into pnf_resource_to_workflow (`PNF_RESOURCE_MODEL_UUID`,`WORKFLOW_ID`) values
("ff2ae348-214a-11e7-93ae-92361f002680", 2);

insert into activity_spec(name, description, version) values
('testActivity1', 'Test Activity 1', 1);

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

INSERT INTO bbname_selection_reference (CONTROLLER_ACTOR,SCOPE,ACTION,BB_NAME)
VALUES
('APPC', 'vfModule', 'healthCheck','GenericVnfHealthCheckBB'),
('APPC', 'vfModule', 'configScaleOut','ConfigurationScaleOutBB'),
('APPC', 'vnf', 'healthCheck','GenericVnfHealthCheckBB');