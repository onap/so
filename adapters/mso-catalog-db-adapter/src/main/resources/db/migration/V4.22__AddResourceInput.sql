use catalogdb;

ALTER TABLE service
ADD RESOURCE_ORDER varchar(255);

ALTER TABLE vnf_resource_customization
ADD RESOURCE_INPUT varchar(2000);

ALTER TABLE network_resource_customization
ADD RESOURCE_INPUT varchar(2000);

ALTER TABLE allotted_resource_customization
ADD RESOURCE_INPUT varchar(2000);