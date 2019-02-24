use requestdb;

ALTER TABLE watchdog_distributionid_status ADD LOCK_VERSION int NOT NULL;
