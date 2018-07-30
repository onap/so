USE catalogdb;

UPDATE northbound_request_ref_lookup SET MIN_API_VERSION = 5 WHERE MACRO_ACTION = 'Service-Macro-Create';
UPDATE northbound_request_ref_lookup SET MIN_API_VERSION = 5 WHERE MACRO_ACTION = 'Service-Macro-Delete';
UPDATE northbound_request_ref_lookup SET MIN_API_VERSION = 5 WHERE MACRO_ACTION = 'Service-Macro-Activate';