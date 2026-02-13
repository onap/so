/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.db.camunda.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@Component
public class CamundaDBClient {
    @Autowired
    @Qualifier("camundaEntityManagerFactory")
    private EntityManager entityManager;

    protected static Logger logger = LoggerFactory.getLogger(CamundaDBClient.class);

    public String findResumeFromBB(String requestId) {
        String resumeFrom = null;
        try {
            logger.info("****** In Try Block to fetch resumeFrom ****** CamundaDBClient");

            // Correct query: Finds the latest started BB that has no corresponding End_
            String sql = "SELECT start.ACT_ID_ AS resume_from " + "FROM ACT_HI_ACTINST start "
                    + "LEFT JOIN ACT_HI_ACTINST end ON end.ACT_ID_ = REPLACE(start.ACT_ID_, 'Start_', 'End_') "
                    + "AND end.PROC_INST_ID_ = start.PROC_INST_ID_ " + "WHERE start.ACT_ID_ LIKE 'Start_%BB' "
                    + "AND start.ACT_ID_ != 'Start_WorkflowActionBB' " + "AND start.PROC_INST_ID_ IN ( "
                    + "  SELECT PROC_INST_ID_ FROM ACT_HI_PROCINST WHERE BUSINESS_KEY_ = ?1" + ") "
                    + "AND end.ACT_ID_ IS NULL " + "ORDER BY start.START_TIME_ ASC LIMIT 1";

            Query query = entityManager.createNativeQuery(sql);
            resumeFrom = query.setParameter(1, requestId).getSingleResult().toString();
            logger.info("****** Found resumeFrom Building block in Camunda DB: {} ******", resumeFrom);

            // Clean prefix to get only BB name
            if (resumeFrom != null && resumeFrom.startsWith("Start_")) {
                resumeFrom = resumeFrom.substring(6);
                logger.info("****** Cleaned resumeFrom Building block: {} ******", resumeFrom);
            }

        } catch (Exception e) {
            logger.error("****** Error querying resumeFrom from Camunda DB: {} ******", e.getMessage());
        }
        return resumeFrom;
    }

    public String findProcessInstanceId(String requestId) {
        String processInstanceId = null;
        try {
            logger.info("****** In Try Block to fetch processInstanceId ****** CamundaDBClient");

            String sql = "select PROC_INST_ID_ from ACT_HI_PROCINST where BUSINESS_KEY_ =?1 AND STATE_='SUSPENDED'";
            Query query = entityManager.createNativeQuery(sql);
            processInstanceId = query.setParameter(1, requestId).getSingleResult().toString();
            logger.info("****** Found processInstanceId in Camunda DB: {} ******", processInstanceId);
        } catch (Exception e) {
            logger.error("****** Error querying from Camunda DB: {} ******", e.getMessage());
        }
        return processInstanceId;
    }
}
