/*
 * ============LICENSE_START======================================================= 
 * ONAP - SO 
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

package org.openecomp.mso.bpmn.mock;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

/**
 * Mock Resource which is used to start, stop the WireMock Server
 * Also up to 50 mock properties can be added at run-time to change the properties used in transformers such as sdnc_delay in SDNCAdapterMockTransformer
 * You can also selectively setup a stub (use reset before setting up), reset all stubs
 */
@Path("/server")
public class MockResource {

    private boolean started = false;
    private final Integer defaultPort = 28090;
    private WireMockServer wireMockServer = null;
    private static Map<String, String> mockProperties = new HashMap<>();

    public static String getMockProperties(String key) {
        return mockProperties.get(key);
    }

    private synchronized void initMockServer(int portNumber) {
        String path = FileUtil.class.getClassLoader().getResource("__files/sdncSimResponse.xml").getFile();
        path = path.substring(0, path.indexOf("__files/"));

        wireMockServer = new WireMockServer(wireMockConfig().port(portNumber).extensions("org.openecomp.mso.bpmn.mock.SDNCAdapterMockTransformer")
                .extensions("org.openecomp.mso.bpmn.mock.SDNCAdapterNetworkTopologyMockTransformer")
                .extensions("org.openecomp.mso.bpmn.mock.VnfAdapterCreateMockTransformer")
                .extensions("org.openecomp.mso.bpmn.mock.VnfAdapterDeleteMockTransformer")
                .extensions("org.openecomp.mso.bpmn.mock.VnfAdapterUpdateMockTransformer")
                .extensions("org.openecomp.mso.bpmn.mock.VnfAdapterRollbackMockTransformer")
                .extensions("org.openecomp.mso.bpmn.mock.VnfAdapterQueryMockTransformer"));
        //.withRootDirectory(path));
        //Mocks were failing - commenting out for now, both mock and transformers seem to work fine
        WireMock.configureFor("localhost", portNumber);
        wireMockServer.start();
//		StubResponse.setupAllMocks();
        started = true;
    }

    public static void main(String[] args) {
        MockResource mockresource = new MockResource();
        mockresource.start(28090);
        mockresource.reset();
//		mockresource.setupStub("MockCreateTenant");
    }

    /**
     * Starts the wiremock server in default port
     *
     * @return
     */
    @GET
    @Path("/start")
    @Produces("application/json")
    public Response start() {
        return startMockServer(defaultPort);
    }

    private Response startMockServer(int port) {
        if (!started) {
            initMockServer(defaultPort);
            System.out.println("Started Mock Server in port " + port);
            return Response.status(200).entity("Started Mock Server in port " + port).build();
        } else {
            return Response.status(200).entity("Mock Server is already running").build();
        }
    }

    /**
     * Starts the wiremock server in a different port
     *
     * @param portNumber
     * @return
     */
    @GET
    @Path("/start/{portNumber}")
    @Produces("application/json")
    public Response start(@PathParam("portNumber") Integer portNumber) {
        if (portNumber == null) portNumber = defaultPort;
        return startMockServer(portNumber.intValue());
    }


    /**
     * Stop the wiremock server
     *
     * @return
     */
    @GET
    @Path("/stop")
    @Produces("application/json")
    public synchronized Response stop() {
        if (wireMockServer.isRunning()) {
            wireMockServer.stop();
            started = false;
            return Response.status(200).entity("Stopped Mock Server in port ").build();
        }
        return Response.status(200).entity("Mock Server is not running").build();
    }


    /**
     * Return list of mock properties
     *
     * @return
     */
    @GET
    @Path("/properties")
    @Produces("application/json")
    public Response getProperties() {
        return Response.status(200).entity(mockProperties).build();
    }

    /**
     * Update a particular mock property at run-time
     *
     * @param name
     * @param value
     * @return
     */
    @POST
    @Path("/properties/{name}/{value}")
    public Response updateProperties(@PathParam("name") String name, @PathParam("value") String value) {
        if (mockProperties.size() > 50) return Response.serverError().build();
        mockProperties.put(name, value);
        return Response.status(200).build();
    }

    /**
     * Reset all stubs
     *
     * @return
     */
    @GET
    @Path("/reset")
    @Produces("application/json")
    public Response reset() {
        WireMock.reset();
        return Response.status(200).entity("Wiremock stubs are reset").build();
    }


    /**
     * Setup a stub selectively
     * Prior to use, make sure that stub method is available in StubResponse class
     *
     * @param methodName
     * @return
     */

    // commenting for now until we figure out a way to use new StubResponse classes to setupStubs
//	@GET
//	@Path("/stub/{methodName}")
//	@Produces("application/json")
//	public Response setupStub(@PathParam("methodName") String methodName) {
//		
//	    @SuppressWarnings("rawtypes")
//		Class params[] = {};
//	    Object paramsObj[] = {};
//
//	    try {
//			Method thisMethod = StubResponse.class.getDeclaredMethod(methodName, params);
//			try {
//				thisMethod.invoke(StubResponse.class, paramsObj);
//			} catch (IllegalAccessException | IllegalArgumentException
//					| InvocationTargetException e) {
//				return Response.status(200).entity("Error invoking " + methodName ).build();
//			}
//		} catch (NoSuchMethodException | SecurityException e) {
//			return Response.status(200).entity("Stub " + methodName + " not found...").build();
//		}		
//		return Response.status(200).entity("Successfully invoked " + methodName).build();
//	}
    public static Map<String, String> getMockProperties() {
        return mockProperties;
    }
}
