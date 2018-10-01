/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.domain.yang.Pserver;
import org.onap.aai.domain.yang.v9.Complex;
import org.onap.so.client.aai.entities.singletransaction.SingleTransactionRequest;
import org.onap.so.client.aai.entities.singletransaction.SingleTransactionResponse;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.defaultproperties.DefaultAAIPropertiesImpl;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class AAISingleTransactionClientTest {

	private final static String AAI_JSON_FILE_LOCATION = "src/test/resources/__files/aai/singletransaction/";
	AAIResourceUri uriA = AAIUriFactory.createResourceUri(AAIObjectType.PSERVER, "pserver-hostname");
	AAIResourceUri uriB = AAIUriFactory.createResourceUri(AAIObjectType.COMPLEX, "my-complex");
	
	ObjectMapper mapper;
	
	@Before
	public void before() throws JsonParseException, JsonMappingException, IOException {
		mapper = new AAICommonObjectMapperProvider().getMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}
	
	@Test
	public void testRequest() throws JSONException,IOException {
		AAIResourcesClient client = createClient();
		Pserver pserver = new Pserver();
		pserver.setHostname("pserver-hostname");
		pserver.setFqdn("pserver-bulk-process-single-transactions-multiple-actions-1-fqdn");
		Pserver pserver2 = new Pserver();
		pserver2.setFqdn("patched-fqdn");
		Complex complex = new Complex();
		complex.setCity("my-city");
		AAISingleTransactionClient singleTransaction = 
		client.beginSingleTransaction()
			.create(uriA, pserver)
			.update(uriA, pserver2)
			.create(uriB, complex);
		
		
		SingleTransactionRequest actual = singleTransaction.getRequest();
		
		SingleTransactionRequest expected = mapper.readValue(this.getJson("sample-request.json"), SingleTransactionRequest.class);
		
		JSONAssert.assertEquals(mapper.writeValueAsString(expected),mapper.writeValueAsString(actual), false);
	}
	
	@Test
	public void testFailure() throws IOException {
		AAIResourcesClient client = createClient();
		AAISingleTransactionClient singleTransaction = client.beginSingleTransaction();
		SingleTransactionResponse expected = mapper.readValue(this.getJson("sample-response-failure.json"), SingleTransactionResponse.class);
		Optional<String> errorMessage = singleTransaction.locateErrorMessages(expected);
		
		assertThat(expected.getOperationResponses().size(), greaterThan(0));
		assertThat(errorMessage.isPresent(), equalTo(true));
		
	}
	
	@Test
	public void testSuccessResponse() throws IOException {
		AAIResourcesClient client = createClient();
		AAISingleTransactionClient singleTransaction = client.beginSingleTransaction();
		SingleTransactionResponse expected = mapper.readValue(this.getJson("sample-response.json"), SingleTransactionResponse.class);
		Optional<String> errorMessage = singleTransaction.locateErrorMessages(expected);
		
		assertThat(expected.getOperationResponses().size(), greaterThan(0));
		assertThat(errorMessage.isPresent(), equalTo(false));
		
	}
	
	@Test
	public void confirmPatchFormat() {
		AAISingleTransactionClient singleTransaction = spy(new AAISingleTransactionClient(AAIVersion.LATEST));
		AAIPatchConverter mock = mock(AAIPatchConverter.class);
		doReturn(mock).when(singleTransaction).getPatchConverter();
		singleTransaction.update(uriA, "{}");
		verify(mock, times(1)).convertPatchFormat(any());
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
