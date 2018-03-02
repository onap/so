package org.openecomp.mso.client.aai;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.mso.client.aai.entities.AAIError;

public class AAIExceptionMapperTest {

	@Mock private AAIError errorObj;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	@Test
	public void nestedReplace() {
		String error = "Error %1 on %2";
		List<String> list = Arrays.asList("PUT", "hello %1");
		AAIErrorFormatter formatter = new AAIErrorFormatter(errorObj);
		String result = formatter.fillInTemplate(error, list);
		assertEquals("equal", "Error PUT on hello PUT", result);
		
	}
	
	@Test
	public void noReplace() {
		String error = "Error";
		List<String> list = new ArrayList<>();
		AAIErrorFormatter formatter = new AAIErrorFormatter(errorObj);
		String result = formatter.fillInTemplate(error, list);
		assertEquals("equal", "Error", result);
		
	}
	
}
