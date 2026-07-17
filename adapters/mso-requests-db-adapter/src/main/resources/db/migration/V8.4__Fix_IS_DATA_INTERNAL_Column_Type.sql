use requestdb;

-- IS_DATA_INTERNAL was originally created as TINYINT (V5.10) but the JPA entity
-- RequestProcessingData maps it to a java.lang.Boolean. Hibernate 6.6 (Spring Boot
-- 3.5) validates a Boolean field against SQL type BIT/BOOLEAN and rejects the TINYINT
-- column, failing startup with ddl-auto: validate. Convert it to BIT to match the
-- entity mapping (aligning with e.g. site_status.STATUS which is already bit(1)).
ALTER TABLE request_processing_data MODIFY COLUMN IS_DATA_INTERNAL BIT NOT NULL DEFAULT 0;
