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

package org.onap.svnfm.simulator.services;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.modelmapper.ModelMapper;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.CreateVnfRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InstantiateVnfRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.LccnSubscriptionRequest;
import org.onap.svnfm.simulator.config.ApplicationConfig;
import org.onap.svnfm.simulator.constants.Constant;
import org.onap.svnfm.simulator.model.VnfInstance;
import org.onap.svnfm.simulator.model.VnfOperation;
import org.onap.svnfm.simulator.model.Vnfds;
import org.onap.svnfm.simulator.notifications.VnfInstantiationNotification;
import org.onap.svnfm.simulator.notifications.VnfmAdapterCreationNotification;
import org.onap.svnfm.simulator.repository.VnfOperationRepository;
import org.onap.svnfm.simulator.repository.VnfmCacheRepository;
import org.onap.svnfm.simulator.repository.VnfmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.stereotype.Service;

/**
 *
 * @author Lathishbabu Ganesan (lathishbabu.ganesan@est.tech)
 * @author Ronan Kenny (ronan.kenny@est.tech)
 */
@Service
public class SvnfmService {

    @Autowired
    VnfmRepository vnfmRepository;

    @Autowired
    VnfmCacheRepository vnfRepository;

    @Autowired
    VnfOperationRepository vnfOperationRepository;

    @Autowired
    private VnfmHelper vnfmHelper;

    @Autowired
    ApplicationConfig applicationConfig;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    Vnfds vnfds;

    @Autowired
    SubscriptionService subscriptionService;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private static final Logger LOGGER = LoggerFactory.getLogger(SvnfmService.class);

    /**
     *
     * @param createVNFRequest
     * @return inlineResponse201
     */
    public InlineResponse201 createVnf(final CreateVnfRequest createVNFRequest, final String id) {
        InlineResponse201 inlineResponse201 = null;
        try {
            final VnfInstance vnfInstance = vnfmHelper.createVnfInstance(createVNFRequest, id);
            vnfmRepository.save(vnfInstance);
            final Thread creationNotification = new Thread(new VnfmAdapterCreationNotification());
            creationNotification.start();
            inlineResponse201 = vnfmHelper.getInlineResponse201(vnfInstance);
            LOGGER.debug("Response from Create VNF {}", inlineResponse201);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("Failed in Create Vnf", e);
        }
        return inlineResponse201;
    }

    /**
     *
     * @param vnfId
     * @param instantiateVNFRequest
     * @param operationId
     * @throws InterruptedException
     */
    public String instantiateVnf(final String vnfId, final InstantiateVnfRequest instantiateVNFRequest) {
        final VnfOperation vnfOperation = buildVnfOperation(InlineResponse200.OperationEnum.INSTANTIATE, vnfId);
        vnfOperationRepository.save(vnfOperation);
        executor.submit(new OperationProgressor(vnfOperation, vnfRepository, vnfOperationRepository, applicationConfig,
                vnfds, subscriptionService));
        return vnfOperation.getId();
    }

    /**
     * vnfOperationRepository
     *
     * @param vnfId
     * @param instantiateOperationId
     */
    public VnfOperation buildVnfOperation(final InlineResponse200.OperationEnum operation, final String vnfId) {
        final VnfOperation vnfOperation = new VnfOperation();
        vnfOperation.setId(UUID.randomUUID().toString());
        vnfOperation.setOperation(operation);
        vnfOperation.setOperationState(InlineResponse200.OperationStateEnum.STARTING);
        vnfOperation.setVnfInstanceId(vnfId);
        return vnfOperation;
    }

    /**
     *
     * @param operationId
     * @throws InterruptedException
     */
    public InlineResponse200 getOperationStatus(final String operationId) {
        LOGGER.info("Getting operation status with id: {}", operationId);
        final Thread instantiationNotification = new Thread(new VnfInstantiationNotification());
        instantiationNotification.start();
        for (final VnfOperation operation : vnfOperationRepository.findAll()) {
            LOGGER.info("Operation found: {}", operation);
            if (operation.getId().equals(operationId)) {
                final ModelMapper modelMapper = new ModelMapper();
                return modelMapper.map(operation, InlineResponse200.class);
            }
        }
        return null;
    }

    /**
     *
     * @param vnfId
     * @return inlineResponse201
     */
    public InlineResponse201 getVnf(final String vnfId) {
        final Cache ca = cacheManager.getCache(Constant.IN_LINE_RESPONSE_201_CACHE);
        final SimpleValueWrapper wrapper = (SimpleValueWrapper) ca.get(vnfId);
        final InlineResponse201 inlineResponse201 = (InlineResponse201) wrapper.get();
        if (inlineResponse201 != null) {
            LOGGER.info("Cache Read Successful");
            return inlineResponse201;
        }
        return null;
    }

    /**
     * @param vnfId
     * @return
     */
    public Object terminateVnf(final String vnfId) {
        // TODO
        return null;
    }

    public void registerSubscription(final LccnSubscriptionRequest subscription) {
        subscriptionService.registerSubscription(subscription);
    }
}
