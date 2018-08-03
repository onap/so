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
package org.onap.so.montoring.db.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.data.repository.InfraActiveRequestsRepository;
import org.onap.so.montoring.model.SoInfraRequest;
import org.onap.so.montoring.model.SoInfraRequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author waqas.ikram@ericsson.com
 */
@Service
public class DatabaseServiceProviderImpl implements DatabaseServiceProvider {

    private final InfraActiveRequestsRepository infraActiveRequestsRepository;

    @Autowired
    public DatabaseServiceProviderImpl(final InfraActiveRequestsRepository infraActiveRequestsRepository) {
        this.infraActiveRequestsRepository = infraActiveRequestsRepository;
    }

    @Override
    public List<SoInfraRequest> getSoInfraRequest(final Map<String, String[]> filters) {
        return getSoInfraRequest(infraActiveRequestsRepository.getInfraActiveRequests(filters));
    }

    @Override
    public List<SoInfraRequest> getSoInfraRequest(final Map<String, String[]> filters, final long startTime, final long endTime,
            final Integer maxResult) {
        final List<InfraActiveRequests> requests = infraActiveRequestsRepository.getInfraActiveRequests(filters, startTime, endTime, maxResult);
        return getSoInfraRequest(requests);
    }
    
    
    private List<SoInfraRequest> getSoInfraRequest(final List<InfraActiveRequests> requests) {
        final List<SoInfraRequest> result = new ArrayList<>(requests.size());
        for (final InfraActiveRequests activeRequests : requests) {
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
