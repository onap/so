-- Fix for https://itrack.web.att.com/browse/AJSCCMDA-90 --
use camundabpmn;

/* uncomment below statement and run for your db, e.g. : use camundabpmn;
 */
-- use <db_name>;

/* 
Drop a archive tables
*/
/*-- TMP_ARCHIVING_PROCINST */
DROP TABLE IF EXISTS TMP_ARCHIVING_PROCINST;

/*-- TMP_ARCHIVING_BYTEARRAY */
DROP TABLE IF EXISTS TMP_ARCHIVING_BYTEARRAY;

/*-- TMP LOG TABLE */
DROP TABLE IF EXISTS TMPLOGTABLE;

/* -- Camunda Hi Tables --*/
DROP TABLE IF EXISTS Camunda_Hi_Tables;

/* drop own extentions columns:
alter table  ARCHIVE_ACT_HI_PROCINST DROP (STAT_EXECUTION_ID, STAT_EXECUTION_TS);
alter table  ARCHIVE_ACT_HI_ACTINST DROP (STAT_EXECUTION_ID, STAT_EXECUTION_TS);
alter table  ARCHIVE_ACT_HI_TASKINST DROP (STAT_EXECUTION_ID, STAT_EXECUTION_TS);
alter table  ARCHIVE_ACT_HI_VARINST DROP (STAT_EXECUTION_ID, STAT_EXECUTION_TS);
alter table  ARCHIVE_ACT_HI_DETAIL DROP (STAT_EXECUTION_ID, STAT_EXECUTION_TS);
alter table  ARCHIVE_ACT_HI_COMMENT DROP (STAT_EXECUTION_ID, STAT_EXECUTION_TS);
alter table  ARCHIVE_ACT_HI_ATTACHMENT DROP (STAT_EXECUTION_ID, STAT_EXECUTION_TS);
alter table  ARCHIVE_ACT_HI_OP_LOG DROP (STAT_EXECUTION_ID, STAT_EXECUTION_TS);
alter table  ARCHIVE_ACT_HI_INCIDENT DROP (STAT_EXECUTION_ID, STAT_EXECUTION_TS);
*/

/*--#1 */
DROP TABLE IF EXISTS ARCHIVE_ACT_HI_PROCINST;
/*--#2 */
DROP TABLE IF EXISTS ARCHIVE_ACT_HI_ACTINST;
/*--#3 */
DROP TABLE IF EXISTS ARCHIVE_ACT_HI_TASKINST;
/*--#4 */
DROP TABLE IF EXISTS ARCHIVE_ACT_HI_VARINST;
/*--#5 */
DROP TABLE IF EXISTS ARCHIVE_ACT_HI_DETAIL;
/*--#6 */
DROP TABLE IF EXISTS ARCHIVE_ACT_HI_COMMENT;
/*--#7 */
DROP TABLE IF EXISTS ARCHIVE_ACT_HI_ATTACHMENT;
/*--#8 */
DROP TABLE IF EXISTS ARCHIVE_ACT_HI_OP_LOG;
/*--#9 */
DROP TABLE IF EXISTS ARCHIVE_ACT_HI_INCIDENT;
/*--#10 */
DROP TABLE IF EXISTS ARCHIVE_ACT_GE_BYTEARRAY;

/* drop PL SQL procedures: */
DROP PROCEDURE IF EXISTS ARCHIVE_CAMUNDA_HISTORY;
DROP PROCEDURE IF EXISTS ROLLB_ARCHIVE_CAMUNDA_HISTORY;
 
/*-- Sequence */
-- as sequence drop doesn't work automatically in MariaDB, use this procedure to drop sequence
 DROP PROCEDURE IF EXISTS DropSequence;
 
/*-- To Drop the MariaDB specific user defined procedures and functions */
DROP FUNCTION IF EXISTS NextVal;
DROP PROCEDURE IF EXISTS CreateSequence;
DROP PROCEDURE IF EXISTS DropSequence; 
DROP TABLE IF EXISTS _sequences;

/* -- If only the sequence: STAT_EXECUTION_SEQ needs to be removed, uncomment and use below statements --*/
/*
  DELIMITER //
  CREATE PROCEDURE DropSequence (vname VARCHAR(30))
  BEGIN
     -- Drop the sequence
     DELETE FROM _sequences WHERE name = vname;  
  END
  //
  DELIMITER ;

-- use the above procedure to drop sequence 
CALL DropSequence('STAT_EXECUTION_SEQ');
*/





/*
 1. Create starts - Add some Camunda Indexes to history schema part (for Archiving)
*/
create INDEX IF NOT EXISTS IDX_ACT_HI_TASKINST_PIID ON ACT_HI_TASKINST (PROC_INST_ID_); 
create INDEX IF NOT EXISTS IDX_ACT_HI_COMMENT_PIID ON ACT_HI_COMMENT (PROC_INST_ID_);
create INDEX IF NOT EXISTS IDX_ACT_HI_ATTACHMENT_PIID ON ACT_HI_ATTACHMENT (PROC_INST_ID_);
create INDEX IF NOT EXISTS IDX_ACT_HI_OP_LOG_PIID ON ACT_HI_OP_LOG (PROC_INST_ID_);
create INDEX IF NOT EXISTS IDX_ACT_HI_INCIDENT_PIID ON ACT_HI_INCIDENT (PROC_INST_ID_);
create INDEX IF NOT EXISTS IDX_ACT_HI_ACTINST_PIID ON ACT_HI_ACTINST(PROC_INST_ID_); 


/*
 2.  Create Archiving Tables in current schema 
*/

/*-- TMP_ARCHIVING_PROCINST */
CREATE TABLE TMP_ARCHIVING_PROCINST
( PROC_INST_ID_ varchar(64) not null,
  END_TIME_ datetime(3)
);
CREATE INDEX AI_TMP_ARCH_PROCINST_PI_ID ON TMP_ARCHIVING_PROCINST(PROC_INST_ID_);

/*-- TMP_ARCHIVING_BYTEARRAY */
CREATE TABLE TMP_ARCHIVING_BYTEARRAY
( BYTEARRAY_ID_ varchar(64) not null,
  PROC_INST_ID_ varchar(64)
);
CREATE INDEX AI_TMP_ARCH_BYTEARRAY_BAID ON TMP_ARCHIVING_BYTEARRAY(BYTEARRAY_ID_);


/*--#1 ARCHIVE_ACT_HI_PROCINST; */
create TABLE ARCHIVE_ACT_HI_PROCINST
AS ( select * from ACT_HI_PROCINST where 1=0);

create index AI_HI_PROCINST_END_TIME on ARCHIVE_ACT_HI_PROCINST(END_TIME_);
ALTER TABLE ARCHIVE_ACT_HI_PROCINST ADD CONSTRAINT  ARCHIVE_ACT_HI_PROCINST_UQ UNIQUE ( PROC_INST_ID_);

/*--#2   ARCHIVE_ACT_HI_ACTINST; */
create TABLE ARCHIVE_ACT_HI_ACTINST
AS ( select * from ACT_HI_ACTINST where 1=0);

create index AI_HI_ACTINST_PROC_INST_ID on ARCHIVE_ACT_HI_ACTINST(PROC_INST_ID_);
create index AI_HI_ACTINST_END_TIME on ARCHIVE_ACT_HI_ACTINST(END_TIME_);

/*--#3  ARCHIVE_ACT_HI_TASKINST; */
create TABLE ARCHIVE_ACT_HI_TASKINST
AS ( select * from ACT_HI_TASKINST where 1=0);

create index AI_HI_TASKINST_PROC_INST_ID on ARCHIVE_ACT_HI_TASKINST(PROC_INST_ID_);
create index AI_HI_TASKINST_END_TIME on ARCHIVE_ACT_HI_TASKINST(END_TIME_);

/*--#4 ARCHIVE_ACT_HI_VARINST; */
create TABLE ARCHIVE_ACT_HI_VARINST
AS ( select * from ACT_HI_VARINST where 1=0);

create index AI_HI_VARINST_PROC_INST_ID on ARCHIVE_ACT_HI_VARINST(PROC_INST_ID_);

/*--#5 ARCHIVE_ACT_HI_DETAIL; */
create TABLE ARCHIVE_ACT_HI_DETAIL
AS ( select * from ACT_HI_DETAIL where 1=0);

create index AI_HI_DETAIL_PROC_INST_ID on ARCHIVE_ACT_HI_DETAIL(PROC_INST_ID_);
create index AI_HI_DETAIL_TIME on ARCHIVE_ACT_HI_DETAIL(TIME_);

/*--#6 ARCHIVE_ACT_HI_COMMENT; */
create TABLE ARCHIVE_ACT_HI_COMMENT
AS ( select * from ACT_HI_COMMENT where 1=0);

create index AI_HI_COMMENT_PROC_INST_ID on ARCHIVE_ACT_HI_COMMENT(PROC_INST_ID_);
create index AI_HI_COMMENT_TIME on ARCHIVE_ACT_HI_COMMENT(TIME_);

/*--#7 ARCHIVE_ACT_HI_ATTACHMENT; */
create TABLE ARCHIVE_ACT_HI_ATTACHMENT
AS ( select * from ACT_HI_ATTACHMENT where 1=0);

create index AI_HI_ATTACHMENT_PROC_INST_ID on ARCHIVE_ACT_HI_ATTACHMENT(PROC_INST_ID_);

/*--#8 ARCHIVE_ACT_HI_OP_LOG; */
create TABLE ARCHIVE_ACT_HI_OP_LOG
AS ( select * from ACT_HI_OP_LOG where 1=0);

create index AI_HI_OP_LOG_PROC_INST_ID on ARCHIVE_ACT_HI_OP_LOG(PROC_INST_ID_);
create index AI_HI_OP_LOG_TIMESTAMP on ARCHIVE_ACT_HI_OP_LOG(TIMESTAMP_);

/*--#9 ARCHIVE_ACT_HI_INCIDENT; */
create TABLE ARCHIVE_ACT_HI_INCIDENT
AS ( select * from ACT_HI_INCIDENT where 1=0);

create index AI_HI_INCIDENT_PROC_INST_ID on ARCHIVE_ACT_HI_INCIDENT(PROC_INST_ID_);

/*--#10 ARCHIVE_ACT_GE_BYTEARRAY; */
create TABLE ARCHIVE_ACT_GE_BYTEARRAY
AS ( select * from ACT_GE_BYTEARRAY where 1=0);

create index AI_GE_BYTEARRAY_ID_ on ARCHIVE_ACT_GE_BYTEARRAY(ID_);

/* -----------------------------------------------------------------------------
Extend a ARCHIVE: Table by two attributes: STAT_EXECUTION_ID, STAT_EXECUTION_TS 
*/
 
/*
--TEMPLATE:
alter table ARCHIVE_%TableName%
    add (STAT_EXECUTION_ID bigint, STAT_EXECUTION_TS timestamp(0) DEFAULT CURRENT_TIMESTAMP );
CREATE INDEX AI_%TableName%_EXE_ID ON ARCHIVE_%TableName%(STAT_EXECUTION_ID);
*/


/*--#1 ACT_HI_PROCINST */
alter table ARCHIVE_ACT_HI_PROCINST
    add (STAT_EXECUTION_ID bigint, STAT_EXECUTION_TS timestamp(0) DEFAULT CURRENT_TIMESTAMP );
CREATE INDEX AI_ACT_HI_PROCINST_EXE_ID ON ARCHIVE_ACT_HI_PROCINST(STAT_EXECUTION_ID);

/*--#2 ACT_HI_ACTINST */
alter table ARCHIVE_ACT_HI_ACTINST
    add (STAT_EXECUTION_ID bigint, STAT_EXECUTION_TS timestamp(0) DEFAULT CURRENT_TIMESTAMP );
CREATE INDEX AI_ACT_HI_ACTINST_EXE_ID ON ARCHIVE_ACT_HI_ACTINST(STAT_EXECUTION_ID);

/*--#3 ACT_HI_TASKINST */
alter table ARCHIVE_ACT_HI_TASKINST
    add (STAT_EXECUTION_ID bigint, STAT_EXECUTION_TS timestamp(0) DEFAULT CURRENT_TIMESTAMP );
CREATE INDEX AI_ACT_HI_TASKINST_EXE_ID ON ARCHIVE_ACT_HI_TASKINST(STAT_EXECUTION_ID);

/*--#4 ACT_HI_VARINST */
alter table ARCHIVE_ACT_HI_VARINST
    add (STAT_EXECUTION_ID bigint, STAT_EXECUTION_TS timestamp(0) DEFAULT CURRENT_TIMESTAMP );
CREATE INDEX AI_ACT_HI_VARINST_EXE_ID ON ARCHIVE_ACT_HI_VARINST(STAT_EXECUTION_ID);

/*--#5 ACT_HI_DETAIL */
alter table ARCHIVE_ACT_HI_DETAIL
    add (STAT_EXECUTION_ID bigint, STAT_EXECUTION_TS timestamp(0) DEFAULT CURRENT_TIMESTAMP );
CREATE INDEX AI_ACT_HI_DETAIL_EXE_ID ON ARCHIVE_ACT_HI_DETAIL(STAT_EXECUTION_ID);

/*--#6 ACT_HI_COMMENT */
alter table ARCHIVE_ACT_HI_COMMENT
    add (STAT_EXECUTION_ID bigint, STAT_EXECUTION_TS timestamp(0) DEFAULT CURRENT_TIMESTAMP );
CREATE INDEX AI_ACT_HI_COMMENT_EXE_ID ON ARCHIVE_ACT_HI_COMMENT(STAT_EXECUTION_ID);

/*--#7 ACT_HI_ATTACHMENT */
alter table ARCHIVE_ACT_HI_ATTACHMENT
    add (STAT_EXECUTION_ID bigint, STAT_EXECUTION_TS timestamp(0) DEFAULT CURRENT_TIMESTAMP );
CREATE INDEX AI_ACT_HI_ATTACHMENT_EXE_ID ON ARCHIVE_ACT_HI_ATTACHMENT(STAT_EXECUTION_ID);

/*--#8 ACT_HI_OP_LOG */
alter table ARCHIVE_ACT_HI_OP_LOG
    add (STAT_EXECUTION_ID bigint, STAT_EXECUTION_TS timestamp(0) DEFAULT CURRENT_TIMESTAMP );
CREATE INDEX AI_ACT_HI_OP_LOG_EXE_ID ON ARCHIVE_ACT_HI_OP_LOG(STAT_EXECUTION_ID);

/*--#9 ACT_HI_INCIDENT */
alter table ARCHIVE_ACT_HI_INCIDENT
    add (STAT_EXECUTION_ID bigint, STAT_EXECUTION_TS timestamp(0) DEFAULT CURRENT_TIMESTAMP );
CREATE INDEX AI_ACT_HI_INCIDENT_EXE_ID ON ARCHIVE_ACT_HI_INCIDENT(STAT_EXECUTION_ID);

/*--#10 ACT_GE_BYTEARRAY */
alter table ARCHIVE_ACT_GE_BYTEARRAY
    add (STAT_EXECUTION_ID bigint, STAT_EXECUTION_TS timestamp(0) DEFAULT CURRENT_TIMESTAMP );
CREATE INDEX AI_ACT_GE_BYTEARRAY_EXE_ID ON ARCHIVE_ACT_GE_BYTEARRAY(STAT_EXECUTION_ID);


/* -- Next Val as a user defined function needed only in MariaDB--*/
DROP FUNCTION IF EXISTS NextVal;
  DELIMITER //
  CREATE FUNCTION NextVal (vname VARCHAR(30))
    RETURNS INT
  BEGIN
     -- Retrieve and update in single statement
     UPDATE _sequences
       SET next = next + 1
       WHERE name = vname;
 
     RETURN (SELECT next FROM _sequences LIMIT 1);
  END
  //
  DELIMITER ;
  
/* -- History tables for use in archive procedure, there is no array type in MariaDB --*/

Create Table Camunda_Hi_Tables (id_ INT NOT NULL,
TableName_ varchar(80) NOT NULL);

Insert Into Camunda_Hi_Tables Values (1,'ACT_HI_PROCINST');
Insert Into Camunda_Hi_Tables Values (2,'ACT_HI_ACTINST');
Insert Into Camunda_Hi_Tables Values (3,'ACT_HI_TASKINST');
Insert Into Camunda_Hi_Tables Values (4,'ACT_HI_VARINST');
Insert Into Camunda_Hi_Tables Values (5,'ACT_HI_DETAIL');
Insert Into Camunda_Hi_Tables Values (6,'ACT_HI_COMMENT');
Insert Into Camunda_Hi_Tables Values (7,'ACT_HI_ATTACHMENT');
Insert Into Camunda_Hi_Tables Values (8,'ACT_HI_OP_LOG');
Insert Into Camunda_Hi_Tables Values (9,'ACT_HI_INCIDENT');

/*-- log table --*/
CREATE TABLE TMPLOGTABLE (LogMessage Varchar(700));


/* -- Below user defined functions and procedures needed only in MariaDB, they are in-built in Oracle --*/
/*-- Create a sequence SP */
DROP PROCEDURE IF EXISTS CreateSequence;
  DELIMITER //
  CREATE PROCEDURE CreateSequence (name VARCHAR(30), start INT, inc INT)
  BEGIN
     -- Create a table to store sequences
     CREATE TABLE IF NOT EXISTS _sequences
     (
         name VARCHAR(70) NOT NULL UNIQUE,
         next INT NOT NULL,
         inc INT NOT NULL
     );
 
     -- Add the new sequence
     INSERT INTO _sequences VALUES (name, start, inc);  
  END
  //
  DELIMITER ;

/*--------------------------------------------------------------------------------------------------
 Add Meta to Archive
 -------------------------------------------------------------------------------------------------- */

/* Create STAT_EXECUTION_SEQ: each Archive Entry has a same Execution ID during one Archiving Run */
CALL CreateSequence('STAT_EXECUTION_SEQ', 1, 1);



/* 
ARCHIVE_CAMUNDA_HISTORY-Default Store Procedure starts
Camunda Version: 7.5.4-ee; MariaDB tested
Date: 11.30.2016 
Balaji Mudipalli, AJSC Camunda Team

DOC.:
--------------------------------------------------------------------------------------
Create  ARCHIVE_CAMUNDA_HISTORY StoreProcedure -function for archiving of history camunda tables.
*/

/* uncomment below statement and run for your db, e.g. : use camundabpmn;
 */

DROP PROCEDURE IF EXISTS ARCHIVE_CAMUNDA_HISTORY;

DELIMITER //

CREATE PROCEDURE ARCHIVE_CAMUNDA_HISTORY(IN IN_periodInDays INT, IN IN_maxProcessInstances INT)
MODIFIES SQL DATA 

BEGIN
 DECLARE P_hiTableCount INT;
 DECLARE P_executionId BIGINT;   
 DECLARE P_piProcessed DOUBLE;        
 DECLARE P_baProcessed DOUBLE;     
 DECLARE P_startDate DATE;          
 DECLARE P_executionDuration double;
 
 DECLARE not_found INT DEFAULT 0;     
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET not_found = 1; 

    /* START TRANSACTION */
    set P_startDate = sysdate();
    set P_executionId = NextVal('STAT_EXECUTION_SEQ');
    
   INSERT INTO TMPLOGTABLE SELECT CONCAT('P_executionId value is ',P_executionId);
    
   DELETE FROM TMP_ARCHIVING_PROCINST;
   DELETE FROM TMP_ARCHIVING_BYTEARRAY;
   -- temp table --
	DELETE FROM TMPLOGTABLE;  
	
		
	/* 1. Set Default Value for Max Pi's  */
	IF IN_maxProcessInstances = 0 THEN SET IN_maxProcessInstances = 1000; END IF;
	IF IN_maxProcessInstances > 1000 THEN SET IN_maxProcessInstances = 1000; END IF;
	
	INSERT INTO TMPLOGTABLE SELECT CONCAT('IN_maxProcessInstances value is: ',IN_maxProcessInstances);

    /* 2. Fill TMP_ARCHIVING_PROCINST with candidates: */
    IF IN_maxProcessInstances = 0 THEN /* all */
        INSERT INTO TMP_ARCHIVING_PROCINST
              SELECT hi.PROC_INST_ID_, hi.END_TIME_ 
                FROM ACT_HI_PROCINST hi 
                               WHERE hi.END_TIME_ IS NOT NULL  
                                     AND hi.END_TIME_ <= ( DATE_SUB(SYSDATE(), INTERVAL IN_periodInDays DAY));
                                    
		ELSE /* limit: IN_maxProcessInstances */
			INSERT INTO TMP_ARCHIVING_PROCINST
				(PROC_INST_ID_, END_TIME_) ( 
				  SELECT hi2.PROC_INST_ID_, hi2.END_TIME_ 
					FROM ACT_HI_PROCINST hi2 
								   WHERE hi2.END_TIME_ IS NOT NULL  
										 AND hi2.END_TIME_ <= ( DATE_SUB(SYSDATE(), INTERVAL IN_periodInDays DAY))  
				) LIMIT IN_maxProcessInstances;
    END IF;
    
    /* 3. Check PI's im TEMP if any found, ready for ACHIVING */
    select count(*) INTO P_piProcessed FROM TMP_ARCHIVING_PROCINST;
    
    IF P_piProcessed = 0 THEN 
    	INSERT INTO TMPLOGTABLE SELECT CONCAT('P_piProcessed value is: ',P_piProcessed);
        ROLLBACK;   
		/* 4. Move data from history to archive (insert to archive and delete in history) */ 
		ELSE
			SELECT COUNT(*)+1 INTO P_hiTableCount FROM camunda_hi_tables;
				INSERT INTO TMPLOGTABLE SELECT CONCAT('P_hiTableCount value is ',P_hiTableCount);
			SET @i = 1;
			WHILE @i < P_hiTableCount 
				DO 
					SELECT TableName_ INTO @P_tableName FROM camunda_hi_tables WHERE id_ = @i;
					INSERT INTO TMPLOGTABLE SELECT CONCAT('P_tableName: ', @P_tableName);
				
				   Set @P_archiveTableName = Concat('ARCHIVE_',IFNULL(@P_tableName, ''));
				   INSERT INTO TMPLOGTABLE SELECT CONCAT('@P_archiveTableName: ', @P_archiveTableName);
				   
				   SET @query1 = CONCAT('INSERT INTO ', @P_archiveTableName , 
						' SELECT hi3.*, ',P_executionId, ', NOW() FROM ', @P_tableName,' hi3 
						WHERE hi3.PROC_INST_ID_ in ( SELECT PROC_INST_ID_ FROM TMP_ARCHIVING_PROCINST)');
					INSERT INTO TMPLOGTABLE SELECT CONCAT('@query1: ', @query1);
						
					PREPARE stmt1 FROM @query1;		   
					EXECUTE stmt1; 
					DEALLOCATE PREPARE stmt1; 
					
					SET @query2 = CONCAT ('DELETE ACT FROM ',@P_tableName,' ACT INNER JOIN TMP_ARCHIVING_PROCINST TMP ON ACT.PROC_INST_ID_ = TMP.PROC_INST_ID_');
					INSERT INTO TMPLOGTABLE SELECT CONCAT('@query2: ', @query2);
						
					PREPARE stmt2 FROM @query2;		   
					EXECUTE stmt2; 
					DEALLOCATE PREPARE stmt2; 
					
        		SET @i = @i+1;
        	END WHILE;		
			/* select bytearray_ids */
			INSERT INTO TMP_ARCHIVING_BYTEARRAY
				SELECT BYTEARRAY_ID_, PROC_INST_ID_ FROM ARCHIVE_ACT_HI_VARINST archvar
				where archvar.PROC_INST_ID_ in (SELECT PROC_INST_ID_ FROM TMP_ARCHIVING_PROCINST)
				AND archvar.BYTEARRAY_ID_ is not null;
			
			INSERT INTO TMP_ARCHIVING_BYTEARRAY
				SELECT BYTEARRAY_ID_, PROC_INST_ID_ FROM ARCHIVE_ACT_HI_DETAIL archvar
				where archvar.PROC_INST_ID_ in (SELECT PROC_INST_ID_ FROM TMP_ARCHIVING_PROCINST)
				AND archvar.BYTEARRAY_ID_ is not null;  
        
			/* 5. Check Bytearrays im TEMP if any found, ready for ACHIVING */
			select count(*) INTO P_baProcessed FROM TMP_ARCHIVING_BYTEARRAY;
			
			/* INSERT */
			INSERT INTO ARCHIVE_ACT_GE_BYTEARRAY  
				SELECT hi4.*, P_executionId, NOW() FROM ACT_GE_BYTEARRAY hi4 
				WHERE hi4.ID_ in ( SELECT BYTEARRAY_ID_ FROM TMP_ARCHIVING_BYTEARRAY);
			
			/* DELETE */
			DELETE FROM ACT_GE_BYTEARRAY WHERE ID_ in (select BYTEARRAY_ID_ FROM TMP_ARCHIVING_BYTEARRAY);
			
			/* COMMIT TRANSACTION */
			COMMIT;
			INSERT INTO TMPLOGTABLE SELECT CONCAT('P_executionId is ', P_executionId);	
			
		set P_executionDuration = DATEDIFF(sysdate(), P_startDate);
		
		INSERT INTO TMPLOGTABLE SELECT CONCAT('SP success and P_executionDuration is ', ifnull((round(P_executionDuration*24*60*60, 1)), ''), ' sec.');
    END IF;
   
 END;
//

DELIMITER ;



/* 
ROLLB_ARCHIVE_CAMUNDA_HISTORY-StoreProcedure starts
Camunda Version: 7.5.4-ee; MariaDB tested
Date: 11.30.2016 
Balaji Mudipalli, AJSC Camunda Team

DOC.:
--------------------------------------------------------------------------------------
Create  ROLLB_ARCHIVE_CAMUNDA_HISTORY StoreProcedure for ROLLBACK (RESTORE)
of archived Camunda history tables.
*/

/* uncomment below statement and run for your db, e.g. : use camundabpmn;
 */
-- use <db_name>;


DROP PROCEDURE IF EXISTS ROLLB_ARCHIVE_CAMUNDA_HISTORY;

DELIMITER //
CREATE PROCEDURE ROLLB_ARCHIVE_CAMUNDA_HISTORY(IN IN_executionId_from INT, 
                                                            IN IN_executionId_til INT,
                                                            IN IN_maxProcessInstances INT)
MODIFIES SQL DATA

BEGIN
 DECLARE P_hiTableCount INT;
 DECLARE P_piProcessed DOUBLE;        
 DECLARE P_baProcessed DOUBLE;        
 DECLARE P_query VARCHAR(600);         
 DECLARE P_startDate DATETIME;             
 DECLARE P_executionDuration DOUBLE;
 DECLARE P_result NVARCHAR(400);
DECLARE not_found INT DEFAULT 0;       
      DECLARE CONTINUE HANDLER FOR NOT FOUND SET not_found = 1;
 
                      
    /* START TRANSACTION */
    SET P_startDate = sysdate();
	DELETE FROM TMPLOGTABLE;  
    
	INSERT INTO TMPLOGTABLE SELECT CONCAT('[ROLLB_ARCHIVE_CAMUNDA_HISTORY]:  START EXECUTION: ' , ifnull(date_format(current_timestamp, '%d.%m.%Y %H:%i:%s ..FF3'), '') ,
                           '; PARAMS: IN_executionId_from: ' , IFNULL(IN_executionId_from, '') , 
                           '; IN_executionId_til: ' , IFNULL(IN_executionId_til, '') ,
                           '; IN_maxProcessInstances: ' , IFNULL(IN_maxProcessInstances, ''));
                                                            
    /* 1. Truncate TMP_ARCHIVING_PROCINST */
    
    DELETE FROM TMP_ARCHIVING_PROCINST;
    DELETE FROM TMP_ARCHIVING_BYTEARRAY;
    
    /* 2. Fill TMP_ARCHIVING_PROCINST with candidates: */
    IF IN_executionId_til = -1 THEN /* IN_executionId_from only */
        SET P_query= CONCAT(' WHERE STAT_EXECUTION_ID = ' , IFNULL(IN_executionId_from, ''));
    
    ELSEIF IN_executionId_til = 0 THEN /* all from IN_executionId_from */
        SET P_query= CONCAT(' WHERE STAT_EXECUTION_ID >= ' , IFNULL(IN_executionId_from, ''));
    
    ELSE /* between IN_executionId_from AND IN_executionId_til */
        SET P_query= CONCAT(' WHERE STAT_EXECUTION_ID between ', IFNULL(IN_executionId_from, '') , ' AND ' , IFNULL(IN_executionId_til, ''));
    END IF;
    
    IF IN_maxProcessInstances = 0 THEN /* all */
    
        SET @P_query1 = CONCAT('INSERT INTO TMP_ARCHIVING_PROCINST ', ifnull(char(13 using ascii), ''),ifnull(char(10 using ascii), ''),
                   '   SELECT PROC_INST_ID_, END_TIME_ FROM ARCHIVE_ACT_HI_PROCINST ', ifnull(char(13 using ascii), ''),ifnull(char(10 using ascii), ''),
                   '   ',  Ifnull(P_query, ''));
                   
    ELSE /* limit: IN_maxProcessInstances */
        SET @P_query1 = CONCAT('INSERT INTO TMP_ARCHIVING_PROCINST ' ,ifnull(char(13 using ascii), ''),ifnull(char(10 using ascii), ''),
                   ' (PROC_INST_ID_, END_TIME_ ) ( ' ,ifnull(char(13 using ascii), ''),ifnull(char(10 using ascii), ''),
                   '   SELECT PROC_INST_ID_, END_TIME_ FROM ARCHIVE_ACT_HI_PROCINST ' ,ifnull(char(13 using ascii), ''),ifnull(char(10 using ascii), ''),
                   '   ',  Ifnull(P_query, '') , ifnull(char(13 using ascii), ''),ifnull(char(10 using ascii), ''),
                   ') LIMIT ', IFNULL(IN_maxProcessInstances, ''));   
    END IF;
    
    INSERT INTO TMPLOGTABLE SELECT CONCAT('QUERY (before execute): /fill temp table with PI candidates/ ' , Ifnull(P_query, ''));
	
	PREPARE stmt1 FROM @P_query1;		   
	EXECUTE stmt1; 
	DEALLOCATE PREPARE stmt1; 
	
    INSERT INTO TMPLOGTABLE SELECT CONCAT('.... rows inserted into TMP_ARCHIVING_PROCINST: ' , IFNULL((ROW_COUNT()), ''));
    
    /* 3. Fill TMP_ARCHIVING_BYTEARRAYS with candidates:  */
    INSERT INTO TMP_ARCHIVING_BYTEARRAY
        SELECT BYTEARRAY_ID_, PROC_INST_ID_ FROM ARCHIVE_ACT_HI_VARINST archvar
        where archvar.PROC_INST_ID_ in (SELECT PROC_INST_ID_ FROM TMP_ARCHIVING_PROCINST)
        AND archvar.BYTEARRAY_ID_ is not null;
        
    INSERT INTO TMP_ARCHIVING_BYTEARRAY
        SELECT BYTEARRAY_ID_, PROC_INST_ID_ FROM ARCHIVE_ACT_HI_DETAIL archvar
        where archvar.PROC_INST_ID_ in (SELECT PROC_INST_ID_ FROM TMP_ARCHIVING_PROCINST)
        AND archvar.BYTEARRAY_ID_ is not null;  
        
    select count(*) INTO P_baProcessed FROM TMP_ARCHIVING_BYTEARRAY;
    INSERT INTO TMPLOGTABLE SELECT CONCAT('[ROLLB_ARCHIVE_CAMUNDA_HISTORY]: ', IFNULL(P_baProcessed, '') ,' ByteArray candidates for rollback found!' , ifnull(char(13 using ascii), ''),ifnull(char(10 using ascii), ''));
    
               
    /* 4. Check PI's im TEMP ready for ROLLBACK */
    select count(*) INTO P_piProcessed FROM TMP_ARCHIVING_PROCINST;
    
    IF P_piProcessed = 0 THEN /* no candidates found */
        INSERT INTO TMPLOGTABLE SELECT CONCAT('[ROLLB_ARCHIVE_CAMUNDA_HISTORY]: NO ProcessInstance-Candidates for archive-Rollback found! ');
        INSERT INTO TMPLOGTABLE SELECT CONCAT('Try TA-ROLLBACK ...');
        ROLLBACK;  /*-- TMP_ARCHIVING_PROCINST un-Delete */
        INSERT INTO TMPLOGTABLE SELECT CONCAT('TA-ROLLBACK DONE! ...' ,ifnull(char(13 using ascii), ''),ifnull(char(10 using ascii), ''));
        
        SET P_result = CONCAT('[ROLLB_ARCHIVE_CAMUNDA_HISTORY]:  NO ProcessInstance candidates for archive-Rollback found!', ifnull(char(13 using ascii), ''),ifnull(char(10 using ascii), ''),
                    ifnull(date_format(current_timestamp, '%d.%m.%Y %H:%i:%s ..FF3'), '') , ifnull(char(13 using ascii), ''),ifnull(char(10 using ascii), ''),
                    ' Used PARAMS: IN_executionId_from: ' , IFNULL(IN_executionId_from, '') , 
                           '; IN_executionId_til: ' , IFNULL(IN_executionId_til, '') ,
                           '; IN_maxProcessInstances: ' , IFNULL(IN_maxProcessInstances, ''));
        
        INSERT INTO TMPLOGTABLE SELECT CONCAT(P_result);
    
    ELSE
        INSERT INTO TMPLOGTABLE SELECT CONCAT('[ROLLB_ARCHIVE_CAMUNDA_HISTORY]: ', IFNULL(P_piProcessed, '') ,' ProcessInstance candidates for Rollback found!');
    
        /* LOOP over tables */
        SELECT COUNT(*)+1 INTO P_hiTableCount FROM camunda_hi_tables;
			
			INSERT INTO TMPLOGTABLE SELECT CONCAT('P_hiTableCount value is ',P_hiTableCount);
				
			SET @i = 1;
			
			WHILE @i < P_hiTableCount 
				DO 
					SELECT TableName_ INTO @P_tableName FROM camunda_hi_tables WHERE id_ = @i;
					
					INSERT INTO TMPLOGTABLE SELECT CONCAT('[ROLLB_ARCHIVE_CAMUNDA_HISTORY]: #######  Start restore from:  ARCHIVE_' , IFNULL(@P_tableName, '') ,'  ...');
					
					SET @P_tableFields = CONCAT(''); 
					
					select GROUP_CONCAT(column_name order by ordinal_position) 
						INTO @P_tableFields 
						from information_schema.columns  
						where table_schema = (select DATABASE()) AND TABLE_NAME = @P_tableName;

					/* INSERT */
					SET @P_query2 = CONCAT('INSERT INTO ', IFNULL(@P_tableName, '') ,
							   ' SELECT ' , @P_tableFields, 
							   '  FROM ARCHIVE_' , IFNULL(@P_tableName, '') ,
							   '  WHERE PROC_INST_ID_ in ( SELECT tmp.PROC_INST_ID_ FROM TMP_ARCHIVING_PROCINST tmp)');
							   
					INSERT INTO TMPLOGTABLE SELECT CONCAT('QUERY (before execute): /copy back to history table/ ' , Ifnull(@P_query2, ''));

						PREPARE stmt2 FROM @P_query2;		   
						EXECUTE stmt2; 
						DEALLOCATE PREPARE stmt2; 
						
					INSERT INTO TMPLOGTABLE SELECT Concat('.... rows inserted: ' , IFNULL((ROW_COUNT()), ''));
					
					/* DELETE */
					INSERT INTO TMPLOGTABLE SELECT CONCAT('[ROLLB_ARCHIVE_CAMUNDA_HISTORY]:         Delete in Archive: ARCHIVE_' , IFNULL(@P_tableName, '') ,'  ...');          
					/* SET @P_query3 = CONCAT(' DELETE FROM ARCHIVE_' , IFNULL(@P_tableName, '') , ' WHERE PROC_INST_ID_ in (select PROC_INST_ID_ FROM TMP_ARCHIVING_PROCINST)'); */
					SET @P_query3 = CONCAT('DELETE ARCH FROM ARCHIVE_' , IFNULL(@P_tableName, '') , 
									' ARCH INNER JOIN TMP_ARCHIVING_PROCINST TMP ON ARCH.PROC_INST_ID_ = TMP.PROC_INST_ID_');
					INSERT INTO TMPLOGTABLE SELECT CONCAT('QUERY (before execute): ' , Ifnull(@P_query3, ''));
						PREPARE stmt3 FROM @P_query3;		   
						EXECUTE stmt3; 
						DEALLOCATE PREPARE stmt3; 
						
					INSERT INTO TMPLOGTABLE SELECT Concat('.... rows deleted: ' , IFNULL((ROW_COUNT()), '') , ifnull(char(13 using ascii), ''),ifnull(char(10 using ascii), ''));
					
				SET @i = @i+1;
			END WHILE;
        
        /* INSERT */
        SET @P_tableFields2 = CONCAT(''); /* reset, becouse had some problems with double columns  */
        /* fetch table column names into P_tableFields : */
        select GROUP_CONCAT(COLUMN_NAME order by ordinal_position) 
			INTO @P_tableFields2 
            from information_schema.columns  
            where table_schema = (select DATABASE()) AND TABLE_NAME = 'ACT_GE_BYTEARRAY';
            
        SET @P_query4 = CONCAT('INSERT INTO ACT_GE_BYTEARRAY ' ,ifnull(char(13 using ascii), ''),ifnull(char(10 using ascii), ''),
                   ' SELECT ' , IFNULL(@P_tableFields2, '') ,ifnull(char(13 using ascii), ''),ifnull(char(10 using ascii), ''),
                   '  FROM ARCHIVE_ACT_GE_BYTEARRAY' ,ifnull(char(13 using ascii), ''),ifnull(char(10 using ascii), ''), 
                   '  WHERE ID_ in ( SELECT tmp.BYTEARRAY_ID_ FROM TMP_ARCHIVING_BYTEARRAY tmp)');
        INSERT INTO TMPLOGTABLE SELECT CONCAT('QUERY (before execute): /copy back to history table/ ' , Ifnull(@P_query4, ''));
		PREPARE stmt4 FROM @P_query4;		   
		EXECUTE stmt4; 
		DEALLOCATE PREPARE stmt4; 
        INSERT INTO TMPLOGTABLE SELECT Concat('.... rows inserted: ' , IFNULL((ROW_COUNT()), ''));
        
        /* DELETE */
        /* DELETE FROM ARCHIVE_ACT_GE_BYTEARRAY WHERE ID_ in (select BYTEARRAY_ID_ FROM TMP_ARCHIVING_BYTEARRAY); */
		DELETE AAGB FROM ARCHIVE_ACT_GE_BYTEARRAY AAGB INNER JOIN TMP_ARCHIVING_BYTEARRAY TMP_B ON AAGB.ID_ = TMP_B.BYTEARRAY_ID_;
        INSERT INTO TMPLOGTABLE SELECT Concat('.... rows deleted: ' , IFNULL((ROW_COUNT()), '') , ifnull(char(13 using ascii), ''),ifnull(char(10 using ascii), '')); 
        
        /* COMMIT TRANSACTION */
        INSERT INTO TMPLOGTABLE SELECT CONCAT('[ROLLB_ARCHIVE_CAMUNDA_HISTORY]: PIs processed: ' , IFNULL(P_piProcessed, '')) ;
        COMMIT;
        INSERT INTO TMPLOGTABLE SELECT CONCAT('TA-COMMIT DONE!' ,ifnull(char(13 using ascii), ''),ifnull(char(10 using ascii), ''));
        
        SET P_executionDuration = DATEDIFF(sysdate(), P_startDate);
        
        SET P_result = CONCAT('[ROLLB_ARCHIVE_CAMUNDA_HISTORY]:  EXECUTED (commited) successfully! ' , ifnull(char(13 using ascii), ''),ifnull(char(10 using ascii), ''),
                ifnull(date_format(current_timestamp, '%d.%m.%Y %H:%i:%s ..FF3'), '') , '; Duration: ' , ifnull((round(P_executionDuration*24*60*60, 1)), ''), ' sec.' ,  ifnull(char(13 using ascii), ''),ifnull(char(10 using ascii), '') ,
                ' PIs processed: ' , IFNULL(P_piProcessed, '') , ifnull(char(13 using ascii), ''),ifnull(char(10 using ascii), ''),
                ' Used PARAMS: IN_executionId_from: ' , IFNULL(IN_executionId_from, '') , 
                           '; IN_executionId_til: ' , IFNULL(IN_executionId_til, '') ,
                           '; IN_maxProcessInstances: ' , IFNULL(IN_maxProcessInstances, ''));
                           
        INSERT INTO TMPLOGTABLE SELECT CONCAT(P_result);                        
    END IF;
 END;
//

DELIMITER ;
