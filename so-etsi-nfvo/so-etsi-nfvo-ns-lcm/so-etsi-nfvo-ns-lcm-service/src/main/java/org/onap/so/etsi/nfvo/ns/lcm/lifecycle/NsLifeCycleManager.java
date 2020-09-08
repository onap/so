/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
package org.onap.so.etsi.nfvo.ns.lcm.lifecycle;

import static org.slf4j.LoggerFactory.getLogger;
import java.net.URI;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.onap.so.etsi.nfvo.ns.lcm.EtsiSoNsLcmManagerUrlProvider;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.service.JobExecutorService;
import org.onap.so.etsi.nfvo.ns.lcm.model.CreateNsRequest;
import org.onap.so.etsi.nfvo.ns.lcm.model.InstantiateNsRequest;
import org.onap.so.etsi.nfvo.ns.lcm.model.NsInstancesNsInstance;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Service
public class NsLifeCycleManager {
    private static final Logger logger = getLogger(NsLifeCycleManager.class);

    private final JobExecutorService jobExecutorService;

    @Value("${etsi-so-ns-workflow-engine.requesttimeout.create.timeoutInSeconds:300}")
    private int timeOutInSeconds;

    private final EtsiSoNsLcmManagerUrlProvider etsiSoNsLcmManagerUrlProvider;

    @Autowired
    public NsLifeCycleManager(final JobExecutorService jobExecutorService,
            final EtsiSoNsLcmManagerUrlProvider etsiSoNsLcmManagerUrlProvider) {
        this.jobExecutorService = jobExecutorService;
        this.etsiSoNsLcmManagerUrlProvider = etsiSoNsLcmManagerUrlProvider;
    }

    public ImmutablePair<URI, NsInstancesNsInstance> createNs(final CreateNsRequest createNsRequest,
            final String globalCustomerId, final String serviceType) {
        logger.info("Will execute Create Ns for CreateNsRequest: {}, globalCustomerId: {} and serviceType: {}",
                createNsRequest, globalCustomerId, serviceType);
        final NsInstancesNsInstance nsInstanceResponse =
                jobExecutorService.runCreateNsJob(createNsRequest, globalCustomerId, serviceType);

        return ImmutablePair.of(etsiSoNsLcmManagerUrlProvider.getCreatedNsResourceUri(nsInstanceResponse.getId()),
                nsInstanceResponse);
    }

    public URI instantiateNs(final String nsInstanceId, final InstantiateNsRequest instantiateNsRequest) {
        logger.info("Will execute Instantiate Ns for InstantiateNsRequest: {} and nsInstanceId: {}",
                instantiateNsRequest, nsInstanceId);
        final String nsLcmOpOccId = jobExecutorService.runInstantiateNsJob(nsInstanceId, instantiateNsRequest);

        return etsiSoNsLcmManagerUrlProvider.getInstantiatedOccUri(nsLcmOpOccId);

    }

}
