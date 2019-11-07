use catalogdb;

ALTER TABLE service
ADD CONTROLLER_ACTOR varchar(200) null default "SO-REF-DATA";

ALTER TABLE vnf_resource_customization
ADD CONTROLLER_ACTOR varchar(200) null default "SO-REF-DATA";

ALTER TABLE pnf_resource_customization
ADD CONTROLLER_ACTOR varchar(200) null default "SO-REF-DATA";


