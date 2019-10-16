use catalogdb;

UPDATE cloud_sites SET SUPPORT_FABRIC = b'0' WHERE CLOUD_VERSION != '1.0.0';