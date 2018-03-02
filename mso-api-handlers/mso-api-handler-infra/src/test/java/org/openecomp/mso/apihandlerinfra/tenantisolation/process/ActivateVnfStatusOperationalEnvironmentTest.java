package org.openecomp.mso.apihandlerinfra.tenantisolation.process;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.mso.apihandlerinfra.Constants;
import org.openecomp.mso.apihandlerinfra.MsoPropertiesUtils;
import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.openecomp.mso.apihandlerinfra.tenantisolation.helpers.AsdcClientHelper;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.Distribution;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.DistributionStatus;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.Status;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.mso.requestsdb.OperationalEnvDistributionStatus;
import org.openecomp.mso.requestsdb.OperationalEnvDistributionStatusDb;
import org.openecomp.mso.requestsdb.OperationalEnvServiceModelStatus;
import org.openecomp.mso.requestsdb.OperationalEnvServiceModelStatusDb;
import org.openecomp.mso.requestsdb.RequestsDBHelper;
import org.openecomp.mso.rest.APIResponse;
import org.openecomp.mso.rest.RESTClient;
import org.openecomp.mso.rest.RESTConfig;

public class ActivateVnfStatusOperationalEnvironmentTest {

	MsoJavaProperties properties = MsoPropertiesUtils.loadMsoProperties();
	AsdcClientHelper asdcClientUtils = new AsdcClientHelper(properties);	
	
	String requestId = "TEST_requestId";
	String operationalEnvironmentId = "TEST_operationalEnvironmentId";	
	CloudOrchestrationRequest request = new CloudOrchestrationRequest();
	String workloadContext = "TEST_workloadContext";
	String recoveryAction  = "RETRY";
	String serviceModelVersionId = "TEST_serviceModelVersionId";
	int retryCount = 3;
	String asdcDistributionId = "TEST_distributionId";
	
	@BeforeClass
	public static void setUp() throws Exception {
		MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
		msoPropertiesFactory.removeAllMsoProperties();
		msoPropertiesFactory.initializeMsoProperties(Constants.MSO_PROP_APIHANDLER_INFRA, "/mso.apihandler-infra.properties");
	}	
	
	@After
	public void tearDown() throws Exception {
		
	}


	@Test
	public void checkOrUpdateOverallStatusTest_Ok() throws Exception {
		
		int retryCount = 0;
		
		ActivateVnfStatusOperationalEnvironment activateVnfStatus = spy(new ActivateVnfStatusOperationalEnvironment(request, requestId));

		// Mockito mock
		OperationalEnvDistributionStatusDb distributionDb = Mockito.mock(OperationalEnvDistributionStatusDb.class);
		OperationalEnvServiceModelStatusDb serviceModelDb = Mockito.mock(OperationalEnvServiceModelStatusDb.class);
		RequestsDBHelper requestDb = mock(RequestsDBHelper.class);
		
		// Prepare data		
		OperationalEnvServiceModelStatus modelStatus = new OperationalEnvServiceModelStatus();
		modelStatus.setWorkloadContext(workloadContext);
		modelStatus.setRecoveryAction(recoveryAction);
		modelStatus.setOperationalEnvId(operationalEnvironmentId);
		modelStatus.setRetryCount(retryCount);
		modelStatus.setServiceModelVersionDistrStatus(DistributionStatus.DISTRIBUTION_COMPLETE_OK.toString());
		
		OperationalEnvServiceModelStatus modelStatus1 = new OperationalEnvServiceModelStatus();
		modelStatus1.setWorkloadContext(workloadContext);
		modelStatus1.setRecoveryAction(recoveryAction);
		modelStatus1.setOperationalEnvId(operationalEnvironmentId);
		modelStatus1.setRetryCount(retryCount);
		modelStatus1.setServiceModelVersionDistrStatus(DistributionStatus.DISTRIBUTION_COMPLETE_OK.toString());
		
		List<OperationalEnvServiceModelStatus> queryServiceModelResponseList = new ArrayList<OperationalEnvServiceModelStatus>();
		queryServiceModelResponseList.add(modelStatus);
		queryServiceModelResponseList.add(modelStatus1);
		
		Mockito.when(serviceModelDb.getOperationalEnvIdStatus(operationalEnvironmentId, requestId)).thenReturn(queryServiceModelResponseList);
		doNothing().when(requestDb).updateInfraSuccessCompletion(any(String.class), any(String.class), any(String.class));
		
		activateVnfStatus.setOperationalEnvDistributionStatusDb(distributionDb);
		activateVnfStatus.setOperationalEnvServiceModelStatusDb(serviceModelDb);
		activateVnfStatus.setRequestsDBHelper(requestDb);
		activateVnfStatus.checkOrUpdateOverallStatus(requestId, operationalEnvironmentId);
		
		verify(requestDb, times(0)).updateInfraFailureCompletion(any(String.class), any(String.class), any(String.class));
		verify(requestDb, times(1)).updateInfraSuccessCompletion(any(String.class), any(String.class), any(String.class));
	    
	}
	
	@Test
	public void checkOrUpdateOverallStatusTest_Error() throws Exception {
		

		int retryCount = 0;  // no more retry
		
		ActivateVnfStatusOperationalEnvironment activateVnfStatus = spy(new ActivateVnfStatusOperationalEnvironment(request, requestId));

		// Mockito mock
		OperationalEnvDistributionStatusDb distributionDb = Mockito.mock(OperationalEnvDistributionStatusDb.class);
		OperationalEnvServiceModelStatusDb serviceModelDb = Mockito.mock(OperationalEnvServiceModelStatusDb.class);
		RequestsDBHelper requestDb = mock(RequestsDBHelper.class);
		
		// Prepare data
		OperationalEnvServiceModelStatus modelStatus = new OperationalEnvServiceModelStatus();
		modelStatus.setWorkloadContext(workloadContext);
		modelStatus.setRecoveryAction(recoveryAction);
		modelStatus.setOperationalEnvId(operationalEnvironmentId);
		modelStatus.setRetryCount(retryCount);
		modelStatus.setServiceModelVersionDistrStatus(DistributionStatus.DISTRIBUTION_COMPLETE_ERROR.toString());

		OperationalEnvServiceModelStatus modelStatus1 = new OperationalEnvServiceModelStatus();
		modelStatus1.setWorkloadContext(workloadContext);
		modelStatus1.setRecoveryAction(recoveryAction);
		modelStatus1.setOperationalEnvId(operationalEnvironmentId);
		modelStatus1.setRetryCount(retryCount);
		modelStatus1.setServiceModelVersionDistrStatus(DistributionStatus.DISTRIBUTION_COMPLETE_OK.toString());
		
		List<OperationalEnvServiceModelStatus> queryServiceModelResponseList = new ArrayList<OperationalEnvServiceModelStatus>();
		queryServiceModelResponseList.add(modelStatus);
		queryServiceModelResponseList.add(modelStatus1);
		
		Mockito.when(serviceModelDb.getOperationalEnvIdStatus(operationalEnvironmentId, requestId)).thenReturn(queryServiceModelResponseList);
		doNothing().when(requestDb).updateInfraSuccessCompletion(any(String.class), any(String.class), any(String.class));
		
		activateVnfStatus.setOperationalEnvDistributionStatusDb(distributionDb);
		activateVnfStatus.setOperationalEnvServiceModelStatusDb(serviceModelDb);
		activateVnfStatus.setRequestsDBHelper(requestDb);
		activateVnfStatus.checkOrUpdateOverallStatus(requestId, operationalEnvironmentId);
		
		verify(requestDb, times(0)).updateInfraSuccessCompletion(any(String.class), any(String.class), any(String.class));
		verify(requestDb, times(1)).updateInfraFailureCompletion(any(String.class), any(String.class), any(String.class));
	    
	}	
	
	@Test
	public void checkOrUpdateOverallStatusTest_Waiting() throws Exception {
		
		int retryCount = 2;  // 2 more retry
		
		ActivateVnfStatusOperationalEnvironment activateVnfStatus = spy(new ActivateVnfStatusOperationalEnvironment(request, requestId));

		// Mockito mock
		OperationalEnvDistributionStatusDb distributionDb = Mockito.mock(OperationalEnvDistributionStatusDb.class);
		OperationalEnvServiceModelStatusDb serviceModelDb = Mockito.mock(OperationalEnvServiceModelStatusDb.class);
		RequestsDBHelper requestDb = mock(RequestsDBHelper.class);

		OperationalEnvServiceModelStatus modelStatus1 = spy(new OperationalEnvServiceModelStatus());
		modelStatus1.setWorkloadContext(workloadContext);
		modelStatus1.setRecoveryAction(recoveryAction);
		modelStatus1.setOperationalEnvId(operationalEnvironmentId);
		modelStatus1.setRetryCount(0);
		modelStatus1.setServiceModelVersionDistrStatus(DistributionStatus.DISTRIBUTION_COMPLETE_OK.toString());
		
		OperationalEnvServiceModelStatus modelStatus2 = spy(new OperationalEnvServiceModelStatus());
		modelStatus2.setWorkloadContext(workloadContext);
		modelStatus2.setRecoveryAction(recoveryAction);
		modelStatus2.setOperationalEnvId(operationalEnvironmentId);
		modelStatus2.setRetryCount(retryCount);
		modelStatus2.setServiceModelVersionDistrStatus(DistributionStatus.DISTRIBUTION_COMPLETE_ERROR.toString());
		List<OperationalEnvServiceModelStatus> queryServiceModelResponseList = new ArrayList<OperationalEnvServiceModelStatus>();
		queryServiceModelResponseList.add(modelStatus1);
		queryServiceModelResponseList.add(modelStatus2);
		
		Mockito.when(serviceModelDb.getOperationalEnvIdStatus(operationalEnvironmentId, requestId)).thenReturn(queryServiceModelResponseList);
		doNothing().when(requestDb).updateInfraSuccessCompletion(any(String.class), any(String.class), any(String.class));
		
		activateVnfStatus.setOperationalEnvDistributionStatusDb(distributionDb);
		activateVnfStatus.setOperationalEnvServiceModelStatusDb(serviceModelDb);
		activateVnfStatus.setRequestsDBHelper(requestDb);
		activateVnfStatus.checkOrUpdateOverallStatus(requestId, operationalEnvironmentId);
		
		verify(requestDb, times(0)).updateInfraSuccessCompletion(any(String.class), any(String.class), any(String.class));
		verify(requestDb, times(0)).updateInfraFailureCompletion(any(String.class), any(String.class), any(String.class));
	    
	}		
	
	@Test
	public void executionTest() throws Exception {

		// Prepare db query mock response data
		OperationalEnvDistributionStatus operEnvDistStatusObj = new OperationalEnvDistributionStatus();
		operEnvDistStatusObj.setServiceModelVersionId(serviceModelVersionId);
		operEnvDistStatusObj.setDistributionId(asdcDistributionId);
		operEnvDistStatusObj.setOperationalEnvId( operationalEnvironmentId);
		operEnvDistStatusObj.setDistributionIdStatus(DistributionStatus.DISTRIBUTION_COMPLETE_OK.toString());
		operEnvDistStatusObj.setRequestId(requestId);
		
		// ServiceModelStatus - getOperationalEnvServiceModelStatus
		OperationalEnvServiceModelStatus operEnvServiceModelStatusObj = new OperationalEnvServiceModelStatus();
		operEnvServiceModelStatusObj.setRequestId(requestId);
		operEnvServiceModelStatusObj.setOperationalEnvId(operationalEnvironmentId);
		operEnvServiceModelStatusObj.setServiceModelVersionDistrStatus(DistributionStatus.DISTRIBUTION_COMPLETE_OK.toString());
		operEnvServiceModelStatusObj.setRecoveryAction(recoveryAction);
		operEnvServiceModelStatusObj.setRetryCount(retryCount);
		operEnvServiceModelStatusObj.setWorkloadContext(workloadContext);
		operEnvServiceModelStatusObj.setServiceModelVersionId(serviceModelVersionId);
		List<OperationalEnvServiceModelStatus> queryServiceModelResponseList = new ArrayList<OperationalEnvServiceModelStatus>();
		queryServiceModelResponseList.add(operEnvServiceModelStatusObj);
		
		// prepare distribution obj
		Distribution distribution = new Distribution();
		distribution.setStatus(Status.DISTRIBUTION_COMPLETE_OK);
		request.setDistribution(distribution);
		request.setDistributionId(asdcDistributionId);
		
		// prepare asdc return data
		String jsonPayload = asdcClientUtils.buildJsonWorkloadContext(workloadContext);
	
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("statusCode", "202");
		jsonObject.put("message", "Success");
		jsonObject.put("distributionId", asdcDistributionId);
		
		// Mockito mock
		OperationalEnvDistributionStatusDb distributionDb = Mockito.mock(OperationalEnvDistributionStatusDb.class);
		OperationalEnvServiceModelStatusDb serviceModelDb = Mockito.mock(OperationalEnvServiceModelStatusDb.class);
		RequestsDBHelper dbUtils = mock(RequestsDBHelper.class);
		AsdcClientHelper asdcClientHelperMock = Mockito.mock(AsdcClientHelper.class);
		RESTConfig configMock = Mockito.mock(RESTConfig.class);
		RESTClient clientMock = Mockito.mock(RESTClient.class);
		APIResponse apiResponseMock = Mockito.mock(APIResponse.class);		
	
		Mockito.when(asdcClientHelperMock.setRestClient(configMock)).thenReturn(clientMock);
		Mockito.when(asdcClientHelperMock.setHttpPostResponse(clientMock, jsonPayload)).thenReturn(apiResponseMock);
		Mockito.when(asdcClientHelperMock.enhanceJsonResponse(jsonObject, 202)).thenReturn(jsonObject);		
		Mockito.when(asdcClientHelperMock.postActivateOperationalEnvironment(serviceModelVersionId, operationalEnvironmentId, workloadContext)).thenReturn(jsonObject);		
		
		Mockito.when(distributionDb.getOperationalEnvDistributionStatus(asdcDistributionId)).thenReturn(operEnvDistStatusObj);
		Mockito.when(serviceModelDb.getOperationalEnvServiceModelStatus(operationalEnvironmentId, serviceModelVersionId)).thenReturn(operEnvServiceModelStatusObj);		
		Mockito.when(serviceModelDb.getOperationalEnvIdStatus(operationalEnvironmentId, requestId)).thenReturn(queryServiceModelResponseList);		
		
		int row = 1;
		Mockito.when(distributionDb.updateOperationalEnvDistributionStatus(distribution.getStatus().toString(), asdcDistributionId, operationalEnvironmentId, serviceModelVersionId)).thenReturn(row);
		Mockito.when(serviceModelDb.updateOperationalEnvRetryCountStatus(operationalEnvironmentId, serviceModelVersionId, distribution.getStatus().toString(), 0)).thenReturn(row);
		
		doNothing().when(dbUtils).updateInfraSuccessCompletion(any(String.class), any(String.class), any(String.class));
		
		request.setOperationalEnvironmentId(operationalEnvironmentId);
		ActivateVnfStatusOperationalEnvironment activateVnfStatus = new ActivateVnfStatusOperationalEnvironment(request, requestId);
		ActivateVnfStatusOperationalEnvironment activateVnfStatusMock = spy(activateVnfStatus);
		activateVnfStatusMock.setOperationalEnvDistributionStatusDb(distributionDb);
		activateVnfStatusMock.setOperationalEnvServiceModelStatusDb(serviceModelDb);
		activateVnfStatusMock.setRequestsDBHelper(dbUtils);		
		activateVnfStatusMock.setAsdcClientHelper(asdcClientHelperMock);

		activateVnfStatusMock.execute();		
		
		verify(distributionDb, times(1)).updateOperationalEnvDistributionStatus(distribution.getStatus().toString(), asdcDistributionId, operationalEnvironmentId, serviceModelVersionId);
		verify(serviceModelDb, times(1)).updateOperationalEnvRetryCountStatus(operationalEnvironmentId, serviceModelVersionId, distribution.getStatus().toString(), 0);		
		
		
	}				
	
	@Test
	public void executionTest_ERROR_Status_And_RETRY() throws Exception {

		int retryCnt = 3;
		String distributionStatus = DistributionStatus.DISTRIBUTION_COMPLETE_ERROR.toString();
		String recoverAction = "RETRY";
		
		// Prepare db query mock response data
		OperationalEnvDistributionStatus operEnvDistStatusObj = new OperationalEnvDistributionStatus();
		operEnvDistStatusObj.setServiceModelVersionId(serviceModelVersionId);
		operEnvDistStatusObj.setDistributionId(asdcDistributionId);
		operEnvDistStatusObj.setOperationalEnvId(operationalEnvironmentId);
		operEnvDistStatusObj.setDistributionIdStatus(distributionStatus);
		operEnvDistStatusObj.setRequestId(requestId);
		
		// ServiceModelStatus - getOperationalEnvServiceModelStatus
		OperationalEnvServiceModelStatus operEnvServiceModelStatusObj = new OperationalEnvServiceModelStatus();
		operEnvServiceModelStatusObj.setRequestId(requestId);
		operEnvServiceModelStatusObj.setOperationalEnvId(operationalEnvironmentId);
		operEnvServiceModelStatusObj.setServiceModelVersionDistrStatus(distributionStatus);
		operEnvServiceModelStatusObj.setRecoveryAction(recoverAction);
		operEnvServiceModelStatusObj.setRetryCount(retryCnt);
		operEnvServiceModelStatusObj.setWorkloadContext(workloadContext);
		operEnvServiceModelStatusObj.setServiceModelVersionId(serviceModelVersionId);
		List<OperationalEnvServiceModelStatus> queryServiceModelResponseList = new ArrayList<OperationalEnvServiceModelStatus>();
		queryServiceModelResponseList.add(operEnvServiceModelStatusObj);
		
		// prepare distribution obj
		Distribution distribution = new Distribution();
		distribution.setStatus(Status.DISTRIBUTION_COMPLETE_ERROR);
		request.setDistribution(distribution);
		request.setDistributionId(asdcDistributionId);
		
		// prepare asdc return data
		String jsonPayload = asdcClientUtils.buildJsonWorkloadContext(workloadContext);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("statusCode", "202");
		jsonObject.put("message", "Success");
		jsonObject.put("distributionId", asdcDistributionId);
		
		// Mockito mock
		OperationalEnvDistributionStatusDb distributionDb = Mockito.mock(OperationalEnvDistributionStatusDb.class);
		OperationalEnvServiceModelStatusDb serviceModelDb = Mockito.mock(OperationalEnvServiceModelStatusDb.class);
		RequestsDBHelper dbUtils = mock(RequestsDBHelper.class);
		AsdcClientHelper asdcClientHelperMock = Mockito.mock(AsdcClientHelper.class);
		RESTConfig configMock = Mockito.mock(RESTConfig.class);
		RESTClient clientMock = Mockito.mock(RESTClient.class);
		APIResponse apiResponseMock = Mockito.mock(APIResponse.class);		
	
		Mockito.when(asdcClientHelperMock.setRestClient(configMock)).thenReturn(clientMock);
		Mockito.when(asdcClientHelperMock.setHttpPostResponse(clientMock, jsonPayload)).thenReturn(apiResponseMock);
		Mockito.when(asdcClientHelperMock.enhanceJsonResponse(jsonObject, 202)).thenReturn(jsonObject);		
		Mockito.when(asdcClientHelperMock.postActivateOperationalEnvironment(serviceModelVersionId, operationalEnvironmentId, workloadContext)).thenReturn(jsonObject);		
		
		Mockito.when(distributionDb.getOperationalEnvDistributionStatus(asdcDistributionId)).thenReturn(operEnvDistStatusObj);
		Mockito.when(serviceModelDb.getOperationalEnvServiceModelStatus(operationalEnvironmentId, serviceModelVersionId)).thenReturn(operEnvServiceModelStatusObj);		
		Mockito.when(serviceModelDb.getOperationalEnvIdStatus(operationalEnvironmentId, requestId)).thenReturn(queryServiceModelResponseList);		
		
		int row = 1;
		Mockito.when(distributionDb.updateOperationalEnvDistributionStatus(distribution.getStatus().toString(), asdcDistributionId, operationalEnvironmentId, serviceModelVersionId)).thenReturn(row);
		Mockito.when(serviceModelDb.updateOperationalEnvRetryCountStatus(operationalEnvironmentId, serviceModelVersionId, distribution.getStatus().toString(), 0)).thenReturn(row);
		
		doNothing().when(dbUtils).updateInfraSuccessCompletion(any(String.class), any(String.class), any(String.class));
		
		request.setOperationalEnvironmentId(operationalEnvironmentId);
		ActivateVnfStatusOperationalEnvironment activateVnfStatus = new ActivateVnfStatusOperationalEnvironment(request, requestId);
		ActivateVnfStatusOperationalEnvironment activateVnfStatusMock = spy(activateVnfStatus);
		activateVnfStatusMock.setOperationalEnvDistributionStatusDb(distributionDb);
		activateVnfStatusMock.setOperationalEnvServiceModelStatusDb(serviceModelDb);
		activateVnfStatusMock.setRequestsDBHelper(dbUtils);		
		activateVnfStatusMock.setAsdcClientHelper(asdcClientHelperMock);

		activateVnfStatusMock.execute();		
		
		// waiting
		verify(dbUtils, times(0)).updateInfraSuccessCompletion(any(String.class), any(String.class), any(String.class));
		verify(dbUtils, times(0)).updateInfraFailureCompletion(any(String.class), any(String.class), any(String.class));
		assertEquals(false, activateVnfStatusMock.isSuccess());
		
	}

	@Test
	public void executionTest_ERROR_Status_And_RETRY_And_RetryZero() throws Exception {

		int retryCnt = 0;
		String distributionStatus = DistributionStatus.DISTRIBUTION_COMPLETE_ERROR.toString();
		String recoverAction = "RETRY";
		
		// Prepare db query mock response data
		OperationalEnvDistributionStatus operEnvDistStatusObj = new OperationalEnvDistributionStatus();
		operEnvDistStatusObj.setServiceModelVersionId(serviceModelVersionId);
		operEnvDistStatusObj.setDistributionId(asdcDistributionId);
		operEnvDistStatusObj.setOperationalEnvId(operationalEnvironmentId);
		operEnvDistStatusObj.setDistributionIdStatus(distributionStatus);
		operEnvDistStatusObj.setRequestId(requestId);
		
		// ServiceModelStatus - getOperationalEnvServiceModelStatus
		OperationalEnvServiceModelStatus operEnvServiceModelStatusObj = new OperationalEnvServiceModelStatus();
		operEnvServiceModelStatusObj.setRequestId(requestId);
		operEnvServiceModelStatusObj.setOperationalEnvId(operationalEnvironmentId);
		operEnvServiceModelStatusObj.setServiceModelVersionDistrStatus(distributionStatus);
		operEnvServiceModelStatusObj.setRecoveryAction(recoverAction);
		operEnvServiceModelStatusObj.setRetryCount(retryCnt);
		operEnvServiceModelStatusObj.setWorkloadContext(workloadContext);
		operEnvServiceModelStatusObj.setServiceModelVersionId(serviceModelVersionId);
		List<OperationalEnvServiceModelStatus> queryServiceModelResponseList = new ArrayList<OperationalEnvServiceModelStatus>();
		queryServiceModelResponseList.add(operEnvServiceModelStatusObj);
		
		// prepare distribution obj
		Distribution distribution = new Distribution();
		distribution.setStatus(Status.DISTRIBUTION_COMPLETE_ERROR);
		request.setDistribution(distribution);
		request.setDistributionId(asdcDistributionId);
		
		// prepare asdc return data
		String jsonPayload = asdcClientUtils.buildJsonWorkloadContext(workloadContext);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("statusCode", "202");
		jsonObject.put("message", "Success");
		jsonObject.put("distributionId", asdcDistributionId);
		
		// Mockito mock
		OperationalEnvDistributionStatusDb distributionDb = Mockito.mock(OperationalEnvDistributionStatusDb.class);
		OperationalEnvServiceModelStatusDb serviceModelDb = Mockito.mock(OperationalEnvServiceModelStatusDb.class);
		RequestsDBHelper dbUtils = mock(RequestsDBHelper.class);
		AsdcClientHelper asdcClientHelperMock = Mockito.mock(AsdcClientHelper.class);
		RESTConfig configMock = Mockito.mock(RESTConfig.class);
		RESTClient clientMock = Mockito.mock(RESTClient.class);
		APIResponse apiResponseMock = Mockito.mock(APIResponse.class);		
	
		Mockito.when(asdcClientHelperMock.setRestClient(configMock)).thenReturn(clientMock);
		Mockito.when(asdcClientHelperMock.setHttpPostResponse(clientMock, jsonPayload)).thenReturn(apiResponseMock);
		Mockito.when(asdcClientHelperMock.enhanceJsonResponse(jsonObject, 202)).thenReturn(jsonObject);		
		Mockito.when(asdcClientHelperMock.postActivateOperationalEnvironment(serviceModelVersionId, operationalEnvironmentId, workloadContext)).thenReturn(jsonObject);		
		
		Mockito.when(distributionDb.getOperationalEnvDistributionStatus(asdcDistributionId)).thenReturn(operEnvDistStatusObj);
		Mockito.when(serviceModelDb.getOperationalEnvServiceModelStatus(operationalEnvironmentId, serviceModelVersionId)).thenReturn(operEnvServiceModelStatusObj);		
		Mockito.when(serviceModelDb.getOperationalEnvIdStatus(operationalEnvironmentId, requestId)).thenReturn(queryServiceModelResponseList);		
		
		int row = 1;
		Mockito.when(distributionDb.updateOperationalEnvDistributionStatus(distribution.getStatus().toString(), asdcDistributionId, operationalEnvironmentId, serviceModelVersionId)).thenReturn(row);
		Mockito.when(serviceModelDb.updateOperationalEnvRetryCountStatus(operationalEnvironmentId, serviceModelVersionId, distribution.getStatus().toString(), 0)).thenReturn(row);
		
		doNothing().when(dbUtils).updateInfraSuccessCompletion(any(String.class), any(String.class), any(String.class));
		
		request.setOperationalEnvironmentId(operationalEnvironmentId);
		ActivateVnfStatusOperationalEnvironment activateVnfStatus = new ActivateVnfStatusOperationalEnvironment(request, requestId);
		ActivateVnfStatusOperationalEnvironment activateVnfStatusMock = spy(activateVnfStatus);
		activateVnfStatusMock.setOperationalEnvDistributionStatusDb(distributionDb);
		activateVnfStatusMock.setOperationalEnvServiceModelStatusDb(serviceModelDb);
		activateVnfStatusMock.setRequestsDBHelper(dbUtils);		
		activateVnfStatusMock.setAsdcClientHelper(asdcClientHelperMock);

		activateVnfStatusMock.execute();		
		
		// waiting
		verify(dbUtils, times(0)).updateInfraSuccessCompletion(any(String.class), any(String.class), any(String.class));
		verify(dbUtils, times(1)).updateInfraFailureCompletion(any(String.class), any(String.class), any(String.class));
		assertEquals(false, activateVnfStatusMock.isSuccess());
		
	}	
	
	@Test
	public void executionTest_ERROR_Status_And_RETRY_And_ErrorAsdc() throws Exception {

		int retryCnt = 3;
		String distributionStatus = DistributionStatus.DISTRIBUTION_COMPLETE_ERROR.toString();
		String recoverAction = "RETRY";
		
		// Prepare db query mock response data
		OperationalEnvDistributionStatus operEnvDistStatusObj = new OperationalEnvDistributionStatus();
		operEnvDistStatusObj.setServiceModelVersionId(serviceModelVersionId);
		operEnvDistStatusObj.setDistributionId(asdcDistributionId);
		operEnvDistStatusObj.setOperationalEnvId(operationalEnvironmentId);
		operEnvDistStatusObj.setDistributionIdStatus(distributionStatus);
		operEnvDistStatusObj.setRequestId(requestId);
		
		// ServiceModelStatus - getOperationalEnvServiceModelStatus
		OperationalEnvServiceModelStatus operEnvServiceModelStatusObj = new OperationalEnvServiceModelStatus();
		operEnvServiceModelStatusObj.setRequestId(requestId);
		operEnvServiceModelStatusObj.setOperationalEnvId(operationalEnvironmentId);
		operEnvServiceModelStatusObj.setServiceModelVersionDistrStatus(distributionStatus);
		operEnvServiceModelStatusObj.setRecoveryAction(recoverAction);
		operEnvServiceModelStatusObj.setRetryCount(retryCnt);
		operEnvServiceModelStatusObj.setWorkloadContext(workloadContext);
		operEnvServiceModelStatusObj.setServiceModelVersionId(serviceModelVersionId);
		List<OperationalEnvServiceModelStatus> queryServiceModelResponseList = new ArrayList<OperationalEnvServiceModelStatus>();
		queryServiceModelResponseList.add(operEnvServiceModelStatusObj);
		
		// prepare distribution obj
		Distribution distribution = new Distribution();
		distribution.setStatus(Status.DISTRIBUTION_COMPLETE_ERROR);
		request.setDistribution(distribution);
		request.setDistributionId(asdcDistributionId);
		
		// prepare asdc return data
		String jsonPayload = asdcClientUtils.buildJsonWorkloadContext(workloadContext);

		// ERROR in asdc
		JSONObject jsonMessages = new JSONObject();
		jsonMessages.put("statusCode", "409");
		jsonMessages.put("message", "Undefined Error Message!");
		jsonMessages.put("messageId", "SVC4675");
		jsonMessages.put("text", "Error: Service state is invalid for this action.");
		JSONObject jsonServException = new JSONObject();
		jsonServException.put("serviceException", jsonMessages);
		JSONObject jsonErrorRequest = new JSONObject();
		jsonErrorRequest.put("requestError", jsonServException);
		
		// Mockito mock
		OperationalEnvDistributionStatusDb distributionDb = Mockito.mock(OperationalEnvDistributionStatusDb.class);
		OperationalEnvServiceModelStatusDb serviceModelDb = Mockito.mock(OperationalEnvServiceModelStatusDb.class);
		RequestsDBHelper dbUtils = mock(RequestsDBHelper.class);
		AsdcClientHelper asdcClientHelperMock = Mockito.mock(AsdcClientHelper.class);
		RESTConfig configMock = Mockito.mock(RESTConfig.class);
		RESTClient clientMock = Mockito.mock(RESTClient.class);
		APIResponse apiResponseMock = Mockito.mock(APIResponse.class);		
	
		Mockito.when(asdcClientHelperMock.setRestClient(configMock)).thenReturn(clientMock);
		Mockito.when(asdcClientHelperMock.setHttpPostResponse(clientMock, jsonPayload)).thenReturn(apiResponseMock);
		Mockito.when(asdcClientHelperMock.enhanceJsonResponse(jsonMessages, 202)).thenReturn(jsonMessages);		
		Mockito.when(asdcClientHelperMock.postActivateOperationalEnvironment(serviceModelVersionId, operationalEnvironmentId, workloadContext)).thenReturn(jsonMessages);		
		
		Mockito.when(distributionDb.getOperationalEnvDistributionStatus(asdcDistributionId)).thenReturn(operEnvDistStatusObj);
		Mockito.when(serviceModelDb.getOperationalEnvServiceModelStatus(operationalEnvironmentId, serviceModelVersionId)).thenReturn(operEnvServiceModelStatusObj);		
		Mockito.when(serviceModelDb.getOperationalEnvIdStatus(operationalEnvironmentId, requestId)).thenReturn(queryServiceModelResponseList);		
		
		int row = 1;
		Mockito.when(distributionDb.updateOperationalEnvDistributionStatus(distribution.getStatus().toString(), asdcDistributionId, operationalEnvironmentId, serviceModelVersionId)).thenReturn(row);
		Mockito.when(serviceModelDb.updateOperationalEnvRetryCountStatus(operationalEnvironmentId, serviceModelVersionId, distribution.getStatus().toString(), 0)).thenReturn(row);
		
		doNothing().when(dbUtils).updateInfraSuccessCompletion(any(String.class), any(String.class), any(String.class));
		
		request.setOperationalEnvironmentId(operationalEnvironmentId);
		ActivateVnfStatusOperationalEnvironment activateVnfStatus = new ActivateVnfStatusOperationalEnvironment(request, requestId);
		ActivateVnfStatusOperationalEnvironment activateVnfStatusMock = spy(activateVnfStatus);
		activateVnfStatusMock.setOperationalEnvDistributionStatusDb(distributionDb);
		activateVnfStatusMock.setOperationalEnvServiceModelStatusDb(serviceModelDb);
		activateVnfStatusMock.setRequestsDBHelper(dbUtils);		
		activateVnfStatusMock.setAsdcClientHelper(asdcClientHelperMock);

		activateVnfStatusMock.execute();		
		
		// waiting
		verify(dbUtils, times(0)).updateInfraSuccessCompletion(any(String.class), any(String.class), any(String.class));
		verify(dbUtils, times(1)).updateInfraFailureCompletion(any(String.class), any(String.class), any(String.class));
		assertEquals(false, activateVnfStatusMock.isSuccess());
		
	}	
	
	@Test
	public void executionTest_ERROR_Status_And_SKIP() throws Exception {

		int retryCnt = 3;
		String distributionStatus = DistributionStatus.DISTRIBUTION_COMPLETE_ERROR.toString();
		String recoverAction = "SKIP";
		
		// Prepare db query mock response data
		OperationalEnvDistributionStatus operEnvDistStatusObj = new OperationalEnvDistributionStatus();
		operEnvDistStatusObj.setServiceModelVersionId(serviceModelVersionId);
		operEnvDistStatusObj.setDistributionId(asdcDistributionId);
		operEnvDistStatusObj.setOperationalEnvId( operationalEnvironmentId);
		operEnvDistStatusObj.setDistributionIdStatus(distributionStatus);
		operEnvDistStatusObj.setRequestId(requestId);
		
		// ServiceModelStatus - getOperationalEnvServiceModelStatus
		OperationalEnvServiceModelStatus operEnvServiceModelStatusObj = new OperationalEnvServiceModelStatus();
		operEnvServiceModelStatusObj.setRequestId(requestId);
		operEnvServiceModelStatusObj.setOperationalEnvId(operationalEnvironmentId);
		operEnvServiceModelStatusObj.setServiceModelVersionDistrStatus(distributionStatus);
		operEnvServiceModelStatusObj.setRecoveryAction(recoverAction);
		operEnvServiceModelStatusObj.setRetryCount(retryCnt);
		operEnvServiceModelStatusObj.setWorkloadContext(workloadContext);
		operEnvServiceModelStatusObj.setServiceModelVersionId(serviceModelVersionId);
		List<OperationalEnvServiceModelStatus> queryServiceModelResponseList = new ArrayList<OperationalEnvServiceModelStatus>();
		queryServiceModelResponseList.add(operEnvServiceModelStatusObj);
		
		// prepare distribution obj
		Distribution distribution = new Distribution();
		distribution.setStatus(Status.DISTRIBUTION_COMPLETE_ERROR);
		request.setDistribution(distribution);
		request.setDistributionId(asdcDistributionId);
		
		// Mockito mock
		OperationalEnvDistributionStatusDb distributionDb = Mockito.mock(OperationalEnvDistributionStatusDb.class);
		OperationalEnvServiceModelStatusDb serviceModelDb = Mockito.mock(OperationalEnvServiceModelStatusDb.class);
		RequestsDBHelper dbUtils = mock(RequestsDBHelper.class);
	
		Mockito.when(distributionDb.getOperationalEnvDistributionStatus(asdcDistributionId)).thenReturn(operEnvDistStatusObj);
		Mockito.when(serviceModelDb.getOperationalEnvServiceModelStatus(operationalEnvironmentId, serviceModelVersionId)).thenReturn(operEnvServiceModelStatusObj);		
		Mockito.when(serviceModelDb.getOperationalEnvIdStatus(operationalEnvironmentId, requestId)).thenReturn(queryServiceModelResponseList);		
		
		int row = 1;
		Mockito.when(distributionDb.updateOperationalEnvDistributionStatus(distribution.getStatus().toString(), asdcDistributionId, operationalEnvironmentId, serviceModelVersionId)).thenReturn(row);
		Mockito.when(serviceModelDb.updateOperationalEnvRetryCountStatus(operationalEnvironmentId, serviceModelVersionId, distribution.getStatus().toString(), 0)).thenReturn(row);
		
		doNothing().when(dbUtils).updateInfraSuccessCompletion(any(String.class), any(String.class), any(String.class));
		
		request.setOperationalEnvironmentId(operationalEnvironmentId);
		ActivateVnfStatusOperationalEnvironment activateVnfStatus = new ActivateVnfStatusOperationalEnvironment(request, requestId);
		ActivateVnfStatusOperationalEnvironment activateVnfStatusMock = spy(activateVnfStatus);
		activateVnfStatusMock.setOperationalEnvDistributionStatusDb(distributionDb);
		activateVnfStatusMock.setOperationalEnvServiceModelStatusDb(serviceModelDb);
		activateVnfStatusMock.setRequestsDBHelper(dbUtils);	
		
		activateVnfStatusMock.execute();		
		
		// waiting
		verify(dbUtils, times(0)).updateInfraSuccessCompletion(any(String.class), any(String.class), any(String.class));
		verify(dbUtils, times(0)).updateInfraFailureCompletion(any(String.class), any(String.class), any(String.class));
		assertEquals(false, activateVnfStatusMock.isSuccess());
		
	}	
	
	@Test
	public void executionTest_ERROR_Status_And_ABORT() throws Exception {

		int retryCnt = 3;
		String distributionStatus = DistributionStatus.DISTRIBUTION_COMPLETE_ERROR.toString();
		String recoverAction = "ABORT";
		
		// Prepare db query mock response data
		OperationalEnvDistributionStatus operEnvDistStatusObj = new OperationalEnvDistributionStatus();
		operEnvDistStatusObj.setServiceModelVersionId(serviceModelVersionId);
		operEnvDistStatusObj.setDistributionId(asdcDistributionId);
		operEnvDistStatusObj.setOperationalEnvId( operationalEnvironmentId);
		operEnvDistStatusObj.setDistributionIdStatus(distributionStatus);
		operEnvDistStatusObj.setRequestId(requestId);
		
		// ServiceModelStatus - getOperationalEnvServiceModelStatus
		OperationalEnvServiceModelStatus operEnvServiceModelStatusObj = new OperationalEnvServiceModelStatus();
		operEnvServiceModelStatusObj.setRequestId(requestId);
		operEnvServiceModelStatusObj.setOperationalEnvId(operationalEnvironmentId);
		operEnvServiceModelStatusObj.setServiceModelVersionDistrStatus(distributionStatus);
		operEnvServiceModelStatusObj.setRecoveryAction(recoverAction);
		operEnvServiceModelStatusObj.setRetryCount(retryCnt);
		operEnvServiceModelStatusObj.setWorkloadContext(workloadContext);
		operEnvServiceModelStatusObj.setServiceModelVersionId(serviceModelVersionId);
		List<OperationalEnvServiceModelStatus> queryServiceModelResponseList = new ArrayList<OperationalEnvServiceModelStatus>();
		queryServiceModelResponseList.add(operEnvServiceModelStatusObj);
		
		// prepare distribution obj
		Distribution distribution = new Distribution();
		distribution.setStatus(Status.DISTRIBUTION_COMPLETE_ERROR);
		request.setDistribution(distribution);
		request.setDistributionId(asdcDistributionId);
		
		// Mockito mock
		OperationalEnvDistributionStatusDb distributionDb = Mockito.mock(OperationalEnvDistributionStatusDb.class);
		OperationalEnvServiceModelStatusDb serviceModelDb = Mockito.mock(OperationalEnvServiceModelStatusDb.class);
		RequestsDBHelper dbUtils = mock(RequestsDBHelper.class);
		
		Mockito.when(distributionDb.getOperationalEnvDistributionStatus(asdcDistributionId)).thenReturn(operEnvDistStatusObj);
		Mockito.when(serviceModelDb.getOperationalEnvServiceModelStatus(operationalEnvironmentId, serviceModelVersionId)).thenReturn(operEnvServiceModelStatusObj);		
		Mockito.when(serviceModelDb.getOperationalEnvIdStatus(operationalEnvironmentId, requestId)).thenReturn(queryServiceModelResponseList);		
		
		int row = 1;
		Mockito.when(distributionDb.updateOperationalEnvDistributionStatus(distribution.getStatus().toString(), asdcDistributionId, operationalEnvironmentId, serviceModelVersionId)).thenReturn(row);
		Mockito.when(serviceModelDb.updateOperationalEnvRetryCountStatus(operationalEnvironmentId, serviceModelVersionId, distribution.getStatus().toString(), 0)).thenReturn(row);
		
		doNothing().when(dbUtils).updateInfraSuccessCompletion(any(String.class), any(String.class), any(String.class));
		
		request.setOperationalEnvironmentId(operationalEnvironmentId);
		ActivateVnfStatusOperationalEnvironment activateVnfStatus = new ActivateVnfStatusOperationalEnvironment(request, requestId);
		ActivateVnfStatusOperationalEnvironment activateVnfStatusMock = spy(activateVnfStatus);
		activateVnfStatusMock.setOperationalEnvDistributionStatusDb(distributionDb);
		activateVnfStatusMock.setOperationalEnvServiceModelStatusDb(serviceModelDb);
		activateVnfStatusMock.setRequestsDBHelper(dbUtils);	
		activateVnfStatusMock.execute();		
		
		assertEquals(false, activateVnfStatusMock.isSuccess());
		
	}		

}
