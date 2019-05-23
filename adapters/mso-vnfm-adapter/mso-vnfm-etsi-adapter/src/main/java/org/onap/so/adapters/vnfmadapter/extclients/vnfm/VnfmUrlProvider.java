/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.adapters.vnfmadapter.extclients.vnfm;

import static org.slf4j.LoggerFactory.getLogger;
import java.net.URI;
import org.onap.aai.domain.yang.EsrSystemInfo;
import org.onap.aai.domain.yang.EsrSystemInfoList;
import org.onap.so.adapters.vnfmadapter.extclients.aai.AaiServiceProvider;
import org.onap.so.adapters.vnfmadapter.rest.exceptions.VnfmNotFoundException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Provides URLs for REST calls to a VNFM.
 */
@Service
public class VnfmUrlProvider {

    private static Logger logger = getLogger(VnfmUrlProvider.class);
    private final AaiServiceProvider aaiServiceProvider;

    @Autowired
    public VnfmUrlProvider(final AaiServiceProvider aaiServiceProvider) {
        this.aaiServiceProvider = aaiServiceProvider;
    }

    /**
     * Get the URL for an operation on a VNFM.
     *
     * @param vnfmId The ID of the VNFM
     * @return the URL of the operation
     */
    public String getOperationUrl(final String vnfmId, final String operationId) {
        final String url = UriComponentsBuilder.fromUri(getBaseUri(vnfmId)).pathSegment("vnf_lcm_op_occs")
                .pathSegment(operationId).build().toString();
        logger.debug("getOperationUrl:" + url);

        return url;
    }

    /**
     * Get the URL for the subscriptions on a VNFM.
     *
     * @param vnfmId The ID of the VNFM
     * @return the URL of the subscriptions
     */
    public String getSubscriptionsUrl(final String vnfmId) {
        final String url =
                UriComponentsBuilder.fromUri(getBaseUri(vnfmId)).pathSegment("subscriptions").build().toString();
        logger.debug("getSubscriptionUrl:" + url);

        return url;
    }

    public String getCreationUrl(final String vnfmId) {
        final String url =
                UriComponentsBuilder.fromUri(getBaseUri(vnfmId)).pathSegment("vnf_instances").build().toString();
        logger.debug("getCreationUrl:" + url);

        return url;
    }

    private URI getBaseUri(final String vnfmId) {
        final EsrSystemInfoList vnfmEsrSystemInfoList = aaiServiceProvider.invokeGetVnfmEsrSystemInfoList(vnfmId);

        if (vnfmEsrSystemInfoList != null) {
            for (final EsrSystemInfo esrSystemInfo : vnfmEsrSystemInfoList.getEsrSystemInfo()) {
                return UriComponentsBuilder.fromHttpUrl(esrSystemInfo.getServiceUrl()).build().toUri();
            }
        }

        throw new VnfmNotFoundException("VNFM, or Service URL for VNFM, not found for VNFM " + vnfmId);
    }
}
