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