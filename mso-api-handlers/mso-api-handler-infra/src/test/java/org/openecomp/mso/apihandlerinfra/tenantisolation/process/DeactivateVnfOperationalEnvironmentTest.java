package org.openecomp.mso.apihandlerinfra.tenantisolation.process;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.openecomp.mso.apihandlerinfra.tenantisolation.helpers.AAIClientHelper;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper;
import org.openecomp.mso.client.aai.objects.AAIOperationalEnvironment;
import org.openecomp.mso.requestsdb.RequestsDBHelper;

public class DeactivateVnfOperationalEnvironmentTest {
	
	@Test
	public void testDeactivateOperationalEnvironment() throws Exception {
		String operationlEnvironmentId = "ff3514e3-5a33-55df-13ab-12abad84e7ff";
		CloudOrchestrationRequest request = new CloudOrchestrationRequest();
		request.setOperationalEnvironmentId(operationlEnvironmentId);
		request.setRequestDetails(null);

		DeactivateVnfOperationalEnvironment deactivate = spy(new DeactivateVnfOperationalEnvironment(request, "ff3514e3-5a33-55df-13ab-12abad84e7fe"));
		RequestsDBHelper dbUtils = mock(RequestsDBHelper.class);
		AAIClientHelper helper = mock(AAIClientHelper.class);
		AAIResultWrapper wrapper = mock(AAIResultWrapper.class);
		AAIOperationalEnvironment operationalEnv = new AAIOperationalEnvironment();
		operationalEnv.setOperationalEnvironmentStatus("ACTIVE");
		
		doNothing().when(dbUtils).updateInfraSuccessCompletion(any(String.class), any(String.class), any(String.class));
		when(helper.getAaiOperationalEnvironment(any(String.class))).thenReturn(wrapper);
		when(wrapper.asBean(AAIOperationalEnvironment.class)).thenReturn(Optional.of((AAIOperationalEnvironment)operationalEnv));
		
		deactivate.setRequestsDBHelper(dbUtils);
		deactivate.setAaiHelper(helper);
		deactivate.execute();
		
		verify(dbUtils, times(1)).updateInfraSuccessCompletion(any(String.class), any(String.class), any(String.class));
	}
	
	@Test
	public void testDeactivateInvalidStatus() throws Exception {
		String operationlEnvironmentId = "ff3514e3-5a33-55df-13ab-12abad84e7ff";
		CloudOrchestrationRequest request = new CloudOrchestrationRequest();
		request.setOperationalEnvironmentId(operationlEnvironmentId);
		request.setRequestDetails(null);

		DeactivateVnfOperationalEnvironment deactivate = spy(new DeactivateVnfOperationalEnvironment(request, "ff3514e3-5a33-55df-13ab-12abad84e7fe"));
		RequestsDBHelper dbUtils = mock(RequestsDBHelper.class);
		AAIClientHelper helper = mock(AAIClientHelper.class);
		AAIResultWrapper wrapper = mock(AAIResultWrapper.class);
		AAIOperationalEnvironment operationalEnv = new AAIOperationalEnvironment();
		operationalEnv.setOperationalEnvironmentStatus("SUCCESS");
		
		doNothing().when(dbUtils).updateInfraSuccessCompletion(any(String.class), any(String.class), any(String.class));
		when(helper.getAaiOperationalEnvironment(any(String.class))).thenReturn(wrapper);
		when(wrapper.asBean(AAIOperationalEnvironment.class)).thenReturn(Optional.of((AAIOperationalEnvironment)operationalEnv));
		
		deactivate.setRequestsDBHelper(dbUtils);
		deactivate.setAaiHelper(helper);
		deactivate.execute();
		
		verify(dbUtils, times(1)).updateInfraFailureCompletion(any(String.class), any(String.class), any(String.class));
	}
}