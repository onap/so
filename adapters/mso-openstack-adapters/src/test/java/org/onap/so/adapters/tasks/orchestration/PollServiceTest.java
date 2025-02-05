package org.onap.so.adapters.tasks.orchestration;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.vnf.VnfAdapterUtils;
import org.onap.so.logging.tasks.AuditMDCSetup;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.utils.MsoHeatUtils;
import com.woorea.openstack.heat.model.Stack;

@RunWith(MockitoJUnitRunner.class)
public class PollServiceTest {

    private String RESOURCE_PATH = "src/test/resources/__files/";

    @Mock
    private ExternalTask mockExternalTask;

    @Mock
    private ExternalTaskService mockExternalTaskService;

    @Mock
    private MsoHeatUtils msoHeatUtils;

    @Mock
    private VnfAdapterUtils vnfAdapterUtils;

    @Mock
    private AuditMDCSetup mdcSetup;

    @InjectMocks
    private PollService pollService;

    @Test
    public void testExecuteExternalTask() throws MsoException, IOException {
        String xmlString =
                new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "/vnfAdapterTaskRequestCreate.xml")));

        Mockito.when(vnfAdapterUtils.isMulticloudMode(Mockito.any(), Mockito.any())).thenReturn(false);
        Mockito.when(mockExternalTask.getVariable("openstackAdapterTaskRequest")).thenReturn(xmlString);
        Mockito.when(mockExternalTask.getVariable("PollRollbackStatus")).thenReturn(false);
        Mockito.when(mockExternalTask.getVariable("stackId")).thenReturn("stackId/stack123");
        Mockito.when(msoHeatUtils.pollStackForStatus(eq(118), any(Stack.class), eq("CREATE_IN_PROGRESS"),
                eq("regionOne"), eq("0422ffb57ba042c0800a29dc85ca70f8"), eq(false))).thenReturn(new Stack());
        Mockito.when(msoHeatUtils.getVfHeatTimeoutValue(any(), eq(false))).thenReturn(118);
        // Mockito.doNothing().when(msoHeatUtils).postProcessStackCreate(Mockito.any(), Mockito.any(), Mockito.any(),
        // Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        pollService.executeExternalTask(mockExternalTask, mockExternalTaskService);

        Mockito.verify(msoHeatUtils).pollStackForStatus(eq(118), any(Stack.class), eq("CREATE_IN_PROGRESS"),
                eq("regionOne"), eq("0422ffb57ba042c0800a29dc85ca70f8"), eq(false));
        Mockito.verify(msoHeatUtils).getVfHeatTimeoutValue(any(), eq(false));

    }

    @Test
    public void testExecuteExternalTask_rollback() throws MsoException, IOException {
        String xmlString =
                new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "/vnfAdapterTaskRequestCreate.xml")));

        Mockito.when(mockExternalTask.getVariable("openstackAdapterTaskRequest")).thenReturn(xmlString);
        Mockito.when(mockExternalTask.getVariable("PollRollbackStatus")).thenReturn(true);
        Mockito.when(mockExternalTask.getVariable("stackId")).thenReturn("stackId/stack123");
        Mockito.when(msoHeatUtils.pollStackForStatus(eq(118), any(), eq("DELETE_IN_PROGRESS"), eq("regionOne"),
                eq("0422ffb57ba042c0800a29dc85ca70f8"), eq(true))).thenReturn(new Stack());
        Mockito.doNothing().when(msoHeatUtils).postProcessStackDelete(Mockito.any());
        Mockito.when(msoHeatUtils.getVfHeatTimeoutValue(any(), eq(false))).thenReturn(118);

        pollService.executeExternalTask(mockExternalTask, mockExternalTaskService);

        Mockito.verify(msoHeatUtils).pollStackForStatus(eq(118), any(), eq("DELETE_IN_PROGRESS"), eq("regionOne"),
                eq("0422ffb57ba042c0800a29dc85ca70f8"), eq(true));
        Mockito.verify(msoHeatUtils).getVfHeatTimeoutValue(any(), eq(false));
    }

}
