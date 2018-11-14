use requestdb;

ALTER TABLE infra_active_requests 
  MODIFY IF EXISTS AIC_CLOUD_REGION varchar(50) NULL,
  MODIFY IF EXISTS AIC_NODE_CLLI varchar(50) NULL;
  
ALTER TABLE archived_infra_requests 
  MODIFY IF EXISTS AIC_CLOUD_REGION varchar(50) NULL,
  MODIFY IF EXISTS AIC_NODE_CLLI varchar(50) NULL;


