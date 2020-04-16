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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.util.Strings;
import org.onap.aai.domain.yang.EsrSystemInfo;
import org.onap.aai.domain.yang.EsrSystemInfoList;
import org.onap.aai.domain.yang.EsrVnfm;
import org.onap.aai.domain.yang.EsrVnfmList;
import org.onap.aai.domain.yang.v18.GenericVnf;
import org.onap.so.adapters.vevnfm.exception.VeVnfmException;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.graphinventory.entities.uri.Depth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AaiConnection {

    private static final Logger logger = LoggerFactory.getLogger(AaiConnection.class);

    private static final int FIRST_INDEX = 0;

    private AAIResourcesClient resourcesClient = null;

    private static void isValid(final List<EsrSystemInfo> infos) throws VeVnfmException {
        if (infos == null || infos.isEmpty() || Strings.isBlank(infos.get(FIRST_INDEX).getServiceUrl())) {
            throw new VeVnfmException("No 'url' field in VNFM info");
        }
    }

    private AAIResourcesClient getResourcesClient() {
        if (resourcesClient == null) {
            resourcesClient = new AAIResourcesClient();
        }

        return resourcesClient;
    }

    public List<EsrSystemInfo> receiveVnfm() throws VeVnfmException {
        List<EsrSystemInfo> infos;

        try {
            infos = receiveVnfmInternal();
        } catch (Exception e) {
            throw new VeVnfmException(e);
        }

        isValid(infos);

        return infos;
    }

    private List<EsrSystemInfo> receiveVnfmInternal() {
        final AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIObjectType.VNFM_LIST);
        final Optional<EsrVnfmList> response = getResourcesClient().get(EsrVnfmList.class, resourceUri);

        if (response.isPresent()) {
            final EsrVnfmList esrVnfmList = response.get();
            logger.info("The AAI ESR replied with: {}", esrVnfmList);
            final List<EsrVnfm> esrVnfm = esrVnfmList.getEsrVnfm();

            final List<EsrSystemInfo> infos = new LinkedList<>();

            for (final EsrVnfm vnfm : esrVnfm) {
                final String vnfmId = vnfm.getVnfmId();
                infos.addAll(receiveVnfmServiceUrl(vnfmId));
            }

            return infos;
        }

        return null;
    }

    private List<EsrSystemInfo> receiveVnfmServiceUrl(final String vnfmId) {
        final Optional<EsrVnfm> response = getResourcesClient().get(EsrVnfm.class,
                AAIUriFactory.createResourceUri(AAIObjectType.VNFM, vnfmId).depth(Depth.ONE));

        if (response.isPresent()) {
            final EsrVnfm esrVnfm = response.get();
            logger.info("The AAI ESR replied with: {}", esrVnfm);
            final EsrSystemInfoList esrSystemInfoList = esrVnfm.getEsrSystemInfoList();

            if (esrSystemInfoList != null) {
                return esrSystemInfoList.getEsrSystemInfo();
            }
        }

        return Collections.emptyList();
    }

    public String checkGenericVnfId(final String vnfId) {
        final AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId);
        final Optional<GenericVnf> response = getResourcesClient().get(GenericVnf.class, resourceUri);

        if (response.isPresent()) {
            final GenericVnf vnf = response.get();
            logger.info("The AAI replied with: {}", vnf);
            return vnf.getVnfInstanceId();
        }

        return null;
    }
}
