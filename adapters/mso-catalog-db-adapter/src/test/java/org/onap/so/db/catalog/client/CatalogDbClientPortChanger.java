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

package org.onap.so.db.catalog.client;

import org.springframework.stereotype.Component;
import java.net.URI;

@Component()
public class CatalogDbClientPortChanger extends CatalogDbClient {

    public String wiremockPort;

    CatalogDbClientPortChanger() {

    }

    CatalogDbClientPortChanger(String baseUri, String auth, String wiremockPort) {
        super(baseUri, auth);
        this.wiremockPort = wiremockPort;
    }

    protected URI getUri(String template) {
        URI uri = URI.create(template);
        String path = uri.getPath();
        String prefix = "http://localhost:" + wiremockPort;
        String query = uri.getQuery();

        return URI.create(prefix + path + (query == null || query.isEmpty() ? "" : "?" + query));
    }
}
