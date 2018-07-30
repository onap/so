USE catalogdb;

ALTER TABLE
  `northbound_request_ref_lookup`
ADD
  ISTOPLEVELFLOW TINYINT(1);
    
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = TRUE WHERE MACRO_ACTION = 'Service-Create';
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = TRUE WHERE MACRO_ACTION = 'Service-Delete';
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = TRUE WHERE MACRO_ACTION = 'Service-Macro-Assign';
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = TRUE WHERE MACRO_ACTION = 'Service-Macro-Activate';
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = TRUE WHERE MACRO_ACTION = 'Service-Macro-Unassign';
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = TRUE WHERE MACRO_ACTION = 'Service-Macro-Create';
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = TRUE WHERE MACRO_ACTION = 'Service-Macro-Delete';
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = TRUE WHERE MACRO_ACTION = 'Network-Create';
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = TRUE WHERE MACRO_ACTION = 'Network-Delete';
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = FALSE WHERE MACRO_ACTION = 'VNF-Macro-Replace';
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = TRUE WHERE MACRO_ACTION = 'VNF-Create';
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = TRUE WHERE MACRO_ACTION = 'VNF-Delete';
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = TRUE WHERE MACRO_ACTION = 'VolumeGroup-Create';
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = TRUE WHERE MACRO_ACTION = 'VolumeGroup-Delete';
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = TRUE WHERE MACRO_ACTION = 'VFModule-Create';
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = TRUE WHERE MACRO_ACTION = 'VFModule-Delete';
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = TRUE WHERE MACRO_ACTION = 'NetworkCollection-Macro-Create';
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = TRUE WHERE MACRO_ACTION = 'NetworkCollection-Macro-Delete';
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = TRUE WHERE MACRO_ACTION = 'AVPNBonding-Macro-Delete';
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = TRUE WHERE MACRO_ACTION = 'AVPNBonding-Macro-Create';
UPDATE northbound_request_ref_lookup SET ISTOPLEVELFLOW = TRUE WHERE MACRO_ACTION = 'VNF-Macro-Recreate';