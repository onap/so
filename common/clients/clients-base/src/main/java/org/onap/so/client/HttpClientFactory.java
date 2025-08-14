/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Nokia.
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
package org.onap.so.client;

import java.net.URL;
import javax.ws.rs.core.MediaType;
import org.onap.logging.filter.base.ONAPComponentsList;

public class HttpClientFactory {

    public HttpClient newJsonClient(URL host, ONAPComponentsList targetEntity) {
        return new HttpClient(host, MediaType.APPLICATION_JSON, targetEntity);
    }

    public HttpClient newXmlClient(URL host, ONAPComponentsList targetEntity) {
        return new HttpClient(host, MediaType.APPLICATION_XML, MediaType.APPLICATION_XML, targetEntity);
    }

    public HttpClient newTextXmlClient(URL host, ONAPComponentsList targetEntity) {
        return new HttpClient(host, MediaType.TEXT_XML, MediaType.TEXT_XML, targetEntity);
    }
}
