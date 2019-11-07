use catalogdb;

ALTER TABLE service
ADD CONTROLLER_ACTOR varchar(200) null;

ALTER TABLE vnf_resource_customization
ADD CONTROLLER_ACTOR varchar(200) null;

ALTER TABLE pnf_resource_customization
ADD CONTROLLER_ACTOR varchar(200) null;


