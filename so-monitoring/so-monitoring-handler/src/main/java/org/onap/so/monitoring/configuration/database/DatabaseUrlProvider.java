/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.monitoring.configuration.database;

import java.net.URI;

import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author waqas.ikram@ericsson.com
 *
 */
public class DatabaseUrlProvider {

    private final URI baseUri;

    public DatabaseUrlProvider(final String baseUrl) {
        this.baseUri = UriComponentsBuilder.fromHttpUrl(baseUrl).build().toUri();
    }

    public String getSearchUrl(final long from, final long to, final Integer maxResult) {
        final UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseUri).pathSegment("v1")
                .pathSegment("getInfraActiveRequests").queryParam("from", from).queryParam("to", to);
        if (maxResult != null) {
            return builder.queryParam("maxResult", maxResult).build().toString();
        }

        return builder.build().toString();
    }

}
