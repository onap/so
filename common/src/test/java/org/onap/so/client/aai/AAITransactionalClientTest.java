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

package org.onap.so.client.aai;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import org.onap.aai.domain.yang.Relationship;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.defaultproperties.DefaultAAIPropertiesImpl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class AAITransactionalClientTest {

	private final static String AAI_JSON_FILE_LOCATION = "src/test/resources/__files/aai/bulkprocess/";
	AAIResourceUri uriA = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test1");
	AAIResourceUri uriB = AAIUriFactory.createResourceUri(AAIObjectType.PSERVER, "test2");
	AAIResourceUri uriC = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test3");
	AAIResourceUri uriD = AAIUriFactory.createResourceUri(AAIObjectType.PSERVER, "test4");
	AAIResourceUri uriE = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test5");
	AAIResourceUri uriF = AAIUriFactory.createResourceUri(AAIObjectType.PSERVER, "test6");
	
	ObjectMapper mapper;
	
	@Before
	public void before() throws JsonParseException, JsonMappingException, IOException {
		mapper = new AAICommonObjectMapperProvider().getMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}
	
	@Test
	public void testCreate() throws IOException {
		final Relationship body = new Relationship();
		body.setRelatedLink(uriB.build().toString());
		
		AAITransactionalClient transactions = createClient().beginTransaction()
				.create(uriA.clone().relationshipAPI(), body);
		
		String serializedTransactions = mapper.writeValueAsString(transactions.getTransactions());
		Map<String, Object> actual = mapper.readValue(serializedTransactions, new TypeReference<Map<String, Object>>(){});
		Map<String, Object> expected = mapper.readValue(getJson("test-request-small.json"), new TypeReference<Map<String, Object>>(){});

		assertEquals(actual, expected);
	}
	
	@Test
	public void testConnect() throws IOException {
		List<AAIResourceUri> uris = new ArrayList<AAIResourceUri>();
		uris.add(uriB);
		
		AAIResourceUri uriAClone = uriA.clone();
		AAITransactionalClient transactions = createClient()
				.beginTransaction().connect(uriA, uris).connect(uriC, uriD)
				.beginNewTransaction().connect(uriE, uriF);
		
		String serializedTransactions = mapper.writeValueAsString(transactions.getTransactions());
		Map<String, Object> actual = mapper.readValue(serializedTransactions, new TypeReference<Map<String, Object>>(){});
		Map<String, Object> expected = mapper.readValue(getJson("test-request.json"), new TypeReference<Map<String, Object>>(){});

		assertEquals(actual, expected);
		assertEquals("uri not manipulated", uriAClone.build().toString(), uriA.build().toString());
	}
	
	@Test
	public void testDisconnect() throws IOException {
		List<AAIResourceUri> uris = new ArrayList<AAIResourceUri>();
		uris.add(uriB);
		
		AAITransactionalClient transactions = createClient().beginTransaction()
				.disconnect(uriA, uris);
		
		String serializedTransactions = mapper.writeValueAsString(transactions.getTransactions());
		Map<String, Object> actual = mapper.readValue(serializedTransactions, new TypeReference<Map<String, Object>>(){});
		Map<String, Object> expected = mapper.readValue(getJson("test-request-small.json").replace("put", "delete"), new TypeReference<Map<String, Object>>(){});

		assertEquals(actual, expected);
	}
	
	@Test
	public void testUpdate() throws IOException {
		final Relationship body = new Relationship();
		body.setRelatedLink(uriB.build().toString());
		
		AAIResourceUri uriAClone = uriA.clone().relationshipAPI();
		AAITransactionalClient transactions = createClient().beginTransaction().update(uriAClone, body);
		
		String serializedTransactions = mapper.writeValueAsString(transactions.getTransactions());
		Map<String, Object> actual = mapper.readValue(serializedTransactions, new TypeReference<Map<String, Object>>(){});
		Map<String, Object> expected = mapper.readValue(getJson("test-request-small.json").replace("put", "patch"), new TypeReference<Map<String, Object>>(){});

		assertEquals(actual, expected);
	}
	
	@Test
	public void verifyResponse() throws IOException {
		AAITransactionalClient transactions = createClient()
				.beginTransaction();
	
		assertEquals("success status", Optional.empty(), transactions.locateErrorMessages(getJson("response-success.json")));
		assertEquals(transactions.locateErrorMessages(getJson("response-failure.json")).get(), "another error message\nmy great error");
	}
	
	private String getJson(String filename) throws IOException {
		 return new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + filename)));
	}
	
	private AAIResourcesClient createClient() {
		AAIResourcesClient client = spy(new AAIResourcesClient());
		doReturn(new DefaultAAIPropertiesImpl()).when(client).getRestProperties();
		return client;
	}
}
