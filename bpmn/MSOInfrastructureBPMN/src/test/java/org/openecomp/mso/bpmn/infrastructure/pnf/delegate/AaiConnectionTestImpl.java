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

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.onap.aai.domain.yang.Pnf;
import org.openecomp.mso.bpmn.infrastructure.pnf.implementation.AaiConnection;

public class AaiConnectionTestImpl implements AaiConnection {

    public static final String ID_WITH_ENTRY_AND_IP = "idWithEntryAndIp";
    public static final String ID_WITH_IP_V6 = "idWithIpV6";
    public static final String ID_WITHOUT_ENTRY = "IdWithoutEntry";
    public static final String ID_WITH_ENTRY_NO_IP = "idWithEntryNoIp";
    public static final String DEFAULT_IP = "1.2.3.4";
    public static final String DEFAULT_IP_V6 = "2001:db8::ff00:42:8329";

    private Map<String, Pnf> created = new HashMap<>();

    @Override
    public Optional<Pnf> getEntryFor(String correlationId) throws IOException {
        if (Objects.equals(correlationId, ID_WITH_ENTRY_AND_IP)) {
            Pnf pnf = new Pnf();
            pnf.setIpaddressV4Oam(DEFAULT_IP);
            return Optional.of(pnf);
        } else if (Objects.equals(correlationId, ID_WITH_IP_V6)) {
            Pnf pnf = new Pnf();
            pnf.setIpaddressV6Oam(DEFAULT_IP_V6);
            return Optional.of(pnf);
        } else if (Objects.equals(correlationId, ID_WITH_ENTRY_NO_IP)) {
            return Optional.of(new Pnf());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void createEntry(String correlationId, Pnf entry) throws IOException {
        created.put(correlationId, entry);
    }

    public Map<String, Pnf> getCreated() {
        return created;
    }

    public void reset() {
        created.clear();
    }
}
