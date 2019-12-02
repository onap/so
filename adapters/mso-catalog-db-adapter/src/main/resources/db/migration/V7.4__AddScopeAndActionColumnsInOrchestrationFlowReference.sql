use catalogdb;

ALTER TABLE orchestration_flow_reference
ADD SCOPE varchar(200) DEFAULT NULL,
ADD ACTION varchar(200) DEFAULT NULL;