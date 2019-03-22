/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Ericsson. All rights reserved.
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

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.onap.svnfm.simulator.notifications.VnfmAdapterCreationNotification;
import org.onap.vnfm.v1.model.InlineResponse201;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class handles the logic of VNF lifecycle
 * 
 * @author Lathishbabu Ganesan (lathishbabu.ganesan@est.tech)
 *
 */
@Service
public class SvnfmService {

	/**
	 * This method read the create VNF response from the json file and return it
	 * to the VNFM Adaptor
	 * 
	 * @return
	 */
	public InlineResponse201 createVNF() {
		Thread creationNodtification = new Thread(new VnfmAdapterCreationNotification());
		creationNodtification.start();
		ObjectMapper mapper = new ObjectMapper();
		InlineResponse201 inlineResponse201 = null;
		try {
			inlineResponse201 = mapper.readValue(
					IOUtils.toString(getClass().getClassLoader().getResource("json/createVNFResponse.json")),
					InlineResponse201.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inlineResponse201;
	}
}
