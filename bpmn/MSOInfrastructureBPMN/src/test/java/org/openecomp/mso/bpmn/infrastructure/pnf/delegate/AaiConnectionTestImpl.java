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

package org.openecomp.mso.bpmn.infrastructure.pnf.delegate;

import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import org.openecomp.mso.bpmn.infrastructure.pnf.implementation.AaiConnection;

public class AaiConnectionTestImpl implements AaiConnection {

    public static final String ID_WITH_ENTRY_AND_IP = "idWithEntryAndIp";
    public static final String ID_WITHOUT_ENTRY = "IdWithoutEntry";
    public static final String ID_WITH_ENTRY_NO_IP = "idWithEntryNoIp";
    public static final String DEFAULT_IP = "1.2.3.4";

    private List<Entry<String, String>> updates = new LinkedList<>();
    private List<String> created = new LinkedList<>();

    @Override
    public boolean containsEntryFor(String correlationId) {
        return correlationId.equals(ID_WITH_ENTRY_AND_IP) || correlationId.equals(ID_WITH_ENTRY_NO_IP);
    }

    @Override
    public String getIpFor(String correlationId) {
        if (correlationId.equals(ID_WITH_ENTRY_AND_IP)) {
            return DEFAULT_IP;
        } else {
            return null;
        }
    }

    @Override
    public void updateEntry(String correlationId, String ipAddress) {
        updates.add(new SimpleEntry<>(correlationId, ipAddress));
    }

    @Override
    public void createEntry(String correlationId) {
        created.add(correlationId);
    }

    public List<Entry<String, String>> getUpdates() {
        return updates;
    }

    public List<String> getCreated() {
        return created;
    }

    public void reset() {
        updates = new LinkedList<>();
        created = new LinkedList<>();
    }
}
