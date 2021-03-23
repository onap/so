package org.onap.so.heatbridge.openstack.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.types.Facing;
import org.openstack4j.model.identity.v3.Endpoint;
import org.openstack4j.model.identity.v3.Service;
import org.openstack4j.model.identity.v3.Token;

@RunWith(MockitoJUnitRunner.Silent.class)
public class OpenstackV3ClientImplTest {

    @Mock
    protected OSClientV3 client;

    @InjectMocks
    OpenstackV3ClientImpl openstackV3ClientImpl;

    @Test
    public void testGetServiceCatalog() throws Exception {

        Token token = Mockito.mock(Token.class);
        Service service = Mockito.mock(Service.class);
        List<Service> serviceList = new ArrayList<>();
        serviceList.add(service);

        Endpoint endpoint = Mockito.mock(Endpoint.class);
        List<Endpoint> endpointsList = new ArrayList<>();
        endpointsList.add(endpoint);

        when(client.getToken()).thenReturn(token);
        when(token.getCatalog()).thenAnswer(x -> serviceList);
        when(service.getType()).thenReturn("volume");
        when(service.getEndpoints()).thenAnswer(x -> endpointsList);
        when(endpoint.getIface()).thenReturn(Facing.PUBLIC);
        when(endpoint.getUrl()).thenReturn(new URL("https://host.com/tenant/123/volumes"));

        URI url = openstackV3ClientImpl.getServiceCatalog();

        assertNotNull(url);
        assertEquals("host.com", url.getHost());

    }
}
