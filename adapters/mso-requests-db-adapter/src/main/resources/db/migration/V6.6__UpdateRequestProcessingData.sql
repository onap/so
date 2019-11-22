use requestdb;

UPDATE request_processing_data 
SET IS_DATA_INTERNAL = 1
WHERE TAG = 'BPMNExecutionData';

UPDATE request_processing_data 
SET IS_DATA_INTERNAL = 0
WHERE TAG = 'StackInformation' OR TAG = 'pincFabricConfigRequest';