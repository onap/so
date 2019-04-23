use catalogdb;

ALTER TABLE vnf_resource_customization
ADD VNFCINSTANCEGROUP_ORDER varchar(255);

ALTER TABLE vnfc_customization
ADD RESOURCE_INPUT varchar(2000);