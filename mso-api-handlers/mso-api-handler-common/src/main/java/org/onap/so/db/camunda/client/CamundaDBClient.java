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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@Component
@Slf4j
public class CamundaDBClient {

    private final EntityManager entityManager;

    public CamundaDBClient(@Qualifier("camundaEntityManagerFactory") EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // Correct query: Finds the latest started BB that has no corresponding End_
    private static final String sqlQuery = "SELECT start.ACT_ID_ AS resume_from " + "FROM ACT_HI_ACTINST start "
            + "LEFT JOIN ACT_HI_ACTINST end ON end.ACT_ID_ = REPLACE(start.ACT_ID_, 'Start_', 'End_') "
            + "AND end.PROC_INST_ID_ = start.PROC_INST_ID_ " + "WHERE start.ACT_ID_ LIKE 'Start_%BB' "
            + "AND start.ACT_ID_ != 'Start_WorkflowActionBB' " + "AND start.PROC_INST_ID_ IN ( "
            + "  SELECT PROC_INST_ID_ FROM ACT_HI_PROCINST WHERE BUSINESS_KEY_ = ?1" + ") " + "AND end.ACT_ID_ IS NULL "
            + "ORDER BY start.START_TIME_ ASC LIMIT 1";

    private static final String sql =
            "select PROC_INST_ID_ from ACT_HI_PROCINST where BUSINESS_KEY_ =?1 AND STATE_='SUSPENDED'";

    public String findResumeFromBB(String requestId) {
        String bbName = null;
        try {
            log.trace("****** In Try Block to fetch resumeFrom ****** CamundaDBClient");

            Query query = entityManager.createNativeQuery(sqlQuery);
            String resumeFrom = query.setParameter(1, requestId).getSingleResult().toString();
            log.info("****** Found resumeFrom Building block in Camunda DB: {} ******", resumeFrom);

            // Clean prefix to get only BB name
            bbName = stripStartPrefix(resumeFrom);
        } catch (Exception e) {
            log.error("****** Error querying resumeFrom from Camunda DB: {} ******", e.getMessage());
        }
        return bbName;
    }

    public String findProcessInstanceId(String requestId) {
        String processInstanceId = null;
        try {
            log.trace("****** In Try Block to fetch processInstanceId ****** CamundaDBClient");

            Query query = entityManager.createNativeQuery(sql);
            processInstanceId = query.setParameter(1, requestId).getSingleResult().toString();
            log.info("****** Found processInstanceId in Camunda DB: {} ******", processInstanceId);
        } catch (Exception e) {
            log.error("****** Error querying from Camunda DB: {} ******", e.getMessage());
        }
        return processInstanceId;
    }

    protected String stripStartPrefix(String resumeFrom) {
        String bbName = "";
        if (resumeFrom != null && resumeFrom.startsWith("Start_")) {
            bbName = resumeFrom.substring(6);
            log.info("Cleaned resumeFrom Building block: {}", bbName);
        }
        return bbName;
    }
}
