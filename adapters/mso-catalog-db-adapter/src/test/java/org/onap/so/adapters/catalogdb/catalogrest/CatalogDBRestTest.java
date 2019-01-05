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

package org.onap.so.adapters.catalogdb.catalogrest;

import static org.junit.Assert.*;


import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import org.json.JSONException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.adapters.catalogdb.CatalogDBApplication;

import org.onap.so.logger.MsoLogger;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import ch.qos.logback.classic.spi.ILoggingEvent;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = CatalogDBApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CatalogDBRestTest {

	private static final String ECOMP_MSO_CATALOG_V2_VF_MODULES = "ecomp/mso/catalog/v2/vfModules";

	private static final String ECOMP_MSO_CATALOG_V2_SERVICE_ALLOTTED_RESOURCES = "ecomp/mso/catalog/v2/serviceAllottedResources";

	private static final String ECOMP_MSO_CATALOG_V2_SERVICE_NETWORKS = "ecomp/mso/catalog/v2/serviceNetworks";

	private static final String ECOMP_MSO_CATALOG_V2_SERVICE_VNFS = "ecomp/mso/catalog/v2/serviceVnfs";

	private static final String ECOMP_MSO_CATALOG_V2_SERVICE_RESOURCES = "ecomp/mso/catalog/v2/serviceResources";

	@LocalServerPort
	private int port;

	TestRestTemplate restTemplate = new TestRestTemplate("test", "test");

	HttpHeaders headers = new HttpHeaders();
	
	private final String expectedServiceResourceResponse = "{\r\n\"serviceResources\": {\r\n\"modelInfo\": {\r\n\"modelName\": \"MSOTADevInfra_vSAMP10a_Service\",\r\n\"modelUuid\": \"5df8b6de-2083-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"9647dfc4-2083-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"1.0\"\r\n},\r\n\"serviceType\": \"NA\",\r\n\"serviceRole\": \"NA\",\r\n\"environmentContext\": \"Luna\",\r\n\"workloadContext\": \"Oxygen\",\r\n\"serviceVnfs\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10a\",\r\n\"modelUuid\": \"ff2ae348-214a-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"2fff5b20-214b-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"68dc9a92-214c-11e7-93ae-92361f002671\",\r\n\"modelInstanceName\": \"vSAMP10a 1\"\r\n},\r\n\"toscaNodeType\": \"VF\",\r\n\"nfFunction\": \"vSAMP\",\r\n\"nfType\": \"vSAMP\",\r\n\"nfRole\": \"vSAMP\",\r\n\"nfNamingCode\": \"vSAMP\",\r\n\"multiStageDesign\": null,\r\n\"vfModules\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::base::module-0\",\r\n\"modelUuid\": \"20c4431c-246d-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"78ca26d0-246d-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"cb82ffd8-252a-11e7-93ae-92361f002671\"\r\n},\r\n\"isBase\": true,\r\n\"vfModuleLabel\": \"base\",\r\n\"initialCount\": 1,\r\n\"hasVolumeGroup\": false\r\n},\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::PCM::module-1\",\r\n\"modelUuid\": \"066de97e-253e-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"64efd51a-2544-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"b4ea86b4-253f-11e7-93ae-92361f002671\"\r\n},\r\n\"isBase\": false,\r\n\"vfModuleLabel\": \"PCM\",\r\n\"initialCount\": 0,\r\n\"hasVolumeGroup\": false\r\n}\r\n]\r\n}\r\n],\r\n\"serviceNetworks\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"CONTRAIL30_GNDIRECT\",\r\n\"modelUuid\": \"10b36f65-f4e6-4be6-ae49-9596dc1c47fc\",\r\n\"modelInvariantUuid\": \"ce4ff476-9641-4e60-b4d5-b4abbec1271d\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"3bdbb104-476c-483e-9f8b-c095b3d308ac\",\r\n\"modelInstanceName\": \"CONTRAIL30_GNDIRECT 9\"\r\n},\r\n\"toscaNodeType\": \"\",\r\n\"networkType\": \"\",\r\n\"networkTechnology\": \"\",\r\n\"networkRole\": \"\",\r\n\"networkScope\": \"\"\r\n}\r\n],\r\n\"serviceAllottedResources\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"Tunnel_Xconn\",\r\n\"modelUuid\": \"f6b7d4c6-e8a4-46e2-81bc-31cad5072842\",\r\n\"modelInvariantUuid\": \"b7a1b78e-6b6b-4b36-9698-8c9530da14af\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"367a8ba9-057a-4506-b106-fbae818597c6\",\r\n\"modelInstanceName\": \"Sec_Tunnel_Xconn 11\"\r\n},\r\n\"toscaNodeType\": \"\",\r\n\"allottedResourceType\": \"\",\r\n\"allottedResourceRole\": null,\r\n\"providingServiceModelName\": null,\r\n\"providingServiceModelInvariantUuid\": null,\r\n\"providingServiceModelUuid\": null,\r\n\"nfFunction\": null,\r\n\"nfType\": null,\r\n\"nfRole\": null,\r\n\"nfNamingCode\": null\r\n}\r\n]\r\n}\r\n}";

	private final String expectedServiceResourceResponsev2 = "{\r\n\"serviceResources\": {\r\n\"modelInfo\": {\r\n\"modelName\": \"MSOTADevInfra_vSAMP10a_Service\",\r\n\"modelUuid\": \"5df8b6de-2083-11e7-93ae-92361f002672\",\r\n\"modelInvariantUuid\": \"9647dfc4-2083-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2.0\"\r\n},\r\n\"serviceType\": \"NA\",\r\n\"serviceRole\": \"NA\",\r\n\"environmentContext\": \"Luna\",\r\n\"workloadContext\": \"Oxygen\",\r\n\"serviceVnfs\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10a\",\r\n\"modelUuid\": \"ff2ae348-214a-11e7-93ae-92361f002672\",\r\n\"modelInvariantUuid\": \"2fff5b20-214b-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2.0\",\r\n\"modelCustomizationUuid\": \"68dc9a92-214c-11e7-93ae-92361f002672\",\r\n\"modelInstanceName\": \"vSAMP10a 2\"\r\n},\r\n\"toscaNodeType\": \"VF\",\r\n\"nfFunction\": \"vSAMP\",\r\n\"nfType\": \"vSAMP\",\r\n\"nfRole\": \"vSAMP\",\r\n\"nfNamingCode\": \"vSAMP\",\r\n\"multiStageDesign\": null,\r\n\"vfModules\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::base::module-0\",\r\n\"modelUuid\": \"20c4431c-246d-11e7-93ae-92361f002672\",\r\n\"modelInvariantUuid\": \"78ca26d0-246d-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"cb82ffd8-252a-11e7-93ae-92361f002672\"\r\n},\r\n\"isBase\": true,\r\n\"vfModuleLabel\": \"base\",\r\n\"initialCount\": 1,\r\n\"hasVolumeGroup\": false\r\n},\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::PCM::module-1\",\r\n\"modelUuid\": \"066de97e-253e-11e7-93ae-92361f002672\",\r\n\"modelInvariantUuid\": \"64efd51a-2544-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"b4ea86b4-253f-11e7-93ae-92361f002672\"\r\n},\r\n\"isBase\": false,\r\n\"vfModuleLabel\": \"PCM\",\r\n\"initialCount\": 0,\r\n\"hasVolumeGroup\": false\r\n}\r\n]\r\n}\r\n],\r\n\"serviceNetworks\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"CONTRAIL30_GNDIRECT\",\r\n\"modelUuid\": \"10b36f65-f4e6-4be6-ae49-9596dc1c47fc\",\r\n\"modelInvariantUuid\": \"ce4ff476-9641-4e60-b4d5-b4abbec1271d\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"3bdbb104-476c-483e-9f8b-c095b3d308ac\",\r\n\"modelInstanceName\": \"CONTRAIL30_GNDIRECT 9\"\r\n},\r\n\"toscaNodeType\": \"\",\r\n\"networkType\": \"\",\r\n\"networkTechnology\": \"\",\r\n\"networkRole\": \"\",\r\n\"networkScope\": \"\"\r\n}\r\n],\r\n\"serviceAllottedResources\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"Tunnel_Xconn\",\r\n\"modelUuid\": \"f6b7d4c6-e8a4-46e2-81bc-31cad5072842\",\r\n\"modelInvariantUuid\": \"b7a1b78e-6b6b-4b36-9698-8c9530da14af\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"367a8ba9-057a-4506-b106-fbae818597c6\",\r\n\"modelInstanceName\": \"Sec_Tunnel_Xconn 11\"\r\n},\r\n\"toscaNodeType\": \"\",\r\n\"allottedResourceType\": \"\",\r\n\"allottedResourceRole\": null,\r\n\"providingServiceModelName\": null,\r\n\"providingServiceModelInvariantUuid\": null,\r\n\"providingServiceModelUuid\": null,\r\n\"nfFunction\": null,\r\n\"nfType\": null,\r\n\"nfRole\": null,\r\n\"nfNamingCode\": null\r\n}\r\n]\r\n}\r\n}";

	
	private final String expectedServiceVnfResponse = "{\r\n\"serviceVnfs\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10a\",\r\n\"modelUuid\": \"ff2ae348-214a-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"2fff5b20-214b-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"68dc9a92-214c-11e7-93ae-92361f002671\",\r\n\"modelInstanceName\": \"vSAMP10a 1\"\r\n},\r\n\"toscaNodeType\": \"VF\",\r\n\"nfFunction\": \"vSAMP\",\r\n\"nfType\": \"vSAMP\",\r\n\"nfRole\": \"vSAMP\",\r\n\"nfNamingCode\": \"vSAMP\",\r\n\"multiStageDesign\": null,\r\n\"vfModules\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::base::module-0\",\r\n\"modelUuid\": \"20c4431c-246d-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"78ca26d0-246d-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"cb82ffd8-252a-11e7-93ae-92361f002671\"\r\n},\r\n\"isBase\": true,\r\n\"vfModuleLabel\": \"base\",\r\n\"initialCount\": 1,\r\n\"hasVolumeGroup\": false\r\n},\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::PCM::module-1\",\r\n\"modelUuid\": \"066de97e-253e-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"64efd51a-2544-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"b4ea86b4-253f-11e7-93ae-92361f002671\"\r\n},\r\n\"isBase\": false,\r\n\"vfModuleLabel\": \"PCM\",\r\n\"initialCount\": 0,\r\n\"hasVolumeGroup\": false\r\n}\r\n]\r\n}\r\n]\r\n}";

	private final String expectedServiceVnfResponseV3 = "{\r\n\"serviceVnfs\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10a\",\r\n\"modelUuid\": \"ff2ae348-214a-11e7-93ae-92361f002672\",\r\n\"modelInvariantUuid\": \"2fff5b20-214b-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2.0\",\r\n\"modelCustomizationUuid\": \"68dc9a92-214c-11e7-93ae-92361f002672\",\r\n\"modelInstanceName\": \"vSAMP10a 2\"\r\n},\r\n\"toscaNodeType\": \"VF\",\r\n\"nfFunction\": \"vSAMP\",\r\n\"nfType\": \"vSAMP\",\r\n\"nfRole\": \"vSAMP\",\r\n\"nfNamingCode\": \"vSAMP\",\r\n\"multiStageDesign\": null,\r\n\"vfModules\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::base::module-0\",\r\n\"modelUuid\": \"20c4431c-246d-11e7-93ae-92361f002672\",\r\n\"modelInvariantUuid\": \"78ca26d0-246d-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"cb82ffd8-252a-11e7-93ae-92361f002672\"\r\n},\r\n\"isBase\": true,\r\n\"vfModuleLabel\": \"base\",\r\n\"initialCount\": 1,\r\n\"hasVolumeGroup\": false\r\n},\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::PCM::module-1\",\r\n\"modelUuid\": \"066de97e-253e-11e7-93ae-92361f002672\",\r\n\"modelInvariantUuid\": \"64efd51a-2544-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"b4ea86b4-253f-11e7-93ae-92361f002672\"\r\n},\r\n\"isBase\": false,\r\n\"vfModuleLabel\": \"PCM\",\r\n\"initialCount\": 0,\r\n\"hasVolumeGroup\": false\r\n}\r\n]\r\n}\r\n]\r\n}";
	
	private final String expectedServiceNetworkResourceResponse = "{\r\n\"serviceNetworks\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"CONTRAIL30_GNDIRECT\",\r\n\"modelUuid\": \"10b36f65-f4e6-4be6-ae49-9596dc1c47fc\",\r\n\"modelInvariantUuid\": \"ce4ff476-9641-4e60-b4d5-b4abbec1271d\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"3bdbb104-476c-483e-9f8b-c095b3d308ac\",\r\n\"modelInstanceName\": \"CONTRAIL30_GNDIRECT 9\"\r\n},\r\n\"toscaNodeType\": \"\",\r\n\"networkType\": \"\",\r\n\"networkTechnology\": \"\",\r\n\"networkRole\": \"\",\r\n\"networkScope\": \"\"\r\n}\r\n]\r\n}";
	
	private final String badQueryParamResponse  = "{\"messageId\":null,\"message\":\"no matching parameters\",\"category\":\"INTERNAL\",\"rolledBack\":false}\"";
	
	private final String expectedAllottedResponse = "{\r\n\"serviceAllottedResources\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"Tunnel_Xconn\",\r\n\"modelUuid\": \"f6b7d4c6-e8a4-46e2-81bc-31cad5072842\",\r\n\"modelInvariantUuid\": \"b7a1b78e-6b6b-4b36-9698-8c9530da14af\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"367a8ba9-057a-4506-b106-fbae818597c6\",\r\n\"modelInstanceName\": \"Sec_Tunnel_Xconn 11\"\r\n},\r\n\"toscaNodeType\": \"\",\r\n\"allottedResourceType\": \"\",\r\n\"allottedResourceRole\": null,\r\n\"providingServiceModelName\": null,\r\n\"providingServiceModelInvariantUuid\": null,\r\n\"providingServiceModelUuid\": null,\r\n\"nfFunction\": null,\r\n\"nfType\": null,\r\n\"nfRole\": null,\r\n\"nfNamingCode\": null\r\n}\r\n]\r\n}";
	
	private final String serviceUUID = "5df8b6de-2083-11e7-93ae-92361f002671";
	
	private final String serviceInvariantUUID = "9647dfc4-2083-11e7-93ae-92361f002671";
	
	/* Health Check Resources Endpoint */
	
	@Test
	public void testHealthcheck() throws JSONException {

		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		ResponseEntity<String> response = restTemplate.exchange(
				createURLWithPort("/manage/health"),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
        for(ILoggingEvent logEvent : TestAppender.events)
            if(logEvent.getLoggerName().equals("org.onap.so.logging.spring.interceptor.LoggingInterceptor") &&
            		logEvent.getMarker() != null && logEvent.getMarker().getName().equals("ENTRY")
                    ){
                Map<String,String> mdc = logEvent.getMDCPropertyMap();
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.INSTANCE_UUID));
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.REQUEST_ID));
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.INVOCATION_ID));
                assertEquals("",mdc.get(ONAPLogConstants.MDCs.PARTNER_NAME));
                assertEquals("/manage/health",mdc.get(ONAPLogConstants.MDCs.SERVICE_NAME));
                assertEquals("INPROGRESS",mdc.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE));
            }else if(logEvent.getLoggerName().equals("org.onap.so.logging.spring.interceptor.LoggingInterceptor") &&
                    logEvent.getMarker()!= null && logEvent.getMarker().getName().equals("EXIT")){
                Map<String,String> mdc = logEvent.getMDCPropertyMap();
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.REQUEST_ID));
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.INVOCATION_ID));
                assertEquals("200",mdc.get(ONAPLogConstants.MDCs.RESPONSE_CODE));
                assertEquals("",mdc.get(ONAPLogConstants.MDCs.PARTNER_NAME));
                assertEquals("/manage/health",mdc.get(ONAPLogConstants.MDCs.SERVICE_NAME));
                assertEquals("COMPLETED",mdc.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE));
            }
	}
	
	/* Service Resources Endpoint */
	
	@Test
	public void testGetServiceModelUUID() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_RESOURCES))
		        .queryParam("serviceModelUuid", serviceUUID);
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedServiceResourceResponse, response.getBody().toString(), JSONCompareMode.LENIENT);
	}
	
	@Test
	public void testGetServiceInvariantUUIDAndVersion() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_RESOURCES))
		        .queryParam("serviceModelInvariantUuid", "9647dfc4-2083-11e7-93ae-92361f002671").queryParam("serviceModelVersion", "1.0");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedServiceResourceResponse, response.getBody().toString(), false);
	}
	
	@Test
	public void testGetServiceInvariantUUID() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
	UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_RESOURCES))
		        .queryParam("serviceModelInvariantUuid", "9647dfc4-2083-11e7-93ae-92361f002671");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedServiceResourceResponsev2, response.getBody().toString(), false);
	}
	
	@Test
	public void testGetServiceInvariantUUIDEmtpyModelVer() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_RESOURCES))
		        .queryParam("serviceModelInvariantUuid", "9647dfc4-2083-11e7-93ae-92361f002671").queryParam("serviceModelVersion", "");;
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedServiceResourceResponsev2, response.getBody().toString(), false);
	}
	
	@Test
	public void testGetServiceModelUUID404() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		String expectedResponse = "\"serviceResources\": null";
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_RESOURCES))
		        .queryParam("serviceModelUuid", "5df8b6de-2083-11e7-93");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedResponse, response.getBody().toString(), false);
	}
	
	@Test
	public void testGetServiceBadQueryParams() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_RESOURCES))
		        .queryParam("BadQueryParam", "5df8b6de-2083-11e7-93");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(badQueryParamResponse, response.getBody().toString(), false);
	}
	
	/* VNF Resources Endpoint */
	
	@Test
	public void testGetVNFResourcesByCustomizationUUID() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		String expectedResponse = "{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10a\",\r\n\"modelUuid\": \"ff2ae348-214a-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"2fff5b20-214b-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"68dc9a92-214c-11e7-93ae-92361f002671\",\r\n\"modelInstanceName\": \"vSAMP10a 1\"\r\n},\r\n\"toscaNodeType\": \"VF\",\r\n\"nfFunction\": \"vSAMP\",\r\n\"nfType\": \"vSAMP\",\r\n\"nfRole\": \"vSAMP\",\r\n\"nfNamingCode\": \"vSAMP\",\r\n\"multiStageDesign\": null,\r\n\"vfModules\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::base::module-0\",\r\n\"modelUuid\": \"20c4431c-246d-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"78ca26d0-246d-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"cb82ffd8-252a-11e7-93ae-92361f002671\"\r\n},\r\n\"isBase\": true,\r\n\"vfModuleLabel\": \"base\",\r\n\"initialCount\": 1,\r\n\"hasVolumeGroup\": false\r\n},\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::PCM::module-1\",\r\n\"modelUuid\": \"066de97e-253e-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"64efd51a-2544-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"b4ea86b4-253f-11e7-93ae-92361f002671\"\r\n},\r\n\"isBase\": false,\r\n\"vfModuleLabel\": \"PCM\",\r\n\"initialCount\": 0,\r\n\"hasVolumeGroup\": false\r\n}\r\n]\r\n}";
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort("ecomp/mso/catalog/v2/vnfResources/68dc9a92-214c-11e7-93ae-92361f002671"))
		       ;
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedResponse, response.getBody().toString(), false);
	}
	
	
	
	@Test
	public void testGetVNFResources404() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort("ecomp/mso/catalog/v2/vnfResources/68dc-11e7-93ae-92361f002671"))
		       ;
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(),response.getStatusCode().value());
		
	}
	
	@Test
	public void testGetServiceVNFResourcesByCustomizationUUID() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_VNFS))
		       .queryParam("vnfModelCustomizationUuid", "68dc9a92-214c-11e7-93ae-92361f002671");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedServiceVnfResponse, response.getBody().toString(), false);
	}
	
	@Test
	public void testGetServiceVNFResourcesByServiceModelUUID() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_VNFS))
				.queryParam("serviceModelUuid", serviceUUID);
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedServiceVnfResponse, response.getBody().toString(), false);
	}
	
	@Test
	public void testGetServiceVNFResourcesByServiceModelInvariantUUIDAndVersion() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_VNFS))
				.queryParam("serviceModelInvariantUuid", "9647dfc4-2083-11e7-93ae-92361f002671")
				.queryParam("serviceModelVersion", "1.0");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedServiceVnfResponse, response.getBody().toString(), false);
	}
	
	@Test
	public void testGetServiceVNFResourcesByServiceModelInvariantUUIDEmptyVersion() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_VNFS))
				.queryParam("serviceModelInvariantUuid", "9647dfc4-2083-11e7-93ae-92361f002671")
				.queryParam("serviceModelVersion", "");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedServiceVnfResponseV3, response.getBody().toString(), false);
	}
	
	
	@Test
	public void testGetServiceVNFResourcesByServiceModelInvariantUUID() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_VNFS))
				.queryParam("serviceModelInvariantUuid", "9647dfc4-2083-11e7-93ae-92361f002671");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedServiceVnfResponseV3, response.getBody().toString(), false);
	}
	
	@Test
	public void testGetServiceVNFResourcesByServiceModelName() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_VNFS))
				.queryParam("serviceModelName", "MSOTADevInfra_vSAMP10a_Service");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedServiceVnfResponseV3, response.getBody().toString(), false);
	}
	
	@Test
	public void testGetServiceVNFResourcesByServiceModelNameEmptyVersion() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_VNFS))
				.queryParam("serviceModelName", "MSOTADevInfra_vSAMP10a_Service").queryParam("serviceModelVersion", "");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedServiceVnfResponseV3, response.getBody().toString(), false);
	}
	
	@Test
	public void testGetServiceVNFResourcesByServiceModelNameAndVersion() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_VNFS))
				.queryParam("serviceModelName", "MSOTADevInfra_vSAMP10a_Service").queryParam("serviceModelVersion", "1.0");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedServiceVnfResponse, response.getBody().toString(), false);
	}
	
	@Test
	public void testSerfviceVNFResources404() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_VNFS))
				.queryParam("serviceModelName", "BADNAME").queryParam("serviceModelVersion", "1.0");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(),response.getStatusCode().value());
		
	}
	
	
	@Test
	public void testSerfviceVNFBadParams() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_VNFS))
				.queryParam("BadParamName", "BADNAME");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(badQueryParamResponse, response.getBody().toString(), false);
		
		
	}
	
	
	
    /* Network Resources Endpoint */
	
	@Test
	public void testGetNetworkResourcesByCustomizationUUID() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		String expectedResponse = "{\r\n\"modelInfo\": {\r\n\"modelName\": \"CONTRAIL30_GNDIRECT\",\r\n\"modelUuid\": \"10b36f65-f4e6-4be6-ae49-9596dc1c47fc\",\r\n\"modelInvariantUuid\": \"ce4ff476-9641-4e60-b4d5-b4abbec1271d\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"3bdbb104-476c-483e-9f8b-c095b3d308ac\",\r\n\"modelInstanceName\": \"CONTRAIL30_GNDIRECT 9\"\r\n},\r\n\"toscaNodeType\": \"\",\r\n\"networkType\": \"\",\r\n\"networkTechnology\": \"\",\r\n\"networkRole\": \"\",\r\n\"networkScope\": \"\"\r\n}";
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort("ecomp/mso/catalog/v2/networkResources/3bdbb104-476c-483e-9f8b-c095b3d308ac"))
		       ;
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedResponse, response.getBody().toString(), false);
	}
	
	
	
	@Test
	public void testGetNetworkResources404() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort("ecomp/mso/catalog/v2/networkResources/3bdbb104-4asdf"))
		       ;
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(),response.getStatusCode().value());
		
	}
	
	/* Service Network Resources Endpoints */
	
	@Test
	public void testGetServiceNetworkResourcesByUnknownQueryParam() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_NETWORKS))
				.queryParam("serviceModelName", "PROVIDER NETWORK").queryParam("serviceModelVersion", "2.0");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(badQueryParamResponse, response.getBody().toString(), false);
	} 
	
	@Test
	public void testGetServiceNetworkResourcesByServiceModelUUID() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_NETWORKS))
				.queryParam("serviceModelUuid", serviceUUID);
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedServiceNetworkResourceResponse, response.getBody().toString(), false);
	}
	
	@Test
	public void testGetServiceNetworkResourcesByServiceModelUUIDNotExist() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_NETWORKS))
				.queryParam("serviceModelUuid", "doesNotExist");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(),response.getStatusCode().value());
		
	}
	
	@Test
	public void testGetServiceNetworkResourcesByNetworkCustomizationUUIDNotExist() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_NETWORKS))
				.queryParam("networkModelCustomizationUuid", "06b8966e-097c-4d63-afda-e0d");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(),response.getStatusCode().value());
		
	}
	
	@Test
	public void testGetServiceNetworkResourcesByServiceModelInvariantUUID() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_NETWORKS))
				.queryParam("serviceModelInvariantUuid", serviceInvariantUUID);
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedServiceNetworkResourceResponse, response.getBody().toString(), false);
	}
	
	@Test
	public void testGetServiceNetworkResourcesByServiceModelInvariantUUIDAndVersion() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_NETWORKS))
				.queryParam("serviceModelInvariantUuid", serviceInvariantUUID)
		        .queryParam("serviceModelVersion", "2.0");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedServiceNetworkResourceResponse, response.getBody().toString(), false);
	}
	
	@Test
	public void testGetServiceNetworkResourcesByServiceModelInvariantAndEmptyVersion() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_NETWORKS))
				.queryParam("serviceModelInvariantUuid", serviceInvariantUUID)
		        .queryParam("serviceModelVersion", "");
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedServiceNetworkResourceResponse, response.getBody().toString(), false);
	}
	
	
	@Test
	public void testGetServiceNetworkResourcesByNetworkCustomizationUUID() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
	
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_NETWORKS))
				.queryParam("networkModelCustomizationUuid", "3bdbb104-476c-483e-9f8b-c095b3d308ac");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedServiceNetworkResourceResponse, response.getBody().toString(), false);
	} 
	
	@Test
	public void testGetServiceNetworkResourcesByNetworkModelName() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_NETWORKS))
				.queryParam("networkModelName", "CONTRAIL30_GNDIRECT");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedServiceNetworkResourceResponse, response.getBody().toString(), false);
	}
	
	/*  Allotted endpoints */
	
	@Test
	public void testGetAllottedResourcesByCustomizationUUID() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		String expectedResponse = "{\r\n\"modelInfo\": {\r\n\"modelName\": \"Tunnel_Xconn\",\r\n\"modelUuid\": \"f6b7d4c6-e8a4-46e2-81bc-31cad5072842\",\r\n\"modelInvariantUuid\": \"b7a1b78e-6b6b-4b36-9698-8c9530da14af\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"367a8ba9-057a-4506-b106-fbae818597c6\",\r\n\"modelInstanceName\": \"Sec_Tunnel_Xconn 11\"\r\n},\r\n\"toscaNodeType\": \"\",\r\n\"allottedResourceType\": \"\",\r\n\"allottedResourceRole\": null,\r\n\"providingServiceModelName\": null,\r\n\"providingServiceModelInvariantUuid\": null,\r\n\"providingServiceModelUuid\": null,\r\n\"nfFunction\": null,\r\n\"nfType\": null,\r\n\"nfRole\": null,\r\n\"nfNamingCode\": null\r\n}";
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort("ecomp/mso/catalog/v2/allottedResources/367a8ba9-057a-4506-b106-fbae818597c6"));
				
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedResponse, response.getBody().toString(), false);
	}
	
	
	@Test
	public void testGetAllottedResourcesByServiceModelUuuid() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_ALLOTTED_RESOURCES))
				.queryParam("serviceModelUuid",serviceUUID);
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedAllottedResponse, response.getBody().toString(), false);
	}
	
	@Test
	public void testGetServiceAllottedResourcesByServiceModelInvariantUuid() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_ALLOTTED_RESOURCES))
				.queryParam("serviceModelInvariantUuid", serviceInvariantUUID);
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedAllottedResponse, response.getBody().toString(), false);
	}
	
	@Test
	public void testGetServiceAllottedResourcesByServiceModelInvariantUuidModelVersion() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_ALLOTTED_RESOURCES))
				.queryParam("serviceModelInvariantUuid", serviceInvariantUUID)
				.queryParam("serviceModelVersion", "1.0");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedAllottedResponse, response.getBody().toString(), false);
	}
	
	@Test
	public void testGetServiceAllottedResourcesByServiceModelInvariantUuidModelVersionEmpty() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_ALLOTTED_RESOURCES))
				.queryParam("serviceModelInvariantUuid", serviceInvariantUUID)
				.queryParam("serviceModelVersion", "1.0");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedAllottedResponse, response.getBody().toString(), false);
	}
	
	@Test
	public void testGetAllottedResourcesByAllottedCustomizationId() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
	
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_ALLOTTED_RESOURCES))
				.queryParam("arModelCustomizationUuid", "367a8ba9-057a-4506-b106-fbae818597c6");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedAllottedResponse, response.getBody().toString(), false);
	}
	
	
	@Test
	public void testGetAllottedResourcesResourcesNonExistResource() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_ALLOTTED_RESOURCES))
				.queryParam("arModelCustomizationUuid", "NOTEXIST");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(),response.getStatusCode().value());
		
	}
	
	/* VF Modules Endpoint */
	
	@Test
	public void testGetVFModulesNonExistResource() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_VF_MODULES))
				.queryParam("vfModuleModelName", "NEUTRON_BASIC");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(),response.getStatusCode().value());
		
	}
	 
	@Test
	public void testGetVFModulesByVfModuleModelName() throws JSONException {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		String expectedResponse = "{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::base::module-0\",\r\n\"modelUuid\": \"20c4431c-246d-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"78ca26d0-246d-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"cb82ffd8-252a-11e7-93ae-92361f002671\"\r\n},\r\n\"isBase\": true,\r\n\"vfModuleLabel\": \"base\",\r\n\"initialCount\": 1,\r\n\"hasVolumeGroup\": false\r\n}";
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_VF_MODULES))
				.queryParam("vfModuleModelName", "vSAMP10aDEV::base::module-0");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
		JSONAssert.assertEquals(expectedResponse, response.getBody().toString(), false);
		
	}
	
	@Test
	public void testGetVFModulesBadQueryParam() throws JSONException, IOException {
	    TestAppender.events.clear();
	    HttpEntity<String> entity = new HttpEntity<String>(null, headers);
	    headers.set("Accept", MediaType.APPLICATION_JSON);

	    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_VF_MODULES))
	            .queryParam("ADASD", "NEUTRON_BASIC");

	    ResponseEntity<String> response = restTemplate.exchange(
	            builder.toUriString(),
	            HttpMethod.GET, entity, String.class);

	    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),response.getStatusCode().value());
	    JSONAssert.assertEquals(badQueryParamResponse, response.getBody().toString(), false);			


	    for(ILoggingEvent logEvent : TestAppender.events)
	        if(logEvent.getLoggerName().equals("org.onap.so.logging.jaxrs.filter.jersey.JaxRsFilterLogging") &&
	                logEvent.getMarker().getName().equals("ENTRY")
	                ){
	            Map<String,String> mdc = logEvent.getMDCPropertyMap();
	            assertNotNull(mdc.get(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP));
	            assertNotNull(mdc.get(ONAPLogConstants.MDCs.REQUEST_ID));
	            assertNotNull(mdc.get(MsoLogger.INVOCATION_ID));	           
	            assertEquals("UNKNOWN",mdc.get(MsoLogger.PARTNERNAME));
	            assertEquals("v2/vfModules",mdc.get(MsoLogger.SERVICE_NAME));
	            assertEquals("INPROGRESS",mdc.get(MsoLogger.STATUSCODE));
	        }else if(logEvent.getLoggerName().equals("org.onap.so.logging.jaxrs.filter.jersey.JaxRsFilterLogging") &&
                    logEvent.getMarker().getName().equals("EXIT")){
	            Map<String,String> mdc = logEvent.getMDCPropertyMap();
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP));
                assertNotNull(mdc.get(MsoLogger.ENDTIME));
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.REQUEST_ID));
                assertNotNull(mdc.get(MsoLogger.INVOCATION_ID));
                assertEquals("500",mdc.get(MsoLogger.RESPONSECODE));
                assertEquals("UNKNOWN",mdc.get(MsoLogger.PARTNERNAME));
                assertEquals("v2/vfModules",mdc.get(MsoLogger.SERVICE_NAME));
                assertEquals("ERROR",mdc.get(MsoLogger.STATUSCODE));
                assertNotNull(mdc.get(MsoLogger.RESPONSEDESC));
	        }
	}
	
	private String createURLWithPort(String uri) {
		return "http://localhost:" + port + uri;
	}
}
