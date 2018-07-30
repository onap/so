use catalogdb;
create table if not exists ar_recipe (
    ID INT(11) not null auto_increment,
    MODEL_NAME varchar(200) NOT NULL,
    ACTION varchar(200) NOT NULL,
    VERSION_STR varchar(200) NOT NULL,
    SERVICE_TYPE varchar(200),
    DESCRIPTION varchar(200),
    ORCHESTRATION_URI varchar(200) NOT NULL,
    AR_PARAM_XSD varchar(200),
    RECIPE_TIMEOUT INT(11),
    CREATION_TIMESTAMP DATETIME NOT NULL default current_timestamp,
    primary key (ID),
    unique key `uk_ar_recipe` (`model_name`,`action`,`version_str`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

alter table network_recipe 
    ADD RESOURCE_CATEGORY varchar(200),
	ADD RESOURCE_SUB_CATEGORY varchar(200);
  
alter table service ADD SERVICE_CATEGORY varchar(200);

alter table vnf_resource 
	ADD RESOURCE_CATEGORY varchar(200),
	ADD RESOURCE_SUB_CATEGORY varchar(200);
	
alter table network_resource 
    ADD RESOURCE_CATEGORY varchar(200),
    ADD RESOURCE_SUB_CATEGORY varchar(200);
  
