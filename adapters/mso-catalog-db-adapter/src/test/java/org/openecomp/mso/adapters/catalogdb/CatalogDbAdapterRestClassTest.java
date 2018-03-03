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

package org.openecomp.mso.adapters.catalogdb;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import javax.json.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.mso.adapters.catalogdb.CatalogDbAdapterRest;
import org.openecomp.mso.adapters.catalogdb.catalogrest.CatalogQueryException;
import org.openecomp.mso.adapters.catalogdb.catalogrest.QueryServiceVnfs;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.junit.Before;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.instanceOf;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CatalogDbAdapterRest.class, CatalogDatabase.class})
public class CatalogDbAdapterRestClassTest {
    @Mock
    private static CatalogDatabase dbMock;
    
    private static List<VnfResourceCustomization> paramList;
	
    @Before
    public void prepare () {
		/*
		 * 1.  for non routing related methods/classes, use mockito
		 * 2. for routing methods, use TJWSEmbeddedJaxrsServer  
		 */
		
		/*
		 * in the setup portion, create a mock db object
		 * 
		 */
    	// set up mock return value
        paramList = new ArrayList<>();
        VnfResourceCustomization d1 = new VnfResourceCustomization();
        d1.setModelCustomizationUuid("16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
        d1.setModelInstanceName("ciVFOnboarded-FNAT-aab06c41 1");
        paramList.add(d1);
        // end 	
        
		PowerMockito.mockStatic(CatalogDatabase.class);
		dbMock = PowerMockito.mock(CatalogDatabase.class);
		PowerMockito.when(CatalogDatabase.getInstance()).thenReturn(dbMock);
		try {
			
			PowerMockito.whenNew(CatalogDatabase.class).withAnyArguments().thenReturn(dbMock);
			PowerMockito.spy (dbMock);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    @Test
    public void respond_Test(){
    	QueryServiceVnfs qryResp = new QueryServiceVnfs(paramList);
		CatalogDbAdapterRest adapter = new CatalogDbAdapterRest();
		CatalogDbAdapterRest spyAdapter = Mockito.spy(adapter);
		
		Response resp = spyAdapter.respond("v1", HttpStatus.SC_OK, false, qryResp);
    	Mockito.verify(spyAdapter,Mockito.times(1)).respond("v1", HttpStatus.SC_OK, false, qryResp);
		
        String s = resp.getEntity().toString();
        // System.out.println(s);

        // prepare to inspect response
        JsonReader reader = Json.createReader(new StringReader(s.replaceAll("\r?\n", "")));
        JsonObject respObj = reader.readObject();
        reader.close();
        // end
        JsonArray jArray = respObj.getJsonArray("serviceVnfs");
        
		assertEquals(resp.getStatus(), HttpStatus.SC_OK);
		assertEquals(jArray.size(), 1);
		assertEquals(jArray.getJsonObject(0).getString("modelCustomizationUuid"), "16ea3e56-a8ce-4ad7-8edd-4d2eae095391");		
    }
    
	    
	@Test
    public void serviceVnfsImpl_vnfUuid_ver_Test()
    {
        PowerMockito.when(dbMock.getAllVnfsByVnfModelCustomizationUuid(Mockito.anyString())).thenReturn (paramList);
		
		try {
			CatalogDbAdapterRest adapter = new CatalogDbAdapterRest();
			
	        // Run
	        Response resp = adapter.serviceVnfsImpl("v1", true, "16ea3e56-a8ce-4ad7-8edd-4d2eae095391", null, null, null, null);
	        String s = resp.getEntity().toString();

	        // prepare to inspect response
	        JsonReader reader = Json.createReader(new StringReader(s.replaceAll("\r?\n", "")));
	        JsonObject respObj = reader.readObject();
	        reader.close();
	        // end
	        JsonArray jArray = respObj.getJsonArray("serviceVnfs");
	        
			assertEquals(resp.getStatus(), HttpStatus.SC_OK);
			assertEquals(jArray.size(), 1);
			assertEquals(jArray.getJsonObject(0).getString("modelCustomizationUuid"), "16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
			//
			Mockito.verify(dbMock, Mockito.times(1)).getAllVnfsByVnfModelCustomizationUuid("16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	@Test
	public void serviceVnfsImpl_smiUuid_NoVer_Test()
	{
        PowerMockito.when(dbMock.getAllVnfsByServiceModelInvariantUuid(Mockito.anyString())).thenReturn (paramList);
		
		try {
			CatalogDbAdapterRest adapter = new CatalogDbAdapterRest();
			
	        // Run
	        Response resp = adapter.serviceVnfsImpl("v1", true, null, null, "16ea3e56-a8ce-4ad7-8edd-4d2eae095391", null, null);
	        String s = resp.getEntity().toString();

	        // prepare to inspect response
	        JsonReader reader = Json.createReader(new StringReader(s.replaceAll("\r?\n", "")));
	        JsonObject respObj = reader.readObject();
	        reader.close();
	        // end
	        JsonArray jArray = respObj.getJsonArray("serviceVnfs");
	        
			assertEquals(resp.getStatus(), HttpStatus.SC_OK);
			assertEquals(jArray.size(), 1);
			assertEquals(jArray.getJsonObject(0).getString("modelCustomizationUuid"), "16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
			Mockito.verify(dbMock, Mockito.times(1)).getAllVnfsByServiceModelInvariantUuid("16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}	
	
	@Test
	public void serviceVnfsImpl_smUuid_ver_Test()
	{
        PowerMockito.when(dbMock.getAllVnfsByServiceModelUuid(Mockito.anyString())).thenReturn (paramList);
		
		try {
			CatalogDbAdapterRest adapter = new CatalogDbAdapterRest();
			
	        // Run
	        Response resp = adapter.serviceVnfsImpl("v1", true, null,"16ea3e56-a8ce-4ad7-8edd-4d2eae095391", null, null, null);
	        String s = resp.getEntity().toString();

	        // prepare to inspect response
	        JsonReader reader = Json.createReader(new StringReader(s.replaceAll("\r?\n", "")));
	        JsonObject respObj = reader.readObject();
	        reader.close();
	        // end
	        JsonArray jArray = respObj.getJsonArray("serviceVnfs");
	        
			assertEquals(resp.getStatus(), HttpStatus.SC_OK);
			assertEquals(jArray.size(), 1);
			assertEquals(jArray.getJsonObject(0).getString("modelCustomizationUuid"), "16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
			Mockito.verify(dbMock, Mockito.times(1)).getAllVnfsByServiceModelUuid("16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	@Test
	public void serviceVnfsImpl_smiUuid_ver_Test()
	{
        PowerMockito.when(dbMock.getAllVnfsByServiceModelInvariantUuid(Mockito.anyString(),Mockito.anyString())).thenReturn (paramList);
		
		try {
			CatalogDbAdapterRest adapter = new CatalogDbAdapterRest();
			
	        // Run
	        Response resp = adapter.serviceVnfsImpl("v1", true, null, null, "16ea3e56-a8ce-4ad7-8edd-4d2eae095391", "v1", null);
	        String s = resp.getEntity().toString();

	        // prepare to inspect response
	        JsonReader reader = Json.createReader(new StringReader(s.replaceAll("\r?\n", "")));
	        JsonObject respObj = reader.readObject();
	        reader.close();
	        JsonArray jArray = respObj.getJsonArray("serviceVnfs");
	        // end
	        
			assertEquals(resp.getStatus(), HttpStatus.SC_OK);
			assertEquals(jArray.size(), 1);
			assertEquals(jArray.getJsonObject(0).getString("modelCustomizationUuid"), "16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
			Mockito.verify(dbMock, Mockito.times(1)).getAllVnfsByServiceModelInvariantUuid("16ea3e56-a8ce-4ad7-8edd-4d2eae095391","v1");
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}	
	
	@Test
	public void serviceVnfsImpl_smName_ver_Test()
	{
        PowerMockito.when(dbMock.getAllVnfsByServiceName(Mockito.anyString(),Mockito.anyString())).thenReturn (paramList);
		
		try {
			CatalogDbAdapterRest adapter = new CatalogDbAdapterRest();
			
	        // Run
	        Response resp = adapter.serviceVnfsImpl("v1", true, null, null, null, "v1", "16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
	        String s = resp.getEntity().toString();

	        // prepare to inspect response
	        JsonReader reader = Json.createReader(new StringReader(s.replaceAll("\r?\n", "")));
	        JsonObject respObj = reader.readObject();
	        reader.close();
	        // end
	        JsonArray jArray = respObj.getJsonArray("serviceVnfs");
	        
			assertEquals(resp.getStatus(), HttpStatus.SC_OK);
			assertEquals(jArray.size(), 1);
			assertEquals(jArray.getJsonObject(0).getString("modelCustomizationUuid"), "16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
			Mockito.verify(dbMock, Mockito.times(1)).getAllVnfsByServiceName("16ea3e56-a8ce-4ad7-8edd-4d2eae095391","v1");
			
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
	
	@Test
	public void serviceVnfsImpl_smName_NoVer_Test()
	{
        PowerMockito.when(dbMock.getAllVnfsByServiceName(Mockito.anyString())).thenReturn (paramList);
		
		try {
			CatalogDbAdapterRest adapter = new CatalogDbAdapterRest();
			
	        // Run
	        Response resp = adapter.serviceVnfsImpl("v1", true, null, null, null, null, "16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
	        String s = resp.getEntity().toString();

	        // prepare to inspect response
	        JsonReader reader = Json.createReader(new StringReader(s.replaceAll("\r?\n", "")));
	        JsonObject respObj = reader.readObject();
	        reader.close();
	        // end
	        JsonArray jArray = respObj.getJsonArray("serviceVnfs");
	        
			assertEquals(resp.getStatus(), HttpStatus.SC_OK);
			assertEquals(jArray.size(), 1);
			assertEquals(jArray.getJsonObject(0).getString("modelCustomizationUuid"), "16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
			Mockito.verify(dbMock, Mockito.times(1)).getAllVnfsByServiceName("16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
			
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}	
    
	@Test
	public void serviceVnfsImpl_Exception_Test()
	{
        PowerMockito.when(dbMock.getAllVnfsByServiceName(Mockito.anyString())).thenReturn (null);
		
		try {
			CatalogDbAdapterRest adapter = new CatalogDbAdapterRest();
			
	        // Run
	        Response resp = adapter.serviceVnfsImpl("v1", true, null, null, null, null, null);
	        
			assertEquals(resp.getStatus(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
			assertThat(resp.getEntity(), instanceOf(CatalogQueryException.class));
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}	
        
    @Test
    public void serviceNetworksImpl_nUuid_ver_Test(){
		CatalogDbAdapterRest adapter = new CatalogDbAdapterRest();
		CatalogDbAdapterRest spyAdapter = Mockito.spy(adapter);
		
		Response resp = Response
				.status(HttpStatus.SC_OK)
				.entity("{\"serviceVnfs\":[{\"version\":\"v1\",\"modelCustomizationUuid\":\"16ea3e56-a8ce-4ad7-8edd-4d2eae095391\",\"modelInstanceName\":\"ciVFOnboarded-FNAT-aab06c41 1\",\"created\":null,\"vnfResourceModelUuid\":null,\"vnfResourceModelUUID\":null,\"minInstances\":null,\"maxInstances\":null,\"availabilityZoneMaxCount\":null,\"vnfResource\":null,\"nfFunction\":null,\"nfType\":null,\"nfRole\":null,\"nfNamingCode\":null,\"multiStageDesign\":null,\"vfModuleCustomizations\":null,\"serviceResourceCustomizations\":null,\"creationTimestamp\":null}]}")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.build();
		
		Mockito.doReturn(resp).when(spyAdapter).serviceNetworksImpl(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		
		// Run
		
		Response ret = spyAdapter.serviceNetworksImpl("v1", false, "16ea3e56-a8ce-4ad7-8edd-4d2eae095391",null, null, null, null);
    	Mockito.verify(spyAdapter).serviceNetworksImpl("v1", false, "16ea3e56-a8ce-4ad7-8edd-4d2eae095391", null, null, null, null);
		
    	assertTrue(ret.getStatus() == HttpStatus.SC_OK);
        String s = resp.getEntity().toString();

        // prepare to inspect response
        JsonReader reader = Json.createReader(new StringReader(s.replaceAll("\r?\n", "")));
        JsonObject respObj = reader.readObject();
        reader.close();
        // end
        JsonArray jArray = respObj.getJsonArray("serviceVnfs");
        
		assertEquals(resp.getStatus(), HttpStatus.SC_OK);
		assertEquals(jArray.size(), 1);
		assertEquals(jArray.getJsonObject(0).getString("modelCustomizationUuid"), "16ea3e56-a8ce-4ad7-8edd-4d2eae095391");    	
    	
    }
    
    @Test
    public void serviceAllottedResourcesImpl_Test()
    {
		CatalogDbAdapterRest adapter = new CatalogDbAdapterRest();
		CatalogDbAdapterRest spyAdapter = Mockito.spy(adapter);
		
		Response resp = Response
				.status(HttpStatus.SC_OK)
				.entity("{\"serviceVnfs\":[{\"version\":\"v1\",\"modelCustomizationUuid\":\"16ea3e56-a8ce-4ad7-8edd-4d2eae095391\",\"modelInstanceName\":\"ciVFOnboarded-FNAT-aab06c41 1\",\"created\":null,\"vnfResourceModelUuid\":null,\"vnfResourceModelUUID\":null,\"minInstances\":null,\"maxInstances\":null,\"availabilityZoneMaxCount\":null,\"vnfResource\":null,\"nfFunction\":null,\"nfType\":null,\"nfRole\":null,\"nfNamingCode\":null,\"multiStageDesign\":null,\"vfModuleCustomizations\":null,\"serviceResourceCustomizations\":null,\"creationTimestamp\":null}]}")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.build();
		
		Mockito.doReturn(resp).when(spyAdapter).serviceAllottedResourcesImpl(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		
		// Run
		
		Response ret = spyAdapter.serviceAllottedResourcesImpl("v1", false, "16ea3e56-a8ce-4ad7-8edd-4d2eae095391",null, null, null);
    	Mockito.verify(spyAdapter).serviceAllottedResourcesImpl("v1", false, "16ea3e56-a8ce-4ad7-8edd-4d2eae095391", null, null, null);
		
    	assertTrue(ret.getStatus() == HttpStatus.SC_OK);
        String s = resp.getEntity().toString();

        // prepare to inspect response
        JsonReader reader = Json.createReader(new StringReader(s.replaceAll("\r?\n", "")));
        JsonObject respObj = reader.readObject();
        reader.close();
        // end
        JsonArray jArray = respObj.getJsonArray("serviceVnfs");
        
		assertEquals(resp.getStatus(), HttpStatus.SC_OK);
		assertEquals(jArray.size(), 1);
		assertEquals(jArray.getJsonObject(0).getString("modelCustomizationUuid"), "16ea3e56-a8ce-4ad7-8edd-4d2eae095391");    	
    	    	
    }
    
}
