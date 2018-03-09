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
package org.openecomp.mso.adapters.sdnc.sdncrest;

import org.openecomp.mso.adapters.sdncrest.SDNCEvent;
import org.openecomp.mso.adapters.sdncrest.SDNCServiceError;
import org.openecomp.mso.adapters.sdncrest.SDNCServiceRequest;
import org.openecomp.mso.adapters.sdncrest.SDNCServiceResponse;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;



/**
 * JSON object mapping tests.
 */
public class ObjectMappingTest {
	private static final String EOL = "\n";

	private final String SDNC_SERVICE_REQUEST =
		"{" + EOL +
		"  \"SDNCServiceRequest\": {" + EOL +
		"    \"requestInformation\": {" + EOL +
		"      \"requestId\": \"413658f4-7f42-482e-b834-23a5c15657da\"," + EOL +
		"      \"source\": \"CCD\"," + EOL +
		"      \"notificationUrl\": \"https://ccd-host:8080/notifications\"" + EOL +
		"  }," + EOL +
		"  \"serviceInformation\": {" + EOL +
		"    \"serviceType\": \"vHNFaaS\"," + EOL +
		"    \"serviceInstanceId\": \"74e65b2b637441bca078e63e44bb511b\"," + EOL +
		"    \"subscriberName\": \"IST_SG_0902_3003\"," + EOL +
		"    \"subscriberGlobalId\": \"IST15_0902_3003\"" + EOL +
		"  }," + EOL +
		"  \"bpNotificationUrl\": \"http://localhost:8080/mso/SDNCAdapterCallbackService\"," + EOL +
		"((BP-TIMEOUT))" +
		"  \"sdncRequestId\": \"413658f4-7f42-482e-b834-23a5c15657da-1474471336781\"," + EOL +
		"  \"sdncService\": \"vhnf\"," + EOL +
		"  \"sdncOperation\": \"service-topology-cust-assign-operation\"," + EOL +
		"  \"sdncServiceDataType\": \"XML\"," + EOL +
		"  \"sdncServiceData\": \"<vhnf-cust-stage-information><dhv-service-instance-id>c26dfed652164d60a17461734422b085</dhv-service-instance-id><hnportal-primary-vnf-host-name>HOSTNAME</hnportal-primary-vnf-host-name></vhnf-cust-stage-information>\"" + EOL +
		"  }" + EOL +
		"}" + EOL;

	private final String SDNC_SERVICE_RESPONSE =
		"{" + EOL +
		"  \"SDNCServiceResponse\": {" + EOL +
		"    \"sdncRequestId\": \"413658f4-7f42-482e-b834-23a5c15657da-1474471336781\"," + EOL +
		"    \"responseCode\": \"200\"," + EOL +
		"((RESPONSE-MESSAGE))" +
		"    \"ackFinalIndicator\": \"Y\"" + EOL +
		"((RESPONSE-PARAMS))" +
		"  }" + EOL +
		"}" + EOL;

	private final String SDNC_SERVICE_ERROR =
		"{" + EOL +
		"  \"SDNCServiceError\": {" + EOL +
		"    \"sdncRequestId\": \"413658f4-7f42-482e-b834-23a5c15657da-1474471336781\"," + EOL +
		"    \"responseCode\": \"500\"," + EOL +
		"((RESPONSE-MESSAGE))" +
		"    \"ackFinalIndicator\": \"Y\"" + EOL +
		"  }" + EOL +
		"}" + EOL;

	private final String SDNC_EVENT =
		"{" + EOL +
		"  \"SDNCEvent\": {" + EOL +
		"    \"eventType\": \"ACTIVATION\"," + EOL +
		"    \"eventCorrelatorType\": \"HOST-NAME\"," + EOL +
		"    \"eventCorrelator\": \"USOSTCDALTX0101UJZZ31\"" + EOL +
		"((EVENT-PARAMS))" +
		"  }" + EOL +
		"}" + EOL;

	private final String PARAMS =
		"{\"entry\":[{\"key\":\"P1\",\"value\":\"V1\"},{\"key\":\"P2\",\"value\":\"V2\"},{\"key\":\"P3\",\"value\":\"V3\"}]}";

	@Test
	public final void jsonToSDNCServiceRequest() throws Exception {
		logTest();
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationConfig.Feature.WRAP_ROOT_VALUE);
		mapper.enable(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE);

		String json = SDNC_SERVICE_REQUEST;
		json = json.replace("((BP-TIMEOUT))", "\"bpTimeout\": \"" + "PT5M" + "\"," + EOL);

		SDNCServiceRequest object = mapper.readValue(json, SDNCServiceRequest.class);
		assertEquals("413658f4-7f42-482e-b834-23a5c15657da", object.getRequestInformation().getRequestId());
		assertEquals("CCD", object.getRequestInformation().getSource());
		assertEquals("https://ccd-host:8080/notifications", object.getRequestInformation().getNotificationUrl());
		assertEquals("vHNFaaS", object.getServiceInformation().getServiceType());
		assertEquals("74e65b2b637441bca078e63e44bb511b", object.getServiceInformation().getServiceInstanceId());
		assertEquals("IST_SG_0902_3003", object.getServiceInformation().getSubscriberName());
		assertEquals("IST15_0902_3003", object.getServiceInformation().getSubscriberGlobalId());
		assertEquals("http://localhost:8080/mso/SDNCAdapterCallbackService", object.getBPNotificationUrl());
		assertEquals("PT5M", object.getBPTimeout());
		assertEquals("413658f4-7f42-482e-b834-23a5c15657da-1474471336781", object.getSDNCRequestId());
		assertEquals("vhnf", object.getSDNCService());
		assertEquals("service-topology-cust-assign-operation", object.getSDNCOperation());
		assertEquals("XML", object.getSDNCServiceDataType());
		assertTrue(object.getSDNCServiceData().startsWith("<vhnf-cust-stage-information>"));
	}

	@Test
	public final void jsonToSDNCServiceRequestWithoutOptionalFields() throws Exception {
		logTest();
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationConfig.Feature.WRAP_ROOT_VALUE);
		mapper.enable(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE);

		// bpTimeout is optional.
		String json = SDNC_SERVICE_REQUEST;
		json = json.replace("((BP-TIMEOUT))", "");

		SDNCServiceRequest object = mapper.readValue(json, SDNCServiceRequest.class);
		assertEquals("413658f4-7f42-482e-b834-23a5c15657da", object.getRequestInformation().getRequestId());
		assertEquals("CCD", object.getRequestInformation().getSource());
		assertEquals("https://ccd-host:8080/notifications", object.getRequestInformation().getNotificationUrl());
		assertEquals("vHNFaaS", object.getServiceInformation().getServiceType());
		assertEquals("74e65b2b637441bca078e63e44bb511b", object.getServiceInformation().getServiceInstanceId());
		assertEquals("IST_SG_0902_3003", object.getServiceInformation().getSubscriberName());
		assertEquals("IST15_0902_3003", object.getServiceInformation().getSubscriberGlobalId());
		assertEquals("http://localhost:8080/mso/SDNCAdapterCallbackService", object.getBPNotificationUrl());
		assertNull(object.getBPTimeout());
		assertEquals("413658f4-7f42-482e-b834-23a5c15657da-1474471336781", object.getSDNCRequestId());
		assertEquals("vhnf", object.getSDNCService());
		assertEquals("service-topology-cust-assign-operation", object.getSDNCOperation());
		assertEquals("XML", object.getSDNCServiceDataType());
		assertTrue(object.getSDNCServiceData().startsWith("<vhnf-cust-stage-information>"));
	}

	@Test
	public final void jsonFromSDNCServiceRequest() throws Exception {
		logTest();
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationConfig.Feature.WRAP_ROOT_VALUE);
		mapper.enable(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE);

		// Convert source json string to object.
		String json1 = SDNC_SERVICE_REQUEST;
		json1 = json1.replace("((BP-TIMEOUT))", "\"bpTimeout\": \"" + "PT5M" + "\"," + EOL);
		SDNCServiceRequest object1 = mapper.readValue(json1, SDNCServiceRequest.class);

		// Convert resulting object back to json.
		String json2 = object1.toJson();
		System.out.println("Generated JSON for " + object1.getClass().getSimpleName()
			+ ":" + System.lineSeparator() + json2);
		assertTrue(json2.replaceAll("\\s+","").startsWith("{\"SDNCServiceRequest\":{"));

		// Convert generated json string to another object.
		SDNCServiceRequest object2 = mapper.readValue(json2, SDNCServiceRequest.class);

		// Compare the first object to the second object.
		assertTrue(serializedEquals(object1, object2));
	}

	@Test
	public final void jsonFromSDNCServiceRequestWithoutOptionalFields() throws Exception {
		logTest();
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationConfig.Feature.WRAP_ROOT_VALUE);
		mapper.enable(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE);

		// Convert source json string to object.
		String json1 = SDNC_SERVICE_REQUEST;
		json1 = json1.replace("((BP-TIMEOUT))", "");
		SDNCServiceRequest object1 = mapper.readValue(json1, SDNCServiceRequest.class);

		// Convert resulting object back to json.
		String json2 = object1.toJson();
		System.out.println("Generated JSON for " + object1.getClass().getSimpleName()
			+ ":" + System.lineSeparator() + json2);
		assertTrue(json2.replaceAll("\\s+","").startsWith("{\"SDNCServiceRequest\":{"));

		// Convert generated json string to another object.
		SDNCServiceRequest object2 = mapper.readValue(json2, SDNCServiceRequest.class);

		// Compare the first object to the second object.
		assertTrue(serializedEquals(object1, object2));
	}

	@Test
	public final void jsonToSDNCServiceResponse() throws Exception {
		logTest();
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationConfig.Feature.WRAP_ROOT_VALUE);
		mapper.enable(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE);

		String json = SDNC_SERVICE_RESPONSE;
		json = json.replace("((RESPONSE-MESSAGE))", "    \"responseMessage\": \"" + "OK" + "\"," + EOL);
		json = json.replace(EOL + "((RESPONSE-PARAMS))", "," + EOL + "    \"params\": " + PARAMS + EOL);

		SDNCServiceResponse object = mapper.readValue(json, SDNCServiceResponse.class);
		assertEquals("413658f4-7f42-482e-b834-23a5c15657da-1474471336781", object.getSDNCRequestId());
		assertEquals("200", object.getResponseCode());
		assertEquals("OK", object.getResponseMessage());
		assertEquals("Y", object.getAckFinalIndicator());
		assertEquals("V1", object.getParams().get("P1"));
		assertEquals("V2", object.getParams().get("P2"));
		assertEquals("V3", object.getParams().get("P3"));
	}

	@Test
	public final void jsonToSDNCServiceResponseWithoutOptionalFields() throws Exception {
		logTest();
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationConfig.Feature.WRAP_ROOT_VALUE);
		mapper.enable(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE);

		// responseMessage is optional.
		String json = SDNC_SERVICE_RESPONSE;
		json = json.replace("((RESPONSE-MESSAGE))", "");
		json = json.replace("((RESPONSE-PARAMS))", "");

		SDNCServiceResponse object = mapper.readValue(json, SDNCServiceResponse.class);
		assertEquals("413658f4-7f42-482e-b834-23a5c15657da-1474471336781", object.getSDNCRequestId());
		assertEquals("200", object.getResponseCode());
		assertNull(object.getResponseMessage());
		assertEquals("Y", object.getAckFinalIndicator());
		assertNull(object.getParams());
	}

	@Test
	public final void jsonFromSDNCServiceResponse() throws Exception {
		logTest();
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationConfig.Feature.WRAP_ROOT_VALUE);
		mapper.enable(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE);

		// Convert source json string to object.
		String json1 = SDNC_SERVICE_RESPONSE;
		json1 = json1.replace("((RESPONSE-MESSAGE))", "\"responseMessage\": \"" + "OK" + "\"," + EOL);
		json1 = json1.replace(EOL + "((RESPONSE-PARAMS))", "," + EOL + "    \"params\": " + PARAMS + EOL);
		SDNCServiceResponse object1 = mapper.readValue(json1, SDNCServiceResponse.class);

		// Convert resulting object back to json.
		String json2 = object1.toJson();
		System.out.println("Generated JSON for " + object1.getClass().getSimpleName()
			+ ":" + System.lineSeparator() + json2);
		assertTrue(json2.replaceAll("\\s+","").startsWith("{\"SDNCServiceResponse\":{"));

		// Convert generated json string to another object.
		SDNCServiceResponse object2 = mapper.readValue(json2, SDNCServiceResponse.class);

		// Compare the first object to the second object.
		assertTrue(serializedEquals(object1, object2));
	}

	@Test
	public final void jsonFromSDNCServiceResponseWithoutOptionalFields() throws Exception {
		logTest();
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationConfig.Feature.WRAP_ROOT_VALUE);
		mapper.enable(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE);

		// Convert source json string to object.
		String json1 = SDNC_SERVICE_RESPONSE;
		json1 = json1.replace("((RESPONSE-MESSAGE))", "");
		json1 = json1.replace("((RESPONSE-PARAMS))", "");
		SDNCServiceResponse object1 = mapper.readValue(json1, SDNCServiceResponse.class);

		// Convert resulting object back to json.
		String json2 = object1.toJson();
		System.out.println("Generated JSON for " + object1.getClass().getSimpleName()
			+ ":" + System.lineSeparator() + json2);
		assertTrue(json2.replaceAll("\\s+","").startsWith("{\"SDNCServiceResponse\":{"));

		// Convert generated json string to another object.
		SDNCServiceResponse object2 = mapper.readValue(json2, SDNCServiceResponse.class);

		// Compare the first object to the second object.
		assertTrue(serializedEquals(object1, object2));
	}

	@Test
	public final void jsonToSDNCServiceError() throws Exception {
		logTest();
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationConfig.Feature.WRAP_ROOT_VALUE);
		mapper.enable(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE);

		String json = SDNC_SERVICE_ERROR;
		json = json.replace("((RESPONSE-MESSAGE))", "\"responseMessage\": \"" + "SOMETHING BAD" + "\"," + EOL);

		SDNCServiceError object = mapper.readValue(json, SDNCServiceError.class);
		assertEquals("413658f4-7f42-482e-b834-23a5c15657da-1474471336781", object.getSDNCRequestId());
		assertEquals("500", object.getResponseCode());
		assertEquals("SOMETHING BAD", object.getResponseMessage());
		assertEquals("Y", object.getAckFinalIndicator());
	}

	@Test
	public final void jsonToSDNCServiceErrorWithoutOptionalFields() throws Exception {
		logTest();
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationConfig.Feature.WRAP_ROOT_VALUE);
		mapper.enable(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE);

		// responseMessage is optional.
		String json = SDNC_SERVICE_ERROR;
		json = json.replace("((RESPONSE-MESSAGE))", "");

		SDNCServiceError object = mapper.readValue(json, SDNCServiceError.class);
		assertEquals("413658f4-7f42-482e-b834-23a5c15657da-1474471336781", object.getSDNCRequestId());
		assertEquals("500", object.getResponseCode());
		assertNull(object.getResponseMessage());
		assertEquals("Y", object.getAckFinalIndicator());
	}

	@Test
	public final void jsonFromSDNCServiceError() throws Exception {
		logTest();
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationConfig.Feature.WRAP_ROOT_VALUE);
		mapper.enable(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE);

		// Convert source json string to object.
		String json1 = SDNC_SERVICE_ERROR;
		json1 = json1.replace("((RESPONSE-MESSAGE))", "\"responseMessage\": \"" + "OK" + "\"," + EOL);
		SDNCServiceError object1 = mapper.readValue(json1, SDNCServiceError.class);

		// Convert resulting object back to json.
		String json2 = object1.toJson();
		System.out.println("Generated JSON for " + object1.getClass().getSimpleName()
			+ ":" + System.lineSeparator() + json2);
		assertTrue(json2.replaceAll("\\s+","").startsWith("{\"SDNCServiceError\":{"));

		// Convert generated json string to another object.
		SDNCServiceError object2 = mapper.readValue(json2, SDNCServiceError.class);

		// Compare the first object to the second object.
		assertTrue(serializedEquals(object1, object2));
	}

	@Test
	public final void jsonFromSDNCServiceErrorWithoutOptionalFields() throws Exception {
		logTest();
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationConfig.Feature.WRAP_ROOT_VALUE);
		mapper.enable(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE);

		// Convert source json string to object.
		String json1 = SDNC_SERVICE_ERROR;
		json1 = json1.replace("((RESPONSE-MESSAGE))", "");
		SDNCServiceError object1 = mapper.readValue(json1, SDNCServiceError.class);

		// Convert resulting object back to json.
		String json2 = object1.toJson();
		System.out.println("Generated JSON for " + object1.getClass().getSimpleName()
			+ ":" + System.lineSeparator() + json2);
		assertTrue(json2.replaceAll("\\s+","").startsWith("{\"SDNCServiceError\":{"));

		// Convert generated json string to another object.
		SDNCServiceError object2 = mapper.readValue(json2, SDNCServiceError.class);

		// Compare the first object to the second object.
		assertTrue(serializedEquals(object1, object2));
	}

	@Test
	public final void jsonToSDNCEvent() throws Exception {
		logTest();
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationConfig.Feature.WRAP_ROOT_VALUE);
		mapper.enable(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE);

		String json = SDNC_EVENT;
		json = json.replace(EOL + "((EVENT-PARAMS))", "," + EOL + "    \"params\": " + PARAMS + EOL);

		SDNCEvent object = mapper.readValue(json, SDNCEvent.class);
		assertEquals("ACTIVATION", object.getEventType());
		assertEquals("HOST-NAME", object.getEventCorrelatorType());
		assertEquals("USOSTCDALTX0101UJZZ31", object.getEventCorrelator());
		assertEquals("V1", object.getParams().get("P1"));
		assertEquals("V2", object.getParams().get("P2"));
		assertEquals("V3", object.getParams().get("P3"));
	}

	@Test
	public final void jsonToSDNCEventWithoutOptionalFields() throws Exception {
		logTest();
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationConfig.Feature.WRAP_ROOT_VALUE);
		mapper.enable(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE);

		// params are optional.
		String json = SDNC_EVENT;
		json = json.replace("((EVENT-PARAMS))", "");

		SDNCEvent object = mapper.readValue(json, SDNCEvent.class);
		assertEquals("ACTIVATION", object.getEventType());
		assertEquals("HOST-NAME", object.getEventCorrelatorType());
		assertEquals("USOSTCDALTX0101UJZZ31", object.getEventCorrelator());
		assertNull(object.getParams());
	}

	@Test
	public final void jsonFromSDNCEvent() throws Exception {
		logTest();
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationConfig.Feature.WRAP_ROOT_VALUE);
		mapper.enable(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE);

		// Convert source json string to object.
		String json1 = SDNC_EVENT;
		json1 = json1.replace(EOL + "((EVENT-PARAMS))", "," + EOL + "    \"params\": " + PARAMS + EOL);
		SDNCEvent object1 = mapper.readValue(json1, SDNCEvent.class);

		// Convert resulting object back to json.
		String json2 = object1.toJson();
		System.out.println("Generated JSON for " + object1.getClass().getSimpleName()
			+ ":" + System.lineSeparator() + json2);
		assertTrue(json2.replaceAll("\\s+","").startsWith("{\"SDNCEvent\":{"));

		// Convert generated json string to another object.
		SDNCEvent object2 = mapper.readValue(json2, SDNCEvent.class);

		// Compare the first object to the second object.
		assertTrue(serializedEquals(object1, object2));
	}

	/**
	 * Tests equality of two objects by comparing their serialized form.
	 * WARNING: this works pretty well as long as the objects don't contain
	 * collections like maps and sets that are semantically equal, but have
	 * different internal ordering of elements.
	 */
	private boolean serializedEquals(Serializable object1, Serializable object2) throws IOException {
		ByteArrayOutputStream byteStream1 = new ByteArrayOutputStream();
		ObjectOutputStream objectStream1 = new ObjectOutputStream(byteStream1);
		objectStream1.writeObject(object1);
		objectStream1.close();

		ByteArrayOutputStream byteStream2 = new ByteArrayOutputStream();
		ObjectOutputStream objectStream2 = new ObjectOutputStream(byteStream2);
		objectStream2.writeObject(object2);
		objectStream2.close();

		return Arrays.equals(byteStream1.toByteArray(), byteStream2.toByteArray());
	}

	private void logTest() {
		StackTraceElement[] st = Thread.currentThread().getStackTrace();
		String method = st[2].getMethodName();
		System.out.println("RUNNING TEST: " + method);
	}
}
