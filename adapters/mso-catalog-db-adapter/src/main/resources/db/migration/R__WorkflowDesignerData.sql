use catalogdb;

DELETE FROM activity_spec_to_activity_spec_parameters;
DELETE FROM activity_spec_to_activity_spec_categories;
DELETE FROM activity_spec_to_user_parameters;
DELETE FROM activity_spec;
DELETE FROM activity_spec_categories;
DELETE FROM activity_spec_parameters;
DELETE FROM user_parameters;
DELETE FROM building_block_detail;


INSERT INTO activity_spec (NAME, DESCRIPTION, VERSION) 
VALUES 
('VNFSetInMaintFlagActivity','Activity to Set InMaint Flag in A&AI',1.0),
('VNFCheckPserversLockedFlagActivity','Activity Check Pservers Locked Flag VNF',1.0),
('VNFCheckInMaintFlagActivity','Activity CheckIn Maint Flag on VNF',1.0),
('VNFCheckClosedLoopDisabledFlagActivity','Activity Check Closed Loop Disabled Flag on VNF',1.0),
('VNFSetClosedLoopDisabledFlagActivity','Activity Set Closed Loop Disabled Flag on VNF',1.0),
('VNFUnsetClosedLoopDisabledFlagActivity','Activity Unset Closed Loop Disabled Flag on VNF',1.0),
('VNFLockActivity','Activity Lock on VNF',1.0),
('VNFUnlockActivity','Activity UnLock on VNF',1.0),
('VNFStopActivity','Activity Stop on VNF',1.0),
('VNFStartActivity','Activity Start on VNF',1.0),
('VNFSnapShotActivity','Activity Snap Shot on VNF',1.0),
('FlowCompleteActivity','Activity Complete on VNF',1.0),
('PauseForManualTaskActivity','Activity Pause For Manual Task on VNF',1.0),
('DistributeTrafficActivity','Activity Distribute Traffic on VNF',1.0),
('DistributeTrafficCheckActivity','Activity Distribute Traffic Check on VNF',1.0),
('VNFHealthCheckActivity','Activity Health Check on VNF',1.0),
('VNFQuiesceTrafficActivity','Activity Quiesce Traffic on VNF',1.0),
('VNFResumeTrafficActivity','Activity Resume Traffic on VNF',1.0),
('VNFUnsetInMaintFlagActivity','Activity Unset InMaint Flag on VNF',1.0),
('VNFUpgradeBackupActivity','Activity Upgrade Backup on VNF',1.0),
('VNFUpgradePostCheckActivity','Activity Upgrade Post Check on VNF',1.0),
('VNFUpgradePreCheckActivity','Activity Upgrade PreCheck on VNF',1.0),
('VNFUpgradeSoftwareActivity','Activity UpgradeS oftware on VNF',1.0),
('VnfInPlaceSoftwareUpdate','Activity InPlace Software Update on VNF',1.0);

INSERT INTO activity_spec_categories (NAME)
VALUES ('VNF');

INSERT INTO activity_spec_to_activity_spec_categories(ACTIVITY_SPEC_ID, ACTIVITY_SPEC_CATEGORIES_ID) 
VALUES
((select ID from activity_spec where NAME='VNFSetInMaintFlagActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFCheckPserversLockedFlagActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFCheckInMaintFlagActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFCheckClosedLoopDisabledFlagActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFSetClosedLoopDisabledFlagActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFUnsetClosedLoopDisabledFlagActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFLockActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFUnlockActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFStopActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFStartActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFSnapShotActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='FlowCompleteActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='PauseForManualTaskActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='DistributeTrafficActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='DistributeTrafficCheckActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFHealthCheckActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFQuiesceTrafficActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFResumeTrafficActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFUnsetInMaintFlagActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFUpgradeBackupActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFUpgradePostCheckActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFUpgradePreCheckActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VNFUpgradeSoftwareActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF')),
((select ID from activity_spec where NAME='VnfInPlaceSoftwareUpdate' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF'));

INSERT INTO activity_spec_parameters (NAME, TYPE, DIRECTION, DESCRIPTION) 
VALUES('WorkflowException','WorkflowException','output','Description');

INSERT INTO activity_spec_to_activity_spec_parameters( ACTIVITY_SPEC_ID, ACTIVITY_SPEC_PARAMETERS_ID) 
VALUES
((select ID from activity_spec where NAME='VNFSetInMaintFlagActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFCheckPserversLockedFlagActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFCheckInMaintFlagActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFCheckClosedLoopDisabledFlagActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFSetClosedLoopDisabledFlagActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFUnsetClosedLoopDisabledFlagActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFLockActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFUnlockActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFStopActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFStartActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFSnapShotActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='FlowCompleteActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='PauseForManualTaskActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='DistributeTrafficActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='DistributeTrafficCheckActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFHealthCheckActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFQuiesceTrafficActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFResumeTrafficActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFUnsetInMaintFlagActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFUpgradeBackupActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFUpgradePostCheckActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFUpgradePreCheckActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output')),
((select ID from activity_spec where NAME='VNFUpgradeSoftwareActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='output'));

INSERT INTO `user_parameters`(`NAME`,`PAYLOAD_LOCATION`,`LABEL`,`TYPE`,`DESCRIPTION`,`IS_REQUIRED`,`MAX_LENGTH`,`ALLOWABLE_CHARS`)
VALUES 
('cloudOwner','cloudConfiguration','Cloud Owner','text','',1,7,''), 
('operations_timeout','userParams','Operations Timeout','text','',1,50,''),
('existing_software_version','userParams','Existing Software Version','text','',1,50,''),
('tenantId','cloudConfiguration','Tenant/Project ID','text','',1,36,''),
('new_software_version','userParams','New Software Version','text','',1,50,''),
('lcpCloudRegionId','cloudConfiguration','Cloud Region ID','text','',1,7,'');

INSERT INTO `activity_spec_to_user_parameters`(`ACTIVITY_SPEC_ID`,`USER_PARAMETERS_ID`)
VALUES
((select ID from activity_spec where NAME='VNFStopActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='lcpCloudRegionId')),
((select ID from activity_spec where NAME='VNFStopActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='tenantId')),
((select ID from activity_spec where NAME='VNFStartActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='lcpCloudRegionId')),
((select ID from activity_spec where NAME='VNFStartActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='tenantId')),
((select ID from activity_spec where NAME='VNFSnapShotActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='lcpCloudRegionId')),
((select ID from activity_spec where NAME='VNFSnapShotActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='tenantId')),
((select ID from activity_spec where NAME='VNFQuiesceTrafficActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='operations_timeout')),
((select ID from activity_spec where NAME='VNFUpgradeBackupActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='existing_software_version')),
((select ID from activity_spec where NAME='VNFUpgradeBackupActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='new_software_version')),
((select ID from activity_spec where NAME='VNFUpgradePostCheckActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='existing_software_version')),
((select ID from activity_spec where NAME='VNFUpgradePostCheckActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='new_software_version')),
((select ID from activity_spec where NAME='VNFUpgradePreCheckActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='existing_software_version')),
((select ID from activity_spec where NAME='VNFUpgradePreCheckActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='new_software_version')),
((select ID from activity_spec where NAME='VNFUpgradeSoftwareActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='existing_software_version')),
((select ID from activity_spec where NAME='VNFUpgradeSoftwareActivity' and VERSION=1.0),
(select ID from user_parameters where NAME='new_software_version'));

INSERT INTO building_block_detail(BUILDING_BLOCK_NAME, RESOURCE_TYPE, TARGET_ACTION)  
VALUES 
('VNFSetInMaintFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFCheckPserversLockedFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFCheckInMaintFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFCheckClosedLoopDisabledFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFSetClosedLoopDisabledFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUnsetClosedLoopDisabledFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFLockActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUnlockActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFStopActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFStartActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFSnapShotActivity', 'NO_VALIDATE', 'CUSTOM'),
('FlowCompleteActivity', 'NO_VALIDATE', 'CUSTOM'),
('PauseForManualTaskActivity', 'NO_VALIDATE', 'CUSTOM'),
('DistributeTrafficActivity', 'NO_VALIDATE', 'CUSTOM'),
('DistributeTrafficCheckActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFHealthCheckActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFQuiesceTrafficActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFResumeTrafficActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUnsetInMaintFlagActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUpgradeBackupActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUpgradePostCheckActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUpgradePreCheckActivity', 'NO_VALIDATE', 'CUSTOM'),
('VNFUpgradeSoftwareActivity', 'NO_VALIDATE', 'CUSTOM'),
('VnfInPlaceSoftwareUpdate', 'NO_VALIDATE', 'CUSTOM');

