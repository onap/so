package org.onap.so.adapters.appc.orchestrator.service;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.appc.client.lcm.model.Status;
import org.onap.so.adapters.appc.orchestrator.client.ApplicationControllerCallback;
import org.onap.so.adapters.appc.orchestrator.client.ApplicationControllerClient;
import org.onap.so.adapters.appc.orchestrator.client.ApplicationControllerOrchestratorException;
import org.onap.so.adapters.appc.orchestrator.client.beans.ConfigurationParameters;
import org.onap.so.adapters.appc.orchestrator.client.beans.Identity;
import org.onap.so.adapters.appc.orchestrator.client.beans.Parameters;
import org.onap.so.adapters.appc.orchestrator.client.beans.RequestParameters;
import org.onap.so.appc.orchestrator.service.beans.ApplicationControllerTaskRequest;
import org.onap.so.appc.orchestrator.service.beans.ApplicationControllerVnf;
import org.onap.aaiclient.client.graphinventory.GraphInventoryCommonObjectMapperProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.onap.appc.client.lcm.model.Action;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationControllerTaskImplTest {

    @Mock
    ApplicationControllerClient applicationControllerClient;

    @InjectMocks
    @Spy
    ApplicationControllerTaskImpl applicationControllerTaskImpl;

    ApplicationControllerTaskRequest request;

    ApplicationControllerCallback listener;

    GraphInventoryCommonObjectMapperProvider mapper = new GraphInventoryCommonObjectMapperProvider();

    @Before
    public void setup() {
        request = new ApplicationControllerTaskRequest();
        request.setBookName("testBookName");
        request.setControllerType("testControllerType");
        request.setFileParameters("testFileParams");
        request.setIdentityUrl("testIdentityUrl");
        request.setNewSoftwareVersion("2.0");
        request.setExistingSoftwareVersion("1.0");
        request.setOperationsTimeout("30");
        request.setRequestorId("testRequestorId");
        Map<String, String> reqConfigParams = new HashMap<>();
        reqConfigParams.put("name1", "value1");
        reqConfigParams.put("name2", "value2");
        request.setConfigParams(reqConfigParams);
        ApplicationControllerVnf applicationControllerVnf = new ApplicationControllerVnf();
        applicationControllerVnf.setVnfHostIpAddress("100.100");
        applicationControllerVnf.setVnfId("testVnfId");
        applicationControllerVnf.setVnfName("testVnfName");
        request.setApplicationControllerVnf(applicationControllerVnf);
        listener = new ApplicationControllerCallback(null, null, null);

    }

    @Test
    public void testExcute_healthCheck() throws JsonProcessingException, ApplicationControllerOrchestratorException {
        request.setAction(Action.HealthCheck);

        Parameters parameters = new Parameters();
        RequestParameters requestParams = new RequestParameters();
        requestParams.setHostIpAddress(request.getApplicationControllerVnf().getVnfHostIpAddress());
        parameters.setRequestParameters(requestParams);
        Optional<String> payload = Optional.of((mapper.getMapper().writeValueAsString(parameters)));

        Mockito.when(applicationControllerClient.vnfCommand(Action.HealthCheck, "testRequestId",
                request.getApplicationControllerVnf().getVnfId(), Optional.empty(), payload, "testControllerType",
                listener, "testRequestorId")).thenReturn(new Status());

        Status status = applicationControllerTaskImpl.execute("testRequestId", request, listener);

        Mockito.verify(applicationControllerClient).vnfCommand(Action.HealthCheck, "testRequestId",
                request.getApplicationControllerVnf().getVnfId(), Optional.empty(), payload, "testControllerType",
                listener, "testRequestorId");

    }

    @Test
    public void testExcute_resumeTraffic() throws JsonProcessingException, ApplicationControllerOrchestratorException {
        request.setAction(Action.ResumeTraffic);

        Parameters parameters = new Parameters();
        ConfigurationParameters configParams = new ConfigurationParameters();
        configParams.setVnfName(request.getApplicationControllerVnf().getVnfName());
        parameters.setConfigurationParameters(configParams);
        Optional<String> payload = Optional.of((mapper.getMapper().writeValueAsString(parameters)));

        Mockito.when(applicationControllerClient.vnfCommand(request.getAction(), "testRequestId",
                request.getApplicationControllerVnf().getVnfId(), Optional.empty(), payload, "testControllerType",
                listener, "testRequestorId")).thenReturn(new Status());

        Status status = applicationControllerTaskImpl.execute("testRequestId", request, listener);

        Mockito.verify(applicationControllerClient).vnfCommand(request.getAction(), "testRequestId",
                request.getApplicationControllerVnf().getVnfId(), Optional.empty(), payload, "testControllerType",
                listener, "testRequestorId");
    }

    @Test
    public void testExcute_stop() throws JsonProcessingException, ApplicationControllerOrchestratorException {
        request.setAction(Action.Stop);

        Identity identity = new Identity();
        identity.setIdentityUrl(request.getIdentityUrl());
        Optional<String> payload = Optional.of((mapper.getMapper().writeValueAsString(identity)));

        Mockito.when(applicationControllerClient.vnfCommand(request.getAction(), "testRequestId",
                request.getApplicationControllerVnf().getVnfId(), Optional.empty(), payload, "testControllerType",
                listener, "testRequestorId")).thenReturn(new Status());

        Status status = applicationControllerTaskImpl.execute("testRequestId", request, listener);

        Mockito.verify(applicationControllerClient).vnfCommand(request.getAction(), "testRequestId",
                request.getApplicationControllerVnf().getVnfId(), Optional.empty(), payload, "testControllerType",
                listener, "testRequestorId");
    }

    @Test
    public void testExcute_lock() throws JsonProcessingException, ApplicationControllerOrchestratorException {
        request.setAction(Action.Lock);

        Mockito.when(applicationControllerClient.vnfCommand(request.getAction(), "testRequestId",
                request.getApplicationControllerVnf().getVnfId(), Optional.empty(), Optional.empty(),
                "testControllerType", listener, "testRequestorId")).thenReturn(new Status());

        Status status = applicationControllerTaskImpl.execute("testRequestId", request, listener);

        Mockito.verify(applicationControllerClient).vnfCommand(request.getAction(), "testRequestId",
                request.getApplicationControllerVnf().getVnfId(), Optional.empty(), Optional.empty(),
                "testControllerType", listener, "testRequestorId");
    }

    @Test
    public void testExcute_quiesceTraffic() throws JsonProcessingException, ApplicationControllerOrchestratorException {
        request.setAction(Action.QuiesceTraffic);


        Parameters parameters = new Parameters();
        parameters.setOperationsTimeout(request.getOperationsTimeout());
        Optional<String> payload = Optional.of((mapper.getMapper().writeValueAsString(parameters)));
        System.out.println("PAYLOAD is: " + payload.get());

        Mockito.when(applicationControllerClient.vnfCommand(request.getAction(), "testRequestId",
                request.getApplicationControllerVnf().getVnfId(), Optional.empty(), payload, "testControllerType",
                listener, "testRequestorId")).thenReturn(new Status());

        Status status = applicationControllerTaskImpl.execute("testRequestId", request, listener);

        Mockito.verify(applicationControllerClient).vnfCommand(request.getAction(), "testRequestId",
                request.getApplicationControllerVnf().getVnfId(), Optional.empty(), payload, "testControllerType",
                listener, "testRequestorId");
    }

    @Test
    public void testExcute_distributeTraffic()
            throws JsonProcessingException, ApplicationControllerOrchestratorException {
        request.setAction(Action.DistributeTraffic);

        Parameters parameters = new Parameters();
        ConfigurationParameters configParams = new ConfigurationParameters();
        configParams.setBookName(request.getBookName());
        configParams.setNodeList(request.getNodeList());
        configParams.setFileParameterContent(request.getFileParameters());
        configParams.setVnfName(request.getApplicationControllerVnf().getVnfName());
        parameters.setConfigurationParameters(configParams);
        Optional<String> payload = Optional.of((mapper.getMapper().writeValueAsString(parameters)));

        Mockito.when(applicationControllerClient.vnfCommand(request.getAction(), "testRequestId",
                request.getApplicationControllerVnf().getVnfId(), Optional.empty(), payload, "testControllerType",
                listener, "testRequestorId")).thenReturn(new Status());

        Status status = applicationControllerTaskImpl.execute("testRequestId", request, listener);

        Mockito.verify(applicationControllerClient).vnfCommand(request.getAction(), "testRequestId",
                request.getApplicationControllerVnf().getVnfId(), Optional.empty(), payload, "testControllerType",
                listener, "testRequestorId");
    }

    @Test
    public void testExcute_distributeTrafficCheck()
            throws JsonProcessingException, ApplicationControllerOrchestratorException {
        request.setAction(Action.DistributeTrafficCheck);

        Parameters parameters = new Parameters();
        ConfigurationParameters configParams = new ConfigurationParameters();
        configParams.setBookName(request.getBookName());
        configParams.setNodeList(request.getNodeList());
        configParams.setFileParameterContent(request.getFileParameters());
        configParams.setVnfName(request.getApplicationControllerVnf().getVnfName());
        parameters.setConfigurationParameters(configParams);
        Optional<String> payload = Optional.of((mapper.getMapper().writeValueAsString(parameters)));

        Mockito.when(applicationControllerClient.vnfCommand(request.getAction(), "testRequestId",
                request.getApplicationControllerVnf().getVnfId(), Optional.empty(), payload, "testControllerType",
                listener, "testRequestorId")).thenReturn(new Status());

        Status status = applicationControllerTaskImpl.execute("testRequestId", request, listener);

        Mockito.verify(applicationControllerClient).vnfCommand(request.getAction(), "testRequestId",
                request.getApplicationControllerVnf().getVnfId(), Optional.empty(), payload, "testControllerType",
                listener, "testRequestorId");
    }

    @Test
    public void testExcute_upgradeBackup() throws JsonProcessingException, ApplicationControllerOrchestratorException {
        request.setAction(Action.UpgradeBackup);

        Parameters parameters = new Parameters();
        parameters.setExistingSoftwareVersion(request.getExistingSoftwareVersion());
        parameters.setNewSoftwareVersion(request.getNewSoftwareVersion());
        Optional<String> payload = Optional.of((mapper.getMapper().writeValueAsString(parameters)));

        Mockito.when(applicationControllerClient.vnfCommand(request.getAction(), "testRequestId",
                request.getApplicationControllerVnf().getVnfId(), Optional.empty(), payload, "testControllerType",
                listener, "testRequestorId")).thenReturn(new Status());

        Status status = applicationControllerTaskImpl.execute("testRequestId", request, listener);

        Mockito.verify(applicationControllerClient).vnfCommand(request.getAction(), "testRequestId",
                request.getApplicationControllerVnf().getVnfId(), Optional.empty(), payload, "testControllerType",
                listener, "testRequestorId");
    }

    @Test
    public void testExcute_configModify() throws JsonProcessingException, ApplicationControllerOrchestratorException {
        request.setAction(Action.ConfigModify);

        Parameters parameters = new Parameters();
        RequestParameters requestParams = new RequestParameters();
        requestParams.setHostIpAddress(request.getApplicationControllerVnf().getVnfHostIpAddress());
        parameters.setRequestParameters(requestParams);
        ConfigurationParameters configParams = new ConfigurationParameters();
        Map<String, String> configParamMap = new HashMap<>();
        configParamMap.put("name1", "value1");
        configParamMap.put("name2", "value2");
        configParams.setAdditionalProperties(configParamMap);
        parameters.setConfigurationParameters(configParams);
        Optional<String> payload = Optional.of((mapper.getMapper().writeValueAsString(parameters)));

        Mockito.when(applicationControllerClient.vnfCommand(request.getAction(), "testRequestId",
                request.getApplicationControllerVnf().getVnfId(), Optional.empty(), payload, "testControllerType",
                listener, "testRequestorId")).thenReturn(new Status());

        Status status = applicationControllerTaskImpl.execute("testRequestId", request, listener);

        Mockito.verify(applicationControllerClient).vnfCommand(request.getAction(), "testRequestId",
                request.getApplicationControllerVnf().getVnfId(), Optional.empty(), payload, "testControllerType",
                listener, "testRequestorId");
    }

    @Test
    @Ignore
    // TODO: Finish this test case
    public void testListener() throws Exception {
        request.setAction(Action.QuiesceTraffic);
        Status status = applicationControllerTaskImpl.execute("testRequestId", request, listener);
    }

}
