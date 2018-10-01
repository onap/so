USE catalogdb;

update building_block_detail set resource_type='CUSTOM', target_action='CUSTOM' where BUILDING_BLOCK_NAME = 'ConfigurationScaleOutBB';

update building_block_detail set resource_type='CUSTOM', target_action='CUSTOM' where BUILDING_BLOCK_NAME = 'GenericVnfHealthCheckBB';

update northbound_request_ref_lookup set ACTION='scaleOut' where MACRO_ACTION='VFModule-ScaleOut';
