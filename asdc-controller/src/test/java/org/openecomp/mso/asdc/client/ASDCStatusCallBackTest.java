package org.openecomp.mso.asdc.client;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import org.onap.sdc.api.notification.IStatusData;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.openecomp.mso.asdc.BaseTest;
import org.openecomp.mso.asdc.client.exceptions.ArtifactInstallerException;
import org.openecomp.mso.asdc.client.test.emulators.JsonStatusData;
import org.springframework.beans.factory.annotation.Autowired;

public class ASDCStatusCallBackTest extends BaseTest {
	@Autowired
	private ASDCStatusCallBack statusCallback;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void activateCallbackTest() throws Exception {
		JsonStatusData statusData = new JsonStatusData();
		
		doNothing().when(toscaInstaller).installTheComponentStatus(isA(JsonStatusData.class));
		
		statusCallback.activateCallback(statusData);
		
		verify(toscaInstaller, times(1)).installTheComponentStatus(statusData);
	}
	
	@Test
	public void activateCallbackDoneErrorStatusTest() throws Exception {
		IStatusData statusData = mock(IStatusData.class);
		
		doReturn("distributionId").when(statusData).getDistributionID();
		doReturn("componentName").when(statusData).getComponentName();
		doReturn(DistributionStatusEnum.COMPONENT_DONE_ERROR).when(statusData).getStatus();
		doNothing().when(toscaInstaller).installTheComponentStatus(isA(IStatusData.class));
		
		statusCallback.activateCallback(statusData);
		
		verify(toscaInstaller, times(1)).installTheComponentStatus(statusData);
	}
	
	@Test
	public void activateCallbackExceptionTest() throws Exception {
		IStatusData statusData = mock(IStatusData.class);
		
		doReturn("distributionId").when(statusData).getDistributionID();
		doReturn("componentName").when(statusData).getComponentName();
		doReturn(DistributionStatusEnum.COMPONENT_DONE_OK).when(statusData).getStatus();
		doThrow(ArtifactInstallerException.class).when(toscaInstaller).installTheComponentStatus(isA(IStatusData.class));
		
		statusCallback.activateCallback(statusData);
	}
}
