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

package org.onap.so.adapters.requestsdb.adapters;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.onap.so.adapters.requestsdb.application.TestAppender;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.adapters.requestsdb.MsoRequestsDbAdapter;
import org.onap.so.adapters.requestsdb.RequestStatusType;
import org.onap.so.adapters.requestsdb.application.MSORequestDBApplication;
import org.onap.so.adapters.requestsdb.exceptions.MsoRequestsDbException;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.beans.OperationStatus;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import org.onap.so.db.request.data.repository.OperationStatusRepository;
import org.onap.so.db.request.data.repository.ResourceOperationStatusRepository;
import org.onap.so.requestsdb.RequestsDbConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ch.qos.logback.classic.spi.ILoggingEvent;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MSORequestDBApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class MSORequestDBImplTest {

	@LocalServerPort
	private int port;

	private MsoRequestsDbAdapter dbAdapter;
		
    @Autowired
    private OperationStatusRepository operationStatusRepository;
    
    @Autowired
    private ResourceOperationStatusRepository resourceOperationStatusRepo;

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
	public InfraActiveRequests setupTestEntities()   {	
		return buildTestRequest();
	}	
	
	@Before
	public void before(){
        JaxWsProxyFactoryBean jaxWsProxyFactory = new JaxWsProxyFactoryBean();
        jaxWsProxyFactory.setServiceClass(MsoRequestsDbAdapter.class);
        jaxWsProxyFactory.setAddress("http://localhost:" + port + "/services/RequestsDbAdapter");
        jaxWsProxyFactory.setUsername("bpel");
        jaxWsProxyFactory.setPassword("mso-db-1507!");
        dbAdapter = (MsoRequestsDbAdapter) jaxWsProxyFactory.create();
	}

	private InfraActiveRequests buildTestRequest() {	
		InfraActiveRequests testRequest= new InfraActiveRequests();
		testRequest.setRequestId("00032ab7-3fb3-42e5-965d-8ea592502017");	
		testRequest.setClientRequestId("00032ab7-3fb3-42e5-965d-8ea592502016");
		testRequest.setRequestStatus("COMPLETE");
		testRequest.setStatusMessage("Vf Module has been deleted successfully.");
		testRequest.setProgress((long) 100);
		testRequest.setSource("VID");		
		testRequest.setTenantId("6accefef3cb442ff9e644d589fb04107");
		testRequest.setServiceInstanceId("e3b5744d-2ad1-4cdd-8390-c999a38829bc");
		testRequest.setRequestAction("deleteInstance");
		testRequest.setRequestScope("vfModule");
		testRequest.setAction("deleteInstance");
		testRequest.setAicCloudRegion("mtn6");
		testRequest.setLastModifiedBy("BPMN");
		testRequest.setVfModuleId("c7d527b1-7a91-49fd-b97d-1c8c0f4a7992");
		testRequest.setVfModuleModelName("vSAMP10aDEV::base::module-0");
		testRequest.setVnfId("b92f60c8-8de3-46c1-8dc1-e4390ac2b005");
		testRequest.setRequestUrl("http://localhost:8080/onap/so/infra/serviceInstantiation/v7/serviceInstances");
		
		return testRequest;
	}

	@Test
	public void getByRequestId() throws MsoRequestsDbException  {
	    
		InfraActiveRequests testRequest = setupTestEntities();
		// Given
		String requestId = "00032ab7-3fb3-42e5-965d-8ea592502017";
		
		// When
		InfraActiveRequests infraRequest = dbAdapter.getInfraRequest(requestId);
		if(infraRequest ==null)
			 fail("Null infraRequest");
		
		// Then
		assertThat(infraRequest, sameBeanAs(testRequest).ignoring("requestBody").ignoring("endTime").ignoring("startTime").ignoring("modifyTime"));		
	}
	
	
	@Test
	public void getByInvalidRequestId() throws MsoRequestsDbException  {		
		// Given
		String requestId = "invalidRequestId";

		try {
			dbAdapter.getInfraRequest(requestId);
			fail("Expected MsoRequestsDbException to be thrown");
		} catch (Exception e) {
		    assertEquals(e.getMessage(),"Error retrieving MSO Infra Requests DB for Request ID invalidRequestId");
		}		
	}
	
	@Test
	public void getByClientRequestId() throws MsoRequestsDbException  {
		InfraActiveRequests testRequest = setupTestEntities();
		// Given
		String clientRequestId = "00032ab7-3fb3-42e5-965d-8ea592502016";
		
		// When
		InfraActiveRequests infraRequest = dbAdapter.getInfraRequest(clientRequestId);
		if(infraRequest ==null)
			 fail("Null infraRequest");
		
		// Then
		assertThat(infraRequest, sameBeanAs(testRequest).ignoring("requestBody").ignoring("endTime").ignoring("startTime").ignoring("modifyTime"));		
	}
	
	
	@Test
	public void updateInfraRequest() throws MsoRequestsDbException  {
		InfraActiveRequests testRequest = setupTestEntities();
		// Given
		String clientRequestId = "00032ab7-3fb3-42e5-965d-8ea592502016";
			

		// When
		String lastModifiedBy = "UNIT TEST";
		String statusMessage = "TESTING THE UDPATES";
		String progress = "50";
		String vnfOutputs = "VNF OUTPUTS";		
		String networkId = "New NetworkID";
		String vnfId = "NEWVNFID";
		String volumeGroupId = "NewVolumeGroupId";
		String serviceInstanceName = "NewServiceInstanceName";
		String configurationId = "NewConfigurationId";
		String configurationName = "NewConfigurationName";
		String vfModuleName = "VFModuleName";
		RequestStatusType requestStatus = RequestStatusType.COMPLETE ;
		String responseBody = "NewResponseBody";
		String vfModuleId = "NEW VF MODULEID";	
		String serviceInstanceId = " new serv ind";
		
		
		testRequest.setVolumeGroupId(volumeGroupId);
		testRequest.setServiceInstanceName(serviceInstanceName);
		testRequest.setConfigurationId(configurationId);
		testRequest.setConfigurationName(configurationName);
		testRequest.setNetworkId(networkId);
		testRequest.setResponseBody(responseBody);
		testRequest.setStatusMessage(statusMessage);
		testRequest.setProgress((long) 50);
		testRequest.setServiceInstanceId(lastModifiedBy);
		testRequest.setLastModifiedBy(lastModifiedBy);
		testRequest.setVfModuleId(vfModuleId);
		testRequest.setVfModuleName(vfModuleName);
		testRequest.setVnfId(vnfId);
		testRequest.setServiceInstanceId(serviceInstanceId);
		testRequest.setVfModuleName(vfModuleName);
		testRequest.setVnfOutputs(vnfOutputs);
				
		
		 dbAdapter.updateInfraRequest ( testRequest.getRequestId(),
                 lastModifiedBy,
                 statusMessage,
                 responseBody,
                 requestStatus,
                 progress,
                 vnfOutputs,
                 serviceInstanceId,
                 networkId,
                 vnfId,
                 vfModuleId,
                 volumeGroupId,
                 serviceInstanceName,
                 configurationId,
                 configurationName,
                 vfModuleName);
		InfraActiveRequests infraRequest = dbAdapter.getInfraRequest(clientRequestId);
		// Then
		assertThat(infraRequest, sameBeanAs(testRequest).ignoring("requestBody").ignoring("endTime").ignoring("startTime").ignoring("modifyTime"));		
	}
	
	@Test
	public void UpdateByInvalidRequestId() throws MsoRequestsDbException  {		
		// Given
		String requestId = "invalidRequestId";

		try {
			dbAdapter.updateInfraRequest ( requestId,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null);
			fail("Expected MsoRequestsDbException to be thrown");	
		} catch (Exception e) {
		    assertEquals(e.getMessage(),"Error retrieving MSO Infra Requests DB for Request ID invalidRequestId");
		}		
	}
	
	
	@Test
	public void updateInfraRequestNulls() throws MsoRequestsDbException  {
		InfraActiveRequests testRequest = setupTestEntities();
		// Given
		String clientRequestId = "00032ab7-3fb3-42e5-965d-8ea592502016";

		// When
		dbAdapter.updateInfraRequest ( testRequest.getRequestId(),
				testRequest.getLastModifiedBy(),
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null);
		InfraActiveRequests infraRequest = dbAdapter.getInfraRequest(clientRequestId);
		// Then
		assertThat(infraRequest, sameBeanAs(testRequest).ignoring("requestBody").ignoring("endTime").ignoring("startTime").ignoring("modifyTime"));		
	}
	
	@Test
	public void getSiteStatusNotDisabled() throws MsoRequestsDbException  {
		setupTestEntities();
		// Given
		String siteName = "siteName";
		
		// When
		boolean siteDisabled = dbAdapter.getSiteStatus(siteName);
		
		// Then
		assertEquals(siteDisabled, true);		
	}
	
	@Test
	public void getSiteStatusDisabled() throws MsoRequestsDbException  {
		setupTestEntities();
		// Given
		String siteName = "testSite";
		
		// When
		boolean siteDisabled = dbAdapter.getSiteStatus(siteName);
		
		// Then
		assertEquals(siteDisabled, false);		
	}
	
	@Test 
	public void updateServiceOperation() throws MsoRequestsDbException{
		String serviceId = "serviceid";
		String operationId = "operationid";
		String serviceName = "servicename";
		String operation = "newOperationType";
		String userId = "NewUserId";
		String result = "NewResult";
		String operationContent = "newOperationContent";
		String progress = "Newprogress";
		String reason = "NewReason";		
		
		OperationStatus updatedOperationStatus = new OperationStatus();
		
		
		
		updatedOperationStatus.setServiceId(serviceId);
		updatedOperationStatus.setServiceName(serviceName);
		updatedOperationStatus.setOperationId(operationId);
		updatedOperationStatus.setOperation(operation);
		updatedOperationStatus.setUserId(userId);
		updatedOperationStatus.setResult(result);
		updatedOperationStatus.setProgress(progress);
		updatedOperationStatus.setReason(reason);
		updatedOperationStatus.setOperationContent(operationContent);
		
		dbAdapter.updateServiceOperationStatus(serviceId, operationId, operation,  userId,
	             result, operationContent,  progress, reason);		
		OperationStatus dbOpStatus = operationStatusRepository.findOneByServiceIdAndOperationId(serviceId,operationId);		
		assertThat(dbOpStatus, sameBeanAs(updatedOperationStatus).ignoring("operateAt").ignoring("finishedAt"));	
	}
	
	
	@Test 
	public void updateServiceOperation_Not_Found() throws MsoRequestsDbException{
	    TestAppender.events.clear();
		String serviceId = "badserviceId";
		String operationId = "operationid";
		String operation = "newOperationType";
		String userId = "NewUserId";
		String result = "NewResult";
		String operationContent = "newOperationContent";
		String progress = "Newprogress";
		String reason = "NewReason";		
		
		OperationStatus updatedOperationStatus = new OperationStatus();
		
		
		
		updatedOperationStatus.setServiceId(serviceId);
		updatedOperationStatus.setOperationId(operationId);
		updatedOperationStatus.setOperation(operation);
		updatedOperationStatus.setUserId(userId);
		updatedOperationStatus.setResult(result);
		updatedOperationStatus.setProgress(progress);
		updatedOperationStatus.setReason(reason);
		updatedOperationStatus.setOperationContent(operationContent);

		dbAdapter.updateServiceOperationStatus(serviceId, operationId, operation,  userId,
	             result, operationContent,  progress, reason);		
		OperationStatus dbOpStatus = operationStatusRepository.findOneByServiceIdAndOperationId(serviceId,operationId);		
		assertThat(dbOpStatus, sameBeanAs(updatedOperationStatus).ignoring("operateAt").ignoring("finishedAt"));		
	}
	
	@Test 
	public void initResourceOperationStatus() throws MsoRequestsDbException{
		String resourceTemplateUUIDs = "template1:template2:template3:";
		String serviceId = "serviceId";
		String operationId = "operationId";
		String operationType = "operationType";
		
	     ResourceOperationStatus resource1 = new ResourceOperationStatus();
	     resource1.setOperationId(operationId);
	     resource1.setServiceId(serviceId);
	     resource1.setResourceTemplateUUID("template1");
	     resource1.setOperType(operationType);
	     resource1.setStatus(RequestsDbConstant.Status.PROCESSING);
	     resource1.setStatusDescription("Waiting for start");
         
         ResourceOperationStatus resource2 = new ResourceOperationStatus();
         resource2.setOperationId(operationId);
         resource2.setServiceId(serviceId);
         resource2.setResourceTemplateUUID("template2");
         resource2.setOperType(operationType);
         resource2.setStatus(RequestsDbConstant.Status.PROCESSING);
         resource2.setStatusDescription("Waiting for start");
         
         ResourceOperationStatus resource3 = new ResourceOperationStatus();
         resource3.setOperationId(operationId);
         resource3.setServiceId(serviceId);
         resource3.setResourceTemplateUUID("template3");
         resource3.setOperType(operationType);
         resource3.setStatus(RequestsDbConstant.Status.PROCESSING);
         resource3.setStatusDescription("Waiting for start");
         
         List<ResourceOperationStatus> expectedResult = new ArrayList<ResourceOperationStatus>();
         expectedResult.add(resource1);
         expectedResult.add(resource2);
         expectedResult.add(resource3);
		
		dbAdapter.initResourceOperationStatus(serviceId, operationId, operationType,resourceTemplateUUIDs);		
		List<ResourceOperationStatus> testList = resourceOperationStatusRepo.findByServiceIdAndOperationId(serviceId,operationId);		
		assertThat(testList, sameBeanAs(expectedResult));	
	}

	@Test
	public void getResourceOperationStatus() throws MsoRequestsDbException{
		String resourceTemplateUUIDs = "template1";
		String serviceId = "serviceId";
		String operationId = "operationId";
		String operationType = "operationType";

		ResourceOperationStatus resource1 = new ResourceOperationStatus();
		resource1.setOperationId(operationId);
		resource1.setServiceId(serviceId);
		resource1.setResourceTemplateUUID("template1");
		resource1.setOperType(operationType);
		resource1.setStatus(RequestsDbConstant.Status.PROCESSING);
		resource1.setStatusDescription("Waiting for start");


		dbAdapter.initResourceOperationStatus(serviceId, operationId, operationType,resourceTemplateUUIDs);

		ResourceOperationStatus actualResource = dbAdapter.getResourceOperationStatus(serviceId, operationId,"template1");
		assertThat(actualResource, sameBeanAs(resource1));
	}

	@Test
	public void updateResourceOperationStatus() throws MsoRequestsDbException{
	    TestAppender.events.clear();
		String resourceTemplateUUID = "template1";
		String serviceId = "serviceId";
		String operationId = "operationId";
		String operationType = "operationType";
		String resourceInstanceID = "resourceInstanceID";
		String jobId = "jobId";
		String status = RequestsDbConstant.Status.FINISHED;
		String progress = "50";
		String errorCode = "errorCode";
		String statusDescription = "statusDescription";


		ResourceOperationStatus expectedResource = new ResourceOperationStatus();
		expectedResource.setOperationId(operationId);
		expectedResource.setServiceId(serviceId);
		expectedResource.setResourceTemplateUUID(resourceTemplateUUID);
		expectedResource.setOperType(operationType);
		expectedResource.setJobId(jobId);
		expectedResource.setErrorCode(errorCode);
		expectedResource.setStatus(RequestsDbConstant.Status.FINISHED);
		expectedResource.setStatusDescription(statusDescription);
		expectedResource.setProgress(progress);
		expectedResource.setResourceInstanceID(resourceInstanceID);


		dbAdapter.updateResourceOperationStatus(serviceId, operationId, resourceTemplateUUID,
				operationType, resourceInstanceID, jobId, status, progress,
				errorCode, statusDescription);

		ResourceOperationStatus actualResource = dbAdapter.getResourceOperationStatus(serviceId, operationId,"template1");
		assertThat(actualResource, sameBeanAs(expectedResource));
		
		for(ILoggingEvent logEvent : TestAppender.events)
            if(logEvent.getLoggerName().equals("org.onap.so.logging.cxf.interceptor.SOAPLoggingInInterceptor") &&
                    logEvent.getMarker().getName().equals("ENTRY")
                    ){
                Map<String,String> mdc = logEvent.getMDCPropertyMap();
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.INSTANCE_UUID));
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.REQUEST_ID));
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.INVOCATION_ID));
                assertEquals("",mdc.get(ONAPLogConstants.MDCs.PARTNER_NAME));
                assertEquals("/services/RequestsDbAdapter",mdc.get(ONAPLogConstants.MDCs.SERVICE_NAME));
                assertEquals("INPROGRESS",mdc.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE));
            }else if(logEvent.getLoggerName().equals("org.onap.so.logging.cxf.interceptor.SOAPLoggingOutInterceptor") &&
                    logEvent.getMarker().getName().equals("EXIT")){
                Map<String,String> mdc = logEvent.getMDCPropertyMap();
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.REQUEST_ID));
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.INVOCATION_ID));
                assertEquals(null,mdc.get(ONAPLogConstants.MDCs.RESPONSE_CODE));
                assertEquals("",mdc.get(ONAPLogConstants.MDCs.PARTNER_NAME));
                assertEquals("/services/RequestsDbAdapter",mdc.get(ONAPLogConstants.MDCs.SERVICE_NAME));
                assertEquals("COMPLETED",mdc.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE));
            }
	}


}
