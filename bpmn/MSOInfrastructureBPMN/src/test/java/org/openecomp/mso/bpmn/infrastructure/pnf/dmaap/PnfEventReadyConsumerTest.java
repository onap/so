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

package org.openecomp.mso.bpmn.infrastructure.pnf.dmaap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration({"classpath:springConfig_PnfEventReadyConsumer.xml"})
public class PnfEventReadyConsumerTest {

    @Autowired
    private PnfEventReadyConsumer pnfEventReadyConsumer;

    private HttpClient httpClientMock;

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        httpClientMock = mock(HttpClient.class);
        setPrivateField();
    }

    @Test
    public void restClientInvokesWithProperURI() throws Exception {
        ArgumentCaptor<HttpGet> captor1 = ArgumentCaptor.forClass(HttpGet.class);
        pnfEventReadyConsumer.notifyWhenPnfReady("correlationId");
        verify(httpClientMock).execute(captor1.capture());
        assertThat(captor1.getValue().getURI()).hasHost("hostTest").hasPort(1234).hasScheme("http")
                .hasPath("/eventsForTesting/eventTopicTest/consumerGroupTest/consumerTestId");
    }

    private void setPrivateField() throws NoSuchFieldException, IllegalAccessException {
        Field field = pnfEventReadyConsumer.getClass().getDeclaredField("httpClient");
        field.setAccessible(true);
        field.set(pnfEventReadyConsumer, httpClientMock);
    }

}
