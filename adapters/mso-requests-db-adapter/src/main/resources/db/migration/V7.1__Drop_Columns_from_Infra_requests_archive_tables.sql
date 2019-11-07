use requestdb;

ALTER TABLE `infra_active_requests` DROP IF EXISTS `PROV_STATUS`;

ALTER TABLE `archived_infra_requests` DROP IF EXISTS `PROV_STATUS`;

ALTER TABLE `infra_active_requests` DROP IF EXISTS `ACTION`;

ALTER TABLE `archived_infra_requests` DROP IF EXISTS `ACTION`;

ALTER TABLE `infra_active_requests` DROP IF EXISTS `AAI_SERVICE_ID`;

ALTER TABLE `archived_infra_requests` DROP IF EXISTS `AAI_SERVICE_ID`;

ALTER TABLE `infra_active_requests` DROP IF EXISTS `REQUEST_TYPE`;

ALTER TABLE `archived_infra_requests` DROP IF EXISTS `REQUEST_TYPE`;
