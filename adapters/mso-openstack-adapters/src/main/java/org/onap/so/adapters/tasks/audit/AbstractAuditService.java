/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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


package org.onap.so.adapters.tasks.audit;

import java.util.Optional;
import org.onap.so.objects.audit.AAIObjectAudit;
import org.onap.so.objects.audit.AAIObjectAuditList;
import org.onap.so.utils.ExternalTaskUtils;
import org.onap.so.utils.RetrySequenceLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractAuditService extends ExternalTaskUtils {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAuditService.class);



    protected static final String UNABLE_TO_FIND_ALL_V_SERVERS_AND_L_INTERACES_IN_A_AI =
            "Unable to find all VServers and L-Interaces in A&AI";

    protected static final String UNABLE_TO_FIND_V_SERVERS_IN_OPENSTACK = "Unable to find VServers in Openstack";

    @Autowired
    public Environment env;

    public AbstractAuditService() {
        super(RetrySequenceLevel.LONG);
    }

    /**
     * @param auditHeatStackFailed
     * @param auditList
     * @return
     */
    protected boolean didCreateAuditFail(Optional<AAIObjectAuditList> auditList) {
        if (auditList.get().getAuditList() != null && !auditList.get().getAuditList().isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("Audit Results: {}", auditList.get().toString());
            }
            return auditList.get().getAuditList().stream().filter(auditObject -> !auditObject.isDoesObjectExist())
                    .findFirst().map(v -> true).orElse(false);
        } else {
            return false;
        }
    }

    /**
     * @param auditHeatStackFailed
     * @param auditList
     * @return
     */
    protected boolean didDeleteAuditFail(AAIObjectAuditList auditList) {
        if (auditList.getAuditList() != null && !auditList.getAuditList().isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("Audit Results: {}", auditList.toString());
            }
            return auditList.getAuditList().stream().filter(AAIObjectAudit::isDoesObjectExist).findFirst()
                    .map(v -> true).orElse(false);
        } else {
            return false;
        }
    }
}
