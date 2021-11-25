package org.onap.so.bpmn.infrastructure.adapter.network.tasks;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertNotNull;
import javax.xml.bind.JAXBException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.FileUtil;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;

@RunWith(MockitoJUnitRunner.Silent.class)
public class NetworkAdapterImplTest extends TestDataSetup {

    @Mock
    protected ExtractPojosForBB extractPojosForBB;
    @InjectMocks
    private NetworkAdapterImpl networkAdapterImpl = new NetworkAdapterImpl();

    private static final String RESPONSE =
            FileUtil.readResourceFile("__files/BuildingBlocks/Network/createNetworkResponse.xml");

    @Test
    public void postProcessNetworkAdapter() throws JAXBException {
        execution.setVariable("WorkflowResponse", RESPONSE);
        networkAdapterImpl.postProcessNetworkAdapter(execution);
        assertNotNull(execution.getVariable("createNetworkResponse"));
        assertThat(networkAdapterImpl.unmarshalXml(RESPONSE, CreateNetworkResponse.class),
                sameBeanAs(execution.getVariable("createNetworkResponse")));
    }

}
