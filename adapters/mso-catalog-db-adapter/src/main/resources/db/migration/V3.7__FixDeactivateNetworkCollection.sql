use catalogdb;

UPDATE orchestration_status_state_transition_directive SET TARGET_ACTION = "ACTIVATE" WHERE TARGET_ACTION = "ACTIVE";

DELETE FROM building_block_detail WHERE BUILDING_BLOCK_NAME = "DeactivateNetworkCollectionBB";