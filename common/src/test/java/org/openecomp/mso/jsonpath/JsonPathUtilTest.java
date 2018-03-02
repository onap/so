package org.openecomp.mso.jsonpath;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;

public class JsonPathUtilTest {

	private static final String json = "{\"test\" : \"hello\", \"test2\" : {\"nested\" : \"value\"}}";
	@Test
	public void pathExistsTest() {		
		assertEquals("test is found", JsonPathUtil.getInstance().pathExists(json, "$.test"), true);
		assertEquals("nothing is not found", JsonPathUtil.getInstance().pathExists(json, "$.nothing"), false);
	}
	
	@Test
	public void locateResultTest() {
		assertEquals("value of hello is found",  Optional.of("hello"), JsonPathUtil.getInstance().locateResult(json, "$.test"));
		assertEquals("nothing returns empty", Optional.empty(), JsonPathUtil.getInstance().locateResult(json, "$.nothing"));
	}
	
	@Test
	public void simpleAndComplexValues() {
		assertEquals("json doc found",  Optional.of("{\"nested\":\"value\"}"), JsonPathUtil.getInstance().locateResult(json, "$.test2"));
		assertEquals("value found",  Optional.of("value"), JsonPathUtil.getInstance().locateResult(json, "$.test2.nested"));
	}
}