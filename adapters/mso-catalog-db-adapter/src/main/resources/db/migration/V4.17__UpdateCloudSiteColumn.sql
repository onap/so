use catalogdb;

ALTER TABLE cloud_sites
 MODIFY IF EXISTS REGION_ID varchar(50) NULL,
 MODIFY IF EXISTS CLLI varchar(50) NULL;