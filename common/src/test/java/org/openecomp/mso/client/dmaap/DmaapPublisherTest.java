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

import org.junit.Test;

import javax.ws.rs.ProcessingException;
import java.io.IOException;
import java.util.Optional;

public class DmaapPublisherTest {

    DmaapPublisher dmaapPublisher = new DmaapPublisher(120) {
        @Override
        public String getUserName() {
            return "test";
        }

        @Override
        public String getPassword() {
            return "test";
        }

        @Override
        public String getTopic() {
            return "test";
        }

        @Override
        public Optional<String> getHost() {
            return Optional.of("http://localhost:8080");
        }
    };

    public DmaapPublisherTest() throws IOException {
    }

    @Test(expected = ProcessingException.class)
    public void sendTest() throws Exception {
            dmaapPublisher.send("{'key': 'value'}");
    }

}