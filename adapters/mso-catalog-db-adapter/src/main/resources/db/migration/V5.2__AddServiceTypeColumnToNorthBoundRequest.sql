use catalogdb;

ALTER  TABLE northbound_request_ref_lookup ADD COLUMN SERVICE_TYPE varchar(200) NULL;
ALTER TABLE northbound_request_ref_lookup
 DROP INDEX UK_northbound_request_ref_lookup;
ALTER TABLE northbound_request_ref_lookup                                                                                                                                                                                                                                                               
ADD UNIQUE INDEX `UK_northbound_request_ref_lookup` (`MIN_API_VERSION` ASC, `REQUEST_SCOPE` ASC, `ACTION` ASC, `IS_ALACARTE` ASC, `MACRO_ACTION` ASC, `CLOUD_OWNER` ASC, `SERVICE_TYPE` ASC);