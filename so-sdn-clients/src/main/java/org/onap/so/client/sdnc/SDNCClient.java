/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.client.sdnc;

import java.util.LinkedHashMap;
import javax.ws.rs.core.UriBuilder;
import org.onap.so.client.BaseClient;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.beans.SDNCProperties;
import org.onap.so.client.sdnc.endpoint.SDNCTopology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class SDNCClient {

    @Autowired
    private SDNCProperties properties;
    @Autowired
    private SdnCommonTasks sdnCommonTasks;

    /**
     * 
     * @param request - takes in a generated object from sdnc client - creates a json request string and sends it to
     *        sdnc - receives and validates the linkedhashmap sent back from sdnc
     * @throws MapperException
     * @throws BadResponseException
     */
    public String post(Object request, SDNCTopology topology) throws MapperException, BadResponseException {
        String jsonRequest = sdnCommonTasks.buildJsonRequest(request);
        String targetUrl = properties.getHost() + properties.getPath() + ":" + topology.toString();
        BaseClient<String, LinkedHashMap<String, Object>> STOClient = new BaseClient<>();

        STOClient.setTargetUrl(targetUrl);
        HttpHeaders httpHeader = sdnCommonTasks.getHttpHeaders(properties.getAuth(), true);
        STOClient.setHttpHeader(httpHeader);
        LinkedHashMap<String, Object> output =
                STOClient.post(jsonRequest, new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {});
        return sdnCommonTasks.validateSDNResponse(output);
    }


    public String post(Object request, String url) throws MapperException, BadResponseException {
        String jsonRequest = sdnCommonTasks.buildJsonRequest(request);
        BaseClient<String, LinkedHashMap<String, Object>> STOClient = new BaseClient<>();
        STOClient.setTargetUrl(url);
        HttpHeaders httpHeader = sdnCommonTasks.getHttpHeaders(properties.getAuth(), true);
        STOClient.setHttpHeader(httpHeader);
        LinkedHashMap<String, Object> output =
                STOClient.post(jsonRequest, new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {});
        return sdnCommonTasks.validateSDNResponse(output);
    }

    /**
     * 
     * @param queryLink - takes in a link to topology that needs to be queried - creates a json request string and sends
     *        it to sdnc - receives and validates the linkedhashmap sent back from sdnc *
     * @throws MapperException
     * @throws BadResponseException
     */
    // Build TargetUrl as per RFC-8040 standards
    public String get(String queryLink) throws MapperException, BadResponseException {
        String request = "";
        String jsonRequest = sdnCommonTasks.buildJsonRequest(request);
        String targetUrl = properties.getHost() + "/" + queryLink;
        BaseClient<String, LinkedHashMap<String, Object>> STOClient = new BaseClient<>();
        STOClient.setTargetUrl(targetUrl);
        HttpHeaders httpHeader = sdnCommonTasks.getHttpHeaders(properties.getAuth(), false);
        STOClient.setHttpHeader(httpHeader);
        LinkedHashMap<String, Object> output =
                STOClient.get(jsonRequest, new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {});
        return sdnCommonTasks.validateSDNGetResponse(output);
    }

}
