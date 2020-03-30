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

package org.onap.so.adapters.tasks.inventory;

import java.util.Optional;
import java.util.stream.Stream;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.objects.audit.AAIObjectAudit;
import org.onap.so.objects.audit.AAIObjectAuditList;
import org.springframework.stereotype.Component;

@Component
public class CreateAAIInventory {

    private AAIResourcesClient aaiClient;

    public void createInventory(AAIObjectAuditList auditList) throws InventoryException {
        if (didAuditFailVserverLInterfaces(auditList)) {
            throw new InventoryException("Audit failed for VServer or LInterface cannot write Sub-Interfaces");
        }
        auditList.getAuditList().parallelStream()
                .filter(auditObject -> !auditObject.isDoesObjectExist()
                        && AAIObjectType.SUB_L_INTERFACE.typeName().equals(auditObject.getAaiObjectType()))
                .forEach(auditObject -> getAaiClient().createIfNotExists(AAIUriFactory.createResourceFromExistingURI(
                        AAIObjectType.fromTypeName(auditObject.getAaiObjectType()), auditObject.getResourceURI()),
                        Optional.of(auditObject.getAaiObject())));
    }


    /**
     * @param auditHeatStackFailed
     * @param auditList
     * @return
     */
    protected boolean didAuditFailVserverLInterfaces(AAIObjectAuditList auditList) {
        Stream<AAIObjectAudit> issue = auditList.getAuditList().stream()
                .filter(auditObject -> auditObject.getAaiObjectType().equals(AAIObjectType.VSERVER.typeName())
                        || auditObject.getAaiObjectType().equals(AAIObjectType.L_INTERFACE.typeName()));

        return issue.filter(auditObject -> !auditObject.isDoesObjectExist()).findFirst().map(v -> true).orElse(false);
    }

    protected AAIResourcesClient getAaiClient() {
        if (aaiClient == null)
            return new AAIResourcesClient();
        else
            return aaiClient;
    }

    protected void setAaiClient(AAIResourcesClient aaiResource) {
        aaiClient = aaiResource;
    }
}
