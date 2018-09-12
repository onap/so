/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.sdnc;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Optional;

import org.junit.Test;

public class SDNCClientLogResponseTest {

	private SDNCClient sdncClient = new SDNCClient();

    @Test
    public void logSDNCResponseTest() {
    	LinkedHashMap<String, String> output = new LinkedHashMap<>();
    	output.put("response-code", "404");
    	output.put("response-message", "not found");
    	Optional<String> response = sdncClient.logSDNCResponse(output);
    	assertEquals(true, response.isPresent());
    	assertEquals("{\"response-code\":\"404\",\"response-message\":\"not found\"}",response.get());
    }
}
