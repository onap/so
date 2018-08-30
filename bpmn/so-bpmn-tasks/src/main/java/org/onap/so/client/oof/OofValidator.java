/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Intel Corp. All rights reserved.
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

package org.onap.so.client.oof;


import org.json.JSONObject;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.logger.MsoLogger;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

import static org.apache.commons.lang.StringUtils.isNotBlank;


@Component
public class OofValidator {

    private static final MsoLogger log = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, OofValidator.class);

    /**
     * Validates the synchronous homing response from oof
     *
     * @throws BadResponseException
     */
    public void validateDemandsResponse(LinkedHashMap<?, ?> response) throws BadResponseException {
        log.debug("Validating oofs synchronous response");
        if(!response.isEmpty()){
            JSONObject jsonResponse = new JSONObject(response);
            if(jsonResponse.has("requestStatus")){
                String status = jsonResponse.getString("requestStatus");
                if(status.equals("accepted")){
                    log.debug("oofs synchronous response indicates accepted");
                }else{
                    String message = jsonResponse.getString("statusMessage");
                    if(isNotBlank(message)){
                        log.debug("oofs response indicates failed: " + message);
                    }else{
                        log.debug("oofs response indicates failed: no status message provided");
                        message = "error message not provided";
                    }
                    throw new BadResponseException("oofs synchronous response indicates failed: " + message);
                }
            }else{
                log.debug("oofs synchronous response does not contain: request status");
                throw new BadResponseException("oofs synchronous response does not contain: request status");
            }
        }else{
            log.debug("oofs synchronous response is empty");
            throw new BadResponseException("oofs synchronous response i is empty");
        }
    }

    /**
     * Validates the asynchronous/callback response from oof which
     * contains the homing and licensing solutions
     *
     * @throws BadResponseException
     */
    public static void validateSolution(String response) throws BadResponseException{
        log.debug("Validating oofs asynchronous callback response");
        if(isNotBlank(response)) {
            JSONObject jsonResponse = new JSONObject(response);
            if(!jsonResponse.has("serviceException")){
                log.debug("oofs asynchronous response is valid");
            }else{
                String message = jsonResponse.getJSONObject("serviceException").getString("text");
                if(isNotBlank(message)){
                    log.debug("oofs response contains a service exception: " + message);
                }else{
                    log.debug("oofs response contains a service exception: no service exception text provided");
                    message = "error message not provided";
                }
                throw new BadResponseException("oofs asynchronous response contains a service exception: " + message);
            }
        }else{
            log.debug("oofs asynchronous response is empty");
            throw new BadResponseException("oofs asynchronous response is empty");
        }
    }

}
