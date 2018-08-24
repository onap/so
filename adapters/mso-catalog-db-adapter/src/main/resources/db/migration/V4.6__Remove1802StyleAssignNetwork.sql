use catalogdb;

DELETE FROM building_block_detail WHERE BUILDING_BLOCK_NAME = 'AssignNetwork1802BB';
DELETE FROM building_block_detail WHERE BUILDING_BLOCK_NAME = 'UnassignNetwork1802BB';

UPDATE orchestration_flow_reference SET `FLOW_NAME`='UnassignNetworkBB' WHERE `COMPOSITE_ACTION`='Network-Delete' and `FLOW_NAME`='UnassignNetwork1802BB';
UPDATE orchestration_flow_reference SET `FLOW_NAME`='AssignNetworkBB' WHERE `COMPOSITE_ACTION`='Network-Create' and `FLOW_NAME`='AssignNetwork1802BB';

