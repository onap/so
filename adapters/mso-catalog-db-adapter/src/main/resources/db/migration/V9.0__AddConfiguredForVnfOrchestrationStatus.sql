INSERT INTO orchestration_status_state_transition_directive(RESOURCE_TYPE, ORCHESTRATION_STATUS, TARGET_ACTION, FLOW_DIRECTIVE)
VALUES ('VNF', 'CONFIGURED', 'ACTIVATE', 'CONTINUE') ON DUPLICATE KEY UPDATE FLOW_DIRECTIVE='CONTINUE';
