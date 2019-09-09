use requestdb;

DROP INDEX `infra_active_requests__aic_cloud_region_idx` on `infra_active_requests`;

ALTER TABLE
  `infra_active_requests` CHANGE AIC_CLOUD_REGION CLOUD_REGION varchar(50) DEFAULT NULL;

ALTER TABLE
  `archived_infra_requests` CHANGE AIC_CLOUD_REGION CLOUD_REGION VARCHAR(50) NULL DEFAULT NULL;

ALTER TABLE `infra_active_requests` 
	ADD INDEX IF NOT EXISTS `infra_active_requests__cloud_region_idx` (`CLOUD_REGION` ASC);
