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

import javax.ws.rs.core.MediaType;

import org.onap.svnfm.simulator.services.SvnfmService;
import org.onap.vnfm.v1.model.CreateVnfRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class contains the VNF life cycle management operations
 * 
 * @author ronan.kenny@est.tech
 *
 */

/**
 * TO DO
 *
 * Implement Exception handling Implement VNFM adaptor call Identify the Create
 * VNF response Test itwith the VNFM Adaptor
 */

@RestController
@RequestMapping("/svnfm")

public class SvnfmController {

	@Autowired
	private SvnfmService svnfmService;

	private static final Logger LOGGER = LoggerFactory.getLogger(SvnfmController.class);

	@RequestMapping(method = RequestMethod.POST, value = "/vnf_instances")
	public ResponseEntity<Object> createVNFInstance(@RequestBody final CreateVnfRequest createVNFRequest) {
		LOGGER.info("Start createVNFInstance");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(svnfmService.createVNF(), headers, HttpStatus.CREATED);
	}
}
