/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 TechMahindra.
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

package org.onap.so.client.cds;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;

@RunWith(JUnit4.class)
public class AbstractCDSProcessingBBUtilsTest {
	@InjectMocks
	private AbstractCDSProcessingBBUtils abstractCDSProcessingBBUtils = new AbstractCDSProcessingBBUtils();

	@Test
	public void preProcessRequestTest() throws Exception {
		String requestObject = "{}";
		String blueprintName = "blueprintName";
		String blueprintVersion = "blueprintVersion";
		String actionName = "actionName";
		String mode = "mode";
		String requestId = "123456";
		String originatorId = "originatorId";
		String subRequestId = UUID.randomUUID().toString();

		DelegateExecution execution = mock(DelegateExecution.class);
		when(execution.getVariable("requestObject")).thenReturn(requestObject);
		when(execution.getVariable("requestId")).thenReturn(requestId);
		when(execution.getVariable("blueprintName")).thenReturn(blueprintName);
		when(execution.getVariable("blueprintVersion")).thenReturn(blueprintVersion);
		when(execution.getVariable("actionName")).thenReturn(actionName);
		when(execution.getVariable("mode")).thenReturn(mode);
		when(execution.getVariable("originatorId")).thenReturn(originatorId);
		when(execution.getVariable("subRequestId")).thenReturn(subRequestId);

		abstractCDSProcessingBBUtils.constructExecutionServiceInputObject(execution);
		assertTrue(true);
	}

	@Test
	public void preProcessingAbstractCDSCallTestForVnfCofigDeploy() {

		Map<String, Object> map = new HashMap<>();
		map.put("vnf-id", "vnf-id");
		map.put("vnf-name", "vnf-name");
		map.put("vnf-customization-uuid", "vnf-customization-uuid");
		map.put("service-instance-id", "service-instance-id");
		map.put("service-model-uuid", "service-model-uuid");
		map.put("resolution-key", "resolution-key");
		Map<String, Object> vnfMap = map;
		abstractCDSProcessingBBUtils.constructPayload(vnfMap, "VNF", "ConfigDeploy");
		assertTrue(true);
	}

	@Test
	public void preProcessingAbstractCDSCallForVnfCofigDeployNullVnfIdTest() {

		Map<String, Object> map = new HashMap<>();
		map.put("vnf-id", null);
		map.put("vnf-name", "vnf-name");
		map.put("vnf-customization-uuid", "vnf-customization-uuid");
		map.put("service-instance-id", "service-instance-id");
		map.put("service-model-uuid", "service-model-uuid");
		map.put("resolution-key", "resolution-key");
		Map<String, Object> vnfMap = map;
		abstractCDSProcessingBBUtils.constructPayload(vnfMap, "VNF", "ConfigDeploy");
		assertTrue(true);
	}

	@Test
	public void preProcessingAbstractCDSCallForVnfCofigDeployNullVnfNameTest() {

		Map<String, Object> map = new HashMap<>();
		map.put("vnf-id", "vnf-id");
		map.put("vnf-name", null);
		map.put("vnf-customization-uuid", "vnf-customization-uuid");
		map.put("service-instance-id", "service-instance-id");
		map.put("service-model-uuid", "service-model-uuid");
		map.put("resolution-key", "resolution-key");
		Map<String, Object> vnfMap = map;
		abstractCDSProcessingBBUtils.constructPayload(vnfMap, "VNF", "ConfigDeploy");
		assertTrue(true);
	}

	@Test
	public void preProcessingAbstractCDSCallForVnfCofigDeployNullServInstIdTest() {

		Map<String, Object> map = new HashMap<>();
		map.put("vnf-id", "vnf-id");
		map.put("vnf-name", "vnf-name");
		map.put("vnf-customization-uuid", "vnf-customization-uuid");
		map.put("service-instance-id", null);
		map.put("service-model-uuid", "service-model-uuid");
		map.put("resolution-key", "resolution-key");
		Map<String, Object> vnfMap = map;
		abstractCDSProcessingBBUtils.constructPayload(vnfMap, "VNF", "ConfigDeploy");
		assertTrue(true);
	}

	@Test
	public void preProcessingAbstractCDSCallForVnfCofigDeployNullServInstUuidTest() {

		Map<String, Object> map = new HashMap<>();
		map.put("vnf-id", "vnf-id");
		map.put("vnf-name", "vnf-name");
		map.put("vnf-customization-uuid", "vnf-customization-uuid");
		map.put("service-instance-id", "service-instance-id");
		map.put("service-model-uuid", null);
		map.put("resolution-key", "resolution-key");
		Map<String, Object> vnfMap = map;
		abstractCDSProcessingBBUtils.constructPayload(vnfMap, "VNF", "ConfigDeploy");
		assertTrue(true);
	}

	@Test
	public void preProcessingAbstractCDSCallForVnfCofigDeployNullResolutionTest() {

		Map<String, Object> map = new HashMap<>();
		map.put("vnf-id", "vnf-id");
		map.put("vnf-name", "vnf-name");
		map.put("vnf-customization-uuid", "vnf-customization-uuid");
		map.put("service-instance-id", "service-instance-id");
		map.put("service-model-uuid", "service-model-uuid");
		map.put("resolution-key", null);
		Map<String, Object> vnfMap = map;
		abstractCDSProcessingBBUtils.constructPayload(vnfMap, "VNF", "ConfigDeploy");
		assertTrue(true);
	}

	@Test
	public void preProcessAbstractCDSCallForVnfCofigDeployNullVnfuuidTest() {

		Map<String, Object> map = new HashMap<>();
		map.put("vnf-id", "vnf-id");
		map.put("vnf-name", "vnf-name");
		map.put("vnf-customization-uuid", null);
		map.put("service-instance-id", "service-instance-id");
		map.put("service-model-uuid", "service-model-uuid");
		map.put("resolution-key", "resolution-key");
		Map<String, Object> vnfMap = map;
		abstractCDSProcessingBBUtils.constructPayload(vnfMap, "VNF", "ConfigDeploy");
		assertTrue(true);
	}

	@Test
	public void preProcessAbstractCDSCallForVnfCofigDeployNullParameterTest() {

		abstractCDSProcessingBBUtils.constructPayload(null, "VNF", "ConfigDeploy");
		assertTrue(true);
	}

	@Test
	public void preProcessAbstractCDSCallForVnfCofigDeployEmptyParameterTest() {

		abstractCDSProcessingBBUtils.constructPayload(null, "VNF", "");
		assertTrue(true);
	}

	@Test
	public void preProcessAbstractCDSCallForVnfCofigDeployNullVnfTest() {

		abstractCDSProcessingBBUtils.constructPayload(null, null, "ConfigDeploy");
		assertTrue(true);
	}

	@Test
	public void preProcessingAbstractCDSCallForVnfCofigDeployEmptyVnfTest() {

		abstractCDSProcessingBBUtils.constructPayload(null, "", "");
		assertTrue(true);
	}

	@Test
	public void preProcessAbstractCDSCallTestForVnfForConfigAssign() {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> userMap = new HashMap<>();
		userMap.put("Instance1", "instance1value");
		userMap.put("Instance2", "instance2value");
		map.put("vnf-id", "vnf-id");
		map.put("vnf-name", "vnf-name");
		map.put("vnf-customization-uuid", "vnf-customization-uuid");
		map.put("service-instance-id", "service-instance-id");
		map.put("service-model-uuid", "service-model-uuid");
		map.put("resolution-key", "resolution-key");
		map.put("user_params", userMap);

		Map<String, Object> vnfMap = map;
		abstractCDSProcessingBBUtils.constructPayload(vnfMap, "VNF", "ConfigAssign");
		assertTrue(true);
	}

	@Test
	public void preProcessAbstractCDSCallForVnfForConfigAssignUserParamNullTest() {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> userMap = null;
		map.put("vnf-id", "vnf-id");
		map.put("vnf-name", "vnf-name");
		map.put("vnf-customization-uuid", "vnf-customization-uuid");
		map.put("service-instance-id", "service-instance-id");
		map.put("service-model-uuid", "service-model-uuid");
		map.put("resolution-key", "resolution-key");
		map.put("user_params", userMap);

		Map<String, Object> vnfMap = map;
		abstractCDSProcessingBBUtils.constructPayload(vnfMap, "VNF", "ConfigAssign");
		assertTrue(true);
	}

	@Test
	public void preProcessAbstractCDSCallTestForPnfConfigAssign() {

		Map<String, Object> map = new HashMap<>();
		Map<String, Object> userMap = new HashMap<>();
		userMap.put("Instance1", "instance1value");
		userMap.put("Instance2", "instance2value");
		map.put("pnf-id", "pnf-id");
		map.put("pnf-name", "pnf-name");
		map.put("pnf-customization-uuid", "pnf-customization-uuid");
		map.put("service-instance-id", "service-instance-id");
		map.put("service-model-uuid", "service-model-uuid");
		map.put("resolution-key", "resolution-key");
		map.put("user_params", userMap);

		Map<String, Object> pnfMap = map;
		abstractCDSProcessingBBUtils.constructPayload(pnfMap, "PNF", "ConfigAssign");
		assertTrue(true);
	}

	@Test
	public void preProcessAbstractCDSCallForPnfConfigAssignNullPnfIdTest() {

		Map<String, Object> map = new HashMap<>();
		Map<String, Object> userMap = new HashMap<>();
		userMap.put("Instance1", "instance1value");
		userMap.put("Instance2", "instance2value");
		map.put("pnf-id", null);
		map.put("pnf-name", "pnf-name");
		map.put("pnf-customization-uuid", "pnf-customization-uuid");
		map.put("service-instance-id", "service-instance-id");
		map.put("service-model-uuid", "service-model-uuid");
		map.put("resolution-key", "resolution-key");
		map.put("user_params", userMap);

		Map<String, Object> pnfMap = map;
		abstractCDSProcessingBBUtils.constructPayload(pnfMap, "PNF", "ConfigAssign");
		assertTrue(true);
	}

	@Test
	public void preProcessAbstractCDSCallForPnfConfigAssignNullPnfNameTest() {

		Map<String, Object> map = new HashMap<>();
		Map<String, Object> userMap = new HashMap<>();
		userMap.put("Instance1", "instance1value");
		userMap.put("Instance2", "instance2value");
		map.put("pnf-id", "pnf-id");
		map.put("pnf-name", null);
		map.put("pnf-customization-uuid", "pnf-customization-uuid");
		map.put("service-instance-id", "service-instance-id");
		map.put("service-model-uuid", "service-model-uuid");
		map.put("resolution-key", "resolution-key");
		map.put("user_params", userMap);

		Map<String, Object> pnfMap = map;
		abstractCDSProcessingBBUtils.constructPayload(pnfMap, "PNF", "ConfigAssign");
		assertTrue(true);
	}

	@Test
	public void preProcessAbstractCDSCallForPnfConfigAssignNullPnfUUIDTest() {

		Map<String, Object> map = new HashMap<>();
		Map<String, Object> userMap = new HashMap<>();
		userMap.put("Instance1", "instance1value");
		userMap.put("Instance2", "instance2value");
		map.put("pnf-id", "pnf-id");
		map.put("pnf-name", "pnf-name");
		map.put("pnf-customization-uuid", null);
		map.put("service-instance-id", "service-instance-id");
		map.put("service-model-uuid", "service-model-uuid");
		map.put("resolution-key", "resolution-key");
		map.put("user_params", userMap);

		Map<String, Object> pnfMap = map;
		abstractCDSProcessingBBUtils.constructPayload(pnfMap, "PNF", "ConfigAssign");
		assertTrue(true);
	}

	@Test
	public void preProcessAbstractCDSCallForPnfConfigAssignNullParameterTest() {

		abstractCDSProcessingBBUtils.constructPayload(null, "PNF", "ConfigAssign");
		assertTrue(true);
	}

	@Test
	public void preProcessAbstractCDSCallTestForPnfConfigDeploy() {

		Map<String, Object> map = new HashMap<>();
		map.put("pnf-id", "pnf-id");
		map.put("pnf-name", "pnf-name");
		map.put("pnf-customization-uuid", "pnf-customization-uuid");
		map.put("service-instance-id", "service-instance-id");
		map.put("service-model-uuid", "service-model-uuid");
		map.put("resolution-key", "resolution-key");

		Map<String, Object> pnfMap = map;
		abstractCDSProcessingBBUtils.constructPayload(pnfMap, "PNF", "ConfigDeploy");
		assertTrue(true);
	}
}
