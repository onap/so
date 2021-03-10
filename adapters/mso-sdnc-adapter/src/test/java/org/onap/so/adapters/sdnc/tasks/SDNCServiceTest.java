package org.onap.so.adapters.sdnc.tasks;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.logging.tasks.AuditMDCSetup;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class SDNCServiceTest {

    private String RESOURCE_PATH = "src/test/resources";

    @Mock
    private ExternalTask mockExternalTask;

    @Mock
    private ExternalTaskService mockExternalTaskService;

    @Mock
    private AuditMDCSetup mdcSetup;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ResponseEntity<Object> response;

    @Mock
    private Environment env;

    @Spy
    @InjectMocks
    private SDNCService sdncService;

    @Test
    public void testExecutePostTask() throws IOException, GeneralSecurityException {
        String payload = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "/sdncRequest.json")));

        Mockito.when(env.getProperty("org.onap.so.adapters.sdnc.sdncHost")).thenReturn("sdncHost");
        Mockito.when(mockExternalTask.getVariable("sdncRequest")).thenReturn(payload);
        Mockito.when(mockExternalTask.getVariable("sdncUri")).thenReturn("/sdnc/action");
        Mockito.doReturn("Basic 123").when(sdncService).getAuth();
        Mockito.when(restTemplate.exchange(Mockito.any(URI.class), Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class), Mockito.eq(Object.class))).thenReturn(response);
        Mockito.when(response.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        Mockito.doNothing().when(mockExternalTaskService).complete(Mockito.any(), Mockito.any());

        sdncService.executePostTask(mockExternalTask, mockExternalTaskService);

        Mockito.verify(mockExternalTaskService).complete(Mockito.eq(mockExternalTask), Mockito.eq(new HashMap<>()));

    }

}
