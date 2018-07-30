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

package org.onap.so.apihandlerinfra;

import java.io.File;
import java.io.IOException;

import javax.transaction.Transactional;

import org.junit.After;
import org.junit.runner.RunWith;
import org.onap.so.db.request.data.repository.InfraActiveRequestsRepository;
import org.onap.so.logger.MsoLogger;
import org.onap.so.logger.MsoLogger.Catalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiHandlerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration
@Transactional
@Sql(executionPhase=ExecutionPhase.AFTER_TEST_METHOD,scripts="classpath:InfraActiveRequestsReset.sql")
@AutoConfigureWireMock(port = 0)
public abstract class BaseTest {
	
	protected MsoLogger logger = MsoLogger.getMsoLogger(Catalog.GENERAL, BaseTest.class);
	protected TestRestTemplate restTemplate = new TestRestTemplate("test", "test");

	protected HttpHeaders headers = new HttpHeaders();	

	@Autowired
	protected Environment env;
	
	@Autowired
	protected InfraActiveRequestsRepository iar;
	
	@LocalServerPort
	private int port;
	
	protected String readJsonFileAsString(String fileLocation) throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(new File(fileLocation));
		return jsonNode.asText();
	}
	
	protected String createURLWithPort(String uri) {
		return "http://localhost:" + port + uri;
	}
	
	@After
	public void tearDown(){
		iar.deleteAll();
		WireMock.reset();
	}
}