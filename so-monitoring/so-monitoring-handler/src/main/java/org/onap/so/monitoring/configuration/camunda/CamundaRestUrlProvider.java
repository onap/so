/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.monitoring.configuration.camunda;

import java.net.URI;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author waqas.ikram@ericsson.com
 */
@Service
public class CamundaRestUrlProvider {

    private static final String HISTORY_PATH = "history";
    private final URI baseUri;

    public CamundaRestUrlProvider(final String httpUrl, final String engineName) {
        this.baseUri = UriComponentsBuilder.fromHttpUrl(httpUrl).path(engineName).build().toUri();
    }

    /**
     * see {@link <a href=
     * "https://docs.camunda.org/manual/7.5/reference/rest/history/process-instance/get-process-instance-query/">Get
     * Process Instances</a>}.
     * 
     * @param requestId the request ID
     * @return URL
     */
    public String getHistoryProcessInstanceUrl(final String requestId) {
        return UriComponentsBuilder.fromUri(baseUri).pathSegment(HISTORY_PATH).pathSegment("process-instance")
                .query("variables=requestId_eq_{requestID}").buildAndExpand(requestId).toString();
    }

    /**
     * see {@link <a href=
     * "https://docs.camunda.org/manual/7.5/reference/rest/history/process-instance/get-process-instance/">Get
     * Single Process Instance</a>}.
     * 
     * @param processInstanceId the process instance id.
     * @return URL
     */
    public String getSingleProcessInstanceUrl(final String processInstanceId) {
        return UriComponentsBuilder.fromUri(baseUri).pathSegment(HISTORY_PATH).pathSegment("process-instance")
                .pathSegment(processInstanceId).build().toString();
    }

    /**
     * see {@link <a href=
     * "https://docs.camunda.org/manual/7.5/reference/rest/process-definition/get-xml/">Get BPMN 2.0
     * XML</a>}.
     * 
     * @param processDefinitionId the process definition id.
     * @return URL
     */
    public String getProcessDefinitionUrl(final String processDefinitionId) {
        return UriComponentsBuilder.fromUri(baseUri).pathSegment("process-definition").pathSegment(processDefinitionId)
                .pathSegment("xml").build().toString();
    }

    /**
     * see {@link <a href=
     * "https://docs.camunda.org/manual/7.5/reference/rest/history/activity-instance/get-activity-instance/">Get
     * Single Activity Instance (Historic)</a>}.
     * 
     * @param processInstanceId the process instance id.
     * @return URL
     */
    public String getActivityInstanceUrl(final String processInstanceId) {
        return UriComponentsBuilder.fromUri(baseUri).pathSegment(HISTORY_PATH).pathSegment("activity-instance")
                .query("processInstanceId={processInstanceId}").queryParam("sortBy", "startTime")
                .queryParam("sortOrder", "asc").buildAndExpand(processInstanceId).toString();
    }

    /**
     * see {@link <a href=
     * "https://docs.camunda.org/manual/7.5/reference/rest/history/variable-instance/get-variable-instance/">Get
     * Single Variable Instance</a>}.
     * 
     * @param processInstanceId the process instance id.
     * @return URL
     */
    public String getProcessInstanceVariablesUrl(final String processInstanceId) {
        return UriComponentsBuilder.fromUri(baseUri).pathSegment(HISTORY_PATH).pathSegment("variable-instance")
                .query("processInstanceId={processInstanceId}").buildAndExpand(processInstanceId).toString();
    }

}
