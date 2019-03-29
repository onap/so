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

package org.onap.so.adapters.vnfmadapter.extclients.aai;

import static org.slf4j.LoggerFactory.getLogger;
import java.net.URI;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Provides URLs for REST calls to AAI.
 */
@Service
public class AaiUrlProvider {

    private static Logger logger = getLogger(AaiUrlProvider.class);
    @Value("${aai.version}")
    private String aaiVersion;
    @Value("${aai.endpoint}")
    private String endpoint;

    /**
     * Get the URL for a generic VNF in AAI.
     *
     * @param vnfId The identifier of the VNF
     * @return the URL of the VNF
     */
    public String getGenericVnfUrl(final String vnfId) {
        final String url = UriComponentsBuilder.fromUri(getBaseUri(AaiService.NETWORK)).pathSegment("generic-vnfs")
                .pathSegment("generic-vnf").pathSegment(vnfId).build().toString();
        logger.debug("getVnfUrl:" + url);

        return url;
    }

    /**
     * Get the URL for VNFMs in AAI ESR
     *
     * @return the URL of the VNFMs
     */
    public String getVnfmsUrl() {
        final String url = UriComponentsBuilder.fromUri(getBaseUri(AaiService.ESR)).pathSegment("esr-vnfm-list").build()
                .toString();
        logger.debug("getVnfmsUrl:" + url);

        return url;
    }

    /**
     * Get the URL for a VNFM in AAI ESR
     *
     * @param vnfmId The identifier of the VNFM
     * @return the URL of the VNFM
     */
    public String getVnfmUrl(final String vnfmId) {
        final String url = UriComponentsBuilder.fromUri(getBaseUri(AaiService.ESR)).pathSegment("esr-vnfm-list")
                .pathSegment("esr-vnfm").pathSegment(vnfmId).build().toString();
        logger.debug("getVnfmUrl:" + url);

        return url;
    }

    /**
     * Get the URL for the esr system info list for a VNFM in AAI ESR
     *
     * @param vnfmId The identifier of the VNFM
     * @return the URL of the VNFM
     */
    public String getVnfmEsrSystemInfoListUrl(final String vnfmId) {
        final String url = UriComponentsBuilder.fromUri(getBaseUri(AaiService.ESR)).pathSegment("esr-vnfm-list")
                .pathSegment("esr-vnfm").pathSegment(vnfmId).pathSegment("esr-system-info-list").build().toString();
        logger.debug("getVnfmUrl:" + url);

        return url;
    }

    /**
     * Get the URL for a resource in AAI.
     *
     * @param aaiResourcePath The path of the resource
     * @return the URL of the resource
     */
    public String getResourceUrl(final String aaiResourcePath) {
        final String url = endpoint + aaiResourcePath;
        logger.debug("getResourceUrl:" + url);

        return url;
    }

    public String convertToRelativeUrl(final String url) {
        return url.substring(url.indexOf('/', url.indexOf(':')));
    }

    private URI getBaseUri(final AaiService aaiService) {
        final String url = endpoint + "/aai/" + aaiVersion + "/" + aaiService.getServiceName();
        return UriComponentsBuilder.fromHttpUrl(url).build().toUri();
    }

    enum AaiService {
        NETWORK {
            @Override
            String getServiceName() {
                return "network";
            }
        },
        ESR {
            @Override
            String getServiceName() {
                return "external-system";
            }
        },
        CLOUD {
            @Override
            String getServiceName() {
                return "cloud-infrastructure";
            }
        };

        abstract String getServiceName();
    }
}
