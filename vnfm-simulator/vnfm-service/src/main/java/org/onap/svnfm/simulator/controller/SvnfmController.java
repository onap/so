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

package org.onap.svnfm.simulator.controller;

import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.onap.svnfm.simulator.repository.VnfmCacheRepository;
import org.onap.svnfm.simulator.services.SvnfmService;
import org.onap.vnfm.v1.model.CreateVnfRequest;
import org.onap.vnfm.v1.model.InlineResponse201;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author Lathishbabu Ganesan (lathishbabu.ganesan@est.tech)
 * @author Ronan Kenny (ronan.kenny@est.tech)
 */
@RestController
@RequestMapping("/svnfm")
public class SvnfmController {

    @Autowired
    private SvnfmService svnfmService;

    @Autowired
    private VnfmCacheRepository vnfmCacheRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(SvnfmController.class);

    /**
     * 
     * @param createVNFRequest
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/vnf_instances")
    public ResponseEntity<InlineResponse201> createVnf(@RequestBody final CreateVnfRequest createVNFRequest) {
        LOGGER.info("Start createVnf------");
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(vnfmCacheRepository.createVnf(createVNFRequest), headers, HttpStatus.CREATED);
    }

    /**
     * 
     * @param vnfId
     * @return vnfm cache repository
     */
    @RequestMapping(method = RequestMethod.GET, value = "/vnf_instances/{vnfInstanceId}",
            produces = MediaType.APPLICATION_JSON)
    @ResponseStatus(code = HttpStatus.OK)
    public InlineResponse201 getVnf(@PathVariable("vnfInstanceId") final String vnfId) {
        LOGGER.info("Start getVnf------");
        return vnfmCacheRepository.getVnf(vnfId);
    }

    /**
     * 
     * @param vnfId
     * @return response entity
     * @throws InterruptedException
     */
    @RequestMapping(method = RequestMethod.POST, value = "/vnf_instances/{vnfInstanceId}/instantiate")
    public ResponseEntity<Object> instantiateVnf(@PathVariable("vnfInstanceId") final String vnfId)
            throws InterruptedException {
        LOGGER.info("Start instantiateVNFRequest");
        final String instantiateJobId = UUID.randomUUID().toString();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON);
        headers.add("Location", instantiateJobId);
        return new ResponseEntity<>(svnfmService.instatiateVnf(vnfId, instantiateJobId), headers, HttpStatus.ACCEPTED);
    }

    /**
     * 
     * @param jobId
     * @return response entity
     * @throws InterruptedException
     */
    public ResponseEntity<Object> getJobStatus(@PathVariable("jobId") final String jobId) throws InterruptedException {
        LOGGER.info("Start getJobStatus");
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(svnfmService.getJobStatus(jobId), headers, HttpStatus.ACCEPTED);
    }

    /**
     * 
     * @param vnfId
     * @return delete VNF
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/vnf_instances/{vnfInstanceId}",
            produces = MediaType.APPLICATION_JSON)
    @ResponseStatus(code = HttpStatus.OK)
    public InlineResponse201 deleteVnf(@PathVariable("vnfInstanceId") final String vnfId) {
        LOGGER.info("Start deleting Vnf------");
        return vnfmCacheRepository.deleteVnf(vnfId);
    }

    /**
     * 
     * @param vnfId
     * @return response entity
     * @throws InterruptedException
     */
    @RequestMapping(method = RequestMethod.POST, value = "/vnf_instances/{vnfInstanceId}/terminate")
    public ResponseEntity<Object> terminateVnf(@PathVariable("vnfInstanceId") final String vnfId)
            throws InterruptedException {
        LOGGER.info("Start terminateVNFRequest");
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(svnfmService.terminateVnf(vnfId), headers, HttpStatus.ACCEPTED);
    }
}
