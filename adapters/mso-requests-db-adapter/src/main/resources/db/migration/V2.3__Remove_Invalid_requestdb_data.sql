USE requestdb;

delete from infra_active_requests where source != 'VID' and source != 'POLO';

delete from infra_active_requests where request_body like '<%';
