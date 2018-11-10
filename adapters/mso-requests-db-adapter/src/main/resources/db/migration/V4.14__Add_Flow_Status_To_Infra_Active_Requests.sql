use requestdb;

ALTER TABLE infra_active_requests ADD FLOW_STATUS LONGTEXT; 
ALTER TABLE archived_infra_requests ADD FLOW_STATUS LONGTEXT; 

ALTER TABLE infra_active_requests ADD RETRY_STATUS_MESSAGE LONGTEXT; 
ALTER TABLE archived_infra_requests ADD RETRY_STATUS_MESSAGE LONGTEXT; 