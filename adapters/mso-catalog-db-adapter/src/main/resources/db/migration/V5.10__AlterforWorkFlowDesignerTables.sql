use catalogdb;

ALTER TABLE activity_spec_to_activity_spec_categories DROP FOREIGN KEY fk_activity_spec_to_activity_spec_categories__activity_spec_c1;

ALTER TABLE activity_spec_categories modify id INT(11) not null auto_increment;

ALTER TABLE activity_spec_to_activity_spec_categories ADD 
 CONSTRAINT `fk_activity_spec_to_activity_spec_categories__activity_spec_c1` 
    FOREIGN KEY (`ACTIVITY_SPEC_CATEGORIES_ID`) 
    REFERENCES `activity_spec_categories` (`ID`) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE; 
    
ALTER TABLE activity_spec_to_activity_spec_categories modify id INT(11) not null auto_increment; 

ALTER TABLE WORKFLOW MODIFY COLUMN VERSION VARCHAR(200)  NOT NULL; 

ALTER TABLE ACTIVITY_SPEC MODIFY COLUMN VERSION VARCHAR(200) NOT NULL;