package org.onap.so.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import org.camunda.bpm.client.ExternalTaskClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

@RunWith(MockitoJUnitRunner.class)
public class ExternalTaskServiceUtilsTest {

    @Spy
    @InjectMocks
    private ExternalTaskServiceUtils utils = new ExternalTaskServiceUtils();

    @Mock
    private Environment mockEnv;

    @Mock
    private ExternalTaskClient mockClient;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        doReturn("3").when(mockEnv).getProperty("workflow.topics.maxClients", "3");
        doReturn("07a7159d3bf51a0e53be7a8f89699be7").when(mockEnv).getRequiredProperty("mso.msoKey");
        doReturn("6B466C603A260F3655DBF91E53CE54667041C01406D10E8CAF9CC24D8FA5388D06F90BFE4C852052B436").when(mockEnv)
                .getRequiredProperty("mso.auth");
        doReturn("someid").when(mockEnv).getRequiredProperty("mso.config.cadi.aafId");
        doReturn("http://camunda.com").when(mockEnv).getRequiredProperty("mso.workflow.endpoint");
    }

    @Test
    public void testCreateExternalTaskClient() throws Exception {
        ExternalTaskClient actualClient = utils.createExternalTaskClient();
        Assert.assertNotNull(actualClient);
    }

    @Test
    public void testGetAuth() throws Exception {
        String actual = utils.getAuth();
        String expected = "Att32054Life!@";
        assertEquals(expected, actual);
    }

    @Test
    public void testGetMaxClients() throws Exception {
        int actual = utils.getMaxClients();
        int expected = 3;
        assertEquals(expected, actual);
    }

}
