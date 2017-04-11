/* 
Camunda Version: 7.5.4-ee; MariaDB tested
Date: 11.30.2016 
Balaji Mudipalli, AJSC Camunda Team

DOC.:
--------------------------------------------------------------------------------------
Create  ARCHIVE_CAMUNDA_HISTORY StoreProcedure -function for archiving of history camunda tables.
*/

/* uncomment below statement and run for your db, e.g. : use camundabpmn;
 */
-- use <db_name>;
use camundabpmn;
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