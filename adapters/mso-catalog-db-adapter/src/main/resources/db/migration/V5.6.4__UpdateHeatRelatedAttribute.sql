use catalogdb;

ALTER TABLE network_resource MODIFY COLUMN AIC_VERSION_MIN varchar(20) NULL;
ALTER TABLE network_resource MODIFY COLUMN HEAT_TEMPLATE_ARTIFACT_UUID varchar(200) NULL;
ALTER TABLE network_resource DROP FOREIGN KEY fk_network_resource__temp_network_heat_template_lookup__mod_nm1;