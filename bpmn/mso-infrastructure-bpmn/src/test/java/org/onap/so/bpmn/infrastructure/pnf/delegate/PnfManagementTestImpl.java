/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.onap.aai.domain.yang.Pnf;
import org.onap.so.bpmn.infrastructure.pnf.management.PnfManagement;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public abstract class PnfManagementTestImpl implements PnfManagement {

    public static final String ID_WITHOUT_ENTRY = "IdWithoutEntry";
    public static final String ID_WITH_ENTRY = "idWithEntryNoIp";

    private Map<String, Pnf> created = new HashMap<>();
    private Map<String, String> serviceAndPnfRelationMap = new HashMap<>();

    @Override
    public Optional<Pnf> getEntryFor(String pnfCorrelationId) {
        if (Objects.equals(pnfCorrelationId, ID_WITH_ENTRY)) {
            return Optional.of(new Pnf());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void createEntry(String pnfCorrelationId, Pnf entry) {
        created.put(pnfCorrelationId, entry);
    }

    @Override
    public void createRelation(String serviceInstanceId, String pnfName) {
        serviceAndPnfRelationMap.put(serviceInstanceId, pnfName);
    }

    @Override
    public void updateEntry(String pnfCorrelationId, Pnf entry) {
        created.put(pnfCorrelationId, entry);
    }


    public Map<String, Pnf> getCreated() {
        return created;
    }

    public Map<String, String> getServiceAndPnfRelationMap() {
        return serviceAndPnfRelationMap;
    }

    public void reset() {
        created.clear();
        serviceAndPnfRelationMap.clear();
    }
}
