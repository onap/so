USE catalogdb;

create table if not exists model_recipe (
	`ID` INT(11) NOT NULL AUTO_INCREMENT,
	`MODEL_ID` INT(11),
	`ACTION` VARCHAR(40),
	`SCHEMA_VERSION` VARCHAR(40),
	`DESCRIPTION` VARCHAR(40),
	`ORCHESTRATION_URI` VARCHAR(20),
	`MODEL_PARAM_XSD` VARCHAR(20),
	`RECIPE_TIMEOUT` INT(11),
	`CREATION_TIMESTAMP` datetime not null default current_timestamp,
	PRIMARY KEY (`ID`),
	CONSTRAINT uk1_model_recipe UNIQUE (`MODEL_ID`, `ACTION`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

create table if not exists model (
	`ID` INT(11) NOT NULL AUTO_INCREMENT,
	`MODEL_CUSTOMIZATION_ID` VARCHAR(40),
	`MODEL_CUSTOMIZATION_NAME` VARCHAR(40),
	`MODEL_INVARIANT_ID` VARCHAR(40),
	`MODEL_NAME` VARCHAR(40),
	`MODEL_TYPE` VARCHAR(20),
	`MODEL_VERSION` VARCHAR(20),
	`MODEL_VERSION_ID` VARCHAR(40),
	`CREATION_TIMESTAMP` datetime not null default current_timestamp,
	`RECIPE` INT(11),
	PRIMARY KEY (`ID`),
	CONSTRAINT uk1_model UNIQUE (`MODEL_TYPE`, `MODEL_VERSION_ID`),
	FOREIGN KEY (`RECIPE`) REFERENCES `model_recipe` (`MODEL_ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;