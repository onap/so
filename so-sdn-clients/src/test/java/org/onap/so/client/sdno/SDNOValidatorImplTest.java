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

package org.onap.so.client.sdno;

import static org.junit.Assert.assertEquals;
import java.util.UUID;
import org.junit.Test;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.so.client.sdno.beans.RequestHealthDiagnostic;
import org.onap.so.client.sdno.beans.SDNO;

public class SDNOValidatorImplTest {

    @Test
    public void buildRequestDiagnosticTest() {
        SDNOValidatorImpl validator = new SDNOValidatorImpl();
        UUID uuid = UUID.randomUUID();
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfName("VNFNAME");
        vnf.setVnfId("test");
        vnf.setIpv4OamAddress("1.2.3.4");
        vnf.setNfRole("VPE");
        SDNO request = validator.buildRequestDiagnostic(vnf, uuid, "mechid");
        assertEquals(request.getNodeType(), "VPE");
        assertEquals(request.getOperation(), "health-diagnostic");

        RequestHealthDiagnostic innerRequest = request.getBody().getInput().getRequestHealthDiagnostic();
        assertEquals(innerRequest.getRequestClientName(), "MSO");
        assertEquals(innerRequest.getRequestNodeName(), "VNFNAME");
        assertEquals(innerRequest.getRequestNodeUuid(), "test");
        assertEquals(innerRequest.getRequestNodeType(), "VPE");
        assertEquals(innerRequest.getRequestNodeIp(), "1.2.3.4");
        assertEquals(innerRequest.getRequestUserId(), "mechid");
        assertEquals(innerRequest.getRequestId(), uuid.toString());
        assertEquals(innerRequest.getHealthDiagnosticCode(), "default");

    }
}
