/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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

package org.openecomp.mso.client.dmaap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import org.junit.Test;
import org.openecomp.mso.client.sdno.dmaap.PnfReadyEventConsumer;

public class PnfReadyEventConsumerTest {

    private static final String CORRELATION_ID = "correlation_id_test";

    private static final String JSON_WITH_CORRELATION_ID = " {\"pnfRegistrationFields\": {\n"
            + "      \"correlationId\": \"correlation_id_test\"\n"
            + "    }}";

    @Test
    public void eventIsFoundForGivenCorrelationId2() throws Exception {
        PnfReadyEventConsumerForTesting testedObjectSpy = spy(new PnfReadyEventConsumerForTesting(CORRELATION_ID));
        Consumer consumerMock = mock(Consumer.class);
        when(testedObjectSpy.getConsumer()).thenReturn(consumerMock);
        when(consumerMock.fetch()).thenReturn(Arrays.asList(JSON_WITH_CORRELATION_ID));
        testedObjectSpy.consume();
        assertThat(testedObjectSpy.continuePolling()).isFalse();
    }

    // TODO this is temporary class, when methods are defined, it will be deleted
    private class PnfReadyEventConsumerForTesting extends PnfReadyEventConsumer {

        public PnfReadyEventConsumerForTesting(String correlationId) throws IOException {
            super(correlationId);
        }

        @Override
        public String getUserName(){
            return "userNameTest";
        }
        @Override
        public String getPassword(){
            return "passTest";
        }
        @Override
        public String getTopic(){
            return "topicTest";
        }
        @Override
        public Optional<String> getHost(){
            return Optional.of("http://localhost");
        }
        @Override
        public boolean isFailure(String message) {
            return false;
        }
        @Override
        public String getRequestId() {
            return "requestTest";
        }
    }

}
