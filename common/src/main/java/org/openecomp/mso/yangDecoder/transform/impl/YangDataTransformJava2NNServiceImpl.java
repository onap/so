package org.openecomp.mso.yangDecoder.transform.impl;

//import org.opendaylight.mdsal.binding.dom.adapter.BindingToNormalizedNodeCodec;

import org.opendaylight.netconf.sal.restconf.impl.ControllerContext;
import org.opendaylight.netconf.sal.restconf.impl.InstanceIdentifierContext;
import org.opendaylight.netconf.sal.restconf.impl.NormalizedNodeContext;
import org.opendaylight.netconf.sal.restconf.impl.WriterParameters;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.*;

import java.util.Map;

/**
 * Created by Administrator on 2017/3/17.
 */
public class YangDataTransformJava2NNServiceImpl {
    BindingNormalizedNodeSerializer mappingService;
    public YangDataTransformJava2NNServiceImpl(BindingNormalizedNodeSerializer mappingService)
    {
        this.mappingService=mappingService;
    }
    public  <T extends DataObject>  NormalizedNodeContext yangDataObjecttoNNC(InstanceIdentifier<T> identifier, String uri, T dobj)
    {
        final InstanceIdentifierContext<?> iiContext = ControllerContext.getInstance().toInstanceIdentifier(uri);
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> temp = mappingService.toNormalizedNode(identifier, dobj);
        WriterParameters.WriterParametersBuilder aa=new WriterParameters.WriterParametersBuilder();
        aa.setPrettyPrint(true);
        return new NormalizedNodeContext( iiContext, temp.getValue() ,aa.build());
    }
    public  <T extends DataObject> NormalizedNode yangDataObjecttoNN( InstanceIdentifier<T> identifier,T dobj) {
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> temp = mappingService.toNormalizedNode(identifier, dobj);
        if (null == temp) {
            return null;
        } else {
            return temp.getValue();
        }
    }
    public NormalizedNodeContext yangNNtoNNC(NormalizedNode nn, String uri){
        final InstanceIdentifierContext<?> iiContext = ControllerContext.getInstance().toInstanceIdentifier(uri);
        WriterParameters.WriterParametersBuilder aa=new WriterParameters.WriterParametersBuilder();
        aa.setPrettyPrint(true);
        return new NormalizedNodeContext(iiContext, nn,aa.build());
    }
    public  <T extends DataObject> T yangDataObjectfromNN(YangInstanceIdentifier identifier,NormalizedNode node) {
        Map.Entry<InstanceIdentifier<?>, DataObject> temp = mappingService.fromNormalizedNode(identifier, node);
        if (null == temp) {
            return null;
        } else {
            return (T) temp.getValue();
        }
    }
    public  <T extends DataObject> T yangDataObjectfromNNC(NormalizedNodeContext nnc)
    {
        return yangDataObjectfromNN(nnc.getInstanceIdentifierContext().getInstanceIdentifier(),nnc.getData());
    }
    public ContainerNode yangRpcDatatoNN(final DataContainer rpcdata){
        return mappingService.toNormalizedNodeRpcData(rpcdata);
    }
    public DataObject yangRpcDatafromNN(SchemaPath path, final ContainerNode data)
    {
        return mappingService.fromNormalizedNodeRpcData(path,data);
    }
    public static DataSchemaNode findDataSchemaNode(String uriPath,String inputoroutput)
    {
        final InstanceIdentifierContext<?> iicontext = ControllerContext.getInstance().toInstanceIdentifier(uriPath);
        DataSchemaNode dsn=JsonParserStream.getWrapSchemaNode( iicontext.getSchemaNode());
        return dsn;
    }
    public static SchemaNode findSchemaNode(final InstanceIdentifierContext<?> iicontext ,String inputoroutput)
    {
        SchemaNode schemaNode;

        final SchemaNode schemaNode0 = iicontext.getSchemaNode();
        if (schemaNode0 instanceof RpcDefinition) {
            if (inputoroutput.contains("output")) {
                schemaNode = ((RpcDefinition) schemaNode0).getOutput();
            } else {
                schemaNode = ((RpcDefinition) schemaNode0).getInput();
            }

        } else if(schemaNode0 instanceof NotificationDefinition)
        {
            schemaNode=schemaNode0;
        }
        else if (schemaNode0 instanceof DataSchemaNode) {
            schemaNode =  schemaNode0;
        } else {
            throw new IllegalStateException("Unknow SchemaNode");
        }
        return schemaNode;
    }

    public static DataSchemaNode findRpcSchemaNode(final InstanceIdentifierContext<?> iicontext,String inputoroutput)
    {
        DataSchemaNode schemaNode;
        final SchemaNode schemaNode0 = iicontext.getSchemaNode();
        boolean isInput = false;
        if (schemaNode0 instanceof RpcDefinition) {
            if (inputoroutput.contains("output")) {
                schemaNode = ((RpcDefinition) schemaNode0).getOutput();
                isInput = false;
            } else {
                schemaNode = ((RpcDefinition) schemaNode0).getInput();
                isInput = true;
            }

        } else if (schemaNode0 instanceof DataSchemaNode) {
            schemaNode = (DataSchemaNode) schemaNode0;
        } else {
            throw new IllegalStateException("Unknow SchemaNode");
        }
        return   schemaNode;
    }
    public DataObject yangRpcDatafromNN(final InstanceIdentifierContext<?> iicontext , final NormalizedNode data) {
       // final InstanceIdentifierContext<?> iicontext = ControllerContext.getInstance().toInstanceIdentifier(uriPath);
        ContainerNode contn= (ContainerNode)data;
        DataSchemaNode schemaNode= findRpcSchemaNode(iicontext,contn.getNodeType().getLocalName());;
    /*    final SchemaNode schemaNode0 = iicontext.getSchemaNode();
        boolean isInput = false;
        if (schemaNode0 instanceof RpcDefinition) {
            if (contn.getNodeType().getLocalName().contains("output")) {
                schemaNode = ((RpcDefinition) schemaNode0).getOutput();
                isInput = false;
            } else {
                schemaNode = ((RpcDefinition) schemaNode0).getInput();
                isInput = true;
            }

        } else if (schemaNode0 instanceof DataSchemaNode) {
            schemaNode = (DataSchemaNode) schemaNode0;
        } else {
            throw new IllegalStateException("Unknow SchemaNode");
        }*/
        SchemaPath path=schemaNode.getPath();
        return  mappingService.fromNormalizedNodeRpcData(path,contn);
    }
}
