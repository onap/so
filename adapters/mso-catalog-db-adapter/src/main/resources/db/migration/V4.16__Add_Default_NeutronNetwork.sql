USE catalogdb;

INSERT INTO `heat_template` (`ARTIFACT_UUID`,`NAME`,`VERSION`,`BODY`,`TIMEOUT_MINUTES`,`DESCRIPTION`,`CREATION_TIMESTAMP`,`ARTIFACT_CHECKSUM`) VALUES
('efee1d84-b8ec-11e7-abc4-cec278b6b50a','Generic NeutronNet','1','heat_template_version: 2013-05-23\n\ndescription:\n HOT template that creates a Generic Neutron Network\n\nparameters:\n network_name:\n type: string\n description: Name of direct network (e.g. core, dmz)\n default: ECOMPNetwork\n network_subnet_name:\n type: string\n description: Name of subnet network (e.g. core, dmz)\n default: ECOMPNetwork\n network_subnet_cidr:\n type: string\n description: CIDR of subnet network (e.g. core, dmz)\n default: 10.0.0.0/16\n\noutputs:\n network_id:\n description: Openstack network identifier\n value: { get_resource: network }\n network_fqdn:\n description: Openstack network identifier\n value: {list_join: [\':\', { get_attr: [network, fq_name] } ] }\n\nresources:\n network:\n type: OS::Neutron::Net\n properties:\n name: {get_param: network_name }\n\n subnet:\n type: OS::Neutron::Subnet\n properties:\n name: { get_param: network_subnet_name }\n network_id: { get_resource: network }\n cidr: { get_param: network_subnet_cidr }\n enable_dhcp: false\n',10,'Generic Neutron Template','2017-10-26 14:44:00', 'MANUAL RECORD');



INSERT INTO `temp_network_heat_template_lookup` (`NETWORK_RESOURCE_MODEL_NAME`, `HEAT_TEMPLATE_ARTIFACT_UUID`,`AIC_VERSION_MIN` , `AIC_VERSION_MAX` ) 
VALUES ('Generic NeutronNet','efee1d84-b8ec-11e7-abc4-cec278b6b50a','2.0','3.0');

