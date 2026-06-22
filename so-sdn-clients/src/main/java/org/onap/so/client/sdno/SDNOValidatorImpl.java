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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import jakarta.ws.rs.NotFoundException;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.so.client.dmaap.DmaapConsumer;
import org.onap.so.client.dmaap.DmaapPublisher;
import org.onap.so.client.sdno.beans.Body;
import org.onap.so.client.sdno.beans.Input;
import org.onap.so.client.sdno.beans.RequestHealthDiagnostic;
import org.onap.so.client.sdno.beans.SDNO;
import org.onap.so.client.sdno.dmaap.SDNOHealthCheckDmaapConsumer;
import org.onap.so.client.sdno.dmaap.SDNOHealthCheckDmaapPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SDNOValidatorImpl implements SDNOValidator {

    private final static String clientName = "MSO";
    private final static String HEALTH_DIAGNOSTIC_CODE_DEFAULT = "default";

    @Override
    public boolean healthDiagnostic(String vnfId, UUID uuid, String requestingUserId) throws IOException, Exception {

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId));
        AAIResourcesClient client = new AAIResourcesClient();
        GenericVnf vnf = client.get(GenericVnf.class, uri)
                .orElseThrow(() -> new NotFoundException(vnfId + " not found in A&AI"));

        SDNO requestDiagnostic = buildRequestDiagnostic(vnf, uuid, requestingUserId);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(requestDiagnostic);
        this.submitRequest(json);
        boolean status = this.pollForResponse(uuid.toString());
        return status;
    }

    protected SDNO buildRequestDiagnostic(GenericVnf vnf, UUID uuid, String requestingUserId) {

        Optional<String> nfRole;
        if (vnf.getNfRole() == null) {
            nfRole = Optional.empty();
        } else {
            nfRole = Optional.of(vnf.getNfRole());
        }
        Input input = new Input();
        SDNO parentRequest = new SDNO();
        Body body = new Body();
        parentRequest.setBody(body);
        parentRequest.setNodeType(nfRole.orElse("NONE").toUpperCase());
        parentRequest.setOperation("health-diagnostic");

        body.setInput(input);

        RequestHealthDiagnostic request = new RequestHealthDiagnostic();

        request.setRequestClientName(clientName);
        request.setRequestNodeName(vnf.getVnfName());
        request.setRequestNodeUuid(vnf.getVnfId());
        request.setRequestNodeType(nfRole.orElse("NONE").toUpperCase());
        request.setRequestNodeIp(vnf.getIpv4OamAddress()); // generic-vnf oam ip
        request.setRequestUserId(requestingUserId); // mech id?
        request.setRequestId(uuid.toString()); // something to identify this request by for polling
        request.setHealthDiagnosticCode(HEALTH_DIAGNOSTIC_CODE_DEFAULT);

        input.setRequestHealthDiagnostic(request);

        return parentRequest;
    }

    protected void submitRequest(String json) throws FileNotFoundException, IOException, InterruptedException {

        DmaapPublisher publisher = new SDNOHealthCheckDmaapPublisher();
        publisher.send(json);
    }

    protected boolean pollForResponse(String uuid) throws Exception {
        DmaapConsumer consumer = this.getConsumer(uuid);
        return consumer.consume();
    }



    protected DmaapConsumer getConsumer(String uuid) throws FileNotFoundException, IOException {
        return new SDNOHealthCheckDmaapConsumer(uuid);
    }



}
