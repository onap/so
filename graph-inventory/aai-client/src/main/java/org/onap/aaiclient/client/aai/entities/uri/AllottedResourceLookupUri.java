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

package org.onap.aaiclient.client.aai.entities.uri;

import java.net.URI;
import java.util.Optional;
import jakarta.ws.rs.core.UriBuilder;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;

public class AllottedResourceLookupUri extends HttpLookupUri {

    private static final long serialVersionUID = -9212594383876793188L;

    protected AllottedResourceLookupUri(AAIObjectType type, UriBuilder builder, Optional<String> cachedValue,
            Object... values) {
        super(type, builder, cachedValue, values);
    }

    protected AllottedResourceLookupUri(AAISingleFragment fragment) {
        super(AAIFluentTypeBuilder.business().customer("").serviceSubscription("").serviceInstance("")
                .allottedResource("").build(), fragment.get().values());
    }

    @Override
    public AllottedResourceLookupUri clone() {
        return new AllottedResourceLookupUri(this.aaiType, this.internalURI.clone(), this.getCachedValue(), values);
    }

    public AAIResourcesClient getResourcesClient() {
        return new AAIResourcesClient();
    }

    @Override
    public URI buildNoNetwork() {
        return super.build(new String[] {"NONE", "NONE", "NONE", (String) this.values[0]});
    }
}
