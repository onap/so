USE catalogdb;

UPDATE northbound_request_ref_lookup SET MIN_API_VERSION = 5 WHERE MACRO_ACTION = 'Service-Macro-Deactivate';