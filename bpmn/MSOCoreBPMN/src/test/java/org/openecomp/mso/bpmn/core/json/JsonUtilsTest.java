/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.bpmn.core.json;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mock;

public class JsonUtilsTest {

	@Mock public DelegateExecution execution;
	private final String fileLocation = "src/test/resources/json-examples/";

	@Test
	public void jsonStringToMapTest() throws IOException {
		
		JsonUtils utils = new JsonUtils();
		String response = this.getJson("SDNCServiceResponseExample.json");
		String entry = utils.getJsonValue(response, "SDNCServiceResponse.params");
		Map<String, String> map = utils.jsonStringToMap(execution, entry);
		assertEquals(map.get("e2e-vpn-key"), "my-key");
	}
	
	@Test
	public void entryArrayToMapTest() throws IOException {
		JsonUtils utils = new JsonUtils();
		String response = this.getJson("SNIROExample.json");
		String entry = utils.getJsonValue(response, "solutionInfo.placementInfo");
		JSONArray arr = new JSONArray(entry);
		JSONObject homingDataJson = arr.getJSONObject(0);
		JSONArray assignmentInfo = homingDataJson.getJSONArray("assignmentInfo");
		Map<String, String> map = utils.entryArrayToMap(execution, assignmentInfo.toString(), "variableName", "variableValue");
		assertEquals(map.get("cloudOwner"), "att-aic");
	}
	private String getJson(String filename) throws IOException {
		return new String(Files.readAllBytes(Paths.get(fileLocation + filename)));
	}
}
