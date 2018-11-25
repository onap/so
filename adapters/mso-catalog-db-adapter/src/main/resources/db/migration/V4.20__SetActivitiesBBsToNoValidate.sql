USE catalogdb;

INSERT INTO building_block_detail (BUILDING_BLOCK_NAME, RESOURCE_TYPE, TARGET_ACTION)
VALUES
('FlowCompleteActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFHealthCheckActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFQuiesceTrafficActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFResumeTrafficActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFSetInMaintFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUnsetInMaintFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUpgradePostCheckActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUpgradePreCheckActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUpgradeSoftwareActivity', 'NO_VALIDATE', 'CUSTOM');