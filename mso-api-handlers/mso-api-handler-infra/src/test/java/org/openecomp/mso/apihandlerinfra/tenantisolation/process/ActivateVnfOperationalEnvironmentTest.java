package org.openecomp.mso.apihandlerinfra.tenantisolation.process;

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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.mso.apihandlerinfra.Constants;
import org.openecomp.mso.apihandlerinfra.MsoPropertiesUtils;
import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.openecomp.mso.apihandlerinfra.tenantisolation.helpers.AAIClientHelper;
import org.openecomp.mso.apihandlerinfra.tenantisolation.helpers.AsdcClientHelper;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.Manifest;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RecoveryAction;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RequestDetails;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RequestParameters;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.ServiceModelList;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.mso.requestsdb.OperationalEnvDistributionStatusDb;
import org.openecomp.mso.requestsdb.OperationalEnvServiceModelStatusDb;
import org.openecomp.mso.requestsdb.RequestsDBHelper;
import org.openecomp.mso.rest.APIResponse;
import org.openecomp.mso.rest.RESTClient;
import org.openecomp.mso.rest.RESTConfig;

public class ActivateVnfOperationalEnvironmentTest {

	MsoJavaProperties properties = MsoPropertiesUtils.loadMsoProperties();
	AsdcClientHelper asdcClientUtils = new AsdcClientHelper(properties);
	
	String requestId = "TEST_requestId";
	String operationalEnvironmentId = "TEST_operationalEnvironmentId";	
	CloudOrchestrationRequest request = new CloudOrchestrationRequest();
	String workloadContext = "TEST_workloadContext";
	String recoveryAction  = "RETRY";
	String serviceModelVersionId = "TEST_serviceModelVersionId";	
	int retryCount = 3;
	String distributionId = "TEST_distributionId";
	
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
	public void getAAIClientHelperTest() throws Exception {
		
		request.setOperationalEnvironmentId(operationalEnvironmentId);
		ActivateVnfOperationalEnvironment activateVnf = new ActivateVnfOperationalEnvironment(request, requestId);
		AAIClientHelper aaiHelper = activateVnf.getAaiHelper();
		
		Assert.assertNotNull(aaiHelper);
		
	}
	
	@Test
	public void getAAIOperationalEnvironmentTest() throws Exception {

		// prepare return data
		JSONObject aaiJsonResponse = new JSONObject();
		aaiJsonResponse.put("operational-environment-id", "testASDCDistributionId");
		aaiJsonResponse.put("operational-environment-name", "testASDCDistributionIName");
		aaiJsonResponse.put("operational-environment-type", "VNF");
		aaiJsonResponse.put("operational-environment-status", "ACTIVE");
		aaiJsonResponse.put("tenant-context", "Test");
		aaiJsonResponse.put("workload-context", "PVT");
		aaiJsonResponse.put("resource-version", "1505228226913");
		String mockGetResponseJson = aaiJsonResponse.toString();  
		
		AAIResultWrapper aaiREsultWrapperObj = new AAIResultWrapper(mockGetResponseJson);  
		
		request.setOperationalEnvironmentId(operationalEnvironmentId);
		AAIClientHelper aaiClientHelperMock = Mockito.mock(AAIClientHelper.class);
		
		ActivateVnfOperationalEnvironment activateVnfMock = Mockito.mock(ActivateVnfOperationalEnvironment.class);
		ActivateVnfOperationalEnvironment activateVnf = new ActivateVnfOperationalEnvironment(request, requestId);

		Mockito.when(aaiClientHelperMock.getAaiOperationalEnvironment(operationalEnvironmentId)).thenReturn(aaiREsultWrapperObj);		
		
		activateVnfMock = spy(activateVnf);
		activateVnfMock.setAaiHelper(aaiClientHelperMock);
		activateVnfMock.getAAIOperationalEnvironment(operationalEnvironmentId);

		verify(activateVnfMock, times(1)).getAaiHelper();
		verify(aaiClientHelperMock, times(1)).getAaiOperationalEnvironment( any(String.class) );
		
	}	
	
	@Test
	public void processActivateASDCRequestTest() throws Exception {

		String jsonPayload = asdcClientUtils.buildJsonWorkloadContext(workloadContext);
		String distributionId = "TEST_distributionId";
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("statusCode", "202");
		jsonObject.put("message", "Success");
		jsonObject.put("distributionId", distributionId);
		
		List<ServiceModelList> serviceModelVersionIdList = new ArrayList<ServiceModelList>();
		ServiceModelList serviceModelList1 = new ServiceModelList(); 
		serviceModelList1.setRecoveryAction(RecoveryAction.retry);
		serviceModelList1.setServiceModelVersionId(serviceModelVersionId);
		serviceModelVersionIdList.add(serviceModelList1);
		
		ActivateVnfOperationalEnvironment activate = new ActivateVnfOperationalEnvironment(request, requestId);
		ActivateVnfOperationalEnvironment activateVnfMock = spy(activate);

		// Mockito mock
		OperationalEnvDistributionStatusDb distributionDb = Mockito.mock(OperationalEnvDistributionStatusDb.class);
		OperationalEnvServiceModelStatusDb serviceModelDb = Mockito.mock(OperationalEnvServiceModelStatusDb.class);
		AsdcClientHelper asdcClientHelperMock = Mockito.mock(AsdcClientHelper.class);
		RESTConfig configMock = Mockito.mock(RESTConfig.class);
		RESTClient clientMock = Mockito.mock(RESTClient.class);
		APIResponse apiResponseMock = Mockito.mock(APIResponse.class);
		
		activateVnfMock.setOperationalEnvDistributionStatusDb(distributionDb);
		activateVnfMock.setOperationalEnvServiceModelStatusDb(serviceModelDb);
		activateVnfMock.setAsdcClientHelper(asdcClientHelperMock);
		
		Mockito.when(asdcClientHelperMock.setRestClient(configMock)).thenReturn(clientMock);
		Mockito.when(asdcClientHelperMock.setHttpPostResponse(clientMock, jsonPayload)).thenReturn(apiResponseMock);
		Mockito.when(asdcClientHelperMock.enhanceJsonResponse(jsonObject, 202)).thenReturn(jsonObject);
		Mockito.when(asdcClientHelperMock.postActivateOperationalEnvironment(serviceModelVersionId, operationalEnvironmentId, workloadContext)).thenReturn(jsonObject);		
		
		activateVnfMock.processActivateASDCRequest(requestId, operationalEnvironmentId, serviceModelVersionIdList, workloadContext);
		
		verify(serviceModelDb, times(1)).insertOperationalEnvServiceModelStatus(requestId, operationalEnvironmentId, serviceModelVersionId, "SENT", "RETRY", retryCount, workloadContext);
		
	}	
	
	@Test
	public void executionTest() throws Exception {

		// prepare request detail
		List<ServiceModelList> serviceModelVersionIdList = new ArrayList<ServiceModelList>();
		ServiceModelList serviceModelList1 = new ServiceModelList(); 
		serviceModelList1.setRecoveryAction(RecoveryAction.retry);
		serviceModelList1.setServiceModelVersionId(serviceModelVersionId);
		serviceModelVersionIdList.add(serviceModelList1);
		
		RequestDetails requestDetails = new RequestDetails();
		RequestParameters requestParameters = new RequestParameters();
		Manifest manifest = new Manifest();
		manifest.setServiceModelList(serviceModelVersionIdList);
		requestParameters.setManifest(manifest);
		requestParameters.setWorkloadContext(workloadContext);
		requestDetails.setRequestParameters(requestParameters);
		
		// prepare aai return data
		JSONObject aaiJsonResponse = new JSONObject();
		aaiJsonResponse.put("operational-environment-id", "testASDCDistributionId");
		aaiJsonResponse.put("operational-environment-name", "testASDCDistributionIName");
		aaiJsonResponse.put("operational-environment-type", "VNF");
		aaiJsonResponse.put("operational-environment-status", "ACTIVE");
		aaiJsonResponse.put("tenant-context", "Test");
		aaiJsonResponse.put("workload-context", workloadContext);
		aaiJsonResponse.put("resource-version", "1505228226913");
		String mockGetResponseJson = aaiJsonResponse.toString();  
		AAIResultWrapper aaiREsultWrapperObj = new AAIResultWrapper(mockGetResponseJson);  
		
		// prepare asdc return data
		String jsonPayload = asdcClientUtils.buildJsonWorkloadContext(workloadContext);
	
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("statusCode", "202");
		jsonObject.put("message", "Success");
		jsonObject.put("distributionId", distributionId);
		
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
		
		AAIClientHelper aaiClientHelperMock = Mockito.mock(AAIClientHelper.class);
		Mockito.when(aaiClientHelperMock.getAaiOperationalEnvironment(operationalEnvironmentId)).thenReturn(aaiREsultWrapperObj);		
		Mockito.when(asdcClientHelperMock.postActivateOperationalEnvironment(serviceModelVersionId, operationalEnvironmentId, workloadContext)).thenReturn(jsonObject);		
		
		doNothing().when(serviceModelDb).insertOperationalEnvServiceModelStatus(requestId, operationalEnvironmentId, serviceModelVersionId, "SENT", recoveryAction, retryCount, workloadContext);
		doNothing().when(distributionDb).insertOperationalEnvDistributionStatus(distributionId, operationalEnvironmentId, serviceModelVersionId, "SENT", requestId);
	
		request.setOperationalEnvironmentId(operationalEnvironmentId);
		request.setRequestDetails(requestDetails);
		ActivateVnfOperationalEnvironment activate = new ActivateVnfOperationalEnvironment(request, requestId);
		ActivateVnfOperationalEnvironment activateVnfMock = spy(activate);
		activateVnfMock.setOperationalEnvDistributionStatusDb(distributionDb);
		activateVnfMock.setOperationalEnvServiceModelStatusDb(serviceModelDb);
		activateVnfMock.setRequestsDBHelper(dbUtils);		
		activateVnfMock.setAsdcClientHelper(asdcClientHelperMock);
		activateVnfMock.setAaiHelper(aaiClientHelperMock);

		activateVnfMock.execute();		
		
		verify(serviceModelDb, times(1)).insertOperationalEnvServiceModelStatus(requestId, operationalEnvironmentId, serviceModelVersionId, "SENT", recoveryAction, retryCount, workloadContext);
		verify(distributionDb, times(1)).insertOperationalEnvDistributionStatus(distributionId, operationalEnvironmentId, serviceModelVersionId, "SENT", requestId);		
		
		
	}			
	
	
}
