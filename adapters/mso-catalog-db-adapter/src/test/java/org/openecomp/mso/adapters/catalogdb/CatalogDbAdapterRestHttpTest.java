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

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.openecomp.mso.adapters.catalogdb.CatalogDbAdapterRest;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.AllottedResourceCustomization;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.db.catalog.beans.ServiceMacroHolder;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.apache.http.HttpStatus;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest({CatalogDatabase.class})
@PowerMockIgnore("javax.net.ssl.*")
public class CatalogDbAdapterRestHttpTest {
    @Mock
    private static CatalogDatabase dbMock;
    private static TJWSEmbeddedJaxrsServer server;
    private static final int PORT = 3099;
    private static Registry registry;
    private static ResteasyProviderFactory factory;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        server = new TJWSEmbeddedJaxrsServer();
        server.setPort(PORT);
        server.start();
        registry = server.getDeployment().getRegistry();
        factory = server.getDeployment().getDispatcher().getProviderFactory();
        registry.addPerRequestResource(CatalogDbAdapterRest.class);
        factory.registerProvider(CatalogDbAdapterRest.class);
    }
    
    @Before
    public void before(){
		PowerMockito.mockStatic(CatalogDatabase.class);
		dbMock = PowerMockito.mock(CatalogDatabase.class);
		PowerMockito.when(CatalogDatabase.getInstance()).thenReturn(dbMock);
		
		try {
			PowerMockito.whenNew(CatalogDatabase.class).withAnyArguments().thenReturn(dbMock);

		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    @Test
    public void healthCheck_Test(){
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target("http://localhost:3099/v1/healthcheck");
        Response response = target.request().get();
        String value = response.readEntity(String.class);
        assertTrue(value.contains("Application v1 ready"));
    }
    
    @Test
    public void vnfResourcesUrl_Test()
    {
    	try {
    	    List<VnfResourceCustomization> paramList;    		
        	// set up mock return value
            paramList = new ArrayList<>();
            VnfResourceCustomization d1 = new VnfResourceCustomization();
            d1.setModelCustomizationUuid("16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
            d1.setModelInstanceName("RG_6-26_mog11 0");
            d1.setVersion("v1");
            paramList.add(d1);
	    	PowerMockito.when(dbMock.getAllVnfsByVnfModelCustomizationUuid(Mockito.anyString())).thenReturn (paramList);
            // end
			
	    	// Run
	        ResteasyClient client = new ResteasyClientBuilder().build();
	        ResteasyWebTarget target = client.target("http://localhost:3099/v1/vnfResources/16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
	        Response response = target.request().get();
	        String value = response.readEntity(String.class);
	        
	        // prepare to inspect response
	        JsonReader reader = Json.createReader(new StringReader(value.replaceAll("\r?\n", "")));
	        JsonObject respObj = reader.readObject();
	        reader.close();
	        JsonArray jArray = respObj.getJsonArray("serviceVnfs");
	        assertTrue(jArray.size() == 1);
	        if(jArray.size() == 1){
	        	JsonObject rec = jArray.getJsonObject(0);
	        	String version = rec.getString("version");
	        	String uuid = rec.getString("modelCustomizationUuid");
	        	
	        	assertTrue(version.equals("v1"));
	        	assertTrue(uuid.equals("16ea3e56-a8ce-4ad7-8edd-4d2eae095391"));
	        }
	        // end
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    @Test
    public void serviceVnfsUrl_smiUuid_smVer_Test(){
    	try {
    	    List<VnfResourceCustomization> paramList;    		
        	// set up mock return value
            paramList = new ArrayList<>();
            VnfResourceCustomization d1 = new VnfResourceCustomization();
            d1.setVnfResourceModelUuid("16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
            d1.setModelInstanceName("RG_6-26_mog11 0");
            d1.setVersion("v1");
            d1.setMaxInstances(50);
            paramList.add(d1);
	    	PowerMockito.when(dbMock.getAllVnfsByServiceModelInvariantUuid(Mockito.anyString(),Mockito.anyString())).thenReturn (paramList);
            // end
			
	    	// Run
	        ResteasyClient client = new ResteasyClientBuilder().build();
	        ResteasyWebTarget target = client.target("http://localhost:3099/v1/serviceVnfs?serviceModelInvariantUuid=16ea3e56-a8ce-4ad7-8edd-4d2eae095391&serviceModelVersion=ver1");
	        Response response = target.request().get();
	        String value = response.readEntity(String.class);
	        
	        // prepare to inspect response
	        JsonReader reader = Json.createReader(new StringReader(value.replaceAll("\r?\n", "")));
	        JsonObject respObj = reader.readObject();
	        reader.close();
	        JsonArray jArray = respObj.getJsonArray("serviceVnfs");
	        assertTrue(jArray.size() == 1);
	        if(jArray.size() == 1){
	        	JsonObject rec = jArray.getJsonObject(0);
	        	String version = rec.getString("version");
	        	String uuid = rec.getString("vnfResourceModelUuid");
	        	int maxInstance = rec.getInt("maxInstances");
	        	
	        	assertTrue(version.equals("v1"));
	        	assertTrue(uuid.equals("16ea3e56-a8ce-4ad7-8edd-4d2eae095391"));
	        	assertTrue(maxInstance == 50);
	        }
	        // end
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}    	
    }
    
    @Test
    public void serviceVnfsUrl_vnfUuid_Test(){
    	try {
    	    List<VnfResourceCustomization> paramList;    		
        	// set up mock return value
            paramList = new ArrayList<>();
            VnfResourceCustomization d1 = new VnfResourceCustomization();
            d1.setModelCustomizationUuid("16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
            d1.setModelInstanceName("RG_6-26_mog11 0");
            d1.setVersion("v1");
            paramList.add(d1);
	    	PowerMockito.when(dbMock.getAllVnfsByVnfModelCustomizationUuid(Mockito.anyString())).thenReturn (paramList);
            // end
			
	    	// Run
	        ResteasyClient client = new ResteasyClientBuilder().build();
	        ResteasyWebTarget target = client.target("http://localhost:3099/v1/serviceVnfs?vnfModelCustomizationUuid=16ea3e56-a8ce-4ad7-8edd-4d2eae095391");
	        Response response = target.request().get();
	        String value = response.readEntity(String.class);
	        
	        // System.out.println(value);
	        
	        // prepare to inspect response
	        JsonReader reader = Json.createReader(new StringReader(value.replaceAll("\r?\n", "")));
	        JsonObject respObj = reader.readObject();
	        reader.close();
	        JsonArray jArray = respObj.getJsonArray("serviceVnfs");
	        assertTrue(jArray.size() == 1);
	        if(jArray.size() == 1){
	        	JsonObject rec = jArray.getJsonObject(0);
	        	String version = rec.getString("version");
	        	String uuid = rec.getString("modelCustomizationUuid");
	        	
	        	assertTrue(version.equals("v1"));
	        	assertTrue(uuid.equals("16ea3e56-a8ce-4ad7-8edd-4d2eae095391"));
	        }
	        // end
	        
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}    	
    }    
    
    @Test
    public void networkResourcesUrl_nUuid_Ver_Test(){
    	try {
    	    List<NetworkResourceCustomization> paramList;    		
        	// set up mock return value
            paramList = new ArrayList<>();
            NetworkResourceCustomization d1 = new NetworkResourceCustomization();
            d1.setNetworkResourceModelUuid("0cb9b26a-9820-48a7-86e5-16c510e993d9");
            paramList.add(d1);
	    	PowerMockito.when(dbMock.getAllNetworksByNetworkModelCustomizationUuid(Mockito.anyString())).thenReturn (paramList);
            // end
			
	    	// Run
	        ResteasyClient client = new ResteasyClientBuilder().build();
	        ResteasyWebTarget target = client.target("http://localhost:3099/v1/networkResources/0cb9b26a-9820-48a7-86e5-16c510e993d9");
	        Response response = target.request().get();
	        String value = response.readEntity(String.class);
	        
	        // System.out.println(value);
	        
	        // prepare to inspect response
	        JsonReader reader = Json.createReader(new StringReader(value.replaceAll("\r?\n", "")));
	        JsonObject respObj = reader.readObject();
	        reader.close();
	        JsonArray jArray = respObj.getJsonArray("serviceNetworks");
	        assertTrue(jArray.size() == 1);
	        if(jArray.size() == 1){
	        	JsonObject rec = jArray.getJsonObject(0);
	        	String uuid = rec.getString("networkResourceModelUuid");
	        	
	        	assertTrue(uuid.equals("0cb9b26a-9820-48a7-86e5-16c510e993d9"));
	        }
	        // end
	        
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}      	
    }
    
    @Test
    public void serviceNetworksUrl_nType_Test(){
    	try {
    	    List<NetworkResourceCustomization> paramList;    		
        	// set up mock return value
            paramList = new ArrayList<>();
            NetworkResourceCustomization d1 = new NetworkResourceCustomization();
            d1.setNetworkResourceModelUuid("0cb9b26a-9820-48a7-86e5-16c510e993d9");
            paramList.add(d1);
	    	PowerMockito.when(dbMock.getAllNetworksByNetworkType(Mockito.anyString())).thenReturn (paramList);
            // end
			
	    	// Run
	        ResteasyClient client = new ResteasyClientBuilder().build();
	        ResteasyWebTarget target = client.target("http://localhost:3099/v1/serviceNetworks?networkModelName=0cb9b26a-9820-48a7-86e5-16c510e993d9");
	        Response response = target.request().get();
	        String value = response.readEntity(String.class);
	        
	        // System.out.println(value);
	        
	        // prepare to inspect response
	        JsonReader reader = Json.createReader(new StringReader(value.replaceAll("\r?\n", "")));
	        JsonObject respObj = reader.readObject();
	        reader.close();
	        JsonArray jArray = respObj.getJsonArray("serviceNetworks");
	        assertTrue(jArray.size() == 1);
	        if(jArray.size() == 1){
	        	JsonObject rec = jArray.getJsonObject(0);
	        	String uuid = rec.getString("networkResourceModelUuid");
	        	
	        	assertTrue(uuid.equals("0cb9b26a-9820-48a7-86e5-16c510e993d9"));
	        }
	        // end
	        
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}      	
    }    
    
    @Test
    public void serviceResourcesUrl_smiUuid_Ver_Test(){
    	try {
    	    ArrayList<NetworkResourceCustomization> paramList;    		
        	// set up mock return value
            paramList = new ArrayList<>();
            NetworkResourceCustomization d1 = new NetworkResourceCustomization();
            d1.setNetworkResourceModelUuid("0cb9b26a-9820-48a7-86e5-16c510e993d9");
            paramList.add(d1);
    		ServiceMacroHolder ret = new ServiceMacroHolder();
    		ret.setNetworkResourceCustomization(paramList);
	    	PowerMockito.when(dbMock.getAllResourcesByServiceModelInvariantUuid(Mockito.anyString(),Mockito.anyString())).thenReturn (ret);
            // end
			
	    	// Run
	        ResteasyClient client = new ResteasyClientBuilder().build();
	        ResteasyWebTarget target = client.target("http://localhost:3099/v1/serviceResources?serviceModelInvariantUuid=0cb9b26a-9820-48a7-86e5-16c510e993d9&serviceModelVersion=ver1");
	        Response response = target.request().get();
	        String value = response.readEntity(String.class);
	        
	        // System.out.println(value);
	        
	        // prepare to inspect response
	        JsonReader reader = Json.createReader(new StringReader(value.replaceAll("\r?\n", "")));
	        JsonObject respObj = reader.readObject();
	        reader.close();
	        JsonObject obj = respObj.getJsonObject("serviceResources");
	        JsonArray jArray = obj.getJsonArray("networkResourceCustomization");
	        assertTrue(jArray.size() == 1);
	        
	        if(jArray.size() == 1){
	        	JsonObject rec = jArray.getJsonObject(0);
	        	String uuid = rec.getString("networkResourceModelUuid");
	        	
	        	assertTrue(uuid.equals("0cb9b26a-9820-48a7-86e5-16c510e993d9"));
	        }
	        // end
	        
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}      	
    }    
    
    @Test
    public void allottedResourcesUrl_aUuid_Test()
    {
    	try {
    	    List<AllottedResourceCustomization> paramList;    		
        	// set up mock return value
            paramList = new ArrayList<>();
            AllottedResourceCustomization d1 = new AllottedResourceCustomization();
            d1.setArModelUuid("0cb9b26a-9820-48a7-86e5-16c510e993d9");
            paramList.add(d1);
	    	PowerMockito.when(dbMock.getAllAllottedResourcesByArModelCustomizationUuid(Mockito.anyString())).thenReturn (paramList);
            // end
			
	    	// Run
	        ResteasyClient client = new ResteasyClientBuilder().build();
	        ResteasyWebTarget target = client.target("http://localhost:3099/v1/allottedResources/0cb9b26a-9820-48a7-86e5-16c510e993d9");
	        Response response = target.request().get();
	        String value = response.readEntity(String.class);
	        
	        // System.out.println(value);
	        
	        // prepare to inspect response
	        JsonReader reader = Json.createReader(new StringReader(value.replaceAll("\r?\n", "")));
	        JsonObject respObj = reader.readObject();
	        reader.close();
	        JsonArray jArray = respObj.getJsonArray("serviceAllottedResources");
	        assertTrue(jArray.size() == 1);
	        if(jArray.size() == 1){
	        	JsonObject rec = jArray.getJsonObject(0);
	        	String uuid = rec.getString("arModelUuid");
	        	
	        	assertTrue(uuid.equals("0cb9b26a-9820-48a7-86e5-16c510e993d9"));
	        }
	        // end
	        
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}      	
    	
    }
    
    @Test
    public void serviceAllottedResourcesUrl_smiUuid_Test()
    {
    	try {
    	    List<AllottedResourceCustomization> paramList;    		
        	// set up mock return value
            paramList = new ArrayList<>();
            AllottedResourceCustomization d1 = new AllottedResourceCustomization();
            d1.setArModelUuid("0cb9b26a-9820-48a7-86e5-16c510e993d9");
            paramList.add(d1);
	    	PowerMockito.when(dbMock.getAllAllottedResourcesByServiceModelInvariantUuid(Mockito.anyString(), Mockito.anyString())).thenReturn (paramList);
            // end
			
	    	// Run
	        ResteasyClient client = new ResteasyClientBuilder().build();
	        ResteasyWebTarget target = client.target("http://localhost:3099/v1/serviceAllottedResources?serviceModelInvariantUuid=0cb9b26a-9820-48a7-86e5-16c510e993d9&serviceModelVersion=ver1");
	        Response response = target.request().get();
	        String value = response.readEntity(String.class);
	        
	        // System.out.println(value);
	        
	        // prepare to inspect response
	        JsonReader reader = Json.createReader(new StringReader(value.replaceAll("\r?\n", "")));
	        JsonObject respObj = reader.readObject();
	        reader.close();
	        JsonArray jArray = respObj.getJsonArray("serviceAllottedResources");
	        assertTrue(jArray.size() == 1);
	        if(jArray.size() == 1){
	        	JsonObject rec = jArray.getJsonObject(0);
	        	String uuid = rec.getString("arModelUuid");
	        	
	        	assertTrue(uuid.equals("0cb9b26a-9820-48a7-86e5-16c510e993d9"));
	        }
	        // end
	        
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}      	
    }

    @Test
    public void vfModulesUrl_modelName_Test()
    {
    	try {
        	// set up mock return value
    		VfModule vfm = new VfModule();
    		vfm.setModelName("test Model");
    		vfm.setModelUUID("0cb9b26a-9820-48a7-86e5-16c510e993d9");
    		
    		VfModuleCustomization ret = new VfModuleCustomization();    		
    		ret.setVfModule(vfm);
	    	PowerMockito.when(dbMock.getVfModuleCustomizationByModelName(Mockito.anyString())).thenReturn (ret);
            // end
			
	    	// Run
	        ResteasyClient client = new ResteasyClientBuilder().build();
	        ResteasyWebTarget target = client.target("http://localhost:3099/v1/vfModules?vfModuleModelName=0cb9b26a-9820-48a7-86e5-16c510e993d9");
	        Response response = target.request().get();
	        String value = response.readEntity(String.class);
	        
	        // System.out.println(value);
	        
	        // prepare to inspect response
	        JsonReader reader = Json.createReader(new StringReader(value.replaceAll("\r?\n", "")));
	        JsonObject respObj = reader.readObject();
	        reader.close();
	        JsonObject jObj = respObj.getJsonObject("modelInfo");
	        String uuid = jObj.getString("modelUuid");
	        String name = jObj.getString("modelName");
	        assertTrue(uuid.equals("0cb9b26a-9820-48a7-86e5-16c510e993d9"));
	        assertTrue(name.equals("test Model"));
	        // end
	        
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}      	
    }    
    
    @AfterClass
    public static void afterClass() throws Exception {
    	server.stop();
    }
}
