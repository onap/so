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
import java.util.Optional;
import org.onap.svnfm.simulator.model.VnfInstance;
import org.onap.svnfm.simulator.model.VnfJob;
import org.onap.svnfm.simulator.notifications.VnfInstantiationNotification;
import org.onap.svnfm.simulator.notifications.VnfmAdapterCreationNotification;
import org.onap.svnfm.simulator.repository.VnfJobRepository;
import org.onap.svnfm.simulator.repository.VnfmRepository;
import org.onap.vnfm.v1.model.CreateVnfRequest;
import org.onap.vnfm.v1.model.InlineResponse201;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    VnfJobRepository vnfJobRepository;

    @Autowired
    private VnfmHelper vnfmHelper;

    private static final Logger LOGGER = LoggerFactory.getLogger(SvnfmService.class);

    /**
     * 
     * @param createVNFRequest
     * @return inlineResponse201
     */
    public InlineResponse201 createVnf(final CreateVnfRequest createVNFRequest) {
        InlineResponse201 inlineResponse201 = null;
        try {
            final VnfInstance vnfInstance = vnfmHelper.createVnfInstance(createVNFRequest);
            vnfmRepository.save(vnfInstance);
            final Thread creationNotification = new Thread(new VnfmAdapterCreationNotification());
            creationNotification.start();
            inlineResponse201 = vnfmHelper.getInlineResponse201(vnfInstance);
            LOGGER.debug("Response from Create VNF", inlineResponse201);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("Failed in Create Vnf", e);
        }
        return inlineResponse201;
    }

    /**
     * 
     * @param vnfId
     * @param instantiateJobId
     * @throws InterruptedException
     */
    public Object instatiateVnf(final String vnfId, final String instantiateJobId) throws InterruptedException {
        final VnfJob vnfJob = buildVnfInstantiation(vnfId, instantiateJobId);
        vnfJobRepository.save(vnfJob);
        getJobStatus(vnfJob.getJobId());
        return null;
    }

    /**
     * 
     * @param vnfId
     * @param instantiateJobId
     */
    public VnfJob buildVnfInstantiation(final String vnfId, final String instantiateJobId) {
        final VnfJob vnfJob = new VnfJob();
        final Optional<VnfInstance> vnfInstance = vnfmRepository.findById(vnfId);

        if (vnfInstance.isPresent()) {
            vnfJob.setJobId(instantiateJobId);
            for (final VnfInstance instance : vnfmRepository.findAll()) {
                if (instance.getId().equals(vnfId)) {
                    vnfJob.setVnfInstanceId(instance.getVnfInstanceDescription());
                }
            }
            vnfJob.setVnfId(vnfId);
            vnfJob.setStatus("STARTING");
        }
        return vnfJob;
    }

    /**
     * 
     * @param jobId
     * @throws InterruptedException
     */
    public Object getJobStatus(final String jobId) throws InterruptedException {
        LOGGER.info("Getting job status with id: " + jobId);
        for (int i = 0; i < 5; i++) {
            LOGGER.info("Instantiation status: RUNNING");
            Thread.sleep(5000);
            for (final VnfJob job : vnfJobRepository.findAll()) {
                if (job.getJobId().equals(jobId)) {
                    job.setStatus("RUNNING");
                    vnfJobRepository.save(job);
                }
            }
        }
        final Thread instantiationNotification = new Thread(new VnfInstantiationNotification());
        instantiationNotification.start();
        for (final VnfJob job : vnfJobRepository.findAll()) {
            if (job.getJobId().equals(jobId)) {
                job.setStatus("COMPLETE");
                vnfJobRepository.save(job);
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
        InlineResponse201 inlineResponse201 = null;

        final Optional<VnfInstance> vnfInstance = vnfmRepository.findById(vnfId);
        try {
            if (vnfInstance.isPresent()) {
                inlineResponse201 = vnfmHelper.getInlineResponse201(vnfInstance.get());
                LOGGER.debug("Response from get VNF", inlineResponse201);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("Failed in get Vnf", e);
        }
        return inlineResponse201;
    }

    /**
     * @param vnfId
     * @return
     */
    public Object terminateVnf(String vnfId) {
        // TODO
        return null;
    }
}
