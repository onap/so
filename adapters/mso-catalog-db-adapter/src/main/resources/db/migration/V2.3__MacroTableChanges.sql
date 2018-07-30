USE catalogdb;


  
UPDATE northbound_request_ref_lookup SET ACTION = 'createService' WHERE MACRO_ACTION = 'Service-Create';
UPDATE northbound_request_ref_lookup SET ACTION = 'deleteService' WHERE MACRO_ACTION = 'Service-Delete';
UPDATE northbound_request_ref_lookup SET ACTION = 'assignService' WHERE MACRO_ACTION = 'Service-Macro-Assign';
UPDATE northbound_request_ref_lookup SET ACTION = 'activateService' WHERE MACRO_ACTION = 'Service-Macro-Activate';
UPDATE northbound_request_ref_lookup SET ACTION = 'unassignService' WHERE MACRO_ACTION = 'Service-Macro-Unassign';
UPDATE northbound_request_ref_lookup SET ACTION = 'createService' WHERE MACRO_ACTION = 'Service-Macro-Create';
UPDATE northbound_request_ref_lookup SET ACTION = 'deleteService' WHERE MACRO_ACTION = 'Service-Macro-Delete';
UPDATE northbound_request_ref_lookup SET ACTION = 'createService' WHERE MACRO_ACTION = 'Network-Create';
UPDATE northbound_request_ref_lookup SET ACTION = 'deleteService' WHERE MACRO_ACTION = 'Network-Delete';
UPDATE northbound_request_ref_lookup SET ACTION = 'replaceService' WHERE MACRO_ACTION = 'VNF-Macro-Replace';
UPDATE northbound_request_ref_lookup SET ACTION = 'createService' WHERE MACRO_ACTION = 'VNF-Create';
UPDATE northbound_request_ref_lookup SET ACTION = 'deleteService' WHERE MACRO_ACTION = 'VNF-Delete';
UPDATE northbound_request_ref_lookup SET ACTION = 'createService' WHERE MACRO_ACTION = 'VolumeGroup-Create';
UPDATE northbound_request_ref_lookup SET ACTION = 'deleteService' WHERE MACRO_ACTION = 'VolumeGroup-Delete';
UPDATE northbound_request_ref_lookup SET ACTION = 'createService' WHERE MACRO_ACTION = 'VFModule-Create';
UPDATE northbound_request_ref_lookup SET ACTION = 'deleteService' WHERE MACRO_ACTION = 'VFModule-Delete';
UPDATE northbound_request_ref_lookup SET ACTION = 'createService' WHERE MACRO_ACTION = 'NetworkCollection-Macro-Create';
UPDATE northbound_request_ref_lookup SET ACTION = 'deleteService' WHERE MACRO_ACTION = 'NetworkCollection-Macro-Delete';
UPDATE northbound_request_ref_lookup SET ACTION = 'deleteService' WHERE MACRO_ACTION = 'AVPNBonding-Macro-Delete';
UPDATE northbound_request_ref_lookup SET ACTION = 'createService' WHERE MACRO_ACTION = 'AVPNBonding-Macro-Create';