package org.onap.aaiclient.client.aai.entities.uri;

import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.Test;
import org.onap.aaiclient.client.generated.fluentbuilders.Pserver;
import org.onap.aaiclient.client.graphinventory.entities.DSLStartNode;
import org.onap.aaiclient.client.graphinventory.entities.DSLTraversal;
import org.onap.aaiclient.client.graphinventory.entities.__;

public class DslQueryBuilderTest {

    @Test
    void s() {
        DSLTraversal<Object> traversal = __.start(new DSLStartNode(new Pserver.Info())).build();
        assertEquals("pserver", traversal.get());
    }
}
