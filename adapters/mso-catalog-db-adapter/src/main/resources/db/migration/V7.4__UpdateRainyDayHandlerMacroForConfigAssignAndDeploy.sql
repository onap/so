use catalogdb;

delete from rainy_day_handler_macro where FLOW_NAME = 'ConfigAssignVnfBB';
delete from rainy_day_handler_macro where FLOW_NAME = 'ConfigDeployVnfBB';

insert into rainy_day_handler_macro (FLOW_NAME, SERVICE_TYPE, VNF_TYPE, ERROR_CODE, WORK_STEP, POLICY) values
('ConfigAssignVnfBB', '*', '*', '*', '*', 'Rollback'),
('ConfigDeployVnfBB', '*', '*', '*', '*', 'Rollback');
