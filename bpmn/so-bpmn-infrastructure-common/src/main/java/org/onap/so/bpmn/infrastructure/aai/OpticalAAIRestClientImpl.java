/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Fujitsu Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.aai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import javax.ws.rs.core.Response;
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.bpmn.core.json.DecomposeJsonUtil;
import org.onap.so.bpmn.core.domain.PInterface;
import org.onap.so.bpmn.core.domain.EsrThirdpartySdnc;
import org.onap.so.bpmn.core.domain.LogicalLink;
import org.onap.so.client.aai.entities.CustomQuery;
import org.onap.so.client.aai.entities.Results;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIQueryClient;
import org.onap.so.client.graphinventory.Format;
import org.onap.so.client.aai.AAICommonObjectMapperProvider;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpticalAAIRestClientImpl {

    private static final String P_INTERFACE_QUERY = "getInterfaceDetails";
    private static final String INTERDOMAIN_LINK_QUERY = "getInterDomainLink";
    private static final String DOMAIN_CONTROLLER_QUERY = "getDomainController";
    JsonUtils jsonUtils = new JsonUtils();
    private static final Logger logger = LoggerFactory.getLogger(OpticalAAIRestClientImpl.class);

    public String getSdncResourceFromJson(DelegateExecution execution, String jsonResp, String resourceName)
            throws IOException {
        ObjectMapper mapper = new AAICommonObjectMapperProvider().getMapper();
        String invariantId = "";
        Results<Map<String, EsrThirdpartySdnc>> resultsFromJson =
                mapper.readValue(jsonResp, new TypeReference<Results<Map<String, EsrThirdpartySdnc>>>() {});
        List<EsrThirdpartySdnc> results = new ArrayList<>();
        for (Map<String, EsrThirdpartySdnc> m : resultsFromJson.getResult()) {
            results.add(m.get(resourceName));
        }
        if (!results.isEmpty()) {
            invariantId = results.get(0).toString();
        }
        return invariantId;
    }

    public String getLLResourceFromJson(DelegateExecution execution, String jsonResp, String resourceName)
            throws IOException {
        ObjectMapper mapper = new AAICommonObjectMapperProvider().getMapper();
        String invariantId = "";
        Results<Map<String, LogicalLink>> resultsFromJson =
                mapper.readValue(jsonResp, new TypeReference<Results<Map<String, LogicalLink>>>() {});
        List<LogicalLink> results = new ArrayList<>();
        for (Map<String, LogicalLink> m : resultsFromJson.getResult()) {
            results.add(m.get(resourceName));
        }
        if (!results.isEmpty()) {
            invariantId = results.get(0).toString();
        }
        return invariantId;
    }

    public String getPifResourceFromJson(DelegateExecution execution, String jsonResp, String resourceName)
            throws IOException {
        ObjectMapper mapper = new AAICommonObjectMapperProvider().getMapper();
        String invariantId = "";
        Results<Map<String, PInterface>> resultsFromJson =
                mapper.readValue(jsonResp, new TypeReference<Results<Map<String, PInterface>>>() {});
        List<PInterface> results = new ArrayList<>();
        for (Map<String, PInterface> m : resultsFromJson.getResult()) {
            results.add(m.get(resourceName));
        }
        if (!results.isEmpty()) {
            invariantId = results.get(0).toString();
        }
        return invariantId;
    }

    public EsrThirdpartySdnc getDomainControllerByIf(DelegateExecution execution, String portId) throws IOException {
        List<AAIResourceUri> startNodes = new ArrayList<>();
        startNodes.add(AAIUriFactory.createResourceUri(AAIObjectType.NETWORK_PNF));
        String payload = DOMAIN_CONTROLLER_QUERY + "?portid=" + portId;
        String jsonResp = new AAIQueryClient().query(Format.RESOURCE, new CustomQuery(startNodes, payload));
        String resource = this.getSdncResourceFromJson(execution, jsonResp, "esr-thirdparty-sdnc");
        EsrThirdpartySdnc res = DecomposeJsonUtil.jsonToSdncResource(resource);
        logger.info("EsrThirdpartySdnc: {}", res.toString());
        return res;
    }

    public LogicalLink getInterDomainLink(DelegateExecution execution, String controller) throws IOException {
        LogicalLink idLink = new LogicalLink();
        List<AAIResourceUri> startNodes = new ArrayList<>();
        startNodes.add(AAIUriFactory.createResourceUri(AAIObjectType.ESR_THIRDPARTY_SDNC));
        String payload = INTERDOMAIN_LINK_QUERY + "?linktype=inter-domain&controller=" + controller;
        String jsonResp = new AAIQueryClient().query(Format.RESOURCE, new CustomQuery(startNodes, payload));
        String resource = this.getLLResourceFromJson(execution, jsonResp, "logical-link");
        idLink = DecomposeJsonUtil.jsonToLLResource(resource);
        logger.info("LogicalLink:", idLink);
        return idLink;
    }

    public PInterface getInterfaceDetails(DelegateExecution execution, String portId) throws IOException {
        PInterface pif = new PInterface();
        List<AAIResourceUri> startNodes = new ArrayList<>();
        startNodes.add(AAIUriFactory.createResourceUri(AAIObjectType.NETWORK_PNF));
        String payload = P_INTERFACE_QUERY + "?portid=" + portId;
        String jsonResp = new AAIQueryClient().query(Format.RESOURCE, new CustomQuery(startNodes, payload));
        String resource = this.getPifResourceFromJson(execution, jsonResp, "p-interface");
        pif = DecomposeJsonUtil.jsonToPifResource(resource);
        logger.info("PInterface:", pif);
        return pif;
    }

}
