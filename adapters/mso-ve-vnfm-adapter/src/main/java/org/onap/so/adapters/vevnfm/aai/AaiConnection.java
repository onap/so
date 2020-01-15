/*-
 * ============LICENSE_START=======================================================
 * SO
 * ================================================================================
 * Copyright (C) 2020 Samsung. All rights reserved.
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

package org.onap.so.adapters.vevnfm.aai;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.onap.so.client.aai.AAIClient;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AaiConnection {

    private static final Logger logger = LoggerFactory.getLogger(AaiConnection.class);

    private static final String NAME = "esr-vnfms";

    @Value("${aai.path}")
    private String aaiPath;

    public Map receiveVnfm() {
        final AAIObjectType vnfms = new AaiObjectTypeExt(aaiPath, NAME);
        final AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(vnfms);
        final AAIClient client = new AaiClientExt();
        final AAIResourcesClient resourcesClient = new AAIResourcesClient(client);
        final Optional<List> response = resourcesClient.get(List.class, resourceUri);

        if (response.isPresent()) {
            final Object vnfm = response.get().get(0);
            logger.info("The VNFM replied with: {}", vnfm);
            return (Map) vnfm;
        }

        return null;
    }
}
