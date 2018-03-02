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
