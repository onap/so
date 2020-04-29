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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.onap.so.client.dmaap.DmaapConsumer;
import org.onap.so.client.dmaap.DmaapPublisher;
import org.onap.so.client.sdno.beans.AAIParamList;
import org.onap.so.client.sdno.beans.Body;
import org.onap.so.client.sdno.beans.Input;
import org.onap.so.client.sdno.beans.RequestHdCustom;
import org.onap.so.client.sdno.beans.SDNO;
import org.onap.so.client.sdno.dmaap.SDNOHealthCheckDmaapConsumer;
import org.onap.so.client.sdno.dmaap.SDNOHealthCheckDmaapPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SDNOHealthCheckClient {

    private static final String NODE_TYPE = "VROUTER";
    private static final String API_OPERATION_TYPE = "health-diagnostic-custom";
    private static final String MIRRORING_CHECK = "mirroring_check";
    private static final String CLIENT_NAME = "MSO";
    private static final String PRE_CHECK_CODE = "VROUTER000003";
    private static final String POST_CHECK_CODE = "VROUTER000004";
    private static final String LPORT_MIRRORING_CHECK = "lport_mirroring_check";
    private static final String CONFIGURATION_ID = "configuration-id";


    public boolean lPortMirrorHealthPreCheck(String userId, String requestId, Optional<String> clliCode,
            String configurationId, String interfaceId) throws Exception {
        String request = buildLPortMirrorCheckPreRequest(userId, requestId, clliCode, configurationId, interfaceId);
        return this.execute(requestId, request);
    }

    public boolean lPortMirrorHealthPostCheck(String userId, String requestId, Optional<String> clliCode,
            String configurationId, String interfaceId) throws Exception {
        String request = buildLPortMirrorCheckPostRequest(userId, requestId, clliCode, configurationId, interfaceId);
        return this.execute(requestId, request);
    }

    public boolean portMirrorHealthPreCheck(String userId, String requestId, Optional<String> clliCode,
            String configurationId) throws Exception {
        final String request = this.buildPortMirrorPreCheckRequest(userId, requestId, clliCode, configurationId);
        return this.execute(requestId, request);
    }

    public boolean portMirrorHealthPostCheck(String userId, String requestId, Optional<String> clliCode,
            String configurationId) throws Exception {
        final String request = this.buildPortMirrorPostCheckRequest(userId, requestId, clliCode, configurationId);
        return this.execute(requestId, request);
    }

    protected String buildLPortMirrorCheckPreRequest(String userId, String requestId, Optional<String> clliCode,
            String configurationId, String interfaceId) throws JsonProcessingException {
        return this.buildLPortMirrorCheckRequest(userId, requestId, clliCode, configurationId, interfaceId,
                PRE_CHECK_CODE);
    }

    protected String buildLPortMirrorCheckPostRequest(String userId, String requestId, Optional<String> clliCode,
            String configurationId, String interfaceId) throws JsonProcessingException {
        return this.buildLPortMirrorCheckRequest(userId, requestId, clliCode, configurationId, interfaceId,
                POST_CHECK_CODE);
    }

    protected String buildPortMirrorPreCheckRequest(String userId, String requestId, Optional<String> clliCode,
            String configurationId) throws JsonProcessingException {
        return this.buildPortMirrorCheckRequest(userId, requestId, clliCode, configurationId, PRE_CHECK_CODE);
    }

    protected String buildPortMirrorPostCheckRequest(String userId, String requestId, Optional<String> clliCode,
            String configurationId) throws JsonProcessingException {
        return this.buildPortMirrorCheckRequest(userId, requestId, clliCode, configurationId, POST_CHECK_CODE);
    }

    protected String buildPortMirrorCheckRequest(String userId, String requestId, Optional<String> clliCode,
            String configurationId, String diagnosticCode) throws JsonProcessingException {
        final AAIParamList list = new AAIParamList();
        list.setKey(CONFIGURATION_ID);
        list.setValue(configurationId);

        return this.buildRequest(userId, requestId, clliCode, diagnosticCode, MIRRORING_CHECK,
                Collections.singletonList(list));
    }

    protected String buildLPortMirrorCheckRequest(String userId, String requestId, Optional<String> clliCode,
            String configurationId, String interfaceId, String diagnosticCode) throws JsonProcessingException {

        final AAIParamList configurationIdParam = new AAIParamList();
        configurationIdParam.setKey(CONFIGURATION_ID);
        configurationIdParam.setValue(configurationId);
        final AAIParamList interfaceIdParam = new AAIParamList();
        interfaceIdParam.setKey("interface-id");
        interfaceIdParam.setValue(interfaceId);
        final List<AAIParamList> list = new ArrayList<>();
        list.add(configurationIdParam);
        list.add(interfaceIdParam);
        return this.buildRequest(userId, requestId, clliCode, diagnosticCode, LPORT_MIRRORING_CHECK, list);
    }


    protected String buildRequest(String userId, String requestId, Optional<String> clliCode, String diagnosticCode,
            String operationType, List<AAIParamList> paramList) throws JsonProcessingException {

        final RequestHdCustom hdCustom = new RequestHdCustom();
        hdCustom.withRequestUserId(userId).withRequestId(requestId).withRequestClientName(CLIENT_NAME)
                .withHealthDiagnosticCode(diagnosticCode).withOperationType(operationType).withAaiParamList(paramList);

        final Input input = new Input();
        input.setRequestHdCustom(hdCustom);
        final Body body = new Body();
        body.setInput(input);
        final SDNO request = new SDNO();
        request.withBody(body).withOperation(API_OPERATION_TYPE).withNodeType(NODE_TYPE);
        if (clliCode.isPresent()) {
            request.setNodeLoc(clliCode.get());
        }
        return this.getJson(request);

    }

    protected String getJson(SDNO obj) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(obj);
    }

    protected DmaapPublisher getPublisher() throws FileNotFoundException, IOException {
        return new SDNOHealthCheckDmaapPublisher();
    }

    protected DmaapConsumer getConsumer(String requestId) throws FileNotFoundException, IOException {
        return new SDNOHealthCheckDmaapConsumer(requestId);
    }

    protected boolean execute(String requestId, String request) throws Exception {
        final DmaapPublisher publisher = this.getPublisher();
        publisher.send(request);

        final DmaapConsumer consumer = this.getConsumer(requestId);

        return consumer.consume();
    }

}
