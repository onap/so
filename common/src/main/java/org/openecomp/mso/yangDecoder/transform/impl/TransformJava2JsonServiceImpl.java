package org.openecomp.mso.yangDecoder.transform.impl;

import org.openecomp.mso.yangDecoder.transform.api.ITransformJava2StringService;
import org.opendaylight.netconf.sal.restconf.impl.ControllerContext;
import org.opendaylight.netconf.sal.restconf.impl.InstanceIdentifierContext;
import org.opendaylight.netconf.sal.restconf.impl.NormalizedNodeContext;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2017/3/20.
 */
public class TransformJava2JsonServiceImpl  implements ITransformJava2StringService {
    private static final Logger LOG = LoggerFactory.getLogger(TransformJava2JsonServiceImpl.class);
    BindingNormalizedNodeSerializer mappingservice;
    SchemaContext schemaContext;
    YangDataTransformNN2JsonServiceImpl nn2jsonService;
    YangDataTransformJava2NNServiceImpl java2nnService;
    public TransformJava2JsonServiceImpl(BindingNormalizedNodeSerializer mappingservice, SchemaContext schemaContext){
       this.mappingservice=mappingservice;
        this.schemaContext=schemaContext;
        nn2jsonService=new YangDataTransformNN2JsonServiceImpl();
        java2nnService=new YangDataTransformJava2NNServiceImpl(mappingservice);
    }
    @Override
    public <T extends DataObject> String transformContrainerDataObjectToString(InstanceIdentifier<T> instanceIdentifier, String uriPath,T dataObject) throws Exception {
        // TODO Auto-generated method stub
        NormalizedNode nn = java2nnService.yangDataObjecttoNN(instanceIdentifier, dataObject);
        NormalizedNodeContext nnc = java2nnService.yangNNtoNNC(nn, uriPath);
        String sjson = nn2jsonService.transformNNCToString(nnc);
        return sjson;
    }
    @Override
    public <T extends Notification> String transformNotificationToString(String uriPath,T notification)throws Exception  {
        NormalizedNode nn = mappingservice.toNormalizedNodeNotification(notification);
        NormalizedNodeContext nnc = java2nnService.yangNNtoNNC(nn, uriPath);
        String sjson = nn2jsonService.transformNNCToString(nnc);
        return sjson;
    }
    @Override
    public <T extends DataObject> String transformRpcDataObjectToString(String uriPath,T dataObject) throws Exception  {
        NormalizedNode nn = mappingservice.toNormalizedNodeRpcData(dataObject);
        NormalizedNodeContext nnc = java2nnService.yangNNtoNNC(nn, uriPath);
        String sjson = nn2jsonService.transformNNCToString(nnc);
        return sjson;
    }
    @Override
    public  DataObject  transformContrainerDataObjectFromString(String uriPath,String sjson,boolean ispost) throws Exception {
        NormalizedNodeContext nnc= nn2jsonService.transformDataObjectNNCFromString(uriPath,sjson,ispost);
        return java2nnService.yangDataObjectfromNNC(nnc);
    }
    @Override
    public Notification transformNotificationFromString(String notficationName,String sjson) throws Exception {
        final InstanceIdentifierContext<?> iicontext = ControllerContext.getInstance().toInstanceIdentifier(notficationName);
        NormalizedNodeContext nnc= nn2jsonService.transformNotficationNNCFromString(notficationName,sjson);
        ContainerNode contn= (ContainerNode)nnc.getData();
        return mappingservice.fromNormalizedNodeNotification(iicontext.getSchemaNode().getPath(),contn);
    }
    @Override
    public DataObject transformRpcDataObjectFromString(String rpcName,String sjson) throws Exception {
        final InstanceIdentifierContext<?> iicontext = ControllerContext.getInstance().toInstanceIdentifier(rpcName);
        NormalizedNodeContext nnc= nn2jsonService.transformRPCNNCFromString(rpcName,sjson);
        return  java2nnService.yangRpcDatafromNN(iicontext,nnc.getData());
    /*    ContainerNode contn= (ContainerNode)nnc.getData();
        DataSchemaNode schemaNode;
        final SchemaNode schemaNode0 = iicontext.getSchemaNode();
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
        }
       return mappingservice.fromNormalizedNodeRpcData(schemaNode.getPath(),contn); */
        //return mappingservice.toNormalizedNodeRpcData((DataContainer) nnc.getData());
      //  return  java2nnService.yangDataObjectfromNNC(nnc);
    }
}
