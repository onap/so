USE catalogdb;

ALTER TABLE building_block_detail
CHANGE COLUMN building_block_name BUILDING_BLOCK_NAME VARCHAR(50) NOT NULL,
CHANGE COLUMN resource_type RESOURCE_TYPE VARCHAR(25) NOT NULL,
CHANGE COLUMN target_action TARGET_ACTION VARCHAR(25) NOT NULL;
/*
SELECT CONCAT('ALTER TABLE ', TABLE_NAME, ' CHANGE ', COLUMN_NAME, ' ', UPPER(COLUMN_NAME), ';')
FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'building_block_detail' AND COLUMN_NAME <> 'id';
*/

ALTER TABLE orchestration_status_state_transition_directive
CHANGE COLUMN resource_type RESOURCE_TYPE VARCHAR(25) NOT NULL,
CHANGE COLUMN orchestration_status ORCHESTRATION_STATUS VARCHAR(25) NOT NULL,
CHANGE COLUMN target_action TARGET_ACTION VARCHAR(25) NOT NULL,
CHANGE COLUMN flow_directive FLOW_DIRECTIVE VARCHAR(25) NOT NULL;

/*
SELECT CONCAT('ALTER TABLE ', TABLE_NAME, ' CHANGE ', COLUMN_NAME, ' ', UPPER(COLUMN_NAME), ';')
FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'orchestration_status_state_transition_directive' AND COLUMN_NAME <> 'id';
*/

ALTER TABLE building_block_detail
DROP INDEX building_block_name;

ALTER TABLE building_block_detail
ADD UNIQUE KEY UK_building_block_name (BUILDING_BLOCK_NAME);


/*
SELECT DISTINCT FLOW_NAME FROM orchestration_flow_reference WHERE FLOW_NAME NOT IN
(SELECT building_block_name FROM building_block_detail);
 */

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
('AssignAndActivateVpnBondingLinksBB', 'CUSTOM', 'CUSTOM'),
('AvpnAssignServiceInstanceBB', 'CUSTOM', 'CUSTOM'),
('CreateCustomerVpnBindingBB', 'CUSTOM', 'CUSTOM'),
('SniroHoming', 'CUSTOM', 'CUSTOM'),
('DeactivateAndUnassignVpnBondingLinksBB', 'CUSTOM', 'CUSTOM'),
('DeactivateNetworkCollectionBB', 'CUSTOM', 'CUSTOM'),
('AAICheckVnfInMaintBB', 'CUSTOM', 'CUSTOM'),
('AAISetVnfInMaintBB', 'CUSTOM', 'CUSTOM'),
('AAIUnsetVnfInMaintBB', 'CUSTOM', 'CUSTOM'),
('SDNOVnfHealthCheckBB', 'CUSTOM', 'CUSTOM'),
('VNF-Macro-Replace', 'CUSTOM', 'CUSTOM'),
('HomingV2', 'CUSTOM', 'CUSTOM');

ALTER TABLE building_block_detail
MODIFY COLUMN BUILDING_BLOCK_NAME VARCHAR(200);

ALTER TABLE orchestration_flow_reference
ADD CONSTRAINT fk_orchestration_flow_reference__building_block_detail
FOREIGN KEY (FLOW_NAME) REFERENCES building_block_detail(BUILDING_BLOCK_NAME)
ON DELETE CASCADE
ON UPDATE CASCADE;

INSERT INTO orchestration_status_state_transition_directive(RESOURCE_TYPE, ORCHESTRATION_STATUS, TARGET_ACTION, FLOW_DIRECTIVE)
VALUES
('CUSTOM', 'ACTIVE', 'CUSTOM', 'CONTINUE'),
('CUSTOM', 'ASSIGNED', 'CUSTOM', 'CONTINUE'),
('CUSTOM', 'CREATED', 'CUSTOM', 'CONTINUE'),
('CUSTOM', 'INVENTORIED', 'CUSTOM', 'CONTINUE'),
('CUSTOM', 'PENDING', 'CUSTOM', 'CONTINUE'),
('CUSTOM', 'PENDING_ACTIVATION', 'CUSTOM', 'CONTINUE'),
('CUSTOM', 'PENDING_CREATE', 'CUSTOM', 'CONTINUE'),
('CUSTOM', 'PENDING_DELETE', 'CUSTOM', 'CONTINUE'),
('CUSTOM', 'PRECREATED', 'CUSTOM', 'CONTINUE');
