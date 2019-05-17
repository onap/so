package org.onap.so.adapters.audit;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.camunda.bpm.client.interceptor.auth.BasicAuthProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.utils.CryptoUtils;
import org.springframework.core.env.Environment;

@RunWith(MockitoJUnitRunner.class)
public class AuditStackServiceTest {

    @Spy
    @InjectMocks
    AuditStackService auditStackService;

    @Mock
    Environment mockEnvironment;


    @Before
    public void before() {
        Mockito.doReturn("5").when(mockEnvironment).getProperty("workflow.topics.maxClients", "10");
        Mockito.doReturn("6B466C603A260F3655DBF91E53CE54667041C01406D10E8CAF9CC24D8FA5388D06F90BFE4C852052B436")
                .when(mockEnvironment).getRequiredProperty("mso.auth");
        Mockito.doReturn("07a7159d3bf51a0e53be7a8f89699be7").when(mockEnvironment).getRequiredProperty("mso.msoKey");
        Mockito.doReturn("something").when(mockEnvironment).getRequiredProperty("mso.config.cadi.aafId");
        Mockito.doReturn("host.com").when(mockEnvironment).getRequiredProperty("mso.workflow.endpoint");
    }

    @Test
    public void testGetMaxClients() throws Exception {
        int actual = auditStackService.getMaxClients();
        assertEquals(5, actual);
    }

    @Test
    public void testCreateClientRequestInterceptor() throws Exception {
        String auth = CryptoUtils.decrypt(
                "6B466C603A260F3655DBF91E53CE54667041C01406D10E8CAF9CC24D8FA5388D06F90BFE4C852052B436",
                "07a7159d3bf51a0e53be7a8f89699be7");
        ClientRequestInterceptor expected = new BasicAuthProvider("something", auth);
        ClientRequestInterceptor actual = auditStackService.createClientRequestInterceptor();
        assertThat(actual, sameBeanAs(expected));

    }

    @Test
    public void testCreateExternalTaskClient() throws Exception {
        String auth = CryptoUtils.decrypt(
                "6B466C603A260F3655DBF91E53CE54667041C01406D10E8CAF9CC24D8FA5388D06F90BFE4C852052B436",
                "07a7159d3bf51a0e53be7a8f89699be7");
        ClientRequestInterceptor inter = new BasicAuthProvider("something", auth);
        Mockito.doReturn(inter).when(auditStackService).createClientRequestInterceptor();
        ExternalTaskClient actual = auditStackService.createExternalTaskClient();
        assertNotNull(actual);
        Mockito.verify(auditStackService, Mockito.times(1)).createClientRequestInterceptor();

    }
}
