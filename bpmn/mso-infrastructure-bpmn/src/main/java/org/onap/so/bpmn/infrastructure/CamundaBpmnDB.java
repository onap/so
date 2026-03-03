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
package org.onap.so.bpmn.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class CamundaBpmnDB {

    private final EntityManager entityManager;

    public CamundaBpmnDB(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private static final String PROCESS_INST_ID_QUERY =
            "select PROC_INST_ID_ from ACT_HI_PROCINST where START_ACT_ID_='Start_WorkflowActionBB' and STATE_='ACTIVE'";

    private static final String BUSINESS_KEY_QUERY =
            "select BUSINESS_KEY_ from ACT_HI_PROCINST where PROC_INST_ID_ =?1";

    public List<String> fetchListOfProcessInstanceId() {
        List<String> listOfProcessInstanceId = new ArrayList<>();
        try {
            log.trace("****** In Try Block to fetch listOfProcessInstanceId ****** CamundaDBClient");
            Query query = entityManager.createNativeQuery(PROCESS_INST_ID_QUERY);
            listOfProcessInstanceId = query.getResultList();
            log.info("****** Found listOfProcessInstanceId in Camunda DB: {} ******", listOfProcessInstanceId);
        } catch (Exception e) {
            log.error("****** Error querying from Camunda DB: {} ******", e.getMessage());
        }
        return listOfProcessInstanceId;
    }

    public String findBusinessKey(String processInstanceId) {
        String businessKey = null;
        try {
            log.trace("****** In Try Block to fetch businessKey ****** CamundaDBClient");
            Query query = entityManager.createNativeQuery(BUSINESS_KEY_QUERY);
            businessKey = query.setParameter(1, processInstanceId).getSingleResult().toString();
            log.info("****** Found businessKey in Camunda DB: {} ******", businessKey);
        } catch (Exception e) {
            log.error("****** Error querying from Camunda DB: {} ******", e.getMessage());
        }
        return businessKey;
    }
}
