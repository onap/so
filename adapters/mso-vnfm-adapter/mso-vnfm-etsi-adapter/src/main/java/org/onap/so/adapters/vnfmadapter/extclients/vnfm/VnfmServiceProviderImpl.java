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

import com.google.common.base.Optional;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class VnfmServiceProviderImpl implements VnfmServiceProvider {

    private final HttpRestServiceProvider httpServiceProvider;
    private final VnfmUrlProvider urlProvider;

    @Autowired
    public VnfmServiceProviderImpl(final VnfmUrlProvider urlProvider,
            @Qualifier("vnfmServiceProvider") final HttpRestServiceProvider httpServiceProvider) {
        this.httpServiceProvider = httpServiceProvider;
        this.urlProvider = urlProvider;
    }

    @Override
    public Optional<InlineResponse201> getVnf(final String vnfSelfLink) {
        return httpServiceProvider.get(vnfSelfLink, InlineResponse201.class);
    }

    @Override
    public Optional<InlineResponse200> getOperation(final String vnfmId, final String operationId) {
        final String url = urlProvider.getOperationUrl(vnfmId, operationId);
        return httpServiceProvider.get(url, InlineResponse200.class);
    }
}
