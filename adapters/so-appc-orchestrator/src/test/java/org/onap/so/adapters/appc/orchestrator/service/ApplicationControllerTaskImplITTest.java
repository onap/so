package org.onap.so.adapters.appc.orchestrator.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.onap.appc.client.lcm.model.Status;
import org.onap.so.adapters.appc.orchestrator.TestApplication;
import org.onap.so.adapters.appc.orchestrator.client.ApplicationControllerCallback;
import org.onap.so.adapters.appc.orchestrator.client.ApplicationControllerSupport;
import org.onap.so.appc.orchestrator.service.beans.ApplicationControllerTaskRequest;
import org.onap.so.appc.orchestrator.service.beans.ApplicationControllerVnf;
import org.onap.aaiclient.client.graphinventory.GraphInventoryCommonObjectMapperProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.onap.appc.client.lcm.model.Action;

@Ignore("This test is currently not working due to missing data source configuration")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration
@AutoConfigureWireMock(port = 0)
public class ApplicationControllerTaskImplITTest {

    @Autowired
    private ApplicationControllerTaskImpl applicationControllerTaskImpl;

    @Mock
    ExternalTask externalTask;

    @Mock
    ExternalTaskService externalTaskService;

    @Mock
    ApplicationControllerSupport appCSupport;

    ApplicationControllerTaskRequest request;

    ApplicationControllerCallback listener;

    GraphInventoryCommonObjectMapperProvider mapper = new GraphInventoryCommonObjectMapperProvider();

    @Before
    public void setup() {
        request = new ApplicationControllerTaskRequest();
        request.setRequestorId("testRequestorId");
        request.setBookName("testBookName");
        request.setControllerType("testControllerType");
        request.setFileParameters("testFileParams");
        request.setIdentityUrl("testIdentityUrl");
        request.setNewSoftwareVersion("2.0");
        request.setExistingSoftwareVersion("1.0");
        request.setOperationsTimeout("30");
        ApplicationControllerVnf applicationControllerVnf = new ApplicationControllerVnf();
        applicationControllerVnf.setVnfHostIpAddress("100.100");
        applicationControllerVnf.setVnfId("testVnfId");
        applicationControllerVnf.setVnfName("testVnfName");
        request.setApplicationControllerVnf(applicationControllerVnf);
        listener = new ApplicationControllerCallback(null, externalTaskService, appCSupport);
    }


    @Test
    public void testListener() throws Exception {
        request.setAction(Action.QuiesceTraffic);
        Status status = applicationControllerTaskImpl.execute("testRequestId", request, listener);
    }

}
