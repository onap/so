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

import java.util.Optional;
import org.onap.aai.domain.yang.EsrVnfm;
import org.onap.aai.domain.yang.EsrVnfmList;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.graphinventory.entities.uri.Depth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AaiConnection {

    private static final Logger logger = LoggerFactory.getLogger(AaiConnection.class);

    private static final int FIRST_INDEX = 0;

    public String receiveVnfm() {
        final AAIResourcesClient resourcesClient = new AAIResourcesClient();
        final Optional<EsrVnfmList> response =
                resourcesClient.get(EsrVnfmList.class, AAIUriFactory.createResourceUri(AAIObjectType.VNFM_LIST));

        if (response.isPresent()) {
            final EsrVnfmList esrVnfmList = response.get();
            logger.info("The VNFM replied with: {}", esrVnfmList);
            final String vnfmId = esrVnfmList.getEsrVnfm().get(FIRST_INDEX).getVnfmId();
            return receiveVnfmServiceUrl(resourcesClient, vnfmId);
        }

        return null;
    }

    private String receiveVnfmServiceUrl(final AAIResourcesClient resourcesClient, final String vnfmId) {
        final Optional<EsrVnfm> response = resourcesClient.get(EsrVnfm.class,
                AAIUriFactory.createResourceUri(AAIObjectType.VNFM, vnfmId).depth(Depth.ONE));

        if (response.isPresent()) {
            final EsrVnfm esrVnfm = response.get();
            logger.info("The VNFM replied with: {}", esrVnfm);
            return esrVnfm.getEsrSystemInfoList().getEsrSystemInfo().get(FIRST_INDEX).getServiceUrl();
        }

        return null;
    }
}
