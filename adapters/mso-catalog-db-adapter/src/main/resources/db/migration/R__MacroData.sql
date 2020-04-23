use catalogdb;

DELETE FROM northbound_request_ref_lookup;
DELETE FROM orchestration_flow_reference;
DELETE FROM rainy_day_handler_macro;
DELETE FROM building_block_detail;
DELETE FROM orchestration_status_state_transition_directive;


INSERT INTO northbound_request_ref_lookup(MACRO_ACTION, ACTION, REQUEST_SCOPE, IS_ALACARTE,IS_TOPLEVELFLOW, MIN_API_VERSION, MAX_API_VERSION, CLOUD_OWNER, SERVICE_TYPE) VALUES
('Service-Create', 'createInstance', 'Service', true,true, '7','7', 'DEFAULT', '*'),
('Service-Delete', 'deleteInstance', 'Service', true,true, '7','7', 'DEFAULT', '*'),
('Service-Macro-Assign', 'assignInstance', 'Service', false,true, '7','7', 'DEFAULT', '*'),
('Service-Macro-Activate', 'activateInstance', 'Service', false,true, '7','7', 'DEFAULT', '*'),
('Service-Macro-Unassign', 'unassignInstance', 'Service', false,true, '7','7', 'DEFAULT', '*'),
('Service-Macro-Create', 'createInstance', 'Service', false,true, '7','7', 'DEFAULT', '*'),
('Service-Macro-Delete', 'deleteInstance', 'Service', false,true, '7','7', 'DEFAULT', '*'),
('Network-Create', 'createInstance', 'Network', true,true, '7','7', 'DEFAULT', '*'),
('Network-Delete', 'deleteInstance', 'Network', true,true, '7','7', 'DEFAULT', '*'),
('VNF-Macro-Recreate', 'recreateInstance', 'Vnf', false,true, '7','7', 'DEFAULT', '*'),
('VNF-Macro-Replace', 'replaceInstance', 'Vnf', false,true, '7','7', 'DEFAULT', '*'),
('VNF-Create', 'createInstance', 'Vnf', true,true, '7', '7', 'DEFAULT', '*'),
('VNF-Delete', 'deleteInstance', 'Vnf', true,true, '7', '7', 'DEFAULT', '*'),
('VolumeGroup-Create', 'createInstance', 'VolumeGroup', true,true, '7','7', 'DEFAULT', '*'),
('VolumeGroup-Delete', 'deleteInstance', 'VolumeGroup', true,true, '7','7', 'DEFAULT', '*'),
('VFModule-Create', 'createInstance', 'VfModule', true,true, '7','7', 'DEFAULT', '*'),
('VFModule-Delete', 'deleteInstance', 'VfModule', true,true, '7','7', 'DEFAULT', '*'),
('VFModule-Replace', 'replaceInstance', 'VfModule', true,true, '7','7', 'DEFAULT', '*'),
('VFModule-Replace-Retain-Assignments', 'replaceInstanceRetainAssignments', 'VfModule', true,true, '7','7', 'DEFAULT', '*'),
('NetworkCollection-Macro-Create', 'createInstance', 'NetworkCollection', false,true, '7','7', 'DEFAULT', '*'),
('NetworkCollection-Macro-Delete', 'deleteInstance', 'NetworkCollection', false,true, '7','7', 'DEFAULT', '*'),
('VFModule-ScaleOut', 'scaleOut', 'VfModule', true, true, '7','7', 'DEFAULT', '*'),
('VNF-InPlaceUpdate', 'inPlaceSoftwareUpdate', 'Vnf', true, true, '7','7', 'DEFAULT', '*');


INSERT INTO orchestration_flow_reference(COMPOSITE_ACTION, SEQ_NO, FLOW_NAME, FLOW_VERSION, NB_REQ_REF_LOOKUP_ID) VALUES
('Service-Create', '1', 'AssignServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Create', '2', 'ActivateServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Delete', '1', 'DeactivateServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Delete' and CLOUD_OWNER = 'DEFAULT')),
('Service-Delete', '2', 'UnassignServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Delete' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Assign', '1', 'AssignServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Assign' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Assign', '2', 'AssignNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Assign' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Assign', '3', 'AssignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Assign' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Assign', '4', 'AssignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Assign' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Assign', '5', 'AssignVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Assign' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Assign', '6', 'ConfigAssignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Assign' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Activate', '1', 'CreateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Activate', '2', 'ActivateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Activate', '3', 'CreateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Activate', '4', 'ActivateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Activate', '5', 'CreateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Activate', '6', 'ActivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Activate', '7', 'ConfigDeployVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Activate', '8', 'ActivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Activate', '9', 'ActivateServiceInstance', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Activate' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Unassign', '1', 'UnassignVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Unassign' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Unassign', '2', 'UnassignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Unassign' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Unassign', '3', 'UnassignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Unassign' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Unassign', '4', 'UnassignNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Unassign' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Unassign', '5', 'UnassignServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Unassign' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Create', '1', 'AssignServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Create', '2', 'CreateNetworkCollectionBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Create', '3', 'AssignNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Create', '4', 'AssignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Create', '5', 'AssignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Create', '6', 'AssignVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Create', '7', 'ConfigAssignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Create', '8', 'AssignPnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Create', '9', 'WaitForPnfReadyBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Create', '10', 'ActivatePnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Create', '11', 'CreateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Create', '12', 'ActivateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Create', '13', 'CreateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Create', '14', 'ActivateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Create', '15', 'CreateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Create', '16', 'ActivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Create', '17', 'ConfigDeployVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Create', '18', 'ActivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Create', '19', 'ActivateNetworkCollectionBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Create', '20', 'ActivateServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Delete', '1', 'DeactivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Delete', '2', 'DeleteVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Delete', '3', 'DeactivateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Delete', '4', 'DeleteVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Delete', '5', 'DeactivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Delete', '6', 'DeactivateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Delete', '7', 'DeleteNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Delete', '8', 'DeactivateNetworkCollectionBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Delete', '9', 'DeleteNetworkCollectionBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Delete', '10', 'DeactivateServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Delete', '11', 'UnassignVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Delete', '12', 'UnassignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Delete', '13', 'UnassignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Delete', '14', 'UnassignNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete' and CLOUD_OWNER = 'DEFAULT')),
('Service-Macro-Delete', '15', 'UnassignServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Delete' and CLOUD_OWNER = 'DEFAULT')),
('Network-Create', '1', 'AssignNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Network-Create' and CLOUD_OWNER = 'DEFAULT')),
('Network-Create', '2', 'CreateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Network-Create' and CLOUD_OWNER = 'DEFAULT')),
('Network-Create', '3', 'ActivateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Network-Create' and CLOUD_OWNER = 'DEFAULT')),
('Network-Delete', '1', 'DeactivateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Network-Delete' and CLOUD_OWNER = 'DEFAULT')),
('Network-Delete', '2', 'DeleteNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Network-Delete' and CLOUD_OWNER = 'DEFAULT')),
('Network-Delete', '3', 'UnassignNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Network-Delete' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Create', '1', 'AssignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Create' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Create', '2', 'ActivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Create' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Delete', '1', 'DeactivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Delete' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Delete', '2', 'UnassignVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Delete' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Recreate', '1', 'AAICheckVnfInMaintBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Recreate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Recreate', '2', 'AAISetVnfInMaintBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Recreate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Recreate', '3', 'DeactivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Recreate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Recreate', '4', 'DeleteVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Recreate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Recreate', '5', 'DeactivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Recreate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Recreate', '6', 'CreateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Recreate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Recreate', '7', 'ActivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Recreate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Recreate', '8', 'ActivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Recreate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Recreate', '9', 'SDNOVnfHealthCheckBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Recreate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Recreate', '10', 'AAIUnsetVnfInMaintBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Recreate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Replace', '1', 'AAICheckVnfInMaintBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Replace', '2', 'AAISetVnfInMaintBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Replace', '5', 'DeactivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Replace', '6', 'DeleteVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Replace', '7', 'DeactivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Replace', '8', 'ChangeModelVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Replace', '9', 'CreateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Replace', '10', 'ActivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Replace', '13', 'ChangeModelVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Replace', '14', 'ActivateVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Replace', '15', 'ChangeModelServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Replace', '16', 'SDNOVnfHealthCheckBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VNF-Macro-Replace', '17', 'AAIUnsetVnfInMaintBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-Macro-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VolumeGroup-Create', '1', 'AssignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VolumeGroup-Create' and CLOUD_OWNER = 'DEFAULT')),
('VolumeGroup-Create', '2', 'CreateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VolumeGroup-Create' and CLOUD_OWNER = 'DEFAULT')),
('VolumeGroup-Create', '3', 'ActivateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VolumeGroup-Create' and CLOUD_OWNER = 'DEFAULT')),
('VolumeGroup-Delete', '1', 'DeactivateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VolumeGroup-Delete' and CLOUD_OWNER = 'DEFAULT')),
('VolumeGroup-Delete', '2', 'DeleteVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VolumeGroup-Delete' and CLOUD_OWNER = 'DEFAULT')),
('VolumeGroup-Delete', '3', 'UnassignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VolumeGroup-Delete' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Create', '1', 'AssignVfModuleBB',  1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Create' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Create', '2', 'CreateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Create' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Create', '3', 'ActivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Create' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Delete', '3', 'DeactivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Delete' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Delete', '4', 'DeleteVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Delete' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Delete', '5', 'UnassignVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Delete' and CLOUD_OWNER = 'DEFAULT')),
('NetworkCollection-Macro-Create', '1', 'CreateNetworkCollectionBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('NetworkCollection-Macro-Create', '2', 'AssignNetworkBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('NetworkCollection-Macro-Create', '3', 'CreateNetworkBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('NetworkCollection-Macro-Create', '4', 'ActivateNetworkBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('NetworkCollection-Macro-Create', '5', 'ActivateNetworkCollectionBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Create' and CLOUD_OWNER = 'DEFAULT')),
('NetworkCollection-Macro-Delete', '1', 'DeactivateNetworkBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Delete' and CLOUD_OWNER = 'DEFAULT')),
('NetworkCollection-Macro-Delete', '2', 'DeleteNetworkBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Delete' and CLOUD_OWNER = 'DEFAULT')),
('NetworkCollection-Macro-Delete', '3', 'UnassignNetworkBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Delete' and CLOUD_OWNER = 'DEFAULT')),
('NetworkCollection-Macro-Delete', '4', 'DeactivateNetworkCollectionBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Delete' and CLOUD_OWNER = 'DEFAULT')),
('NetworkCollection-Macro-Delete', '5', 'DeleteNetworkCollectionBB',1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'NetworkCollection-Macro-Delete' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-ScaleOut', '1', 'GenericVnfHealthCheckBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-ScaleOut' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-ScaleOut', '2', 'AssignVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-ScaleOut' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-ScaleOut', '3', 'CreateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-ScaleOut' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-ScaleOut', '4', 'ActivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-ScaleOut' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-ScaleOut', '5', 'ConfigurationScaleOutBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-ScaleOut' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-ScaleOut', '6', 'GenericVnfHealthCheckBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-ScaleOut' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace', '1', 'DeactivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace', '2', 'DeleteVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace', '3', 'DeactivateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace', '4', 'DeleteVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace', '5', 'UnassignVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace', '6', 'UnassignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace', '7', 'AssignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace', '8', 'AssignVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace', '9', 'CreateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace', '10', 'ActivateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace', '11', 'CreateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace', '12', 'ActivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace', '13', 'ChangeModelVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace', '14', 'ChangeModelServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace-Retain-Assignments', '1', 'DeactivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace-Retain-Assignments' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace-Retain-Assignments', '2', 'DeleteVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace-Retain-Assignments' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace-Retain-Assignments', '3', 'DeactivateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace-Retain-Assignments' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace-Retain-Assignments', '4', 'DeleteVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace-Retain-Assignments' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace-Retain-Assignments', '5', 'UnassignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace-Retain-Assignments' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace-Retain-Assignments', '6', 'AssignVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace-Retain-Assignments' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace-Retain-Assignments', '7', 'ChangeModelVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace-Retain-Assignments' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace-Retain-Assignments', '8', 'CreateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace-Retain-Assignments' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace-Retain-Assignments', '9', 'ActivateVolumeGroupBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace-Retain-Assignments' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace-Retain-Assignments', '10', 'CreateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace-Retain-Assignments' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace-Retain-Assignments', '11', 'ActivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace-Retain-Assignments' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace-Retain-Assignments', '12', 'ChangeModelVnfBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace-Retain-Assignments' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-Replace-Retain-Assignments', '13', 'ChangeModelServiceInstanceBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-Replace-Retain-Assignments' and CLOUD_OWNER = 'DEFAULT')),
('VNF-InPlaceUpdate', '1', 'VNFCheckPserversLockedFlagActivity', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-InPlaceUpdate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-InPlaceUpdate', '2', 'VNFCheckInMaintFlagActivity', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-InPlaceUpdate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-InPlaceUpdate', '3', 'VNFSetInMaintFlagActivity', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-InPlaceUpdate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-InPlaceUpdate', '4', 'VNFCheckClosedLoopDisabledFlagActivity', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-InPlaceUpdate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-InPlaceUpdate', '5', 'VNFSetClosedLoopDisabledFlagActivity', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-InPlaceUpdate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-InPlaceUpdate', '6', 'VNFLockActivity', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-InPlaceUpdate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-InPlaceUpdate', '7', 'VNFUpgradePreCheckActivity', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-InPlaceUpdate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-InPlaceUpdate', '8', 'VNFQuiesceTrafficActivity', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-InPlaceUpdate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-InPlaceUpdate', '9', 'VNFStopActivity', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-InPlaceUpdate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-InPlaceUpdate', '10', 'VNFSnapShotActivity', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-InPlaceUpdate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-InPlaceUpdate', '11', 'VNFStartActivity', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-InPlaceUpdate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-InPlaceUpdate', '12', 'VNFUpgradeBackupActivity', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-InPlaceUpdate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-InPlaceUpdate', '13', 'VNFUpgradeSoftwareActivity', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-InPlaceUpdate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-InPlaceUpdate', '14', 'VNFUpgradePostCheckActivity', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-InPlaceUpdate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-InPlaceUpdate', '15', 'VNFResumeTrafficActivity', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-InPlaceUpdate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-InPlaceUpdate', '16', 'VNFUnlockActivity', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-InPlaceUpdate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-InPlaceUpdate', '17', 'VNFUnsetInMaintFlagActivity', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-InPlaceUpdate' and CLOUD_OWNER = 'DEFAULT')),
('VNF-InPlaceUpdate', '18', 'VNFUnsetClosedLoopDisabledFlagActivity', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VNF-InPlaceUpdate' and CLOUD_OWNER = 'DEFAULT'));



INSERT INTO rainy_day_handler_macro (FLOW_NAME, SERVICE_TYPE, VNF_TYPE, ERROR_CODE, WORK_STEP, POLICY)
VALUES
('ActivateNetworkBB', '*', '*', '*', '*' , 'Rollback'),
('ActivateNetworkCollectionBB', '*', '*', '*', '*' , 'Rollback'),
('ActivateServiceInstanceBB', '*', '*', '*', '*' , 'Rollback'),
('ActivateVfModuleBB', '*', '*', '*', '*' , 'RollbackToAssigned'),
('ActivateVnfBB', '*', '*', '*', '*' , 'Rollback'),
('ActivateVolumeGroupBB', '*', '*', '*', '*' , 'Rollback'),
('AssignNetworkBB', '*', '*', '*', '*' , 'Rollback'),
('AssignServiceInstanceBB', '*', '*', '*', '*' , 'Rollback'),
('AssignVfModuleBB', '*', '*', '*', '*' , 'Rollback'),
('AssignVnfBB', '*', '*', '*', '*' , 'Rollback'),
('AssignVolumeGroupBB', '*', '*', '*', '*' , 'Rollback'),
('CreateNetworkBB', '*', '*', '*', '*' , 'Retry'),
('CreateNetworkCollectionBB', '*', '*', '*', '*' , 'Retry'),
('CreateVfModuleBB', '*', '*', '*', '*' , 'Retry'),
('CreateVolumeGroupBB', '*', '*', '*', '*' , 'Retry'),
('ChangeModelServiceInstanceBB', '*', '*', '*', '*' , 'Abort'),
('ChangeModelVfModuleBB', '*', '*', '*', '*' , 'Abort'),
('ChangeModelVnfBB', '*', '*', '*', '*' , 'Abort'),
('CreateCustomerBB', '*', '*', '*', '*' , 'Abort'),
('DeactivateNetworkBB', '*', '*', '*', '*' , 'Abort'),
('DeactivateServiceInstanceBB', '*', '*', '*', '*' , 'Abort'),
('DeactivateVfModuleBB', '*', '*', '*', '*' , 'Abort'),
('DeactivateVnfBB', '*', '*', '*', '*' , 'Abort'),
('DeactivateVolumeGroupBB', '*', '*', '*', '*' , 'Abort'),
('DeleteNetworkBB', '*', '*', '*', '*' , 'Retry'),
('DeleteNetworkCollectionBB', '*', '*', '*', '*' , 'Retry'),
('DeleteVfModuleBB', '*', '*', '*', '*' , 'Retry'),
('DeleteVolumeGroupBB', '*', '*', '*', '*' , 'Retry'),
('UnassignNetworkBB', '*', '*', '*', '*' , 'Retry'),
('UnassignServiceInstanceBB', '*', '*', '*', '*' , 'Retry'),
('UnassignVfModuleBB', '*', '*', '*', '*' , 'Retry'),
('UnassignVnfBB', '*', '*', '*', '*' , 'Retry'),
('UnassignVolumeGroupBB', '*', '*', '*', '*' , 'Retry'),
('UpdateNetworkBB', '*', '*', '*', '*' , 'Retry'),
('VnfAdapterBB', '*', '*', '*', '*' , 'Retry'),
('AAICheckVnfInMaintBB', '*', '*', '*', '*' , 'Abort'),
('AAISetVnfInMaintBB', '*', '*', '*', '*' , 'Abort'),
('AAIUnsetVnfInMaintBB', '*', '*', '*', '*' , 'Abort'),
('ConfigDeployVnfBB', '*', '*', '*', '*' , 'Retry');


INSERT INTO building_block_detail (building_block_name, resource_type, target_action)
VALUES
('AssignServiceInstanceBB', 'SERVICE', 'ASSIGN'),
('AssignVnfBB', 'VNF', 'ASSIGN'),
('AssignVolumeGroupBB', 'VOLUME_GROUP', 'ASSIGN'),
('AssignVfModuleBB', 'VF_MODULE', 'ASSIGN'),
('AssignNetworkBB', 'NETWORK', 'ASSIGN'),
('AssignPnfBB', 'NO_VALIDATE', 'ASSIGN'),

('UnassignServiceInstanceBB', 'SERVICE', 'UNASSIGN'),
('UnassignVnfBB', 'VNF', 'UNASSIGN'),
('UnassignVolumeGroup', 'VOLUME_GROUP', 'UNASSIGN'),
('UnassignNetworkBB', 'NETWORK', 'UNASSIGN'),

('ActivateServiceInstanceBB', 'SERVICE', 'ACTIVATE'),
('ActivateVnfBB', 'VNF', 'ACTIVATE'),
('ActivateVolumeGroupBB', 'VOLUME_GROUP', 'ACTIVATE'),
('ActivateVfModuleBB', 'VF_MODULE', 'ACTIVATE'),
('ActivateNetworkBB', 'NETWORK', 'ACTIVATE'),
('ActivateNetworkCollectionBB', 'NETWORK', 'ACTIVATE'),
('ActivatePnfBB', 'NO_VALIDATE', 'ACTIVATE'),

('DeactivateServiceInstanceBB', 'SERVICE', 'DEACTIVATE'),
('DeactivateVnfBB', 'VNF', 'DEACTIVATE'),
('DeactivateVolumeGroupBB', 'VOLUME_GROUP', 'DEACTIVATE'),
('DeactivateVfModuleBB', 'VF_MODULE', 'DEACTIVATE'),
('DeactivateNetworkBB', 'NETWORK', 'DEACTIVATE'),


('ChangeModelServiceInstanceBB', 'SERVICE', 'CHANGEMODEL'),
('ChangeModelVnfBB', 'VNF', 'CHANGEMODEL'),
('ChangeModelVfModuleBB', 'VF_MODULE', 'CHANGEMODEL'),


('CreateVolumeGroupBB', 'VOLUME_GROUP', 'CREATE'),
('CreateVfModuleBB', 'VF_MODULE', 'CREATE'),
('CreateNetworkBB', 'NETWORK', 'CREATE'),
('CreateNetworkCollectionBB', 'NETWORK', 'CREATE'),

('DeleteVolumeGroupBB', 'VOLUME_GROUP', 'DELETE'),
('DeleteVfModuleBB', 'VF_MODULE', 'DELETE'),
('DeleteNetworkBB', 'NETWORK', 'DELETE'),
('DeleteNetworkCollectionBB', 'NETWORK', 'DELETE'),

('ConfigurationScaleOutBB', 'NO_VALIDATE', 'CUSTOM'),
('GenericVnfHealthCheckBB', 'NO_VALIDATE', 'CUSTOM'),

('WaitForPnfReadyBB', 'NO_VALIDATE', 'CUSTOM');

INSERT INTO orchestration_status_state_transition_directive (resource_type, orchestration_status, target_action, flow_directive)
VALUES

('SERVICE', 'PRECREATED', 'ASSIGN', 'CONTINUE'),
('VNF', 'PRECREATED', 'ASSIGN', 'CONTINUE'),
('VOLUME_GROUP', 'PRECREATED', 'ASSIGN', 'CONTINUE'),
('VF_MODULE', 'PRECREATED', 'ASSIGN', 'CONTINUE'),
('NETWORK', 'PRECREATED', 'ASSIGN', 'CONTINUE'),

('SERVICE', 'INVENTORIED', 'ASSIGN', 'CONTINUE'),
('VNF', 'INVENTORIED', 'ASSIGN', 'CONTINUE'),
('VOLUME_GROUP', 'INVENTORIED', 'ASSIGN', 'FAIL'),
('VF_MODULE', 'INVENTORIED', 'ASSIGN', 'CONTINUE'),
('NETWORK', 'INVENTORIED', 'ASSIGN', 'CONTINUE'),

('SERVICE', 'ASSIGNED', 'ASSIGN', 'SILENT_SUCCESS'),
('VNF', 'ASSIGNED', 'ASSIGN', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'ASSIGNED', 'ASSIGN', 'SILENT_SUCCESS'),
('VF_MODULE', 'ASSIGNED', 'ASSIGN', 'SILENT_SUCCESS'),
('NETWORK', 'ASSIGNED', 'ASSIGN', 'SILENT_SUCCESS'),

('SERVICE', 'CREATED', 'ASSIGN', 'SILENT_SUCCESS'),
('VNF', 'CREATED', 'ASSIGN', 'CONTINUE'),
('VOLUME_GROUP', 'CREATED', 'ASSIGN', 'FAIL'),
('VF_MODULE', 'CREATED', 'ASSIGN', 'SILENT_SUCCESS'),
('NETWORK', 'CREATED', 'ASSIGN', 'SILENT_SUCCESS'),

('SERVICE', 'ACTIVE', 'ASSIGN', 'SILENT_SUCCESS'),
('VNF', 'ACTIVE', 'ASSIGN', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'ACTIVE', 'ASSIGN', 'SILENT_SUCCESS'),
('VF_MODULE', 'ACTIVE', 'ASSIGN', 'SILENT_SUCCESS'),
('NETWORK', 'ACTIVE', 'ASSIGN', 'SILENT_SUCCESS'),

('SERVICE', 'PENDING_CREATE', 'ASSIGN', 'FAIL'),
('VNF', 'PENDING_CREATE', 'ASSIGN', 'FAIL'),
('VOLUME_GROUP', 'PENDING_CREATE', 'ASSIGN', 'FAIL'),
('VF_MODULE', 'PENDING_CREATE', 'ASSIGN', 'CONTINUE'),
('NETWORK', 'PENDING_CREATE', 'ASSIGN', 'SILENT_SUCCESS'),

('SERVICE', 'PENDING_DELETE', 'ASSIGN', 'SILENT_SUCCESS'),
('VNF', 'PENDING_DELETE', 'ASSIGN', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'PENDING_DELETE', 'ASSIGN', 'FAIL'),
('VF_MODULE', 'PENDING_DELETE', 'ASSIGN', 'SILENT_SUCCESS'),
('NETWORK', 'PENDING_DELETE', 'ASSIGN', 'SILENT_SUCCESS'),

('SERVICE', 'PENDING_ACTIVATION', 'ASSIGN', 'FAIL'),
('VNF', 'PENDING_ACTIVATION', 'ASSIGN', 'FAIL'),
('VOLUME_GROUP', 'PENDING_ACTIVATION', 'ASSIGN', 'FAIL'),
('VF_MODULE', 'PENDING_ACTIVATION', 'ASSIGN', 'SILENT_SUCCESS'),
('NETWORK', 'PENDING_ACTIVATION', 'ASSIGN', 'FAIL'),

('SERVICE', 'PENDING', 'ASSIGN', 'FAIL'),
('VNF', 'PENDING', 'ASSIGN', 'FAIL'),
('VOLUME_GROUP', 'PENDING', 'ASSIGN', 'SILENT_SUCCESS'),
('VF_MODULE', 'PENDING', 'ASSIGN', 'FAIL'),
('NETWORK', 'PENDING', 'ASSIGN', 'FAIL'),


('SERVICE', 'PRECREATED', 'UNASSIGN', 'SILENT_SUCCESS'),
('VNF', 'PRECREATED', 'UNASSIGN', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'PRECREATED', 'UNASSIGN', 'SILENT_SUCCESS'),
('VF_MODULE', 'PRECREATED', 'UNASSIGN', 'SILENT_SUCCESS'),
('NETWORK', 'PRECREATED', 'UNASSIGN', 'SILENT_SUCCESS'),

('SERVICE', 'INVENTORIED', 'UNASSIGN', 'CONTINUE'),
('VNF', 'INVENTORIED', 'UNASSIGN', 'CONTINUE'),
('VOLUME_GROUP', 'INVENTORIED', 'UNASSIGN', 'FAIL'),
('VF_MODULE', 'INVENTORIED', 'UNASSIGN', 'CONTINUE'),
('NETWORK', 'INVENTORIED', 'UNASSIGN', 'CONTINUE'),

('SERVICE', 'ASSIGNED', 'UNASSIGN', 'CONTINUE'),
('VNF', 'ASSIGNED', 'UNASSIGN', 'CONTINUE'),
('VOLUME_GROUP', 'ASSIGNED', 'UNASSIGN', 'CONTINUE'),
('VF_MODULE', 'ASSIGNED', 'UNASSIGN', 'CONTINUE'),
('NETWORK', 'ASSIGNED', 'UNASSIGN', 'CONTINUE'),

('SERVICE', 'CREATED', 'UNASSIGN', 'CONTINUE'),
('VNF', 'CREATED', 'UNASSIGN', 'CONTINUE'),
('VOLUME_GROUP', 'CREATED', 'UNASSIGN', 'FAIL'),
('VF_MODULE', 'CREATED', 'UNASSIGN', 'FAIL'),
('NETWORK', 'CREATED', 'UNASSIGN', 'FAIL'),

('SERVICE', 'ACTIVE', 'UNASSIGN', 'FAIL'),
('VNF', 'ACTIVE', 'UNASSIGN', 'FAIL'),
('VOLUME_GROUP', 'ACTIVE', 'UNASSIGN', 'FAIL'),
('VF_MODULE', 'ACTIVE', 'UNASSIGN', 'FAIL'),
('NETWORK', 'ACTIVE', 'UNASSIGN', 'FAIL'),

('SERVICE', 'PENDING_CREATE', 'UNASSIGN', 'FAIL'),
('VNF', 'PENDING_CREATE', 'UNASSIGN', 'FAIL'),
('VOLUME_GROUP', 'PENDING_CREATE', 'UNASSIGN', 'FAIL'),
('VF_MODULE', 'PENDING_CREATE', 'UNASSIGN', 'CONTINUE'),
('NETWORK', 'PENDING_CREATE', 'UNASSIGN', 'CONTINUE'),

('SERVICE', 'PENDING_DELETE', 'UNASSIGN', 'CONTINUE'),
('VNF', 'PENDING_DELETE', 'UNASSIGN', 'CONTINUE'),
('VOLUME_GROUP', 'PENDING_DELETE', 'UNASSIGN', 'FAIL'),
('VF_MODULE', 'PENDING_DELETE', 'UNASSIGN', 'CONTINUE'),
('NETWORK', 'PENDING_DELETE', 'UNASSIGN', 'CONTINUE'),

('SERVICE', 'PENDING_ACTIVATION', 'UNASSIGN', 'FAIL'),
('VNF', 'PENDING_ACTIVATION', 'UNASSIGN', 'FAIL'),
('VOLUME_GROUP', 'PENDING_ACTIVATION', 'UNASSIGN', 'FAIL'),
('VF_MODULE', 'PENDING_ACTIVATION', 'UNASSIGN', 'CONTINUE'),
('NETWORK', 'PENDING_ACTIVATION', 'UNASSIGN', 'FAIL'),

('SERVICE', 'PENDING', 'UNASSIGN', 'FAIL'),
('VNF', 'PENDING', 'UNASSIGN', 'FAIL'),
('VOLUME_GROUP', 'PENDING', 'UNASSIGN', 'CONTINUE'),
('VF_MODULE', 'PENDING', 'UNASSIGN', 'FAIL'),
('NETWORK', 'PENDING', 'UNASSIGN', 'FAIL'),


('SERVICE', 'PRECREATED', 'ACTIVATE', 'FAIL'),
('VNF', 'PRECREATED', 'ACTIVATE', 'FAIL'),
('VOLUME_GROUP', 'PRECREATED', 'ACTIVATE', 'FAIL'),
('VF_MODULE', 'PRECREATED', 'ACTIVATE', 'FAIL'),
('NETWORK', 'PRECREATED', 'ACTIVATE', 'FAIL'),

('SERVICE', 'INVENTORIED', 'ACTIVATE', 'FAIL'),
('VNF', 'INVENTORIED', 'ACTIVATE', 'FAIL'),
('VOLUME_GROUP', 'INVENTORIED', 'ACTIVATE', 'FAIL'),
('VF_MODULE', 'INVENTORIED', 'ACTIVATE', 'FAIL'),
('NETWORK', 'INVENTORIED', 'ACTIVATE', 'FAIL'),

('SERVICE', 'ASSIGNED', 'ACTIVATE', 'CONTINUE'),
('VNF', 'ASSIGNED', 'ACTIVATE', 'CONTINUE'),
('VOLUME_GROUP', 'ASSIGNED', 'ACTIVATE', 'FAIL'),
('VF_MODULE', 'ASSIGNED', 'ACTIVATE', 'FAIL'),
('NETWORK', 'ASSIGNED', 'ACTIVATE', 'FAIL'),

('SERVICE', 'CREATED', 'ACTIVATE', 'CONTINUE'),
('VNF', 'CREATED', 'ACTIVATE', 'FAIL'),
('VOLUME_GROUP', 'CREATED', 'ACTIVATE', 'CONTINUE'),
('VF_MODULE', 'CREATED', 'ACTIVATE', 'CONTINUE'),
('NETWORK', 'CREATED', 'ACTIVATE', 'CONTINUE'),

('SERVICE', 'ACTIVE', 'ACTIVATE', 'SILENT_SUCCESS'),
('VNF', 'ACTIVE', 'ACTIVATE', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'ACTIVE', 'ACTIVATE', 'SILENT_SUCCESS'),
('VF_MODULE', 'ACTIVE', 'ACTIVATE', 'SILENT_SUCCESS'),
('NETWORK', 'ACTIVE', 'ACTIVATE', 'SILENT_SUCCESS'),

('SERVICE', 'PENDING_CREATE', 'ACTIVATE', 'FAIL'),
('VNF', 'PENDING_CREATE', 'ACTIVATE', 'FAIL'),
('VOLUME_GROUP', 'PENDING_CREATE', 'ACTIVATE', 'FAIL'),
('VF_MODULE', 'PENDING_CREATE', 'ACTIVATE', 'FAIL'),
('NETWORK', 'PENDING_CREATE', 'ACTIVATE', 'FAIL'),

('SERVICE', 'PENDING_DELETE', 'ACTIVATE', 'CONTINUE'),
('VNF', 'PENDING_DELETE', 'ACTIVATE', 'CONTINUE'),
('VOLUME_GROUP', 'PENDING_DELETE', 'ACTIVATE', 'FAIL'),
('VF_MODULE', 'PENDING_DELETE', 'ACTIVATE', 'FAIL'),
('NETWORK', 'PENDING_DELETE', 'ACTIVATE', 'FAIL'),

('SERVICE', 'PENDING_ACTIVATION', 'ACTIVATE', 'FAIL'),
('VNF', 'PENDING_ACTIVATION', 'ACTIVATE', 'FAIL'),
('VOLUME_GROUP', 'PENDING_ACTIVATION', 'ACTIVATE', 'FAIL'),
('VF_MODULE', 'PENDING_ACTIVATION', 'ACTIVATE', 'SILENT_SUCCESS'),
('NETWORK', 'PENDING_ACTIVATION', 'ACTIVATE', 'FAIL'),

('SERVICE', 'PENDING', 'ACTIVATE', 'FAIL'),
('VNF', 'PENDING', 'ACTIVATE', 'FAIL'),
('VOLUME_GROUP', 'PENDING', 'ACTIVATE', 'FAIL'),
('VF_MODULE', 'PENDING', 'ACTIVATE', 'FAIL'),
('NETWORK', 'PENDING', 'ACTIVATE', 'FAIL'),

('SERVICE', 'PRECREATED', 'DEACTIVATE', 'FAIL'),
('VNF', 'PRECREATED', 'DEACTIVATE', 'FAIL'),
('VOLUME_GROUP', 'PRECREATED', 'DEACTIVATE', 'FAIL'),
('VF_MODULE', 'PRECREATED', 'DEACTIVATE', 'FAIL'),
('NETWORK', 'PRECREATED', 'DEACTIVATE', 'FAIL'),

('SERVICE', 'INVENTORIED', 'DEACTIVATE', 'FAIL'),
('VNF', 'INVENTORIED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'INVENTORIED', 'DEACTIVATE', 'FAIL'),
('VF_MODULE', 'INVENTORIED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('NETWORK', 'INVENTORIED', 'DEACTIVATE', 'SILENT_SUCCESS'),

('SERVICE', 'ASSIGNED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('VNF', 'ASSIGNED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'ASSIGNED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('VF_MODULE', 'ASSIGNED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('NETWORK', 'ASSIGNED', 'DEACTIVATE', 'SILENT_SUCCESS'),

('SERVICE', 'CREATED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('VNF', 'CREATED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'CREATED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('VF_MODULE', 'CREATED', 'DEACTIVATE', 'SILENT_SUCCESS'),
('NETWORK', 'CREATED', 'DEACTIVATE', 'SILENT_SUCCESS'),

('SERVICE', 'ACTIVE', 'DEACTIVATE', 'CONTINUE'),
('VNF', 'ACTIVE', 'DEACTIVATE', 'CONTINUE'),
('VOLUME_GROUP', 'ACTIVE', 'DEACTIVATE', 'CONTINUE'),
('VF_MODULE', 'ACTIVE', 'DEACTIVATE', 'CONTINUE'),
('NETWORK', 'ACTIVE', 'DEACTIVATE', 'CONTINUE'),

('SERVICE', 'PENDING_CREATE', 'DEACTIVATE', 'FAIL'),
('VNF', 'PENDING_CREATE', 'DEACTIVATE', 'FAIL'),
('VOLUME_GROUP', 'PENDING_CREATE', 'DEACTIVATE', 'FAIL'),
('VF_MODULE', 'PENDING_CREATE', 'DEACTIVATE', 'SILENT_SUCCESS'),
('NETWORK', 'PENDING_CREATE', 'DEACTIVATE', 'SILENT_SUCCESS'),

('SERVICE', 'PENDING_DELETE', 'DEACTIVATE', 'SILENT_SUCCESS'),
('VNF', 'PENDING_DELETE', 'DEACTIVATE', 'SILENT_SUCCESS'),
('VOLUME_GROUP', 'PENDING_DELETE', 'DEACTIVATE', 'FAIL'),
('VF_MODULE', 'PENDING_DELETE', 'DEACTIVATE', 'SILENT_SUCCESS'),
('NETWORK', 'PENDING_DELETE', 'DEACTIVATE', 'SILENT_SUCCESS'),

('SERVICE', 'PENDING_ACTIVATION', 'DEACTIVATE', 'FAIL'),
('VNF', 'PENDING_ACTIVATION', 'DEACTIVATE', 'FAIL'),
('VOLUME_GROUP', 'PENDING_ACTIVATION', 'DEACTIVATE', 'FAIL'),
('VF_MODULE', 'PENDING_ACTIVATION', 'DEACTIVATE', 'SILENT_SUCCESS'),
('NETWORK', 'PENDING_ACTIVATION', 'DEACTIVATE', 'FAIL'),

('SERVICE', 'PENDING', 'DEACTIVATE', 'FAIL'),
('VNF', 'PENDING', 'DEACTIVATE', 'FAIL'),
('VOLUME_GROUP', 'PENDING', 'DEACTIVATE', 'SILENT_SUCCESS'),
('VF_MODULE', 'PENDING', 'DEACTIVATE', 'FAIL'),
('NETWORK', 'PENDING', 'DEACTIVATE', 'FAIL'),


('SERVICE', 'PRECREATED', 'CHANGE_MODEL', 'FAIL'),
('VNF', 'PRECREATED', 'CHANGE_MODEL', 'FAIL'),
('VF_MODULE', 'PRECREATED', 'CHANGE_MODEL', 'FAIL'),

('SERVICE', 'INVENTORIED', 'CHANGE_MODEL', 'FAIL'),
('VNF', 'INVENTORIED', 'CHANGE_MODEL', 'FAIL'),
('VF_MODULE', 'INVENTORIED', 'CHANGE_MODEL', 'FAIL'),

('SERVICE', 'ASSIGNED', 'CHANGE_MODEL', 'FAIL'),
('VNF', 'ASSIGNED', 'CHANGE_MODEL', 'CONTINUE'),
('VF_MODULE', 'ASSIGNED', 'CHANGE_MODEL', 'CONTINUE'),

('SERVICE', 'CREATED', 'CHANGE_MODEL', 'FAIL'),
('VNF', 'CREATED', 'CHANGE_MODEL', 'FAIL'),
('VF_MODULE', 'CREATED', 'CHANGE_MODEL', 'FAIL'),

('SERVICE', 'ACTIVE', 'CHANGE_MODEL', 'CONTINUE'),
('VNF', 'ACTIVE', 'CHANGE_MODEL', 'CONTINUE'),
('VF_MODULE', 'ACTIVE', 'CHANGE_MODEL', 'CONTINUE'),

('SERVICE', 'PENDING_CREATE', 'CHANGE_MODEL', 'FAIL'),
('VNF', 'PENDING_CREATE', 'CHANGE_MODEL', 'FAIL'),
('VF_MODULE', 'PENDING_CREATE', 'CHANGE_MODEL', 'FAIL'),

('SERVICE', 'PENDING_DELETE', 'CHANGE_MODEL', 'FAIL'),
('VNF', 'PENDING_DELETE', 'CHANGE_MODEL', 'CONTINUE'),
('VF_MODULE', 'PENDING_DELETE', 'CHANGE_MODEL', 'CONTINUE'),

('SERVICE', 'PENDING_ACTIVATION', 'CHANGE_MODEL', 'FAIL'),
('VNF', 'PENDING_ACTIVATION', 'CHANGE_MODEL', 'CONTINUE'),
('VF_MODULE', 'PENDING_ACTIVATION', 'CHANGE_MODEL', 'CONTINUE'),

('SERVICE', 'PENDING', 'CHANGE_MODEL', 'FAIL'),
('VNF', 'PENDING', 'CHANGE_MODEL', 'FAIL'),
('VF_MODULE', 'PENDING', 'CHANGE_MODEL', 'FAIL'),


('VOLUME_GROUP', 'PRECREATED', 'CREATE', 'FAIL'),
('VF_MODULE', 'PRECREATED', 'CREATE', 'FAIL'),
('NETWORK', 'PRECREATED', 'CREATE', 'FAIL'),

('VOLUME_GROUP', 'INVENTORIED', 'CREATE', 'FAIL'),
('VF_MODULE', 'INVENTORIED', 'CREATE', 'FAIL'),
('NETWORK', 'INVENTORIED', 'CREATE', 'FAIL'),

('VOLUME_GROUP', 'ASSIGNED', 'CREATE', 'CONTINUE'),
('VF_MODULE', 'ASSIGNED', 'CREATE', 'CONTINUE'),
('NETWORK', 'ASSIGNED', 'CREATE', 'CONTINUE'),

('VOLUME_GROUP', 'CREATED', 'CREATE', 'SILENT_SUCCESS'),
('VF_MODULE', 'CREATED', 'CREATE', 'SILENT_SUCCESS'),
('NETWORK', 'CREATED', 'CREATE', 'SILENT_SUCCESS'),

('VOLUME_GROUP', 'ACTIVE', 'CREATE', 'SILENT_SUCCESS'),
('VF_MODULE', 'ACTIVE', 'CREATE', 'SILENT_SUCCESS'),
('NETWORK', 'ACTIVE', 'CREATE', 'SILENT_SUCCESS'),

('VOLUME_GROUP', 'PENDING_CREATE', 'CREATE', 'FAIL'),
('VF_MODULE', 'PENDING_CREATE', 'CREATE', 'FAIL'),
('NETWORK', 'PENDING_CREATE', 'CREATE', 'CONTINUE'),

('VOLUME_GROUP', 'PENDING_DELETE', 'CREATE', 'FAIL'),
('VF_MODULE', 'PENDING_DELETE', 'CREATE', 'CONTINUE'),
('NETWORK', 'PENDING_DELETE', 'CREATE', 'CONTINUE'),

('VOLUME_GROUP', 'PENDING_ACTIVATION', 'CREATE', 'FAIL'),
('VF_MODULE', 'PENDING_ACTIVATION', 'CREATE', 'SILENT_SUCCESS'),
('NETWORK', 'PENDING_ACTIVATION', 'CREATE', 'FAIL'),

('VOLUME_GROUP', 'PENDING', 'CREATE', 'CONTINUE'),
('VF_MODULE', 'PENDING', 'CREATE', 'FAIL'),
('NETWORK', 'PENDING', 'CREATE', 'FAIL'),


('VOLUME_GROUP', 'PRECREATED', 'DELETE', 'FAIL'),
('VF_MODULE', 'PRECREATED', 'DELETE', 'FAIL'),
('NETWORK', 'PRECREATED', 'DELETE', 'FAIL'),

('VOLUME_GROUP', 'INVENTORIED', 'DELETE', 'FAIL'),
('VF_MODULE', 'INVENTORIED', 'DELETE', 'SILENT_SUCCESS'),
('NETWORK', 'INVENTORIED', 'DELETE', 'SILENT_SUCCESS'),

('VOLUME_GROUP', 'ASSIGNED', 'DELETE', 'CONTINUE'),
('VF_MODULE', 'ASSIGNED', 'DELETE', 'CONTINUE'),
('NETWORK', 'ASSIGNED', 'DELETE', 'CONTINUE'),

('VOLUME_GROUP', 'CREATED', 'DELETE', 'CONTINUE'),
('VF_MODULE', 'CREATED', 'DELETE', 'CONTINUE'),
('NETWORK', 'CREATED', 'DELETE', 'CONTINUE'),

('VOLUME_GROUP', 'ACTIVE', 'DELETE', 'FAIL'),
('VF_MODULE', 'ACTIVE', 'DELETE', 'FAIL'),
('NETWORK', 'ACTIVE', 'DELETE', 'FAIL'),

('VOLUME_GROUP', 'PENDING_CREATE', 'DELETE', 'FAIL'),
('VF_MODULE', 'PENDING_CREATE', 'DELETE', 'SILENT_SUCCESS'),
('NETWORK', 'PENDING_CREATE', 'DELETE', 'SILENT_SUCCESS'),

('VOLUME_GROUP', 'PENDING_DELETE', 'DELETE', 'FAIL'),
('VF_MODULE', 'PENDING_DELETE', 'DELETE', 'SILENT_SUCCESS'),
('NETWORK', 'PENDING_DELETE', 'DELETE', 'SILENT_SUCCESS'),

('VOLUME_GROUP', 'PENDING_ACTIVATION', 'DELETE', 'FAIL'),
('VF_MODULE', 'PENDING_ACTIVATION', 'DELETE', 'SILENT_SUCCESS'),
('NETWORK', 'PENDING_ACTIVATION', 'DELETE', 'FAIL'),

('VOLUME_GROUP', 'PENDING', 'DELETE', 'SILENT_SUCCESS'),
('VF_MODULE', 'PENDING', 'DELETE', 'FAIL'),
('NETWORK', 'PENDING', 'DELETE', 'FAIL');

UPDATE orchestration_flow_reference SET FLOW_NAME = 'HomingBB' WHERE FLOW_NAME = 'SniroHoming';

UPDATE orchestration_flow_reference
SET FLOW_NAME = 'ActivateServiceInstanceBB'
WHERE FLOW_NAME = 'ActivateServiceInstance';

DELETE FROM orchestration_flow_reference
WHERE FLOW_NAME = 'DeactivateNetworkCollectionBB';

UPDATE orchestration_flow_reference
SET SEQ_NO = SEQ_NO - 1
WHERE COMPOSITE_ACTION = 'Service-Macro-Delete' AND SEQ_NO > 8;

UPDATE orchestration_flow_reference
SET SEQ_NO = SEQ_NO - 1
WHERE COMPOSITE_ACTION = 'NetworkCollection-Macro-Delete' AND SEQ_NO > 4;

UPDATE building_block_detail
SET BUILDING_BLOCK_NAME = 'UnassignVolumeGroupBB'
WHERE BUILDING_BLOCK_NAME = 'UnassignVolumeGroup';

UPDATE building_block_detail
SET TARGET_ACTION = 'CHANGE_MODEL'
WHERE TARGET_ACTION = 'CHANGEMODEL';

INSERT INTO building_block_detail(BUILDING_BLOCK_NAME, RESOURCE_TYPE, TARGET_ACTION)
VALUES
('UnassignVfModuleBB', 'VF_MODULE', 'UNASSIGN'),
('SniroHoming', 'NO_VALIDATE', 'CUSTOM'),
('DeactivateAndUnassignVpnBondingLinksBB', 'NO_VALIDATE', 'CUSTOM'),
('DeactivateNetworkCollectionBB', 'NO_VALIDATE', 'CUSTOM'),
('AAICheckVnfInMaintBB', 'NO_VALIDATE', 'CUSTOM'),
('AAISetVnfInMaintBB', 'NO_VALIDATE', 'CUSTOM'),
('AAIUnsetVnfInMaintBB', 'NO_VALIDATE', 'CUSTOM'),
('SDNOVnfHealthCheckBB', 'NO_VALIDATE', 'CUSTOM'),
('VNF-Macro-Replace', 'NO_VALIDATE', 'CUSTOM'),
('HomingBB', 'NO_VALIDATE', 'CUSTOM');

INSERT INTO orchestration_status_state_transition_directive(RESOURCE_TYPE, ORCHESTRATION_STATUS, TARGET_ACTION, FLOW_DIRECTIVE)
VALUES
('NO_VALIDATE', 'ACTIVE', 'CUSTOM', 'CONTINUE'),
('NO_VALIDATE', 'ASSIGNED', 'CUSTOM', 'CONTINUE'),
('NO_VALIDATE', 'CREATED', 'CUSTOM', 'CONTINUE'),
('NO_VALIDATE', 'INVENTORIED', 'CUSTOM', 'CONTINUE'),
('NO_VALIDATE', 'PENDING', 'CUSTOM', 'CONTINUE'),
('NO_VALIDATE', 'PENDING_ACTIVATION', 'CUSTOM', 'CONTINUE'),
('NO_VALIDATE', 'PENDING_CREATE', 'CUSTOM', 'CONTINUE'),
('NO_VALIDATE', 'PENDING_DELETE', 'CUSTOM', 'CONTINUE'),
('NO_VALIDATE', 'PRECREATED', 'CUSTOM', 'CONTINUE');

INSERT INTO northbound_request_ref_lookup(MACRO_ACTION, ACTION, REQUEST_SCOPE, IS_ALACARTE, MIN_API_VERSION, MAX_API_VERSION, IS_TOPLEVELFLOW, CLOUD_OWNER, SERVICE_TYPE) VALUES
('VFModule-DeactivateAndCloudDelete', 'deactivateAndCloudDelete', 'VfModule', true, '7','7', true, 'DEFAULT', '*');

INSERT INTO orchestration_flow_reference(COMPOSITE_ACTION, SEQ_NO, FLOW_NAME, FLOW_VERSION, NB_REQ_REF_LOOKUP_ID) VALUES
('VFModule-DeactivateAndCloudDelete', '1', 'DeactivateVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-DeactivateAndCloudDelete' and CLOUD_OWNER = 'DEFAULT')),
('VFModule-DeactivateAndCloudDelete', '2', 'DeleteVfModuleBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'VFModule-DeactivateAndCloudDelete' and CLOUD_OWNER = 'DEFAULT'));

INSERT INTO northbound_request_ref_lookup (REQUEST_SCOPE, MACRO_ACTION, ACTION, IS_ALACARTE, MIN_API_VERSION, MAX_API_VERSION, IS_TOPLEVELFLOW, CLOUD_OWNER, SERVICE_TYPE)
values ( 'Service', 'Service-Macro-Deactivate', 'deactivateInstance', '0', '7', '7', '1', 'DEFAULT', '*');

INSERT INTO orchestration_flow_reference (COMPOSITE_ACTION, SEQ_NO, FLOW_NAME, FLOW_VERSION, NB_REQ_REF_LOOKUP_ID)
values ( 'Service-Macro-Deactivate', '1', 'DeactivateServiceInstanceBB', '1', (SELECT id FROM northbound_request_ref_lookup WHERE MACRO_ACTION = 'Service-Macro-Deactivate' and CLOUD_OWNER = 'DEFAULT'));


UPDATE northbound_request_ref_lookup SET MIN_API_VERSION = 5 WHERE MACRO_ACTION = 'Service-Macro-Create';
UPDATE northbound_request_ref_lookup SET MIN_API_VERSION = 5 WHERE MACRO_ACTION = 'Service-Macro-Delete';
UPDATE northbound_request_ref_lookup SET MIN_API_VERSION = 5 WHERE MACRO_ACTION = 'Service-Macro-Activate';

UPDATE northbound_request_ref_lookup SET MIN_API_VERSION = 5 WHERE MACRO_ACTION = 'Service-Macro-Deactivate';

INSERT INTO northbound_request_ref_lookup(MACRO_ACTION, ACTION, REQUEST_SCOPE, IS_ALACARTE, MIN_API_VERSION, MAX_API_VERSION, IS_TOPLEVELFLOW, CLOUD_OWNER, SERVICE_TYPE) VALUES
('Network-Update', 'updateInstance', 'Network', true, '7','7','1', 'DEFAULT', '*');
INSERT INTO orchestration_flow_reference(COMPOSITE_ACTION, SEQ_NO, FLOW_NAME, FLOW_VERSION, NB_REQ_REF_LOOKUP_ID) VALUES
('Network-Update', '1', 'UpdateNetworkBB', 1.0,(SELECT id from northbound_request_ref_lookup WHERE MACRO_ACTION = 'Network-Update' and CLOUD_OWNER = 'DEFAULT'));

INSERT INTO building_block_detail(BUILDING_BLOCK_NAME,RESOURCE_TYPE,TARGET_ACTION) VALUES
('UpdateNetworkBB','NETWORK','UPDATE');

INSERT INTO orchestration_status_state_transition_directive(RESOURCE_TYPE, ORCHESTRATION_STATUS,TARGET_ACTION,FLOW_DIRECTIVE) VALUES
('NETWORK','PRECREATED','UPDATE','FAIL'),
('NETWORK','INVENTORIED','UPDATE','FAIL'),
('NETWORK','ASSIGNED','UPDATE','FAIL'),
('NETWORK','CREATED','UPDATE','FAIL'),
('NETWORK','PENDING_CREATE','UPDATE','FAIL'),
('NETWORK','PENDING_DELETE','UPDATE','FAIL'),
('NETWORK','PENDING_ACTIVATION','UPDATE','FAIL'),
('NETWORK','ACTIVE','UPDATE','CONTINUE');

UPDATE building_block_detail SET RESOURCE_TYPE = 'NETWORK_COLLECTION' WHERE BUILDING_BLOCK_NAME = 'ActivateNetworkCollectionBB';
UPDATE building_block_detail SET RESOURCE_TYPE = 'NETWORK_COLLECTION' WHERE BUILDING_BLOCK_NAME = 'CreateNetworkCollectionBB';
UPDATE building_block_detail SET RESOURCE_TYPE = 'NETWORK_COLLECTION' WHERE BUILDING_BLOCK_NAME = 'DeleteNetworkCollectionBB';
UPDATE building_block_detail SET RESOURCE_TYPE = 'NETWORK_COLLECTION' WHERE BUILDING_BLOCK_NAME = 'DeactivateNetworkCollectionBB';
UPDATE building_block_detail SET TARGET_ACTION = 'DEACTIVATE' WHERE BUILDING_BLOCK_NAME = 'DeactivateNetworkCollectionBB';

INSERT into orchestration_status_state_transition_directive (RESOURCE_TYPE, ORCHESTRATION_STATUS, TARGET_ACTION, FLOW_DIRECTIVE) values
('NETWORK_COLLECTION', 'PRECREATED', 'CREATE', 'CONTINUE'),
('NETWORK_COLLECTION', 'ASSIGNED', 'CREATE', 'FAIL'),
('NETWORK_COLLECTION', 'CREATED', 'CREATE', 'FAIL'),
('NETWORK_COLLECTION', 'ACTIVE', 'CREATE', 'FAIL'),
('NETWORK_COLLECTION', 'PENDING_CREATE', 'CREATE', 'FAIL'),
('NETWORK_COLLECTION', 'PENDING_DELETE', 'CREATE', 'FAIL'),
('NETWORK_COLLECTION', 'PENDING_ACTIVATION', 'CREATE', 'FAIL'),
('NETWORK_COLLECTION', 'PENDING', 'CREATE', 'FAIL'),
('NETWORK_COLLECTION', 'INVENTORIED', 'CREATE', 'SILENT_SUCCESS'),

('NETWORK_COLLECTION', 'PRECREATED', 'DELETE', 'FAIL'),
('NETWORK_COLLECTION', 'ASSIGNED', 'DELETE', 'FAIL'),
('NETWORK_COLLECTION', 'CREATED', 'DELETE', 'FAIL'),
('NETWORK_COLLECTION', 'ACTIVE', 'DELETE', 'CONTINUE'),
('NETWORK_COLLECTION', 'PENDING_CREATE', 'DELETE', 'FAIL'),
('NETWORK_COLLECTION', 'PENDING_DELETE', 'DELETE', 'FAIL'),
('NETWORK_COLLECTION', 'PENDING_ACTIVATION', 'DELETE', 'FAIL'),
('NETWORK_COLLECTION', 'PENDING', 'DELETE', 'FAIL'),
('NETWORK_COLLECTION', 'INVENTORIED', 'DELETE', 'CONTINUE'),

('NETWORK_COLLECTION', 'PRECREATED', 'ACTIVE', 'FAIL'),
('NETWORK_COLLECTION', 'ASSIGNED', 'ACTIVE', 'FAIL'),
('NETWORK_COLLECTION', 'CREATED', 'ACTIVE', 'FAIL'),
('NETWORK_COLLECTION', 'ACTIVE', 'ACTIVE', 'SILENT_SUCCESS'),
('NETWORK_COLLECTION', 'PENDING_CREATE', 'ACTIVE', 'FAIL'),
('NETWORK_COLLECTION', 'PENDING_DELETE', 'ACTIVE', 'FAIL'),
('NETWORK_COLLECTION', 'PENDING_ACTIVATION', 'ACTIVE', 'FAIL'),
('NETWORK_COLLECTION', 'PENDING', 'ACTIVE', 'FAIL'),
('NETWORK_COLLECTION', 'INVENTORIED', 'ACTIVE', 'CONTINUE');

UPDATE orchestration_status_state_transition_directive SET TARGET_ACTION = 'ACTIVATE' WHERE TARGET_ACTION = 'ACTIVE';

INSERT INTO orchestration_status_state_transition_directive(RESOURCE_TYPE, ORCHESTRATION_STATUS, TARGET_ACTION, FLOW_DIRECTIVE)
VALUES
('CONFIGURATION', 'PRECREATED', 'UNASSIGN', 'SILENT SUCCESS'),
('CONFIGURATION', 'INVENTORIED', 'UNASSIGN', 'CONTINUE'),
('CONFIGURATION', 'ASSIGNED', 'UNASSIGN', 'CONTINUE'),
('CONFIGURATION', 'CREATED', 'UNASSIGN', 'FAIL'),
('CONFIGURATION', 'ACTIVE', 'UNASSIGN', 'FAIL'),
('CONFIGURATION', 'PENDING CREATE', 'UNASSIGN', 'FAIL'),
('CONFIGURATION', 'PENDING DELETE', 'UNASSIGN', 'FAIL'),
('CONFIGURATION', 'PENDING ACTIVATION', 'UNASSIGN', 'FAIL'),
('CONFIGURATION', 'PENDING', 'UNASSIGN', 'FAIL');

UPDATE orchestration_flow_reference
SET SEQ_NO = SEQ_NO + 2 WHERE COMPOSITE_ACTION = 'SERVICE-MACRO-DELETE';


INSERT INTO orchestration_status_state_transition_directive(RESOURCE_TYPE, ORCHESTRATION_STATUS, TARGET_ACTION, FLOW_DIRECTIVE)
VALUES
('CONFIGURATION', 'PRECREATED', 'ASSIGN', 'CONTINUE'),
('CONFIGURATION', 'INVENTORIED', 'ASSIGN', 'CONTINUE'),
('CONFIGURATION', 'ASSIGNED', 'ASSIGN', 'SILENT SUCCESS'),
('CONFIGURATION', 'CREATED', 'ASSIGN', 'SILENT SUCCESS'),
('CONFIGURATION', 'ACTIVE', 'ASSIGN', 'SILENT SUCCESS'),
('CONFIGURATION', 'PENDING CREATE', 'ASSIGN', 'FAIL'),
('CONFIGURATION', 'PENDING DELETE', 'ASSIGN', 'FAIL'),
('CONFIGURATION', 'PENDING ACTIVATION', 'ASSIGN', 'FAIL'),
('CONFIGURATION', 'PENDING', 'ASSIGN', 'FAIL'),

('CONFIGURATION', 'PRECREATED', 'ACTIVATE', 'FAIL'),
('CONFIGURATION', 'INVENTORIED', 'ACTIVATE', 'FAIL'),
('CONFIGURATION', 'ASSIGNED', 'ACTIVATE', 'CONTINUE'),
('CONFIGURATION', 'CREATED', 'ACTIVATE', 'CONTINUE'),
('CONFIGURATION', 'ACTIVE', 'ACTIVATE', 'SILENT SUCCESS'),
('CONFIGURATION', 'PENDING CREATE', 'ACTIVATE', 'FAIL'),
('CONFIGURATION', 'PENDING DELETE', 'ACTIVATE', 'FAIL'),
('CONFIGURATION', 'PENDING ACTIVATION', 'ACTIVATE', 'FAIL'),
('CONFIGURATION', 'PENDING', 'ACTIVATE', 'FAIL'),

('CONFIGURATION', 'PRECREATED', 'DEACTIVATE', 'FAIL'),
('CONFIGURATION', 'INVENTORIED', 'DEACTIVATE', 'SILENT SUCCESS'),
('CONFIGURATION', 'ASSIGNED', 'DEACTIVATE', 'SILENT SUCCESS'),
('CONFIGURATION', 'CREATED', 'DEACTIVATE', 'SILENT SUCCESS'),
('CONFIGURATION', 'ACTIVE', 'DEACTIVATE', 'CONTINUE'),
('CONFIGURATION', 'PENDING CREATE', 'DEACTIVATE', 'FAIL'),
('CONFIGURATION', 'PENDING DELETE', 'DEACTIVATE', 'FAIL'),
('CONFIGURATION', 'PENDING ACTIVATION', 'DEACTIVATE', 'FAIL'),
('CONFIGURATION', 'PENDING', 'DEACTIVATE', 'FAIL');

UPDATE orchestration_flow_reference
SET SEQ_NO = SEQ_NO + 2 WHERE COMPOSITE_ACTION = 'Service-Macro-Create' AND SEQ_NO > 12;

UPDATE orchestration_flow_reference
SET SEQ_NO = SEQ_NO + 1 WHERE COMPOSITE_ACTION = 'Service-Macro-Activate' AND SEQ_NO > 6;



UPDATE rainy_day_handler_macro SET Secondary_Policy = 'Abort' WHERE POLICY = 'Rollback';
UPDATE rainy_day_handler_macro SET Secondary_Policy = 'Abort' WHERE POLICY = 'Abort';
UPDATE rainy_day_handler_macro SET Secondary_Policy = 'Rollback' WHERE FLOW_NAME = 'CreateNetworkBB';
UPDATE rainy_day_handler_macro SET Secondary_Policy = 'Rollback' WHERE FLOW_NAME = 'CreateNetworkCollectionBB';
UPDATE rainy_day_handler_macro SET Secondary_Policy = 'RollbackToAssigned' WHERE FLOW_NAME = 'CreateVfModuleBB';
UPDATE rainy_day_handler_macro SET Secondary_Policy = 'Rollback' WHERE FLOW_NAME = 'CreateVolumeGroupBB';
UPDATE rainy_day_handler_macro SET Secondary_Policy = 'Abort' WHERE FLOW_NAME = 'DeleteNetworkBB';
UPDATE rainy_day_handler_macro SET Secondary_Policy = 'Abort' WHERE FLOW_NAME = 'DeleteNetworkCollectionBB';
UPDATE rainy_day_handler_macro SET Secondary_Policy = 'Abort' WHERE FLOW_NAME = 'DeleteVfModuleBB';
UPDATE rainy_day_handler_macro SET Secondary_Policy = 'Abort' WHERE FLOW_NAME = 'DeleteVolumeGroupBB';
UPDATE rainy_day_handler_macro SET Secondary_Policy = 'Abort' WHERE FLOW_NAME = 'UnassignNetworkBB';
UPDATE rainy_day_handler_macro SET Secondary_Policy = 'Abort' WHERE FLOW_NAME = 'UnassignServiceInstanceBB';
UPDATE rainy_day_handler_macro SET Secondary_Policy = 'Abort' WHERE FLOW_NAME = 'UnassignVfModuleBB';
UPDATE rainy_day_handler_macro SET Secondary_Policy = 'Abort' WHERE FLOW_NAME = 'UnassignVnfBB';
UPDATE rainy_day_handler_macro SET Secondary_Policy = 'Abort' WHERE FLOW_NAME = 'UnassignVolumeGroupBB';
UPDATE rainy_day_handler_macro SET Secondary_Policy = 'Rollback' WHERE FLOW_NAME = 'UpdateNetworkBB';
UPDATE rainy_day_handler_macro SET Secondary_Policy = 'Rollback' WHERE FLOW_NAME = 'VnfAdapterBB';




INSERT INTO rainy_day_handler_macro (FLOW_NAME, SERVICE_TYPE, VNF_TYPE, ERROR_CODE, WORK_STEP, POLICY)
VALUES
('GenericVnfHealthCheckBB', '*', '*', '*', "*" , 'Retry'),
('ConfigurationScaleOutBB', '*', '*', '*', "*" , 'Retry');

INSERT INTO building_block_detail(BUILDING_BLOCK_NAME, RESOURCE_TYPE, TARGET_ACTION)  
VALUES
('VNFSetInMaintFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFCheckPserversLockedFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFCheckInMaintFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFCheckClosedLoopDisabledFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFSetClosedLoopDisabledFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUnsetClosedLoopDisabledFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFLockActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUnlockActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFStopActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFStartActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFSnapShotActivity', 'NO_VALIDATE', 'CUSTOM'),
('FlowCompleteActivity', 'NO_VALIDATE', 'CUSTOM'),
('PauseForManualTaskActivity', 'NO_VALIDATE', 'CUSTOM'),
('DistributeTrafficActivity', 'NO_VALIDATE', 'CUSTOM'),
('DistributeTrafficCheckActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFHealthCheckActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFQuiesceTrafficActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFResumeTrafficActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUnsetInMaintFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUpgradeBackupActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUpgradePostCheckActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUpgradePreCheckActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUpgradeSoftwareActivity', 'NO_VALIDATE', 'CUSTOM'),
('VnfInPlaceSoftwareUpdate', 'NO_VALIDATE', 'CUSTOM');

UPDATE northbound_request_ref_lookup SET SERVICE_TYPE = '*' WHERE SERVICE_TYPE IS NULL;

INSERT INTO building_block_detail(BUILDING_BLOCK_NAME, RESOURCE_TYPE, TARGET_ACTION)
VALUES
('ConfigAssignVnfBB', 'NO_VALIDATE', 'CUSTOM'),
('ConfigDeployVnfBB', 'NO_VALIDATE', 'CUSTOM');

UPDATE rainy_day_handler_macro SET reg_ex_error_message = '*' WHERE reg_ex_error_message IS null;

UPDATE rainy_day_handler_macro SET SERVICE_ROLE = '*' WHERE SERVICE_ROLE IS null;

INSERT INTO vnf_components_recipe (VNF_TYPE, VNF_COMPONENT_TYPE, ACTION, VERSION, ORCHESTRATION_URI, RECIPE_TIMEOUT)
VALUES
(NULL, 'vfModule', 'replaceInstanceRetainAssignments', '1', '/mso/async/services/WorkflowActionBB', 180);

UPDATE vnf_recipe
SET ORCHESTRATION_URI = '/mso/async/services/WorkflowActionBB'
WHERE NF_ROLE = 'GR-API-DEFAULT' AND ACTION = 'inPlaceSoftwareUpdate';

INSERT INTO rainy_day_handler_macro (FLOW_NAME, SERVICE_TYPE, VNF_TYPE, ERROR_CODE, WORK_STEP, POLICY, SECONDARY_POLICY, REG_EX_ERROR_MESSAGE, SERVICE_ROLE)
VALUES 
('VNFCheckPserversLockedFlagActivity','*','*','*','*','Manual','Abort','*', '*'),
('VNFCheckInMaintFlagActivity','*','*','*','*','Manual','Abort','*', '*'),
('VNFSetInMaintFlagActivity','*','*','*','*','Manual','Abort','*', '*'),
('VNFCheckClosedLoopDisabledFlagActivity','*','*','*','*','Manual','Abort','*', '*'),
('VNFSetClosedLoopDisabledFlagActivity','*','*','*','*','Manual','Abort','*', '*'),
('VNFLockActivity','*','*','*','*','Manual','Abort','*', '*'),
('VNFUpgradePreCheckActivity','*','*','*','*','Manual','Abort','*', '*'),
('VNFQuiesceTrafficActivity','*','*','*','*','Manual','Abort','*', '*'),
('VNFStopActivity','*','*','*','*','Manual','Abort','*', '*'),
('VNFSnapShotActivity','*','*','*','*','Manual','Abort','*', '*'),
('VNFStartActivity','*','*','*','*','Manual','Abort','*', '*'),
('VNFUpgradeBackupActivity','*','*','*','*','Manual','Abort','*', '*'),
('VNFUpgradeSoftwareActivity','*','*','*','*','Manual','Abort','*', '*'),
('VNFUpgradePostCheckActivity','*','*','*','*','Manual','Abort','*', '*'),
('VNFResumeTrafficActivity','*','*','*','*','Manual','Abort','*', '*'),
('VNFUnlockActivity','*','*','*','*','Manual','Abort','*', '*'),
('SDNOVnfHealthCheckBB','*','*','*','*','Manual','Abort','*', '*'),
('VNFUnsetInMaintFlagActivity','*','*','*','*','Manual','Abort','*', '*'),
('VNFUnsetClosedLoopDisabledActivity','*','*','*','*','Manual','Abort','*', '*');

INSERT INTO building_block_detail(BUILDING_BLOCK_NAME, RESOURCE_TYPE, TARGET_ACTION)
VALUES
('ControllerExecutionBB', 'NO_VALIDATE', 'CUSTOM');

INSERT INTO orchestration_status_state_transition_directive(RESOURCE_TYPE, ORCHESTRATION_STATUS, TARGET_ACTION, FLOW_DIRECTIVE)
VALUES
('VNF','CONFIGDEPLOYED','ACTIVATE','CONTINUE');
