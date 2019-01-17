use catalogdb;

ALTER TABLE vnf_resource_customization
  MODIFY IF EXISTS RESOURCE_INPUT varchar(20000);

ALTER TABLE network_resource_customization
  MODIFY IF EXISTS RESOURCE_INPUT varchar(20000);

ALTER TABLE allotted_resource_customization
  MODIFY IF EXISTS RESOURCE_INPUT varchar(20000);