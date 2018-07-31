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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.so.client.RestClient;
import org.onap.so.client.aai.entities.CustomQuery;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.graphinventory.Format;


@RunWith(MockitoJUnitRunner.class) 
public class AAIQueryClientTest {
	
	@Mock
	Response response;
	
	@Mock
	RestClient restClient;
	
	@Spy
	AAIQueryClient aaiQueryClient = new AAIQueryClient();
	
	@Test
	public void testQuery() {
		List<AAIResourceUri> uris = Arrays.asList(AAIUriFactory.createResourceUri(AAIObjectType.CUSTOM_QUERY));
		
		Format format = Format.SIMPLE;
		CustomQuery query = new CustomQuery(uris);
		
		doReturn(restClient).when(aaiQueryClient).createClient(isA(AAIUri.class));
		aaiQueryClient.query(format, query);
		verify(aaiQueryClient, times(1)).createClient(AAIUriFactory.createResourceUri(AAIObjectType.CUSTOM_QUERY).queryParam("format", format.toString()));
		verify(restClient, times(1)).put(query, String.class);
	}
	
	@Test
	public void testCreateClient() {
		String depth = "testDepth";
		AAISubgraphType subgraph = AAISubgraphType.STAR;
		
		aaiQueryClient.depth(depth);
		aaiQueryClient.nodesOnly();
		aaiQueryClient.subgraph(subgraph);
		
		AAIUri aaiUri = spy(AAIUriFactory.createResourceUri(AAIObjectType.CUSTOM_QUERY));
		doReturn(aaiUri).when(aaiUri).clone();
		aaiQueryClient.setupQueryParams(aaiUri);
		
		verify(aaiUri, times(1)).queryParam("depth", depth);
		verify(aaiUri, times(1)).queryParam("nodesOnly", "");
		verify(aaiUri, times(1)).queryParam("subgraph", subgraph.toString());
	}
}
