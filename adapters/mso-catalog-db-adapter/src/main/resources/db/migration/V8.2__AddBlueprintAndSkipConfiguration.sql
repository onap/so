use catalogdb;

ALTER TABLE service
ADD CDS_BLUEPRINT_NAME varchar(200) null;

ALTER TABLE service
ADD CDS_BLUEPRINT_VERSION varchar(20) null;

ALTER TABLE service
ADD SKIP_POST_INSTANTIATION_CONFIGURATION boolean default true;

ALTER TABLE vf_module_customization
ADD SKIP_POST_INSTANTIATION_CONFIGURATION boolean default true;

