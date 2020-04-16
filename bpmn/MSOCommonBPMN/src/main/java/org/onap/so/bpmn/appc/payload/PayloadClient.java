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

package org.onap.so.bpmn.appc.payload;

import java.util.Optional;
import org.onap.so.bpmn.appc.payload.beans.ConfigurationParametersQuiesce;
import org.onap.so.bpmn.appc.payload.beans.ConfigurationParametersResumeTraffic;
import org.onap.so.bpmn.appc.payload.beans.ConfigurationParametersDistributeTraffic;
import org.onap.so.bpmn.appc.payload.beans.ConfigurationParametersUpgrade;
import org.onap.so.bpmn.appc.payload.beans.HealthCheckAction;
import org.onap.so.bpmn.appc.payload.beans.QuiesceTrafficAction;
import org.onap.so.bpmn.appc.payload.beans.RequestParametersHealthCheck;
import org.onap.so.bpmn.appc.payload.beans.ResumeTrafficAction;
import org.onap.so.bpmn.appc.payload.beans.DistributeTrafficAction;
import org.onap.so.bpmn.appc.payload.beans.DistributeTrafficCheckAction;
import org.onap.so.bpmn.appc.payload.beans.SnapshotAction;
import org.onap.so.bpmn.appc.payload.beans.StartStopAction;
import org.onap.so.bpmn.appc.payload.beans.UpgradeAction;
import org.onap.so.bpmn.core.json.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PayloadClient {

    protected static ObjectMapper mapper = new ObjectMapper();

    private PayloadClient() {}

    public static Optional<String> upgradeFormat(Optional<String> payload, String vnfName)
            throws JsonProcessingException {
        UpgradeAction payloadResult = new UpgradeAction();
        ConfigurationParametersUpgrade configParams = new ConfigurationParametersUpgrade();
        String payloadString = payload.isPresent() ? payload.get() : "";
        String existingSoftware = JsonUtils.getJsonValue(payloadString, "existing_software_version");
        String newSoftware = JsonUtils.getJsonValue(payloadString, "new_software_version");
        configParams.setExistingSoftwareVersion(existingSoftware);
        configParams.setNewSoftwareVersion(newSoftware);
        configParams.setVnfName(vnfName);
        payloadResult.setConfigurationParameters(configParams);
        return Optional.of(mapper.writeValueAsString(payloadResult));
    }

    public static Optional<String> distributeTrafficFormat(Optional<String> payload, String vnfName)
            throws JsonProcessingException {
        DistributeTrafficAction payloadResult = new DistributeTrafficAction();
        ConfigurationParametersDistributeTraffic configParams = new ConfigurationParametersDistributeTraffic();
        String payloadString = payload.isPresent() ? payload.get() : "";
        String bookName = JsonUtils.getJsonValue(payloadString, "book_name");
        String nodeList = JsonUtils.getJsonValue(payloadString, "node_list");
        String fileParameterContent = JsonUtils.getJsonValue(payloadString, "file_parameter_content");
        configParams.setBookName(bookName);
        configParams.setNodeList(nodeList);
        configParams.setFileParameterContent(fileParameterContent);
        configParams.setVnfName(vnfName);
        payloadResult.setConfigurationParameters(configParams);
        return Optional.of(mapper.writeValueAsString(payloadResult));
    }

    public static Optional<String> distributeTrafficCheckFormat(Optional<String> payload, String vnfName)
            throws JsonProcessingException {
        DistributeTrafficCheckAction payloadResult = new DistributeTrafficCheckAction();
        ConfigurationParametersDistributeTraffic configParams = new ConfigurationParametersDistributeTraffic();
        String payloadString = payload.isPresent() ? payload.get() : "";
        String bookName = JsonUtils.getJsonValue(payloadString, "book_name");
        String nodeList = JsonUtils.getJsonValue(payloadString, "node_list");
        String fileParameterContent = JsonUtils.getJsonValue(payloadString, "file_parameter_content");
        configParams.setBookName(bookName);
        configParams.setNodeList(nodeList);
        configParams.setFileParameterContent(fileParameterContent);
        configParams.setVnfName(vnfName);
        payloadResult.setConfigurationParameters(configParams);
        return Optional.of(mapper.writeValueAsString(payloadResult));
    }

    public static Optional<String> resumeTrafficFormat(String vnfName) throws JsonProcessingException {
        ResumeTrafficAction payloadResult = new ResumeTrafficAction();
        ConfigurationParametersResumeTraffic configParams = new ConfigurationParametersResumeTraffic();
        configParams.setVnfName(vnfName);
        payloadResult.setConfigurationParameters(configParams);
        return Optional.of(mapper.writeValueAsString(payloadResult));
    }

    public static Optional<String> quiesceTrafficFormat(Optional<String> payload, String vnfName)
            throws JsonProcessingException {
        QuiesceTrafficAction payloadResult = new QuiesceTrafficAction();
        ConfigurationParametersQuiesce configParams = new ConfigurationParametersQuiesce();
        String payloadString = payload.isPresent() ? payload.get() : "";
        String operationsTimeout = JsonUtils.getJsonValue(payloadString, "operations_timeout");
        configParams.setOperationsTimeout(operationsTimeout);
        configParams.setVnfName(vnfName);
        payloadResult.setConfigurationParameters(configParams);
        return Optional.of(mapper.writeValueAsString(payloadResult));
    }

    public static Optional<String> startStopFormat(String aicIdentity) throws JsonProcessingException {
        StartStopAction payloadResult = new StartStopAction();
        payloadResult.setAicIdentity(aicIdentity);
        return Optional.of(mapper.writeValueAsString(payloadResult));
    }

    public static Optional<String> healthCheckFormat(String vnfName, String vnfHostIpAddress)
            throws JsonProcessingException {
        HealthCheckAction payloadResult = new HealthCheckAction();
        RequestParametersHealthCheck requestParams = new RequestParametersHealthCheck();
        requestParams.setHostIpAddress(vnfHostIpAddress);
        payloadResult.setRequestParameters(requestParams);
        return Optional.of((mapper.writeValueAsString(payloadResult)));
    }

    public static Optional<String> snapshotFormat(String vmId, String identityUrl) throws JsonProcessingException {
        SnapshotAction payloadResult = new SnapshotAction();
        payloadResult.setVmId(vmId);
        payloadResult.setIdentityUrl(identityUrl);
        return Optional.of(mapper.writeValueAsString(payloadResult));
    }

}
