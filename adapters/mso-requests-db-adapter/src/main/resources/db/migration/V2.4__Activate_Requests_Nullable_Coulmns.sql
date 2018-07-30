USE requestdb;

ALTER TABLE active_requests 
  MODIFY IF EXISTS SERVICE_INSTANCE_ID varchar(50) NULL,
  MODIFY IF EXISTS REQUEST_ACTION varchar(45) NULL,
  MODIFY IF EXISTS SERVICE_TYPE varchar(65) NULL;