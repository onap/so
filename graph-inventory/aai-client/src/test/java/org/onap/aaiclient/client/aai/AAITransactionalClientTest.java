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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.GenericType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIClientUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.graphinventory.GraphInventoryPatchConverter;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@RunWith(MockitoJUnitRunner.class)
public class AAITransactionalClientTest {

    private final static String AAI_JSON_FILE_LOCATION = "src/test/resources/__files/aai/bulkprocess/";
    AAIResourceUri uriA = AAIClientUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("test1"));
    AAIResourceUri uriB =
            AAIClientUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().pserver("test2"));
    AAIResourceUri uriC = AAIClientUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("test3"));
    AAIResourceUri uriD =
            AAIClientUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().pserver("test4"));
    AAIResourceUri uriE = AAIClientUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("test5"));
    AAIResourceUri uriF =
            AAIClientUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().pserver("test6"));
    AAIResourceUri uriG = AAIClientUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("test7"));

    ObjectMapper mapper;

    public AAIClient client = new AAIClient();

    @Spy
    public AAIResourcesClient aaiClient = new AAIResourcesClient();

    @Before
    public void before() throws JsonParseException, JsonMappingException, IOException {
        mapper = new AAICommonObjectMapperProvider().getMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Test
    public void testCreate() throws IOException {
        final Relationship body = new Relationship();
        body.setRelatedLink(uriB.build().toString());

        AAITransactionalClient transactions = aaiClient.beginTransaction().create(uriA.clone().relationshipAPI(), body);

        String serializedTransactions = mapper.writeValueAsString(transactions.getTransactions());
        Map<String, Object> actual =
                mapper.readValue(serializedTransactions, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> expected =
                mapper.readValue(getJson("test-request-small.json"), new TypeReference<Map<String, Object>>() {});

        assertEquals(actual, expected);
    }

    @Test
    public void testConnect() throws IOException {
        List<AAIResourceUri> uris = new ArrayList<>();
        uris.add(uriB);

        Map<String, Object> map = new HashMap<>();
        map.put("resource-version", "1234");
        doReturn(Optional.of(map)).when(aaiClient).get(any(GenericType.class), eq(uriG));
        AAIResourceUri uriAClone = uriA.clone();
        AAITransactionalClient transactions = aaiClient.beginTransaction().connect(uriA, uris).connect(uriC, uriD)
                .beginNewTransaction().connect(uriE, uriF).beginNewTransaction().delete(uriG);

        String serializedTransactions = mapper.writeValueAsString(transactions.getTransactions());
        Map<String, Object> actual =
                mapper.readValue(serializedTransactions, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> expected =
                mapper.readValue(getJson("test-request.json"), new TypeReference<Map<String, Object>>() {});

        assertEquals(actual, expected);
        assertEquals("uri not manipulated", uriAClone.build().toString(), uriA.build().toString());
    }

    @Test
    public void testDisconnect() throws IOException {
        List<AAIResourceUri> uris = new ArrayList<>();
        uris.add(uriB);

        AAITransactionalClient transactions = aaiClient.beginTransaction().disconnect(uriA, uris);

        String serializedTransactions = mapper.writeValueAsString(transactions.getTransactions());
        Map<String, Object> actual =
                mapper.readValue(serializedTransactions, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> expected = mapper.readValue(getJson("test-request-small.json").replace("put", "delete"),
                new TypeReference<Map<String, Object>>() {});

        assertEquals(actual, expected);
    }

    @Test
    public void testUpdate() throws IOException {
        final Relationship body = new Relationship();
        body.setRelatedLink(uriB.build().toString());

        AAIResourceUri uriAClone = uriA.clone().relationshipAPI();
        AAITransactionalClient transactions = aaiClient.beginTransaction().update(uriAClone, body);

        String serializedTransactions = mapper.writeValueAsString(transactions.getTransactions());
        Map<String, Object> actual =
                mapper.readValue(serializedTransactions, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> expected = mapper.readValue(getJson("test-request-small.json").replace("put", "patch"),
                new TypeReference<Map<String, Object>>() {});

        assertEquals(actual, expected);
    }

    @Test
    public void verifyResponse() throws IOException {
        AAITransactionalClient transactions = aaiClient.beginTransaction();

        assertEquals("success status", Optional.empty(),
                transactions.locateErrorMessages(getJson("response-success.json")));
        assertEquals(transactions.locateErrorMessages(getJson("response-failure.json")).get(),
                "another error message\nmy great error");
    }

    @Test
    public void confirmPatchFormat() {
        AAITransactionalClient transactionClient = spy(new AAITransactionalClient(aaiClient, client));
        GraphInventoryPatchConverter mock = mock(GraphInventoryPatchConverter.class);
        doReturn(mock).when(transactionClient).getPatchConverter();
        transactionClient.update(uriA, "{}");
        verify(mock, times(1)).convertPatchFormat(any());
    }

    private String getJson(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + filename)));
    }

}
