use catalogdb;

DELETE FROM activity_spec_to_activity_spec_parameters;
DELETE FROM activity_spec_to_activity_spec_categories;
DELETE FROM activity_spec;
DELETE FROM activity_spec_categories;
DELETE FROM activity_spec_parameters;

INSERT INTO activity_spec (NAME, DESCRIPTION, VERSION) 
VALUES ('VNFSetInMaintFlagActivity','Activity to Set InMaint Flag in A&AI',1.0);

INSERT INTO activity_spec_categories (NAME)
VALUES ('VNF');

INSERT INTO activity_spec_parameters (NAME, TYPE, DIRECTION, DESCRIPTION) 
VALUES('WorkflowException','WorkflowException','outputParameters','Description');

INSERT INTO activity_spec_to_activity_spec_categories(ACTIVITY_SPEC_ID, ACTIVITY_SPEC_CATEGORIES_ID) 
VALUES
(
(select ID from ACTIVITY_SPEC where NAME='VNFSetInMaintFlagActivity' and VERSION=1.0),
(select ID from activity_spec_categories where NAME='VNF'));

INSERT INTO activity_spec_to_activity_spec_parameters( ACTIVITY_SPEC_ID, ACTIVITY_SPEC_PARAMETERS_ID) 
VALUES(
(select ID from ACTIVITY_SPEC where NAME='VNFSetInMaintFlagActivity' and VERSION=1.0),
(select ID from activity_spec_parameters where NAME='WorkflowException' and DIRECTION='outputParameters'));