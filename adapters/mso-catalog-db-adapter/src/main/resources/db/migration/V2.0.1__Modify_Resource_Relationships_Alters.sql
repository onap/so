
USE catalogdb;
ALTER TABLE collection_resource_customization
ADD FOREIGN KEY ( CR_MODEL_UUID) 
REFERENCES collection_resource(MODEL_UUID)
ON DELETE CASCADE;

ALTER TABLE vnf_resource_customization 
ADD COLUMN 
INSTANCE_GROUP_MODEL_UUID varchar(200);


ALTER TABLE instance_group
ADD FOREIGN KEY ( CR_MODEL_UUID) 
REFERENCES collection_resource(MODEL_UUID)
ON DELETE CASCADE;


ALTER TABLE collection_resource_customization_to_service 
ADD FOREIGN KEY (service_model_uuid) 
REFERENCES service(MODEL_UUID)
ON DELETE CASCADE;

ALTER TABLE allotted_resource_customization_to_service 
ADD FOREIGN KEY (service_model_uuid) 
REFERENCES service(MODEL_UUID)
ON DELETE CASCADE;


ALTER TABLE vnf_resource_customization_to_service 
ADD FOREIGN KEY (service_model_uuid) 
REFERENCES service(MODEL_UUID)
ON DELETE CASCADE;


ALTER TABLE network_resource_customization_to_service 
ADD FOREIGN KEY (service_model_uuid) 
REFERENCES service(MODEL_UUID)
ON DELETE CASCADE;


ALTER TABLE network_resource_customization_to_service 
ADD FOREIGN KEY (resource_model_customization_uuid) 
REFERENCES network_resource_customization(model_customization_uuid)
ON DELETE CASCADE;

ALTER TABLE vnf_resource_customization_to_service 
ADD FOREIGN KEY (resource_model_customization_uuid) 
REFERENCES vnf_resource_customization(model_customization_uuid)
ON DELETE CASCADE;

ALTER TABLE allotted_resource_customization_to_service 
ADD FOREIGN KEY (resource_model_customization_uuid) 
REFERENCES allotted_resource_customization(model_customization_uuid)
ON DELETE CASCADE;  

ALTER TABLE collection_resource_customization_to_service 
ADD FOREIGN KEY (resource_model_customization_uuid) 
REFERENCES collection_resource_customization(model_customization_uuid)
ON DELETE CASCADE;

INSERT INTO network_resource_customization_to_service SELECT service_model_uuid,resource_model_customization_uuid 
FROM service_to_resource_customizations WHERE model_type = 'network' and service_model_uuid in(select model_uuid from service)
AND resource_model_customization_uuid in ( SELECT MODEL_CUSTOMIZATION_UUID from network_resource_customization);

INSERT INTO allotted_resource_customization_to_service SELECT service_model_uuid,resource_model_customization_uuid 
FROM service_to_resource_customizations WHERE model_type = 'allottedResource' and service_model_uuid in(select model_uuid from service)
AND resource_model_customization_uuid in ( SELECT MODEL_CUSTOMIZATION_UUID from allotted_resource_customization);

INSERT INTO vnf_resource_customization_to_service SELECT service_model_uuid,resource_model_customization_uuid 
FROM service_to_resource_customizations WHERE model_type = 'vnf' and service_model_uuid in(select model_uuid from service)
AND resource_model_customization_uuid in ( SELECT MODEL_CUSTOMIZATION_UUID from vnf_resource_customization);

DROP TABLE service_to_resource_customizations;


INSERT INTO vnf_recipe (VNF_TYPE, ACTION, VERSION_STR, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT)
VALUES
('GR-API-DEFAULT', 'createInstance', '1', 'Gr api recipe to create vnf', '/mso/async/services/WorkflowActionBB', 180),
('GR-API-DEFAULT', 'deleteInstance', '1', 'Gr api recipe to delete vnf', '/mso/async/services/WorkflowActionBB', 180),
('GR-API-DEFAULT', 'updateInstance', '1', 'Gr api recipe to update vnf', '/mso/async/services/WorkflowActionBB', 180),
('GR-API-DEFAULT', 'replaceInstance', '1', 'Gr api recipe to replace vnf', '/mso/async/services/WorkflowActionBB', 180),
('GR-API-DEFAULT', 'inPlaceSoftwareUpdate', '1', 'Gr api recipe to do an in place software update', '/mso/async/services/WorkflowActionBB', 180),
('GR-API-DEFAULT', 'applyUpdatedConfig', '1', 'Gr api recipe to apply updated config', '/mso/async/services/WorkflowActionBB', 180);

UPDATE vnf_recipe
SET vnf_type = 'VNF-API-DEFAULT'
WHERE vnf_type = 'VID_DEFAULT';

UPDATE vnf_recipe
SET description = 'Vnf api recipe to create vnf'
WHERE description = 'VID_DEFAULT recipe to create VNF if no custom BPMN flow is found';

UPDATE vnf_recipe
SET description = 'Vnf api recipe to delete vnf'
WHERE description = 'VID_DEFAULT recipe to delete VNF if no custom BPMN flow is found';

UPDATE vnf_recipe
SET description = 'Vnf api recipe to update vnf'
WHERE description = 'VID_DEFAULT update';

UPDATE vnf_recipe
SET description = 'Vnf api recipe to replace vnf'
WHERE description = 'VID_DEFAULT replace';

UPDATE vnf_recipe
SET description = 'Vnf api recipe to do an in place software update'
WHERE description = 'VID_DEFAULT inPlaceSoftwareUpdate';

UPDATE vnf_recipe
SET description = 'Vnf api recipe to apply updated config'
WHERE description = 'VID_DEFAULT applyUpdatedConfig';

INSERT INTO service (MODEL_UUID, MODEL_NAME, MODEL_INVARIANT_UUID, MODEL_VERSION, DESCRIPTION)
VALUES
('DummyGRApiDefaultModelUUID?', 'GR-API-DEFAULT', 'DummyGRApiDefaultModelInvariantUUID?', '1.0', 'Gr api service for VID to use for infra APIH orchestration');

UPDATE service
SET model_name = 'VNF-API-DEFAULT',
    description = 'Vnf api service for VID to use for infra APIH orchestration'
WHERE model_name = 'VID_DEFAULT';

INSERT INTO service_recipe (ACTION, VERSION_STR, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT, SERVICE_MODEL_UUID)
VALUES
('activateInstance', '1.0', 'Gr api recipe to activate service-instance', '/mso/async/services/WorkflowActionBB', 180, 'DummyGRApiDefaultModelUUID?'),
('createInstance', '1.0', 'Gr api recipe to create service-instance', '/mso/async/services/WorkflowActionBB', 180, 'DummyGRApiDefaultModelUUID?'),
('deactivateInstance', '1.0', 'Gr api recipe to deactivate service-instance', '/mso/async/services/WorkflowActionBB', 180, 'DummyGRApiDefaultModelUUID?'),
('deleteInstance', '1.0', 'Gr api recipe to delete service-instance', '/mso/async/services/WorkflowActionBB', 180, 'DummyGRApiDefaultModelUUID?');

UPDATE service_recipe
SET description = 'Vnf api recipe to activate service-instance'
WHERE description = 'VID_DEFAULT activate';

UPDATE service_recipe
SET description = 'Vnf api recipe to create service-instance'
WHERE description = 'VID_DEFAULT recipe to create service-instance if no custom BPMN flow is found';

UPDATE service_recipe
SET description = 'Vnf api recipe to deactivate service-instance'
WHERE description = 'VID_DEFAULT deactivate';

UPDATE service_recipe
SET description = 'Vnf api recipe to delete service-instance'
WHERE description = 'VID_DEFAULT recipe to delete service-instance if no custom BPMN flow is found';

INSERT INTO vnf_components_recipe (VNF_COMPONENT_TYPE, ACTION, VERSION, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT, VF_MODULE_MODEL_UUID)
VALUES
('volumeGroup', 'createInstance', '1', 'Gr api recipe to create volume-group', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT'),
('volumeGroup', 'deleteInstance', '1', 'Gr api recipe to delete volume-group', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT'),
('volumeGroup', 'updateInstance', '1', 'Gr api recipe to update volume-group', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT'),
('vfModule', 'createInstance', '1', 'Gr api recipe to create vf-module', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT'),
('vfModule', 'deleteInstance', '1', 'Gr api recipe to delete vf-module', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT'),
('vfModule', 'updateInstance', '1', 'Gr api recipe to update vf-module', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT'),
('vfModule', 'replaceInstance', '1', 'Gr api recipe to replace vf-module', '/mso/async/services/WorkflowActionBB', 180, 'GR-API-DEFAULT');

UPDATE vnf_components_recipe
SET vf_module_model_uuid = 'VNF-API-DEFAULT'
WHERE vf_module_model_uuid = 'VID_DEFAULT';

UPDATE vnf_components_recipe
SET description = 'Vnf api recipe to create volume-group'
WHERE description = 'VID_DEFAULT recipe to create volume-group if no custom BPMN flow is found';

UPDATE vnf_components_recipe
SET description = 'Vnf api recipe to delete volume-group'
WHERE description = 'VID_DEFAULT recipe to delete volume-group if no custom BPMN flow is found';

UPDATE vnf_components_recipe
SET description = 'Vnf api recipe to update volume-group'
WHERE description = 'VID_DEFAULT recipe to update volume-group if no custom BPMN flow is found';

UPDATE vnf_components_recipe
SET description = 'Vnf api recipe to create vf-module'
WHERE description = 'VID_DEFAULT recipe to create vf-module if no custom BPMN flow is found';

UPDATE vnf_components_recipe
SET description = 'Vnf api recipe to delete vf-module'
WHERE description = 'VID_DEFAULT recipe to delete vf-module if no custom BPMN flow is found';

UPDATE vnf_components_recipe
SET description = 'Vnf api recipe to update vf-module'
WHERE description = 'VID_DEFAULT recipe to update vf-module if no custom BPMN flow is found';

UPDATE vnf_components_recipe
SET description = 'Vnf api recipe to replace vf-module'
WHERE description = 'VID_DEFAULT vfModule replace';

INSERT INTO network_recipe (MODEL_NAME, ACTION, DESCRIPTION, ORCHESTRATION_URI, RECIPE_TIMEOUT, VERSION_STR)
VALUES
('GR-API-DEFAULT', 'createInstance', 'Gr api recipe to create network', '/mso/async/services/WorkflowActionBB', 180, '1.0'),
('GR-API-DEFAULT', 'updateInstance', 'Gr api recipe to update network', '/mso/async/services/WorkflowActionBB', 180, '1.0'),
('GR-API-DEFAULT', 'deleteInstance', 'Gr api recipe to delete network', '/mso/async/services/WorkflowActionBB', 180, '1.0');
    
UPDATE network_recipe
SET model_name = 'VNF-API-DEFAULT'
WHERE model_name = 'VID_DEFAULT';

UPDATE network_recipe
SET description = 'Vnf api recipe to create network'
WHERE description = 'VID_DEFAULT recipe to create network if no custom BPMN flow is found';

UPDATE network_recipe
SET description = 'Vnf api recipe to update network'
WHERE description = 'VID_DEFAULT recipe to update network if no custom BPMN flow is found';

UPDATE network_recipe
SET description = 'Vnf api recipe to delete network'
WHERE description = 'VID_DEFAULT recipe to delete network if no custom BPMN flow is found';