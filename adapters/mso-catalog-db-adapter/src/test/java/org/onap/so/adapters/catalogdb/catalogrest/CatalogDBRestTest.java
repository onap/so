/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2022 Samsung Electronics Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONException;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.onap.so.adapters.catalogdb.CatalogDbAdapterBaseTest;
import org.onap.so.db.catalog.beans.ProcessingFlags;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


public class CatalogDBRestTest extends CatalogDbAdapterBaseTest {

    private static final String ECOMP_MSO_CATALOG_V2_VF_MODULES = "/ecomp/mso/catalog/v2/vfModules";

    private static final String SERVICE_RECIPE = "/serviceRecipe";

    private static final String ECOMP_MSO_CATALOG_V2_SERVICE_ALLOTTED_RESOURCES =
            "/ecomp/mso/catalog/v2/serviceAllottedResources";

    private static final String ECOMP_MSO_CATALOG_V2_RESOURCE_RECEIPE = "/ecomp/mso/catalog/v2/resourceRecipe";

    private static final String ECOMP_MSO_CATALOG_V2_SERVICE_NETWORKS = "/ecomp/mso/catalog/v2/serviceNetworks";

    private static final String ECOMP_MSO_CATALOG_V2_SERVICE_VNFS = "/ecomp/mso/catalog/v2/serviceVnfs";

    private static final String ECOMP_MSO_CATALOG_V2_SERVICE_RESOURCES = "/ecomp/mso/catalog/v2/serviceResources";

    private static final String ECOMP_MSO_CATALOG_PROCESSING_FLAGS = "/ecomp/mso/catalog/v2/processingFlags";

    TestRestTemplate restTemplate = new TestRestTemplate("test", "test");

    HttpHeaders headers = new HttpHeaders();

    private final String expectedServiceResourceResponsev2 =
            "{\r\n\"serviceResources\": {\r\n\"modelInfo\": {\r\n\"modelName\": \"MSOTADevInfra_vSAMP10a_Service\",\r\n\"modelUuid\": \"5df8b6de-2083-11e7-93ae-92361f002672\",\r\n\"modelInvariantUuid\": \"9647dfc4-2083-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2.0\"\r\n},\r\n\"serviceType\": \"NA\",\r\n\"serviceRole\": \"NA\",\r\n\"environmentContext\": \"Luna\",\r\n\"workloadContext\": \"Oxygen\",\r\n\"serviceVnfs\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10a\",\r\n\"modelUuid\": \"ff2ae348-214a-11e7-93ae-92361f002672\",\r\n\"modelInvariantUuid\": \"2fff5b20-214b-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2.0\",\r\n\"modelCustomizationUuid\": \"68dc9a92-214c-11e7-93ae-92361f002672\",\r\n\"modelInstanceName\": \"vSAMP10a 2\"\r\n},\r\n\"toscaNodeType\": \"VF\",\r\n\"nfFunction\": \"vSAMP\",\r\n\"nfType\": \"vSAMP\",\r\n\"nfRole\": \"vSAMP\",\r\n\"nfNamingCode\": \"vSAMP\",\r\n\"multiStageDesign\": null,\r\n\"vfModules\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::base::module-0\",\r\n\"modelUuid\": \"20c4431c-246d-11e7-93ae-92361f002672\",\r\n\"modelInvariantUuid\": \"78ca26d0-246d-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"cb82ffd8-252a-11e7-93ae-92361f002672\"\r\n},\r\n\"isBase\": true,\r\n\"vfModuleLabel\": \"base\",\r\n\"initialCount\": 1,\r\n\"hasVolumeGroup\": false\r\n},\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::PCM::module-1\",\r\n\"modelUuid\": \"066de97e-253e-11e7-93ae-92361f002672\",\r\n\"modelInvariantUuid\": \"64efd51a-2544-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"b4ea86b4-253f-11e7-93ae-92361f002672\"\r\n},\r\n\"isBase\": false,\r\n\"vfModuleLabel\": \"PCM\",\r\n\"initialCount\": 0,\r\n\"hasVolumeGroup\": false\r\n}\r\n]\r\n}\r\n],\r\n\"serviceNetworks\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"CONTRAIL30_GNDIRECT\",\r\n\"modelUuid\": \"10b36f65-f4e6-4be6-ae49-9596dc1c47fc\",\r\n\"modelInvariantUuid\": \"ce4ff476-9641-4e60-b4d5-b4abbec1271d\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"3bdbb104-476c-483e-9f8b-c095b3d308ac\",\r\n\"modelInstanceName\": \"CONTRAIL30_GNDIRECT 9\"\r\n},\r\n\"toscaNodeType\": \"\",\r\n\"networkType\": \"\",\r\n\"networkTechnology\": \"\",\r\n\"networkRole\": \"\",\r\n\"networkScope\": \"\"\r\n}\r\n],\r\n\"serviceAllottedResources\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"Tunnel_Xconn\",\r\n\"modelUuid\": \"f6b7d4c6-e8a4-46e2-81bc-31cad5072842\",\r\n\"modelInvariantUuid\": \"b7a1b78e-6b6b-4b36-9698-8c9530da14af\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"367a8ba9-057a-4506-b106-fbae818597c6\",\r\n\"modelInstanceName\": \"Sec_Tunnel_Xconn 11\"\r\n},\r\n\"toscaNodeType\": \"\",\r\n\"allottedResourceType\": \"\",\r\n\"allottedResourceRole\": null,\r\n\"providingServiceModelName\": null,\r\n\"providingServiceModelInvariantUuid\": null,\r\n\"providingServiceModelUuid\": null,\r\n\"nfFunction\": null,\r\n\"nfType\": null,\r\n\"nfRole\": null,\r\n\"nfNamingCode\": null\r\n}\r\n]\r\n}\r\n}";

    private final String expectedServiceVnfResponse =
            "{\r\n\"serviceVnfs\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10a\",\r\n\"modelUuid\": \"ff2ae348-214a-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"2fff5b20-214b-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"68dc9a92-214c-11e7-93ae-92361f002671\",\r\n\"modelInstanceName\": \"vSAMP10a 1\"\r\n},\r\n\"toscaNodeType\": \"VF\",\r\n\"nfFunction\": \"vSAMP\",\r\n\"nfType\": \"vSAMP\",\r\n\"nfRole\": \"vSAMP\",\r\n\"nfNamingCode\": \"vSAMP\",\r\n\"multiStageDesign\": null,\r\n\"vfModules\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::base::module-0\",\r\n\"modelUuid\": \"20c4431c-246d-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"78ca26d0-246d-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"cb82ffd8-252a-11e7-93ae-92361f002671\"\r\n},\r\n\"isBase\": true,\r\n\"vfModuleLabel\": \"base\",\r\n\"initialCount\": 1,\r\n\"hasVolumeGroup\": false\r\n},\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::PCM::module-1\",\r\n\"modelUuid\": \"066de97e-253e-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"64efd51a-2544-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"b4ea86b4-253f-11e7-93ae-92361f002671\"\r\n},\r\n\"isBase\": false,\r\n\"vfModuleLabel\": \"PCM\",\r\n\"initialCount\": 0,\r\n\"hasVolumeGroup\": false\r\n}\r\n]\r\n}\r\n]\r\n}";

    private final String expectedServiceVnfResponseV3 =
            "{\r\n\"serviceVnfs\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10a\",\r\n\"modelUuid\": \"ff2ae348-214a-11e7-93ae-92361f002672\",\r\n\"modelInvariantUuid\": \"2fff5b20-214b-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2.0\",\r\n\"modelCustomizationUuid\": \"68dc9a92-214c-11e7-93ae-92361f002672\",\r\n\"modelInstanceName\": \"vSAMP10a 2\"\r\n},\r\n\"toscaNodeType\": \"VF\",\r\n\"nfFunction\": \"vSAMP\",\r\n\"nfType\": \"vSAMP\",\r\n\"nfRole\": \"vSAMP\",\r\n\"nfNamingCode\": \"vSAMP\",\r\n\"multiStageDesign\": null,\r\n\"vfModules\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::base::module-0\",\r\n\"modelUuid\": \"20c4431c-246d-11e7-93ae-92361f002672\",\r\n\"modelInvariantUuid\": \"78ca26d0-246d-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"cb82ffd8-252a-11e7-93ae-92361f002672\"\r\n},\r\n\"isBase\": true,\r\n\"vfModuleLabel\": \"base\",\r\n\"initialCount\": 1,\r\n\"hasVolumeGroup\": false\r\n},\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::PCM::module-1\",\r\n\"modelUuid\": \"066de97e-253e-11e7-93ae-92361f002672\",\r\n\"modelInvariantUuid\": \"64efd51a-2544-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"b4ea86b4-253f-11e7-93ae-92361f002672\"\r\n},\r\n\"isBase\": false,\r\n\"vfModuleLabel\": \"PCM\",\r\n\"initialCount\": 0,\r\n\"hasVolumeGroup\": false\r\n}\r\n]\r\n}\r\n]\r\n}";

    private final String expectedServiceNetworkResourceResponse =
            "{\r\n\"serviceNetworks\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"CONTRAIL30_GNDIRECT\",\r\n\"modelUuid\": \"10b36f65-f4e6-4be6-ae49-9596dc1c47fc\",\r\n\"modelInvariantUuid\": \"ce4ff476-9641-4e60-b4d5-b4abbec1271d\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"3bdbb104-476c-483e-9f8b-c095b3d308ac\",\r\n\"modelInstanceName\": \"CONTRAIL30_GNDIRECT 9\"\r\n},\r\n\"toscaNodeType\": \"\",\r\n\"networkType\": \"\",\r\n\"networkTechnology\": \"\",\r\n\"networkRole\": \"\",\r\n\"networkScope\": \"\"\r\n}\r\n]\r\n}";

    private final String badQueryParamResponse =
            "{\"messageId\":null,\"message\":\"no matching parameters\",\"category\":\"INTERNAL\",\"rolledBack\":false}\"";

    private final String expectedAllottedResponse =
            "{\r\n\"serviceAllottedResources\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"Tunnel_Xconn\",\r\n\"modelUuid\": \"f6b7d4c6-e8a4-46e2-81bc-31cad5072842\",\r\n\"modelInvariantUuid\": \"b7a1b78e-6b6b-4b36-9698-8c9530da14af\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"367a8ba9-057a-4506-b106-fbae818597c6\",\r\n\"modelInstanceName\": \"Sec_Tunnel_Xconn 11\"\r\n},\r\n\"toscaNodeType\": \"\",\r\n\"allottedResourceType\": \"\",\r\n\"allottedResourceRole\": null,\r\n\"providingServiceModelName\": null,\r\n\"providingServiceModelInvariantUuid\": null,\r\n\"providingServiceModelUuid\": null,\r\n\"nfFunction\": null,\r\n\"nfType\": null,\r\n\"nfRole\": null,\r\n\"nfNamingCode\": null\r\n}\r\n]\r\n}";

    private final String expectedFilteredServiceResourceResponse =
            "{\r\n\"serviceResources\": {\r\n\"modelInfo\": {\r\n\"modelName\": \"MSOTADevInfra_vSAMP10a_Service\",\r\n\"modelUuid\": \"5df8b6de-2083-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"9647dfc4-2083-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"1.0\"\r\n},\r\n\"serviceType\": \"NA\",\r\n\"serviceRole\": \"NA\",\r\n\"environmentContext\": \"Luna\",\r\n\"workloadContext\": \"Oxygen\",\r\n\"serviceVnfs\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10a\",\r\n\"modelUuid\": \"ff2ae348-214a-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"2fff5b20-214b-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"68dc9a92-214c-11e7-93ae-92361f002671\",\r\n\"modelInstanceName\": \"vSAMP10a 1\"\r\n},\r\n\"toscaNodeType\": \"VF\",\r\n\"nfFunction\": \"vSAMP\",\r\n\"nfType\": \"vSAMP\",\r\n\"nfRole\": \"vSAMP\",\r\n\"nfNamingCode\": \"vSAMP\",\r\n\"multiStageDesign\": null,\r\n\"vfModules\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::base::module-0\",\r\n\"modelUuid\": \"20c4431c-246d-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"78ca26d0-246d-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"cb82ffd8-252a-11e7-93ae-92361f002671\"\r\n},\r\n\"isBase\": true,\r\n\"vfModuleLabel\": \"base\",\r\n\"initialCount\": 1,\r\n\"hasVolumeGroup\": false\r\n},\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::PCM::module-1\",\r\n\"modelUuid\": \"066de97e-253e-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"64efd51a-2544-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"b4ea86b4-253f-11e7-93ae-92361f002671\"\r\n},\r\n\"isBase\": false,\r\n\"vfModuleLabel\": \"PCM\",\r\n\"initialCount\": 0,\r\n\"hasVolumeGroup\": false\r\n}\r\n]\r\n}\r\n],\r\n\"serviceNetworks\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"CONTRAIL30_GNDIRECT\",\r\n\"modelUuid\": \"10b36f65-f4e6-4be6-ae49-9596dc1c47fc\",\r\n\"modelInvariantUuid\": \"ce4ff476-9641-4e60-b4d5-b4abbec1271d\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"3bdbb104-476c-483e-9f8b-c095b3d308ac\",\r\n\"modelInstanceName\": \"CONTRAIL30_GNDIRECT 9\"\r\n},\r\n\"toscaNodeType\": \"\",\r\n\"networkType\": \"\",\r\n\"networkTechnology\": \"\",\r\n\"networkRole\": \"\",\r\n\"networkScope\": \"\"\r\n}\r\n],\r\n\"serviceAllottedResources\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"Tunnel_Xconn\",\r\n\"modelUuid\": \"f6b7d4c6-e8a4-46e2-81bc-31cad5072842\",\r\n\"modelInvariantUuid\": \"b7a1b78e-6b6b-4b36-9698-8c9530da14af\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"367a8ba9-057a-4506-b106-fbae818597c6\",\r\n\"modelInstanceName\": \"Sec_Tunnel_Xconn 11\"\r\n},\r\n\"toscaNodeType\": \"\",\r\n\"allottedResourceType\": \"\",\r\n\"allottedResourceRole\": null,\r\n\"providingServiceModelName\": null,\r\n\"providingServiceModelInvariantUuid\": null,\r\n\"providingServiceModelUuid\": null,\r\n\"nfFunction\": null,\r\n\"nfType\": null,\r\n\"nfRole\": null,\r\n\"nfNamingCode\": null\r\n}\r\n]\r\n}\r\n}";

    private final String serviceUUID = "5df8b6de-2083-11e7-93ae-92361f002671";

    private final String arResourceUUID = "25e2d69b-3b22-47b8-b4c9-7b14fd4a80df";

    private final String serviceInvariantUUID = "9647dfc4-2083-11e7-93ae-92361f002671";

    /* Health Check Resources Endpoint */

    @Test
    public void testHealthcheck() throws JSONException {

        HttpEntity<String> entity = new HttpEntity<String>(null, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(createURLWithPort("/manage/health"), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
    }

    /* Service Resources Endpoint */

    @Test
    public void testGetServiceModelUUID() throws JSONException, IOException, ParseException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_RESOURCES))
                        .queryParam("serviceModelUuid", serviceUUID);

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(getJson("ExpectedServiceResourceEscaped.json"), response.getBody().toString(),
                JSONCompareMode.LENIENT);
    }

    @Test
    public void testGetFilteredVnfResourceInputServiceModelUUID() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_RESOURCES))
                        .queryParam("serviceModelUuid", serviceUUID).queryParam("filter", "resourceInput");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedFilteredServiceResourceResponse, response.getBody().toString(),
                JSONCompareMode.LENIENT);
    }

    @Test
    public void testGetServiceInvariantUUIDAndVersion() throws JSONException, IOException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);
        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_RESOURCES))
                        .queryParam("serviceModelInvariantUuid", "9647dfc4-2083-11e7-93ae-92361f002671")
                        .queryParam("serviceModelVersion", "1.0").queryParam("filter", "resourceInput");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedFilteredServiceResourceResponse, response.getBody().toString(),
                JSONCompareMode.LENIENT);
    }

    @Test
    public void testGetServiceInvariantUUID() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);
        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_RESOURCES))
                        .queryParam("serviceModelInvariantUuid", "9647dfc4-2083-11e7-93ae-92361f002671");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedServiceResourceResponsev2, response.getBody().toString(), false);
    }

    @Test
    public void testGetServiceInvariantUUIDEmtpyModelVer() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);
        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_RESOURCES))
                        .queryParam("serviceModelInvariantUuid", "9647dfc4-2083-11e7-93ae-92361f002671")
                        .queryParam("serviceModelVersion", "");;

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedServiceResourceResponsev2, response.getBody().toString(), false);
    }

    @Test
    public void testGetServiceModelUUID404() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);
        String expectedResponse = "\"serviceResources\": null";
        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_RESOURCES))
                        .queryParam("serviceModelUuid", "5df8b6de-2083-11e7-93");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedResponse, response.getBody().toString(), false);
    }

    @Test
    public void testGetServiceBadQueryParams() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);
        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_RESOURCES))
                        .queryParam("BadQueryParam", "5df8b6de-2083-11e7-93");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(badQueryParamResponse, response.getBody().toString(), false);
    }

    /* VNF Resources Endpoint */

    @Test
    public void testGetVNFResourcesByCustomizationUUID() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);
        String expectedResponse =
                "{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10a\",\r\n\"modelUuid\": \"ff2ae348-214a-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"2fff5b20-214b-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"68dc9a92-214c-11e7-93ae-92361f002671\",\r\n\"modelInstanceName\": \"vSAMP10a 1\"\r\n},\r\n\"toscaNodeType\": \"VF\",\r\n\"nfFunction\": \"vSAMP\",\r\n\"nfType\": \"vSAMP\",\r\n\"nfRole\": \"vSAMP\",\r\n\"nfNamingCode\": \"vSAMP\",\r\n\"multiStageDesign\": null,\r\n\"vfModules\": [\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::base::module-0\",\r\n\"modelUuid\": \"20c4431c-246d-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"78ca26d0-246d-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"cb82ffd8-252a-11e7-93ae-92361f002671\"\r\n},\r\n\"isBase\": true,\r\n\"vfModuleLabel\": \"base\",\r\n\"initialCount\": 1,\r\n\"hasVolumeGroup\": false\r\n},\r\n{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::PCM::module-1\",\r\n\"modelUuid\": \"066de97e-253e-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"64efd51a-2544-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"b4ea86b4-253f-11e7-93ae-92361f002671\"\r\n},\r\n\"isBase\": false,\r\n\"vfModuleLabel\": \"PCM\",\r\n\"initialCount\": 0,\r\n\"hasVolumeGroup\": false\r\n}\r\n]\r\n}";
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(
                        createURLWithPort("/ecomp/mso/catalog/v2/vnfResources/68dc9a92-214c-11e7-93ae-92361f002671"))
                .queryParam("filter", "resourceInput");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedResponse, response.getBody().toString(), false);
    }



    @Test
    public void testGetVNFResources404() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/ecomp/mso/catalog/v2/vnfResources/68dc-11e7-93ae-92361f002671"));

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode().value());

    }

    @Test
    public void testGetServiceVNFResourcesByCustomizationUUID() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_VNFS))
                        .queryParam("vnfModelCustomizationUuid", "68dc9a92-214c-11e7-93ae-92361f002671")
                        .queryParam("filter", "resourceInput");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedServiceVnfResponse, response.getBody().toString(), false);
    }

    @Test
    public void testGetServiceVNFResourcesByServiceModelUUID() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_VNFS))
                        .queryParam("serviceModelUuid", serviceUUID).queryParam("filter", "resourceInput");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedServiceVnfResponse, response.getBody().toString(), false);
    }

    @Test
    public void testGetServiceVNFResourcesByServiceModelInvariantUUIDAndVersion() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_VNFS))
                        .queryParam("serviceModelInvariantUuid", "9647dfc4-2083-11e7-93ae-92361f002671")
                        .queryParam("serviceModelVersion", "1.0").queryParam("filter", "resourceInput");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedServiceVnfResponse, response.getBody().toString(), false);
    }

    @Test
    public void testGetServiceVNFResourcesByServiceModelInvariantUUIDEmptyVersion() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_VNFS))
                        .queryParam("serviceModelInvariantUuid", "9647dfc4-2083-11e7-93ae-92361f002671")
                        .queryParam("serviceModelVersion", "");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedServiceVnfResponseV3, response.getBody().toString(), false);
    }


    @Test
    public void testGetServiceVNFResourcesByServiceModelInvariantUUID() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_VNFS))
                        .queryParam("serviceModelInvariantUuid", "9647dfc4-2083-11e7-93ae-92361f002671");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedServiceVnfResponseV3, response.getBody().toString(), false);
    }

    @Test
    public void testGetServiceVNFResourcesByServiceModelName() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_VNFS))
                        .queryParam("serviceModelName", "MSOTADevInfra_vSAMP10a_Service");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedServiceVnfResponseV3, response.getBody().toString(), false);
    }

    @Test
    public void testGetServiceVNFResourcesByServiceModelNameEmptyVersion() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_VNFS))
                .queryParam("serviceModelName", "MSOTADevInfra_vSAMP10a_Service").queryParam("serviceModelVersion", "");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedServiceVnfResponseV3, response.getBody().toString(), false);
    }

    @Test
    public void testGetServiceVNFResourcesByServiceModelNameAndVersion() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_VNFS))
                        .queryParam("serviceModelName", "MSOTADevInfra_vSAMP10a_Service")
                        .queryParam("serviceModelVersion", "1.0").queryParam("filter", "resourceInput");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedServiceVnfResponse, response.getBody().toString(), false);
    }

    @Test
    public void testSerfviceVNFResources404() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_VNFS))
                        .queryParam("serviceModelName", "BADNAME").queryParam("serviceModelVersion", "1.0");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode().value());

    }


    @Test
    public void testSerfviceVNFBadParams() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_VNFS))
                        .queryParam("BadParamName", "BADNAME");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(badQueryParamResponse, response.getBody().toString(), false);


    }



    /* Network Resources Endpoint */

    @Test
    public void testGetNetworkResourcesByCustomizationUUID() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);
        String expectedResponse =
                "{\r\n\"modelInfo\": {\r\n\"modelName\": \"CONTRAIL30_GNDIRECT\",\r\n\"modelUuid\": \"10b36f65-f4e6-4be6-ae49-9596dc1c47fc\",\r\n\"modelInvariantUuid\": \"ce4ff476-9641-4e60-b4d5-b4abbec1271d\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"3bdbb104-476c-483e-9f8b-c095b3d308ac\",\r\n\"modelInstanceName\": \"CONTRAIL30_GNDIRECT 9\"\r\n},\r\n\"toscaNodeType\": \"\",\r\n\"networkType\": \"\",\r\n\"networkTechnology\": \"\",\r\n\"networkRole\": \"\",\r\n\"networkScope\": \"\"\r\n}";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                createURLWithPort("/ecomp/mso/catalog/v2/networkResources/3bdbb104-476c-483e-9f8b-c095b3d308ac"));

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedResponse, response.getBody().toString(), false);
    }



    @Test
    public void testGetNetworkResources404() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/ecomp/mso/catalog/v2/networkResources/3bdbb104-4asdf"));

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode().value());

    }

    /* Service Network Resources Endpoints */

    @Test
    public void testGetServiceNetworkResourcesByUnknownQueryParam() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_NETWORKS))
                        .queryParam("serviceModelName", "PROVIDER NETWORK").queryParam("serviceModelVersion", "2.0");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(badQueryParamResponse, response.getBody().toString(), false);
    }

    @Test
    public void testGetServiceNetworkResourcesByServiceModelUUID() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_NETWORKS))
                        .queryParam("serviceModelUuid", serviceUUID);

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedServiceNetworkResourceResponse, response.getBody().toString(), false);
    }

    @Test
    public void testGetServiceNetworkResourcesByServiceModelUUIDNotExist() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_NETWORKS))
                        .queryParam("serviceModelUuid", "doesNotExist");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode().value());

    }

    @Test
    public void testGetServiceNetworkResourcesByNetworkCustomizationUUIDNotExist() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_NETWORKS))
                        .queryParam("networkModelCustomizationUuid", "06b8966e-097c-4d63-afda-e0d");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode().value());

    }

    @Test
    public void testGetServiceNetworkResourcesByServiceModelInvariantUUID() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_NETWORKS))
                        .queryParam("serviceModelInvariantUuid", serviceInvariantUUID);

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedServiceNetworkResourceResponse, response.getBody().toString(), false);
    }

    @Test
    public void testGetServiceNetworkResourcesByServiceModelInvariantUUIDAndVersion() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_NETWORKS))
                .queryParam("serviceModelInvariantUuid", serviceInvariantUUID).queryParam("serviceModelVersion", "2.0");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedServiceNetworkResourceResponse, response.getBody().toString(), false);
    }

    @Test
    public void testGetServiceNetworkResourcesByServiceModelInvariantAndEmptyVersion() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_NETWORKS))
                .queryParam("serviceModelInvariantUuid", serviceInvariantUUID).queryParam("serviceModelVersion", "");
        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedServiceNetworkResourceResponse, response.getBody().toString(), false);
    }


    @Test
    public void testGetServiceNetworkResourcesByNetworkCustomizationUUID() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_NETWORKS))
                        .queryParam("networkModelCustomizationUuid", "3bdbb104-476c-483e-9f8b-c095b3d308ac");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedServiceNetworkResourceResponse, response.getBody().toString(), false);
    }

    @Test
    public void testGetServiceNetworkResourcesByNetworkModelName() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_NETWORKS))
                        .queryParam("networkModelName", "CONTRAIL30_GNDIRECT");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedServiceNetworkResourceResponse, response.getBody().toString(), false);
    }

    /* Allotted endpoints */

    @Test
    public void testGetAllottedResourcesByCustomizationUUID() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);
        String expectedResponse =
                "{\r\n\"modelInfo\": {\r\n\"modelName\": \"Tunnel_Xconn\",\r\n\"modelUuid\": \"f6b7d4c6-e8a4-46e2-81bc-31cad5072842\",\r\n\"modelInvariantUuid\": \"b7a1b78e-6b6b-4b36-9698-8c9530da14af\",\r\n\"modelVersion\": \"1.0\",\r\n\"modelCustomizationUuid\": \"367a8ba9-057a-4506-b106-fbae818597c6\",\r\n\"modelInstanceName\": \"Sec_Tunnel_Xconn 11\"\r\n},\r\n\"toscaNodeType\": \"\",\r\n\"allottedResourceType\": \"\",\r\n\"allottedResourceRole\": null,\r\n\"providingServiceModelName\": null,\r\n\"providingServiceModelInvariantUuid\": null,\r\n\"providingServiceModelUuid\": null,\r\n\"nfFunction\": null,\r\n\"nfType\": null,\r\n\"nfRole\": null,\r\n\"nfNamingCode\": null\r\n}";

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                createURLWithPort("/ecomp/mso/catalog/v2/allottedResources/367a8ba9-057a-4506-b106-fbae818597c6"));


        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedResponse, response.getBody().toString(), false);
    }


    @Test
    public void testGetAllottedResourcesByServiceModelUuuid() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_ALLOTTED_RESOURCES))
                        .queryParam("serviceModelUuid", serviceUUID);

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedAllottedResponse, response.getBody().toString(), false);
    }

    @Test
    public void testResourceReceipe() throws JSONException {
        String expectedResourceRecipe =
                "{\"orchestrationUri\":\"/mso/async/services/CreateSDNCNetworkResource\",\"action\":\"createInstance\",\"description\":\"sotnvpnattachmentvF\",\"id\":\"1\",\"recipeTimeout\":\"180\",\"paramXSD\":\"\"}";

        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_RESOURCE_RECEIPE))
                        .queryParam("resourceModelUuid", arResourceUUID).queryParam("action", "createInstance");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedResourceRecipe, response.getBody().toString(), false);
    }

    @Test
    public void testResourceReceipeNotMatched() throws JSONException {

        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_RESOURCE_RECEIPE))
                        .queryParam("resourceModelUuid", arResourceUUID).queryParam("action", "invalid_action");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void testGetServiceAllottedResourcesByServiceModelInvariantUuid() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_ALLOTTED_RESOURCES))
                        .queryParam("serviceModelInvariantUuid", serviceInvariantUUID);

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedAllottedResponse, response.getBody().toString(), false);
    }

    @Test
    public void testGetServiceAllottedResourcesByServiceModelInvariantUuidModelVersion() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_ALLOTTED_RESOURCES))
                .queryParam("serviceModelInvariantUuid", serviceInvariantUUID).queryParam("serviceModelVersion", "1.0");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedAllottedResponse, response.getBody().toString(), false);
    }

    @Test
    public void testGetServiceAllottedResourcesByServiceModelInvariantUuidModelVersionEmpty() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_ALLOTTED_RESOURCES))
                .queryParam("serviceModelInvariantUuid", serviceInvariantUUID).queryParam("serviceModelVersion", "1.0");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedAllottedResponse, response.getBody().toString(), false);
    }

    @Test
    public void testGetAllottedResourcesByAllottedCustomizationId() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_ALLOTTED_RESOURCES))
                        .queryParam("arModelCustomizationUuid", "367a8ba9-057a-4506-b106-fbae818597c6");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedAllottedResponse, response.getBody().toString(), false);
    }


    @Test
    public void testGetAllottedResourcesResourcesNonExistResource() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_SERVICE_ALLOTTED_RESOURCES))
                        .queryParam("arModelCustomizationUuid", "NOTEXIST");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode().value());

    }

    /* VF Modules Endpoint */

    @Test
    public void testGetVFModulesNonExistResource() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_VF_MODULES))
                        .queryParam("vfModuleModelName", "NEUTRON_BASIC");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode().value());

    }

    @Test
    public void testGetVFModulesByVfModuleModelName() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);
        String expectedResponse =
                "{\r\n\"modelInfo\": {\r\n\"modelName\": \"vSAMP10aDEV::base::module-0\",\r\n\"modelUuid\": \"20c4431c-246d-11e7-93ae-92361f002671\",\r\n\"modelInvariantUuid\": \"78ca26d0-246d-11e7-93ae-92361f002671\",\r\n\"modelVersion\": \"2\",\r\n\"modelCustomizationUuid\": \"cb82ffd8-252a-11e7-93ae-92361f002671\"\r\n},\r\n\"isBase\": true,\r\n\"vfModuleLabel\": \"base\",\r\n\"initialCount\": 1,\r\n\"hasVolumeGroup\": false\r\n}";
        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_VF_MODULES))
                        .queryParam("vfModuleModelName", "vSAMP10aDEV::base::module-0");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedResponse, response.getBody().toString(), false);

    }

    @Test
    public void testGetVFModulesBadQueryParam() throws JSONException, IOException {
        TestAppender.events.clear();
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_V2_VF_MODULES)).queryParam("ADASD", "NEUTRON_BASIC");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
        JSONAssert.assertEquals(badQueryParamResponse, response.getBody(), false);
    }

    @Test
    public void testCreateServiceRecipe() throws JSONException {
        ServiceRecipe recipe = new ServiceRecipe();
        recipe.setAction("action");
        recipe.setDescription("description");
        recipe.setOrchestrationUri("http://test");
        recipe.setRecipeTimeout(120);
        recipe.setServiceModelUUID(serviceUUID);
        HttpEntity<ServiceRecipe> entity = new HttpEntity<ServiceRecipe>(recipe, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(SERVICE_RECIPE));

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, String.class);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void testGetProcessingFlagsByFlag() throws Exception {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_PROCESSING_FLAGS)).pathSegment("TESTFLAG");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        ObjectMapper mapper = new ObjectMapper();
        ProcessingFlags processingFlagsResponse = mapper.readValue(response.getBody(), ProcessingFlags.class);

        assertEquals("TESTFLAG", processingFlagsResponse.getFlag());
        assertEquals("NO", processingFlagsResponse.getValue());
        assertEquals("TESTENDPOINT", processingFlagsResponse.getEndpoint());
        assertEquals("TEST FLAG", processingFlagsResponse.getDescription());
    }

    @Test
    public void testGetAllProcessingFlags() throws Exception {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_PROCESSING_FLAGS));

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        ObjectMapper mapper = new ObjectMapper();

        List<ProcessingFlags> processingFlagsResponse =
                mapper.readValue(response.getBody(), new TypeReference<List<ProcessingFlags>>() {});

        boolean testFlagFound = false;
        for (int i = 0; i < processingFlagsResponse.size(); i++) {
            if (processingFlagsResponse.get(i).getFlag().equals("TESTFLAG")) {
                assertEquals("TESTENDPOINT", processingFlagsResponse.get(i).getEndpoint());
                assertEquals("TEST FLAG", processingFlagsResponse.get(i).getDescription());
                testFlagFound = true;
            }
        }
        assertTrue(testFlagFound);
    }

    @Test
    public void testSetProcessingFlagsFlagValue() throws JSONException {
        ProcessingFlags updatedProcessingFlag = new ProcessingFlags();
        updatedProcessingFlag.setFlag("TESTFLAG");
        updatedProcessingFlag.setValue("YES");
        HttpEntity<ProcessingFlags> entity = new HttpEntity<ProcessingFlags>(updatedProcessingFlag, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort(ECOMP_MSO_CATALOG_PROCESSING_FLAGS)).pathSegment("TESTFLAG");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.PUT, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    private String getJson(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/" + filename)));
    }
}
