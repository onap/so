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

package org.openecomp.mso.bpmn.appc.payload;

import org.openecomp.mso.bpmn.appc.payload.beans.*;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.openecomp.mso.bpmn.core.json.JsonUtils;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PayloadClient {

	protected static ObjectMapper mapper = new ObjectMapper();
	
	
	public static Optional<String> upgradeFormat(Optional<String> payload, String vnfName) throws JsonProcessingException{
			UpgradeAction payloadResult = new UpgradeAction();
			ConfigurationParametersUpgrade configParams = new ConfigurationParametersUpgrade();
		    String payloadString = payload.get();
			String existingSoftware = JsonUtils.getJsonValue(payloadString, "existing-software-version");
			String newSoftware = JsonUtils.getJsonValue(payloadString, "new-software-version");
			configParams.setExistingSoftwareVersion(existingSoftware);
			configParams.setNewSoftwareVersion(newSoftware);
			configParams.setVnfName(vnfName);
			payloadResult.setConfigurationParameters(configParams);
			return Optional.of(mapper.writeValueAsString(payloadResult));
	}
	
	public static Optional<String> resumeTrafficFormat(String vnfName) throws JsonProcessingException{
			ResumeTrafficAction payloadResult = new ResumeTrafficAction();
			ConfigurationParametersResumeTraffic configParams = new ConfigurationParametersResumeTraffic();
			configParams.setVnfName(vnfName);
			payloadResult.setConfigurationParameters(configParams);
			return Optional.of(mapper.writeValueAsString(payloadResult));
	} 
	
	public static Optional<String> quiesceTrafficFormat(Optional<String> payload, String vnfName) throws JsonProcessingException{
			QuiesceTrafficAction payloadResult = new QuiesceTrafficAction();
			ConfigurationParametersQuiesce configParams = new ConfigurationParametersQuiesce();
			String payloadString = payload.get();
			String operationsTimeout = JsonUtils.getJsonValue(payloadString, "operations-timeout");
			configParams.setOperationsTimeout(operationsTimeout);
			configParams.setVnfName(vnfName);
			payloadResult.setConfigurationParameters(configParams);
			return Optional.of(mapper.writeValueAsString(payloadResult));
	}
	
	public static Optional<String> startStopFormat(String aicIdentity) throws JsonProcessingException{
			StartStopAction payloadResult = new StartStopAction();
			payloadResult.setAICIdentity(aicIdentity);
			return Optional.of(mapper.writeValueAsString(payloadResult));
	}
	
	public static Optional<String> healthCheckFormat(String vnfName, String vnfHostIpAddress) throws JsonProcessingException{
			HealthCheckAction payloadResult = new HealthCheckAction();
			RequestParametersHealthCheck requestParams = new RequestParametersHealthCheck();
			ConfigurationParametersHealthCheck configParams = new ConfigurationParametersHealthCheck();
			requestParams.setVnfName(vnfName);
			configParams.setVnfName(vnfName);
			payloadResult.setConfigurationParameters(configParams);
			payloadResult.setRequestParameters(requestParams);
			return Optional.of(mapper.writeValueAsString(payloadResult));
	}
	
	public static Optional<String> snapshotFormat(String vmId, String identityUrl)throws JsonProcessingException{
			SnapshotAction payloadResult = new SnapshotAction();
			payloadResult.setVmId(vmId);
			payloadResult.setIdentityUrl(identityUrl);
			return Optional.of(mapper.writeValueAsString(payloadResult));
	}
	
	/*public Optional<String>  verifySnapshotFormat(Optional<String> payload) throws Exception, JsonProcessingException, JsonMappingException{
		final Snapshot check = mapper.readValue(payload.get(), Snapshot.class);
		return Optional.of(mapper.writeValueAsString(check));
	}
	
	public Optional<String> verifyUpgradeFormat(Optional<String> payload) throws Exception, JsonProcessingException, JsonMappingException{
		final UpdateCheck check = mapper.readValue(payload.get(), UpdateCheck.class);
		return Optional.of(mapper.writeValueAsString(check));
	}
	
	public Optional<String> verifyQuiesceTrafficFormat(Optional<String> payload)throws Exception, JsonProcessingException, JsonMappingException{
		final QuiesceTraffic check = mapper.readValue(payload.get(), QuiesceTraffic.class);
		return Optional.of(mapper.writeValueAsString(check));
	}
	*/
	
}
