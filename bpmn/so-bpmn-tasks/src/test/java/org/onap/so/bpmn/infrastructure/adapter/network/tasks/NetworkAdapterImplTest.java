package org.onap.so.bpmn.infrastructure.adapter.network.tasks;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertNotNull;
import javax.xml.bind.JAXBException;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.onap.so.FileUtil;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.bpmn.BaseTaskTest;

public class NetworkAdapterImplTest extends BaseTaskTest {

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
