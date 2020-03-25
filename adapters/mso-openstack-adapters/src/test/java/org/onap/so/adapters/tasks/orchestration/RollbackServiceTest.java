package org.onap.so.adapters.tasks.orchestration;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.xml.ws.Holder;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.tasks.orchestration.RollbackService;
import org.onap.so.adapters.vnf.MsoVnfAdapterImpl;
import org.onap.so.adapters.vnf.exceptions.VnfException;
import org.onap.so.entity.MsoRequest;
import org.onap.so.logging.tasks.AuditMDCSetup;
import org.onap.so.openstack.beans.VnfRollback;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.utils.MsoHeatUtils;
import com.woorea.openstack.heat.model.Stack;

@RunWith(MockitoJUnitRunner.class)
public class RollbackServiceTest {

    private String RESOURCE_PATH = "src/test/resources/__files/";

    @Mock
    private ExternalTask mockExternalTask;

    @Mock
    private ExternalTaskService mockExternalTaskService;

    @Mock
    private MsoVnfAdapterImpl vnfAdapterImpl;

    @Mock
    private MsoHeatUtils msoHeatUtils;

    @Mock
    private AuditMDCSetup mdcSetup;

    @InjectMocks
    private RollbackService rollbackService;


    @Test
    public void findRequestTypeTest() throws IOException {
        String payload = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "/vnfAdapterTaskRequestCreate.xml")));

        Optional<String> actual = rollbackService.findRequestType(payload);

        assertEquals("createVfModuleRequest", actual.get());
    }

    @Test
    public void testExecuteExternalTask() throws VnfException, MsoException, IOException {
        String payload = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "/vnfAdapterTaskRequestCreate.xml")));

        Stack stack = new Stack();
        stack.setId("heatId");
        Mockito.when(mockExternalTask.getVariable("vnfAdapterTaskRequest")).thenReturn(payload);
        Mockito.doNothing().when(vnfAdapterImpl).deleteVfModule(Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(mockExternalTaskService).complete(Mockito.any(), Mockito.any());

        rollbackService.executeExternalTask(mockExternalTask, mockExternalTaskService);

        Mockito.verify(vnfAdapterImpl, Mockito.times(1)).deleteVfModule(Mockito.eq("regionOne"),
                Mockito.eq("CloudOwner"), Mockito.eq("0422ffb57ba042c0800a29dc85ca70f8"), Mockito.eq("dummy_id"),
                Mockito.any(String.class), Mockito.any(String.class), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(mockExternalTaskService).complete(Mockito.eq(mockExternalTask), Mockito.any());

    }

}
