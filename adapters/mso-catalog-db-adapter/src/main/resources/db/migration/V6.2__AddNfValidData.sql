USE catalogdb;

ALTER TABLE vnf_resource_customization
ADD IF NOT EXISTS NF_DATA_VALID tinyint(1) DEFAULT 0;

