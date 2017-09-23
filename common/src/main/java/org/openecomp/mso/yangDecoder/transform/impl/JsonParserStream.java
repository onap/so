package org.openecomp.mso.yangDecoder.transform.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.util.RpcAsContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

/**
 * Created by 10112215 on 2017/9/17.
 */
public class JsonParserStream {
    public static DataSchemaNode getWrapSchemaNode(SchemaNode parentNode) {
        if(parentNode instanceof RpcDefinition) {
            return new RpcAsContainer((RpcDefinition)parentNode);
        } else if(parentNode instanceof NotificationDefinition) {
            return new NotificationAsContainer((NotificationDefinition)parentNode);
        } else {
            Preconditions.checkArgument(parentNode instanceof DataSchemaNode, "Instance of DataSchemaNode class awaited.");
            return (DataSchemaNode)parentNode;
        }
    }
}
