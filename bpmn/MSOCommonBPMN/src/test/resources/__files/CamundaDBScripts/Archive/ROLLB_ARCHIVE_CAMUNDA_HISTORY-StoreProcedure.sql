/* 
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
use camundabpmn;

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
