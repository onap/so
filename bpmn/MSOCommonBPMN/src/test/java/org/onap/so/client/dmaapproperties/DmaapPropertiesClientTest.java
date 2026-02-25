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

package org.onap.so.client.dmaapproperties;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.io.File;
import java.io.IOException;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.so.BaseTest;
import org.onap.so.client.avpn.dmaap.beans.AVPNDmaapBean;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DmaapPropertiesClientTest extends BaseTest {

    @Autowired
    private DmaapPropertiesClient dmaapPropertiesClient;


    private final String file = "src/test/resources/org/onap/so/client/avpn/dmaap/avpnDmaapAsyncRequestStatus.json";
    private String requestId = "rq1234d1-5a33-55df-13ab-12abad84e331";
    private String clientSource = "SPP";
    private String correlator = "myClientId123";
    private String serviceInstanceId = "bc305d54-75b4-431b-adb2-eb6b9e546014";
    private String startTime = "2017-11-17T09:30:47Z";
    private String finishTime = "2017-11-17T09:30:47Z";
    private String requestScope = "service";
    private String requestType = "createInstance";
    private String timestamp = "2017-11-17T09:30:47Z";
    private String requestState = "COMPLETE";
    private String statusMessage = "Success";
    private String percentProgress = "100";

    @Test
    public void testBuildRequestJson() throws IOException {
        AVPNDmaapBean actualBean = dmaapPropertiesClient.buildRequestJson(requestId, clientSource, correlator,
                serviceInstanceId, startTime, finishTime, requestScope, requestType, timestamp, requestState,
                statusMessage, percentProgress, true);

        AVPNDmaapBean expected = new ObjectMapper().readValue(new File(file), AVPNDmaapBean.class);

        assertNotNull(actualBean);
        assertThat(actualBean, sameBeanAs(expected));
    }

    @Test
    public void testDmaapPublishRequest() {
        DmaapPropertiesClient client = Mockito.spy(DmaapPropertiesClient.class);
        GlobalDmaapPublisher mockedClientDmaapPublisher = Mockito.mock(GlobalDmaapPublisher.class);
        AVPNDmaapBean mockedDmaapBean = Mockito.mock(AVPNDmaapBean.class);
        String request = "test";

        doReturn(mockedDmaapBean).when(client).buildRequestJson(requestId, clientSource, correlator, serviceInstanceId,
                startTime, finishTime, requestScope, requestType, timestamp, requestState, statusMessage,
                percentProgress, false);

        AVPNDmaapBean actualDmaapBean =
                client.buildRequestJson(requestId, clientSource, correlator, serviceInstanceId, startTime, finishTime,
                        requestScope, requestType, timestamp, requestState, statusMessage, percentProgress, false);
        mockedClientDmaapPublisher.send(request);

        doNothing().when(mockedClientDmaapPublisher).send(anyString());

        verify(client, times(1)).buildRequestJson(requestId, clientSource, correlator, serviceInstanceId, startTime,
                finishTime, requestScope, requestType, timestamp, requestState, statusMessage, percentProgress, false);
        verify(mockedClientDmaapPublisher, times(1)).send(request);

        assertNotNull(actualDmaapBean);
    }
}
