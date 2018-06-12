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
package org.openecomp.mso;

import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openecomp.mso.bpmn.buildingblock.SniroHomingV2;
import org.openecomp.mso.bpmn.common.DelegateExecutionImpl;
import org.openecomp.mso.bpmn.common.InjectionHelper;
import org.openecomp.mso.bpmn.common.MockLoggerDelegate;
import org.openecomp.mso.bpmn.common.aai.tasks.AAIFlagTasks;
import org.openecomp.mso.bpmn.sdno.tasks.SDNOHealthCheckTasks;
import org.openecomp.mso.bpmn.servicedecomposition.tasks.BBInputSetup;
import org.openecomp.mso.bpmn.servicedecomposition.tasks.BBInputSetupMapperLayer;
import org.openecomp.mso.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.openecomp.mso.client.aai.AAIResourcesClient;
import org.openecomp.mso.client.aai.mapper.AAIObjectMapper;
import org.openecomp.mso.client.adapter.network.NetworkAdapterClientImpl;
import org.openecomp.mso.client.adapter.network.mapper.NetworkAdapterObjectMapper;
import org.openecomp.mso.client.adapter.vnf.VnfVolumeAdapterClientImpl;
import org.openecomp.mso.client.adapter.vnf.mapper.VnfAdapterObjectMapper;
import org.openecomp.mso.client.adapter.vnf.mapper.VnfAdapterVfModuleObjectMapper;
import org.openecomp.mso.client.db.catalog.CatalogDbClient;
import org.openecomp.mso.client.db.request.RequestsDbClient;
import org.openecomp.mso.client.exception.ExceptionBuilder;
import org.openecomp.mso.client.orchestration.AAIVnfResources;
import org.openecomp.mso.client.orchestration.SDNOHealthCheckResources;
import org.openecomp.mso.client.sdnc.SDNCClient;
import org.openecomp.mso.client.sdnc.mapper.NetworkTopologyOperationRequestMapper;
import org.openecomp.mso.client.sdnc.mapper.ServiceTopologyOperationMapper;
import org.openecomp.mso.client.sdnc.mapper.VnfTopologyOperationRequestMapper;
import org.openecomp.mso.client.sdno.SDNOValidator;
import org.openecomp.mso.client.sniro.SniroClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration
public abstract class BaseTest extends BuildingBlockTestDataSetup {
	

	protected Map<String, Object> variables = new HashMap<>();

	protected TestRestTemplate restTemplate = new TestRestTemplate();

	protected HttpHeaders headers = new HttpHeaders();

	
	@Autowired
	protected RuntimeService runtimeService;

	@Autowired
	private RepositoryService repositoryService;
	/*
	 * Mocked for injection via autowiring
	 */
	
	@Value("${mso.catalog.db.spring.endpoint}")
	protected String endpoint;
	
	@MockBean
	protected CatalogDbClient MOCK_catalogDbClient;
	
	@SpyBean
	protected InjectionHelper MOCK_injectionHelper;

	@SpyBean
	protected NetworkAdapterObjectMapper MOCK_networkAdapterObjectMapper;

	@SpyBean
	protected AAIObjectMapper MOCK_aaiObjectMapper;
	@SpyBean
	protected NetworkAdapterClientImpl MOCK_networkAdapterClient;

	
	@SpyBean
	protected AAIFlagTasks aaiFlagTasks;
	
	@SpyBean
	protected AAIVnfResources aaiVnfResources;
	
	@SpyBean
	protected ExceptionBuilder exceptionUtil;
	
	@SpyBean
	protected SDNOHealthCheckResources MOCK_sdnoHealthCheckResources;
	

	




	/*
	 *  Classes that cannot be simply mocked because they are both
	 *  needed for testing another class, and must be autowired when
	 *  being tested themselves....or classes with private methods that
	 *  must be stubbed during testing
	 */


	
	
	@SpyBean
	protected BBInputSetupMapperLayer SPY_bbInputSetupMapperLayer;
	@SpyBean
	protected BBInputSetupUtils SPY_bbInputSetupUtils;
	@SpyBean
	protected BBInputSetup SPY_bbInputSetup;
	@SpyBean
	protected SniroHomingV2 sniroHoming;
	
	@SpyBean
	protected SniroClient sniroClient;
	
	@SpyBean
	protected SDNOHealthCheckTasks sdnoHealthCheckTasks;	
	
	/*
	 *  Mocked for injection via the IntectionHelper
	 */
	

	
	@Before
	public void baseTestBefore() {
		variables.put("gBuildingBlockExecution", new DelegateExecutionImpl(new HashMap<>()));
	
		
	}

	@LocalServerPort
	private int port;
	
	protected String readFile(String path) throws IOException {
		return readFile(path, Charset.defaultCharset());
	}
	
	protected String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
	protected String readJsonFileAsString(String fileLocation) throws IOException{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(new File(fileLocation));
		return jsonNode.asText();
	}

	protected String createURLWithPort(String uri) {
		return "http://localhost:" + port + uri;
	}
	/**
	 * Create and deploy a process model with one logger delegate as service task.
	 *
	 * @param origProcessKey
	 *            key to call
	 * @param mockProcessName
	 *            process name
	 * @param fileName
	 *            file name without extension
	 */
	protected void mockSubprocess(String origProcessKey, String mockProcessName, String fileName) {
		BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(origProcessKey).name(mockProcessName)
				.startEvent().name("Start Point").serviceTask().name("Log Something for Test")
				.camundaClass(MockLoggerDelegate.class.getName()).endEvent().name("End Point").done();
		repositoryService.createDeployment().addModelInstance(fileName + ".bpmn", modelInstance).deploy();
	}
	
}
