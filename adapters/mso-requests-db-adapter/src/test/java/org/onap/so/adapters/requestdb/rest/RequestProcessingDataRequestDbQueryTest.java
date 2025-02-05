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

package org.onap.so.adapters.requestdb.rest;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import jakarta.transaction.Transactional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.requestsdb.application.MSORequestDBApplication;
import org.onap.so.db.request.beans.RequestProcessingData;
import org.onap.so.db.request.client.RequestsDbClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MSORequestDBApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class RequestProcessingDataRequestDbQueryTest {
    @Autowired
    private RequestsDbClient client;

    @LocalServerPort
    private int port;

    @Before
    public void setPort() {
        client.removePortFromEndpoint();
        client.setPortToEndpoint(Integer.toString(port));
    }

    @Test
    @Transactional
    public void RequestProcessingDataBySoRequestIdTest() {
        String soRequestId = "00032ab7-na18-42e5-965d-8ea592502018";
        String tag = "pincFabricConfigRequest";
        RequestProcessingData firstEntry = new RequestProcessingData();
        RequestProcessingData secondEntry = new RequestProcessingData();
        List<RequestProcessingData> expectedList = new ArrayList<>();
        firstEntry.setSoRequestId(soRequestId);
        firstEntry.setGroupingId("7d2e8c07-4d10-456d-bddc-37abf38ca715");
        firstEntry.setName("configurationId");
        firstEntry.setValue("52234bc0-d6a6-41d4-a901-79015e4877e2");
        firstEntry.setTag(tag);
        secondEntry.setSoRequestId(soRequestId);
        secondEntry.setGroupingId("7d2e8c07-4d10-456d-bddc-37abf38ca714");
        secondEntry.setName("requestAction");
        secondEntry.setValue("assign");
        secondEntry.setTag(tag);
        expectedList.add(firstEntry);
        expectedList.add(secondEntry);

        List<RequestProcessingData> dataFound = client.getRequestProcessingDataBySoRequestId(soRequestId);
        // bean comparison with shazam fails serialization: Forgot to register a type adapter?
        assertEquals(dataFound.get(0).getSoRequestId(), firstEntry.getSoRequestId());
        assertEquals(dataFound.get(0).getGroupingId(), firstEntry.getGroupingId());
        assertEquals(dataFound.get(0).getName(), firstEntry.getName());
        assertEquals(dataFound.get(0).getValue(), firstEntry.getValue());
        assertEquals(dataFound.get(0).getTag(), firstEntry.getTag());
        assertEquals(dataFound.get(1).getSoRequestId(), secondEntry.getSoRequestId());
        assertEquals(dataFound.get(1).getGroupingId(), secondEntry.getGroupingId());
        assertEquals(dataFound.get(1).getName(), secondEntry.getName());
        assertEquals(dataFound.get(1).getValue(), secondEntry.getValue());
        assertEquals(dataFound.get(1).getTag(), secondEntry.getTag());
    }

    @Test
    @Transactional
    public void testGetRequestProcessingDataBySoRequestIdAndIsInternalData() {
        String soRequestId = "00032ab7-na18-42e5-965d-8ea592502018";
        String tag = "pincFabricConfigRequest";
        RequestProcessingData firstEntry = new RequestProcessingData();
        List<RequestProcessingData> expectedList = new ArrayList<>();
        firstEntry.setSoRequestId(soRequestId);
        firstEntry.setGroupingId("7d2e8c07-4d10-456d-bddc-37abf38ca715");
        firstEntry.setName("configurationId");
        firstEntry.setValue("52234bc0-d6a6-41d4-a901-79015e4877e2");
        firstEntry.setTag(tag);
        expectedList.add(firstEntry);

        List<RequestProcessingData> dataFound = client.getExternalRequestProcessingDataBySoRequestId(soRequestId);

        assertEquals(1, dataFound.size());
        assertEquals(dataFound.get(0).getSoRequestId(), firstEntry.getSoRequestId());
        assertEquals(dataFound.get(0).getGroupingId(), firstEntry.getGroupingId());
        assertEquals(dataFound.get(0).getName(), firstEntry.getName());
        assertEquals(dataFound.get(0).getValue(), firstEntry.getValue());
        assertEquals(dataFound.get(0).getTag(), firstEntry.getTag());
    }
}
