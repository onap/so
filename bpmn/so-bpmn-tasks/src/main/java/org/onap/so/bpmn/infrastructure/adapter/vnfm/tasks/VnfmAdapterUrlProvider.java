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

package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks;

import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 
 * Provides VNFM adapter {@link java.net.URL}
 * 
 * @author waqas.ikram@est.tech
 *
 */
@Service
public class VnfmAdapterUrlProvider {

    private final URI baseUri;

    @Autowired
    public VnfmAdapterUrlProvider(
            @Qualifier("VnfmBasicHttpConfigProvider") final VnfmBasicHttpConfigProvider etsiVnfmAdapter) {
        this.baseUri = UriComponentsBuilder.fromHttpUrl(etsiVnfmAdapter.getUrl()).build().toUri();
    }

    /**
     * Get VNFM create and instantiate URL
     * 
     * @param vnfId The identifier of the VNF. This must be the vnf-id of an existing generic-vnf in AAI.
     * @return VNFM create and instantiate URL
     */
    public String getCreateInstantiateUrl(final String vnfId) {
        return UriComponentsBuilder.fromUri(baseUri).pathSegment("vnfs").pathSegment(vnfId).build().toString();
    }

    /**
     * Get VNFM delete URL
     * 
     * @param vnfId The identifier of the VNF. This must be the vnf-id of an existing generic-vnf in AAI.
     * @return VNFM delete URL
     */
    public String getDeleteUrl(final String vnfId) {
        return UriComponentsBuilder.fromUri(baseUri).pathSegment("vnfs").pathSegment(vnfId).build().toString();
    }

    /**
     * Get job status URL
     * 
     * @param jobId The instantiation job identifier
     * @return job status URL
     */
    public String getJobStatusUrl(final String jobId) {
        return UriComponentsBuilder.fromUri(baseUri).pathSegment("jobs").pathSegment(jobId).build().toString();
    }

}
