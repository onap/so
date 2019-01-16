/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
package org.onap.so.monitoring.db.service;

import static org.onap.so.monitoring.configuration.rest.HttpServiceProviderConfiguration.DATABASE_HTTP_REST_SERVICE_PROVIDER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.onap.so.monitoring.camunda.model.SoActiveInfraRequests;
import org.onap.so.monitoring.configuration.database.DatabaseUrlProvider;
import org.onap.so.monitoring.model.SoInfraRequest;
import org.onap.so.monitoring.model.SoInfraRequestBuilder;
import org.onap.so.monitoring.rest.service.HttpRestServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;

/**
 * @author waqas.ikram@ericsson.com
 */
@Service
public class DatabaseServiceProviderImpl implements DatabaseServiceProvider {

    private final DatabaseUrlProvider urlProvider;

    private final HttpRestServiceProvider httpRestServiceProvider;

    @Autowired
    public DatabaseServiceProviderImpl(final DatabaseUrlProvider urlProvider,
            @Qualifier(DATABASE_HTTP_REST_SERVICE_PROVIDER) @Autowired final HttpRestServiceProvider httpRestServiceProvider) {
        this.urlProvider = urlProvider;
        this.httpRestServiceProvider = httpRestServiceProvider;
    }

    @Override
    public List<SoInfraRequest> getSoInfraRequest(final Map<String, String[]> filters, final long startTime,
            final long endTime, final Integer maxResult) {
        final String url = urlProvider.getSearchUrl(startTime, endTime, maxResult);

        final Optional<SoActiveInfraRequests[]> optionalRequests =
                httpRestServiceProvider.postHttpRequest(filters, url, SoActiveInfraRequests[].class);
        if (optionalRequests.isPresent()) {
            return getSoInfraRequest(optionalRequests.get());
        }
        return Collections.emptyList();
    }


    private List<SoInfraRequest> getSoInfraRequest(final SoActiveInfraRequests[] requests) {
        final List<SoInfraRequest> result = new ArrayList<>(requests.length);
        for (final SoActiveInfraRequests activeRequests : requests) {
            final SoInfraRequest soInfraRequest =
                    new SoInfraRequestBuilder().setRequestId(activeRequests.getRequestId())
                            .setServiceInstanceId(activeRequests.getServiceInstanceId())
                            .setNetworkId(activeRequests.getNetworkId()).setEndTime(activeRequests.getEndTime())
                            .setRequestStatus(activeRequests.getRequestStatus())
                            .setServiceIstanceName(activeRequests.getServiceInstanceName())
                            .setServiceType(activeRequests.getServiceType()).setStartTime(activeRequests.getStartTime())
                            .build();
            result.add(soInfraRequest);

        }
        return result;
    }


}
