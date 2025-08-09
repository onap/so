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

package org.onap.so.client.dmaap;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.onap.so.client.dmaap.rest.RestPublisher;

public abstract class DmaapPublisher extends DmaapClient {

    private long seconds;
    private final Publisher publisher;

    public DmaapPublisher() throws FileNotFoundException, IOException {
        super("dmaap/default-consumer.properties");
        this.publisher = new RestPublisher(properties);
        this.seconds = 20;

    }

    public DmaapPublisher(long seconds) throws FileNotFoundException, IOException {
        this();
        this.seconds = seconds;
    }

    public void send(String json) {
        logger.info("publishing message to dmaap topic {}: {}", this.getTopic(), json);
        publisher.send(json);
        // publisher.close(seconds, TimeUnit.SECONDS);
    }


}
