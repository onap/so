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

package org.onap.so.adapters.requestsdb.client;


import org.onap.so.db.request.client.RequestsDbClient;
import org.springframework.stereotype.Component;
import java.net.URI;

@Component
public class RequestDbClientPortChanger extends RequestsDbClient {
    private int port;

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public URI getUri(String uri) {
        uri = uri.replace("8081", String.valueOf(port));
        return URI.create(uri);
    }
}

