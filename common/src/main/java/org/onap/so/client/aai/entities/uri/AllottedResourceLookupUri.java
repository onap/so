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

package org.onap.so.client.aai.entities.uri;

import java.net.URI;
import java.util.Optional;
import javax.ws.rs.core.UriBuilder;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;

public class AllottedResourceLookupUri extends HttpLookupUri {

    private static final long serialVersionUID = -9212594383876793188L;

    protected AllottedResourceLookupUri(Object... values) {
        super(AAIObjectType.ALLOTTED_RESOURCE, values);
    }

    protected AllottedResourceLookupUri(UriBuilder builder, Optional<String> cachedValue, Object... values) {
        super(AAIObjectType.ALLOTTED_RESOURCE, builder, cachedValue, values);
    }

    @Override
    public AllottedResourceLookupUri clone() {
        return new AllottedResourceLookupUri(this.internalURI.clone(), this.getCachedValue(), values);
    }

    public AAIResourcesClient getResourcesClient() {
        return new AAIResourcesClient();
    }

    @Override
    public URI buildNoNetwork() {
        return super.build(new String[] {"NONE", "NONE", "NONE", (String) this.values[0]});
    }
}
