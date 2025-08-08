package org.onap.so.adapters.tasks.orchestration;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.vnf.MsoVnfAdapterImpl;
import org.onap.so.adapters.vnf.VnfAdapterUtils;
import org.onap.so.adapters.vnf.exceptions.VnfException;
import org.onap.so.logging.tasks.AuditMDCSetup;
import org.onap.so.openstack.utils.MsoHeatUtils;
import com.woorea.openstack.heat.model.Stack;

@RunWith(MockitoJUnitRunner.class)
public class StackServiceTest {

    private String RESOURCE_PATH = "src/test/resources/__files/";

    @Mock
    private ExternalTask mockExternalTask;

    @Mock
    private ExternalTaskService mockExternalTaskService;

    @Mock
    private MsoVnfAdapterImpl vnfAdapterImpl;

    @Mock
    private VnfAdapterUtils vnfAdapterUtils;

    @Mock
    private MsoHeatUtils msoHeatUtils;

    @Mock
    private AuditMDCSetup mdcSetup;

    @InjectMocks
    private StackService stackService;

    @Test
    public void findRequestTypeTest() throws IOException {
        String payload = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "/vnfAdapterTaskRequestCreate.xml")));

        Optional<String> actual = stackService.findRequestType(payload);

        assertEquals("createVfModuleRequest", actual.get());
    }

    @Test
    public void testExecuteExternalTask() throws VnfException, IOException {
        String payload = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "/vnfAdapterTaskRequestCreate.xml")));

        Stack stack = new Stack();
        stack.setId("heatId");
        Mockito.when(vnfAdapterUtils.isMulticloudMode(Mockito.any(), Mockito.any())).thenReturn(false);
        Mockito.when(mockExternalTask.getVariable("openstackAdapterTaskRequest")).thenReturn(payload);
        Mockito.doNothing().when(vnfAdapterImpl).createVfModule(Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any());
        Mockito.doNothing().when(mockExternalTaskService).complete(Mockito.any(), Mockito.any());

        stackService.executeExternalTask(mockExternalTask, mockExternalTaskService);

        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("vf_module_id", "985a468b-328b-4c2b-ad0e-b8f1e19501c4");
        paramsMap.put("vnf_id", "6640feba-55f6-4946-9694-4d9558c8870a");
        paramsMap.put("vnf_name", "Robot_VNF_For_Volume_Group");
        paramsMap.put("availability_zone_0", "AZ-MN02");
        paramsMap.put("environment_context", "General_Revenue-Bearing");
        paramsMap.put("user_directives", "{}");
        paramsMap.put("workload_context", "");
        paramsMap.put("vf_module_name", "dummy_id");
        paramsMap.put("vf_module_index", "0");
        paramsMap.put("sdnc_directives",
                "{ \"attributes\": [ {\"attribute_name\": \"availability_zone_0\", \"attribute_value\": \"AZ-MN02\"}] }");

        Map<String, Object> variables = new HashMap<>();
        variables.put("backout", true);
        variables.put("OpenstackInvokeSuccess", true);
        variables.put("stackId", null);
        variables.put("openstackAdapterErrorMessage", "");
        variables.put("PollRollbackStatus", false);
        variables.put("rollbackPerformed", false);
        variables.put("OpenstackRollbackSuccess", false);
        variables.put("OpenstackPollSuccess", false);
        variables.put("os3Nw", false);


        Mockito.verify(vnfAdapterImpl, Mockito.times(1)).createVfModule(Mockito.eq("regionOne"),
                Mockito.eq("CloudOwner"), Mockito.eq("0422ffb57ba042c0800a29dc85ca70f8"),
                Mockito.eq(
                        "Vf zrdm5bpxmc02092017-Service/Vf zrdm5bpxmc02092017-VF 0::VfZrdm5bpxmc02092017Vf..pxmc_base..module-0"),
                Mockito.eq("1.0"), Mockito.eq("6640feba-55f6-4946-9694-4d9558c8870a"), Mockito.eq("dummy_id"),
                Mockito.eq("985a468b-328b-4c2b-ad0e-b8f1e19501c4"), Mockito.eq(null), Mockito.eq(null),
                Mockito.eq(null), Mockito.eq("074c64d0-7e13-4bcc-8bdb-ea922331102d"), Mockito.eq(paramsMap),
                Mockito.eq(false), Mockito.eq(false), Mockito.eq(null), Mockito.any(), Mockito.any());
        Mockito.verify(mockExternalTaskService).complete(Mockito.eq(mockExternalTask), Mockito.eq(variables));

    }

}
