package org.onap.aaiclient.client.aai;

import static org.junit.Assert.assertTrue;
import java.net.URISyntaxException;
import org.javatuples.Pair;
import org.junit.Test;

public class AAIDSLQueryClientTest {



    @Test
    public void verifyHeadersTest() throws URISyntaxException {

        AAIDSLQueryClient client = new AAIDSLQueryClient();
        assertTrue(client.getClient().getAdditionalHeaders().get("ALL").contains(Pair.with("X-DslApiVersion", "V2")));
    }
}
