use catalogdb;

ALTER TABLE vnf_resource_customization
ADD CDS_BLUEPRINT_NAME varchar(200) null;

ALTER TABLE vnf_resource_customization
ADD CDS_BLUEPRINT_VERSION varchar(20) null;

