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

package org.openecomp.mso.bpmn.infrastructure.pnf.implementation;

import java.io.IOException;
import java.util.Optional;
import org.onap.aai.domain.yang.Pnf;

public class CheckAaiForCorrelationIdImplementation {

    public AaiResponse check(String correlationId, AaiConnection aaiConnection) throws IOException {
        Optional<Pnf> pnf = aaiConnection.getEntryFor(correlationId);
        if (!pnf.isPresent()) {
            return new AaiResponse(false, null, null);
        }

        Optional<String> ip = extractIp(pnf.get());
        return ip.map(
                s -> new AaiResponse(true, true, s)
        ).orElseGet(
                () -> new AaiResponse(true, false, null)
        );
    }

    private Optional<String> extractIp(Pnf pnf) {
        if (pnf.getIpaddressV4Oam() != null) {
            return Optional.of(pnf.getIpaddressV4Oam());
        } else {
            return Optional.ofNullable(pnf.getIpaddressV6Oam());
        }
    }

}
