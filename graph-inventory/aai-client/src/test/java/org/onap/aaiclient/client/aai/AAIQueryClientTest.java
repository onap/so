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

package org.onap.aaiclient.client.aai;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.GenericType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.Complex;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.CustomQuery;
import org.onap.aaiclient.client.aai.entities.Results;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.aaiclient.client.graphinventory.Format;
import org.onap.aaiclient.client.graphinventory.GraphInventoryClient;
import org.onap.aaiclient.client.graphinventory.GraphInventorySubgraphType;
import org.onap.aaiclient.client.graphinventory.entities.Pathed;
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth;
import org.onap.so.client.RestClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


@RunWith(MockitoJUnitRunner.class)
public class AAIQueryClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private GraphInventoryClient client;

    @InjectMocks
    @Spy
    private AAIQueryClient aaiQueryClient = new AAIQueryClient();

    private String AAI_JSON_FILE_LOCATION = "src/test/resources/__files/aai/query/";

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testQuery() {
        List<AAIResourceUri> uris = Arrays.asList(AAIUriFactory.createResourceUri(AAIObjectType.CUSTOM_QUERY));

        Format format = Format.SIMPLE;
        CustomQuery query = new CustomQuery(uris);

        doReturn(restClient).when(client).createClient(isA(AAIResourceUri.class));
        aaiQueryClient.query(format, query);
        verify(client, times(1))
                .createClient(AAIUriFactory.createResourceUri(AAIObjectType.CUSTOM_QUERY).format(format));
        verify(restClient, times(1)).put(query, String.class);
    }

    @Test
    public void testCreateClient() {
        Depth depth = Depth.ZERO;
        GraphInventorySubgraphType subgraph = GraphInventorySubgraphType.STAR;

        aaiQueryClient.depth(depth);
        aaiQueryClient.nodesOnly();
        aaiQueryClient.subgraph(subgraph);

        AAIResourceUri aaiUri = spy(AAIUriFactory.createResourceUri(AAIObjectType.CUSTOM_QUERY));
        doReturn(aaiUri).when(aaiUri).clone();
        aaiQueryClient.setupQueryParams(aaiUri);

        verify(aaiUri, times(1)).queryParam("depth", "0");
        verify(aaiUri, times(1)).queryParam("nodesOnly", "");
        verify(aaiUri, times(1)).queryParam("subgraph", subgraph.toString());
    }

    @Test
    public void querySingleResourceTest() throws IOException {
        doReturn(getJson("single-query-result.json")).when(aaiQueryClient).query(eq(Format.RESOURCE_AND_URL),
                any(CustomQuery.class));
        List<Complex> result = aaiQueryClient.querySingleResource(
                new CustomQuery(Arrays.asList(AAIUriFactory.createNodesUri(Types.COMPLEX.getFragment("test")))),
                Complex.class);
        assertEquals(2, result.size());
        assertEquals("complex-id-15100-jc689q2", result.get(1).getPhysicalLocationId());
    }

    @Test
    public void getResourceAndUrlTest() throws IOException {
        doReturn(getJson("single-query-result.json")).when(aaiQueryClient).query(eq(Format.RESOURCE_AND_URL),
                any(CustomQuery.class));
        List<ResourceAndUrl<AAIResultWrapper>> result = aaiQueryClient.getResourceAndUrl(
                new CustomQuery(Arrays.asList(AAIUriFactory.createNodesUri(Types.COMPLEX.getFragment("test")))));
        assertEquals(2, result.size());

        assertEquals(1, result.get(1).getWrapper().getRelationships().get().getRelatedUris(Types.PSERVER).size());
    }

    @Test
    public void querySingleTypeTest() throws IOException {
        when(client.createClient(isA(AAIResourceUri.class))).thenReturn(restClient);
        when(restClient.put(any(Object.class), any(GenericType.class))).thenReturn(
                mapper.readValue(getJson("pathed-result.json"), new TypeReference<Results<Map<String, Object>>>() {}));


        List<Pathed> results = aaiQueryClient.queryPathed(
                new CustomQuery(Arrays.asList(AAIUriFactory.createNodesUri(Types.COMPLEX.getFragment("test")))));

        assertEquals(2, results.size());
        assertEquals("service-instance", results.get(1).getResourceType());
    }

    private String getJson(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + filename)));
    }
}
