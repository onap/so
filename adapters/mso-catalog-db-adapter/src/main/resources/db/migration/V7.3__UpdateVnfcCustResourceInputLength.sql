use catalogdb;

ALTER TABLE vnfc_customization
  MODIFY IF EXISTS RESOURCE_INPUT varchar(20000);