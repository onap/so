use catalogdb;


ALTER TABLE `service_info`
    ADD COLUMN IF NOT EXISTS SERVICE_MODEL_UUID varchar (200)
    NOT NULL;


ALTER TABLE `service_artifact` DROP FOREIGN KEY `fk_service_artifact_service_info1`;

ALTER TABLE `service_info`
	ADD CONSTRAINT `fk_service_info_service1` FOREIGN KEY (`SERVICE_MODEL_UUID`) REFERENCES `service` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `service_artifact`
	ADD CONSTRAINT `fk_service_artifact_service1` FOREIGN KEY (`SERVICE_MODEL_UUID`) REFERENCES `service` (`MODEL_UUID`) ON DELETE CASCADE ON UPDATE CASCADE;

DROP TABLE IF EXISTS `service_to_service_info`;
