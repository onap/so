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

package org.onap.so.bpmn.infrastructure.pnf.management;

import java.util.Optional;
import org.onap.aai.domain.yang.Pnf;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.AAIRestClientImpl;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.aai.entities.uri.AAIClientUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.springframework.stereotype.Component;

@Component
public class PnfManagementImpl implements PnfManagement {

    @Override
    public Optional<Pnf> getEntryFor(String pnfCorrelationId) {
        AAIRestClientImpl restClient = new AAIRestClientImpl();
        return restClient.getPnfByName(pnfCorrelationId);
    }

    @Override
    public void createEntry(String pnfCorrelationId, Pnf entry) {
        AAIRestClientImpl restClient = new AAIRestClientImpl();
        restClient.createPnf(pnfCorrelationId, entry);
    }

    public void updateEntry(String pnfCorrelationId, Pnf entry) {
        AAIRestClientImpl restClient = new AAIRestClientImpl();
        restClient.updatePnf(pnfCorrelationId, entry);
    }

    @Override
    public void createRelation(String serviceInstanceId, String pnfName) {
        AAIResourceUri serviceInstanceURI =
                AAIClientUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId));
        AAIResourceUri pnfUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().pnf(pnfName));
        new AAIResourcesClient().connect(serviceInstanceURI, pnfUri);
    }
}
