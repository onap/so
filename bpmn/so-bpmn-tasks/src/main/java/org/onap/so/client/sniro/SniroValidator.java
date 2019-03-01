/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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
 * ============LICENSE_END=========================================================
 */

package org.onap.so.client.sniro;


import static org.apache.commons.lang.StringUtils.*;

import java.util.LinkedHashMap;

import org.json.JSONObject;
import org.onap.so.client.exception.BadResponseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;



@Component
public class SniroValidator {

	private static final Logger logger = LoggerFactory.getLogger(SniroValidator.class);

	/**
	 * Validates the synchronous homing response from sniro manager
	 *
	 * @throws BadResponseException
	 */
	public void validateDemandsResponse(LinkedHashMap<String, Object> response) throws BadResponseException {
		logger.debug("Validating Sniro Managers synchronous response");
		if(!response.isEmpty()){
			JSONObject jsonResponse = new JSONObject(response);
			if(jsonResponse.has("requestStatus")){
				String status = jsonResponse.getString("requestStatus");
				if(status.equals("accepted")){
					logger.debug("Sniro Managers synchronous response indicates accepted");
				}else{
					String message = jsonResponse.getString("statusMessage");
					if(isNotBlank(message)){
						logger.debug("Sniro Managers response indicates failed: " + message);
					}else{
						logger.debug("Sniro Managers response indicates failed: no status message provided");
						message = "error message not provided";
					}
					throw new BadResponseException("Sniro Managers synchronous response indicates failed: " + message);
				}
			}else{
				logger.debug("Sniro Managers synchronous response does not contain: request status");
				throw new BadResponseException("Sniro Managers synchronous response does not contain: request status");
			}
		}else{
			logger.debug("Sniro Managers synchronous response is empty");
			throw new BadResponseException("Sniro Managers synchronous response i is empty");
		}
	}

	/**
	 * Validates the asynchronous/callback response from sniro manager which
	 * contains the homing and licensing solutions
	 *
	 * @throws BadResponseException
	 */
	public static void validateSolution(String response) throws BadResponseException{
		logger.debug("Validating Sniro Managers asynchronous callback response");
		if(isNotBlank(response)) {
			JSONObject jsonResponse = new JSONObject(response);
			if(!jsonResponse.has("serviceException")){
				logger.debug("Sniro Managers asynchronous response is valid");
			}else{
				String message = jsonResponse.getJSONObject("serviceException").getString("text");
				if(isNotBlank(message)){
					logger.debug("Sniro Managers response contains a service exception: " + message);
				}else{
					logger.debug("Sniro Managers response contains a service exception: no service exception text provided");
					message = "error message not provided";
				}
				throw new BadResponseException("Sniro Managers asynchronous response contains a service exception: " + message);
			}
		}else{
			logger.debug("Sniro Managers asynchronous response is empty");
			throw new BadResponseException("Sniro Managers asynchronous response is empty");
		}
	}


	/**
	 * Validates the release response from sniro conductor
	 *
	 * @throws BadResponseException
	 */
	public void validateReleaseResponse(LinkedHashMap<String, Object> response) throws BadResponseException {
		logger.debug("Validating Sniro Conductors response");
		if(!response.isEmpty()){
			String status = (String) response.get("status");
			if(isNotBlank(status)){
				if(status.equals("success")){
					logger.debug("Sniro Conductors synchronous response indicates success");
				}else{
					String message = (String) response.get("message");
					if(isNotBlank(message)){
						logger.debug("Sniro Conductors response indicates failed: " + message);
					}else{
						logger.debug("Sniro Conductors response indicates failed: error message not provided");
						message = "error message not provided";
					}
					throw new BadResponseException("Sniro Conductors synchronous response indicates failed: " + message);
				}
			}else{
				logger.debug("Sniro Managers Conductors response does not contain: status");
				throw new BadResponseException("Sniro Conductors synchronous response does not contain: status");
			}
		}else{
			logger.debug("Sniro Conductors response is empty");
			throw new BadResponseException("Sniro Conductors response is empty");
		}

	}

}
