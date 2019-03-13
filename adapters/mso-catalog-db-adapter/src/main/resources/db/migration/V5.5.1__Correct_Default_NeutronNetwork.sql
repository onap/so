USE catalogdb;
DELETE FROM temp_network_heat_template_lookup WHERE HEAT_TEMPLATE_ARTIFACT_UUID = 'efee1d84-b8ec-11e7-abc4-cec278b6b50a';
INSERT INTO `heat_template` (`ARTIFACT_UUID`,`NAME`,`VERSION`,`BODY`,`TIMEOUT_MINUTES`,`DESCRIPTION`,`CREATION_TIMESTAMP`,`ARTIFACT_CHECKSUM`) VALUES
('efee1d84-b8ec-11e7-abc4-cec278b6b50a','Generic NeutronNet','1','
heat_template_version: 2013-05-23
description: A simple Neutron network
parameters:
  network_name:
    type: string
    description: Name of the Neutron Network
    default: ONAP-NW1
  shared:
  type: boolean
  description: Shared amongst tenants
  default: False
outputs:
  network_id:
    description: Openstack network identifier
    value: { get_resource: network }
resources:
  network:
    type: OS::Neutron::Net
    properties:
      name: { get_param: network_name }
      shared: { get_param: shared }',10,'Generic Neutron Template','2017-10-26 14:44:00', 'MANUAL RECORD');



INSERT INTO `temp_network_heat_template_lookup` (`NETWORK_RESOURCE_MODEL_NAME`, `HEAT_TEMPLATE_ARTIFACT_UUID`,`AIC_VERSION_MIN` , `AIC_VERSION_MAX` ) 
VALUES ('Generic NeutronNet','efee1d84-b8ec-11e7-abc4-cec278b6b50a','2.0','3.0');

