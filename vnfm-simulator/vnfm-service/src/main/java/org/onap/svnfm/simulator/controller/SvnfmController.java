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
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.CreateVnfRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse2001;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InstantiateVnfRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.LccnSubscriptionRequest;
import org.onap.svnfm.simulator.constants.Constant;
import org.onap.svnfm.simulator.repository.VnfmCacheRepository;
import org.onap.svnfm.simulator.services.SvnfmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Lathishbabu Ganesan (lathishbabu.ganesan@est.tech)
 * @author Ronan Kenny (ronan.kenny@est.tech)
 */
@RestController
@RequestMapping(path = Constant.BASE_URL, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
public class SvnfmController {

    @Autowired
    private SvnfmService svnfmService;

    @Autowired
    private VnfmCacheRepository vnfmCacheRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(SvnfmController.class);

    /**
     * To create the Vnf and stores the response in cache
     *
     * @param CreateVnfRequest
     * @return InlineResponse201
     */
    @PostMapping(value = "/vnf_instances")
    public ResponseEntity<InlineResponse201> createVnf(@RequestBody final CreateVnfRequest createVNFRequest) {
        LOGGER.info("Start createVnf {}", createVNFRequest);
        final String id = UUID.randomUUID().toString();
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        final ResponseEntity<InlineResponse201> responseEntity =
                new ResponseEntity<>(vnfmCacheRepository.createVnf(createVNFRequest, id), headers, HttpStatus.CREATED);
        LOGGER.info("Finished create {}", responseEntity);
        return responseEntity;
    }

    /**
     * Get the vnf by id from cache
     *
     * @param vnfId
     * @return InlineResponse201
     */
    @GetMapping(value = "/vnf_instances/{vnfInstanceId}")
    @ResponseStatus(code = HttpStatus.OK)
    public InlineResponse201 getVnf(@PathVariable("vnfInstanceId") final String vnfId) {
        LOGGER.info("Start getVnf------");
        return vnfmCacheRepository.getVnf(vnfId);
    }

    /**
     * To instantiate the vnf and returns the operation id
     *
     * @param vnfId
     * @throws InterruptedException
     */
    @PostMapping(value = "/vnf_instances/{vnfInstanceId}/instantiate")
    public ResponseEntity<Void> instantiateVnf(@PathVariable("vnfInstanceId") final String vnfId,
            @RequestBody final InstantiateVnfRequest instantiateVNFRequest) {
        LOGGER.info("Start instantiateVNFRequest {} ", instantiateVNFRequest);

        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.LOCATION, svnfmService.instantiateVnf(vnfId, instantiateVNFRequest));
        return new ResponseEntity<>(headers, HttpStatus.ACCEPTED);
    }

    /**
     * To delete the vnf by id
     *
     * @param vnfId
     * @return InlineResponse201
     */
    @DeleteMapping(value = "/vnf_instances/{vnfInstanceId}")
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<Void> deleteVnf(@PathVariable("vnfInstanceId") final String vnfId) {
        LOGGER.info("Start deleting Vnf------");
        vnfmCacheRepository.deleteVnf(vnfId);
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    /**
     * To terminate the vnf by id
     *
     * @param vnfId
     * @throws InterruptedException
     */
    @PostMapping(value = "/vnf_instances/{vnfInstanceId}/terminate")
    public ResponseEntity<Object> terminateVnf(@PathVariable("vnfInstanceId") final String vnfId) {
        LOGGER.info("Start terminateVNFRequest {}", vnfId);
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.LOCATION, svnfmService.terminateVnf(vnfId));
        return new ResponseEntity<>(headers, HttpStatus.ACCEPTED);
    }


    /**
     * To get the status of the operation by id
     *
     * @param operationId
     * @return response entity
     * @throws InterruptedException
     */
    @GetMapping(value = "/vnf_lcm_op_occs/{vnfLcmOpOccId}")
    public ResponseEntity<InlineResponse200> getOperationStatus(
            @PathVariable("vnfLcmOpOccId") final String operationId) {
        LOGGER.info("Start getOperationStatus");
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(svnfmService.getOperationStatus(operationId), headers, HttpStatus.OK);
    }

    @PostMapping(value = "/subscriptions")
    public ResponseEntity<InlineResponse2001> subscribeForNotifications(
            @RequestBody final LccnSubscriptionRequest lccnSubscriptionRequest) {
        LOGGER.info("Subscription request received: {}", lccnSubscriptionRequest);
        svnfmService.registerSubscription(lccnSubscriptionRequest);
        final InlineResponse2001 response = new InlineResponse2001();

        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }
}
