use catalogdb;

ALTER TABLE vnf_resource_customization
ADD SKIP_POST_INSTANTIATION_CONFIGURATION boolean default true;

ALTER TABLE pnf_resource_customization
ADD SKIP_POST_INSTANTIATION_CONFIGURATION boolean default true;

