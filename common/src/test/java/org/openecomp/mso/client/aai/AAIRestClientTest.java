package org.openecomp.mso.client.aai;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import javax.ws.rs.core.Response;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aai.domain.yang.GenericVnf;
import org.openecomp.mso.client.aai.exceptions.AAIPatchDepthExceededException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class AAIRestClientTest {

	@Mock
	private AAIProperties props;
	
	private ObjectMapper mapper = new AAICommonObjectMapperProvider().getMapper();
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void convertObjectToPatchFormatTest() throws URISyntaxException, JsonParseException, JsonMappingException, IOException {
		AAIRestClient client = new AAIRestClient(props, new URI(""));
		GenericVnf vnf = new GenericVnf();
		vnf.setIpv4Loopback0Address("");
		String result = client.convertObjectToPatchFormat(vnf);
		GenericVnf resultObj = mapper.readValue(result.toString(), GenericVnf.class);
		assertTrue("expect object to become a String to prevent double marshalling", result instanceof String);
		assertNull("expect null because of custom mapper", resultObj.getIpv4Loopback0Address());
		
	}
	
	@Test
	public void convertStringToPatchFormatTest() throws URISyntaxException, JsonParseException, JsonMappingException, IOException {
		AAIRestClient client = new AAIRestClient(props, new URI(""));
		String payload = "{\"ipv4-loopback0-address\":\"\"}";
		String result = client.convertObjectToPatchFormat(payload);
		
		assertEquals("expect no change", payload, result);
	}
	
	@Test
	public void convertMapToPatchFormatTest() throws URISyntaxException, JsonParseException, JsonMappingException, IOException {
		AAIRestClient client = new AAIRestClient(props, new URI(""));
		HashMap<String, String> map = new HashMap<>();
		map.put("ipv4-loopback0-address", "");
		String result = client.convertObjectToPatchFormat(map);
		
		assertEquals("expect string", "{\"ipv4-loopback0-address\":\"\"}", result);
	}
	
	@Test
	public void failPatchOnComplexObject() throws URISyntaxException {
		AAIRestClient client = new AAIRestClient(props, new URI(""));
		this.thrown.expect(AAIPatchDepthExceededException.class); 
		this.thrown.expectMessage(containsString("Object exceeds allowed depth for update action"));
		client.patch("{ \"hello\" : \"world\", \"nestedSimple\" : [\"value1\" , \"value2\"], \"relationship-list\" : [{\"key\" : \"value\"}], \"nested\" : { \"key\" : \"value\" }}");

	}
	
	@Test
	public void hasComplexObjectTest() throws URISyntaxException {
		AAIRestClient client = new AAIRestClient(props, new URI(""));
		String hasNesting = "{ \"hello\" : \"world\", \"nested\" : { \"key\" : \"value\" } }";
		String noNesting = "{ \"hello\" : \"world\" }";
		String arrayCase = "{ \"hello\" : \"world\", \"nestedSimple\" : [\"value1\" , \"value2\"], \"nestedComplex\" : [{\"key\" : \"value\"}]}";
		String empty = "{}";
		String arrayCaseSimpleOnly = "{ \"hello\" : \"world\", \"nestedSimple\" : [\"value1\" , \"value2\"]}";
		String relationshipListCaseNesting = "{ \"hello\" : \"world\", \"nestedSimple\" : [\"value1\" , \"value2\"], \"relationship-list\" : [{\"key\" : \"value\"}], \"nested\" : { \"key\" : \"value\" }}";
		String relationshipListCase = "{ \"hello\" : \"world\", \"nestedSimple\" : [\"value1\" , \"value2\"], \"relationship-list\" : [{\"key\" : \"value\"}]}";
		String nothing = "";
		
		assertTrue("expect has nesting", client.hasComplexObject(hasNesting));
		assertFalse("expect no nesting", client.hasComplexObject(noNesting));
		assertTrue("expect has nesting", client.hasComplexObject(arrayCase));
		assertFalse("expect no nesting", client.hasComplexObject(empty));
		assertFalse("expect no nesting", client.hasComplexObject(arrayCaseSimpleOnly));
		assertFalse("expect no nesting", client.hasComplexObject(relationshipListCase));
		assertTrue("expect has nesting", client.hasComplexObject(relationshipListCaseNesting));
		assertFalse("expect no nesting", client.hasComplexObject(nothing));
	}
}
