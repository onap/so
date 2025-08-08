package org.onap.aaiclient.client.aai;

import static org.junit.Assert.assertTrue;
import org.javatuples.Pair;
import org.junit.Test;

public class AAIDSLQueryClientTest {



    @Test
    public void verifyHeadersTest() {

        AAIDSLQueryClient client = new AAIDSLQueryClient();
        assertTrue(client.getClient().getAdditionalHeaders().get("ALL").contains(Pair.with("X-DslApiVersion", "V2")));
    }
}
