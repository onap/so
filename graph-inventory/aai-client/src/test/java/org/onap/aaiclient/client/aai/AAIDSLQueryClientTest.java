package org.onap.aaiclient.client.aai;

import static org.junit.Assert.assertEquals;
import java.net.URISyntaxException;
import org.junit.Test;

public class AAIDSLQueryClientTest {



    @Test
    public void verifyHeadersTest() throws URISyntaxException {

        AAIDSLQueryClient client = new AAIDSLQueryClient();
        assertEquals("V2", client.getClient().getAdditionalHeaders().get("X-DslApiVersion"));
    }
}
