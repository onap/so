package org.onap.aaiclient.client.aai.entities.uri;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.aaiclient.client.generated.fluentbuilders.Pserver;
import org.onap.aaiclient.client.graphinventory.entities.DSLQueryBuilder;
import org.onap.aaiclient.client.graphinventory.entities.DSLStartNode;
import org.onap.aaiclient.client.graphinventory.entities.DSLTraversal;
import org.onap.aaiclient.client.graphinventory.entities.Start;
import org.onap.aaiclient.client.graphinventory.entities.TraversalBuilder;
import org.onap.aaiclient.client.graphinventory.entities.__;

public class DslQueryBuilderTest {

    @Test
    void s() {
        DSLTraversal<Object> traversal = __.start(new DSLStartNode(new Pserver.Info())).build();
        assertEquals("pserver", traversal.get());
    }

    @Test
    void twoTraversalsWithSameQueryAreEqual() {
        DSLTraversal<Object> a = __.start(new DSLStartNode(new Pserver.Info())).build();
        DSLTraversal<Object> b = __.start(new DSLStartNode(new Pserver.Info())).build();

        assertEquals(a.hashCode(), b.hashCode(), "equal traversals must have equal hash codes");
        assertTrue(a.equals(b), "traversals wrapping the same query must be equal");
        assertTrue(b.equals(a), "equals must be symmetric");

        Set<DSLTraversal<Object>> set = new HashSet<>();
        set.add(a);
        assertTrue(set.contains(b), "an equal traversal must be found in a hash-based collection");
    }

    @Test
    void outputAppliesToNodeReachedThroughLambdaStep() {
        // .to(...) pushes a lambda QueryStep; the trailing .output() must reflect into that lambda
        // and mark the wrapped node for output. This depends on the JDK's synthetic lambda class name,
        // which changed shape in JDK 21+, so it guards against a silently-skipped reflection branch.
        DSLQueryBuilder<Start, Start> builder =
                TraversalBuilder.fragment(new DSLStartNode(Types.L_INTERFACE, __.key("interface-id", "myId")));

        builder.to(Types.VSERVER, __.key("vserver-name", "myName")).output().to(Types.P_INTERFACE).output();

        assertEquals("l-interface('interface-id', 'myId') > vserver*('vserver-name', 'myName') > p-interface*",
                builder.build().get());
    }

    @Test
    void traversalIsNotEqualToNonTraversalTypes() {
        DSLTraversal<Object> traversal = __.start(new DSLStartNode(new Pserver.Info())).build();

        // a DSLTraversal must not be considered equal to its raw query string or any other type
        assertNotEquals(traversal, traversal.get(), "a traversal must not equal a bare String");
        assertNotEquals(traversal, new Object(), "a traversal must not equal an unrelated object");
    }
}
