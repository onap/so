package org.onap.so.openstack.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.HeatTemplateParam;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MsoHeatUtilsUnitTest {

	
	private ObjectMapper mapper = new ObjectMapper();
	@Test
	public void convertInputMapTest() throws JsonParseException, JsonMappingException, IOException {
		MsoHeatUtils utils = new MsoHeatUtils();
		
		Map<String, Object> input = new HashMap<>();
		HeatTemplate template = new HeatTemplate();
		template.setArtifactUuid("my-uuid");
		Set<HeatTemplateParam> parameters = template.getParameters();
		HeatTemplateParam paramNum = new HeatTemplateParam();
		paramNum.setParamType("number");
		paramNum.setParamName("my-number");
		input.put("my-number", "3");
		
		HeatTemplateParam paramString = new HeatTemplateParam();
		paramString.setParamType("string");
		paramString.setParamName("my-string");
		input.put("my-string", "hello");
		
		HeatTemplateParam paramJson = new HeatTemplateParam();
		paramJson.setParamType("json");
		paramJson.setParamName("my-json");
		
		HeatTemplateParam paramJsonEscaped = new HeatTemplateParam();
		paramJsonEscaped.setParamType("json");
		paramJsonEscaped.setParamName("my-json-escaped");
		
		Map<String, Object> jsonMap = mapper.readValue(getJson("free-form.json"), new TypeReference<Map<String, Object>>(){});
		input.put("my-json", jsonMap);
		
		input.put("my-json-escaped", getJson("free-form.json"));
		
		parameters.add(paramNum);
		parameters.add(paramString);
		parameters.add(paramJson);
		parameters.add(paramJsonEscaped);
		
		Map<String, Object> output = utils.convertInputMap(input, template);
		
		assertEquals(3, output.get("my-number"));
		assertEquals("hello", output.get("my-string"));
		assertTrue("expect no change in type", output.get("my-json") instanceof Map);
		assertTrue("expect string to become jsonNode", output.get("my-json-escaped") instanceof JsonNode);

		JSONAssert.assertEquals(getJson("free-form.json"), mapper.writeValueAsString(output.get("my-json-escaped")), false);
	}
	
	
	private String getJson(String filename) throws IOException {
		 return new String(Files.readAllBytes(Paths.get("src/test/resources/__files/MsoHeatUtils/" + filename)));
	}
	
}
