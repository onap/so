USE catalogdb;

ALTER TABLE 
  `northbound_request_ref_lookup` CHANGE `ACTION` `MACRO_ACTION` VARCHAR(200);
  
Alter TABLE
  `northbound_request_ref_lookup`
ADD
  ACTION VARCHAR(200);
  
INSERT INTO northbound_request_ref_lookup (ACTION) VALUES ('createService') WHERE MACRO_ACTION = 'Service-Create';
INSERT INTO northbound_request_ref_lookup (ACTION) VALUES ('deleteService') WHERE MACRO_ACTION = 'Service-Delete';
INSERT INTO northbound_request_ref_lookup (ACTION) VALUES ('assignService') WHERE MACRO_ACTION = 'Service-Macro-Assign';
INSERT INTO northbound_request_ref_lookup (ACTION) VALUES ('activateService') WHERE MACRO_ACTION = 'Service-Macro-Activate';
INSERT INTO northbound_request_ref_lookup (ACTION) VALUES ('unassignService') WHERE MACRO_ACTION = 'Service-Macro-Unassign';
INSERT INTO northbound_request_ref_lookup (ACTION) VALUES ('createService') WHERE MACRO_ACTION = 'Service-Macro-Create';
INSERT INTO northbound_request_ref_lookup (ACTION) VALUES ('deleteService') WHERE MACRO_ACTION = 'Service-Macro-Delete';
INSERT INTO northbound_request_ref_lookup (ACTION) VALUES ('createService') WHERE MACRO_ACTION = 'Network-Create';
INSERT INTO northbound_request_ref_lookup (ACTION) VALUES ('deleteService') WHERE MACRO_ACTION = 'Network-Delete';
INSERT INTO northbound_request_ref_lookup (ACTION) VALUES ('replaceService') WHERE MACRO_ACTION = 'VNF-Macro-Replace';
INSERT INTO northbound_request_ref_lookup (ACTION) VALUES ('createService') WHERE MACRO_ACTION = 'VNF-Create';
INSERT INTO northbound_request_ref_lookup (ACTION) VALUES ('deleteService') WHERE MACRO_ACTION = 'VNF-Delete';
INSERT INTO northbound_request_ref_lookup (ACTION) VALUES ('createService') WHERE MACRO_ACTION = 'VolumeGroup-Create';
INSERT INTO northbound_request_ref_lookup (ACTION) VALUES ('deleteService') WHERE MACRO_ACTION = 'VolumeGroup-Delete';
INSERT INTO northbound_request_ref_lookup (ACTION) VALUES ('createService') WHERE MACRO_ACTION = 'VFModule-Create';
INSERT INTO northbound_request_ref_lookup (ACTION) VALUES ('deleteService') WHERE MACRO_ACTION = 'VFModule-Delete';
INSERT INTO northbound_request_ref_lookup (ACTION) VALUES ('createService') WHERE MACRO_ACTION = 'NetworkCollection-Macro-Create';
INSERT INTO northbound_request_ref_lookup (ACTION) VALUES ('deleteService') WHERE MACRO_ACTION = 'NetworkCollection-Macro-Delete';
INSERT INTO northbound_request_ref_lookup (ACTION) VALUES ('deleteService') WHERE MACRO_ACTION = 'AVPNBonding-Macro-Delete';
INSERT INTO northbound_request_ref_lookup (ACTION) VALUES ('createService') WHERE MACRO_ACTION = 'AVPNBonding-Macro-Create';