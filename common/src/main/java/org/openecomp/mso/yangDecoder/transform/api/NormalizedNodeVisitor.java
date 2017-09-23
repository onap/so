package org.openecomp.mso.yangDecoder.transform.api;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public interface NormalizedNodeVisitor {
    void visitNode(int var1, String var2, NormalizedNode<?, ?> var3, boolean start);
}