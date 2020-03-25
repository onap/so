USE `requestdb`;

UPDATE infra_active_requests SET request_status='COMPLETE' where request_status = 'COMPLETED';
UPDATE archived_infra_requests SET request_status='COMPLETE' where request_status = 'COMPLETED';