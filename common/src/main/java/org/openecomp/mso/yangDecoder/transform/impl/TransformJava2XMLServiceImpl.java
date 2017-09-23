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
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by Administrator on 2017/3/20.
 */
public class TransformJava2XMLServiceImpl implements ITransformJava2StringService {
    private static final Logger LOG = LoggerFactory.getLogger(TransformJava2XMLServiceImpl.class);
    BindingNormalizedNodeSerializer mappingservice;
    SchemaContext schemaContext;
    YangDataTransformJava2NNServiceImpl  java2nnService;
    YangDataTransformNN2XMLServiceImpl   nn2xmlService;
    YangOdlNN2XMLImpl yangODLnn2xml;
    public TransformJava2XMLServiceImpl(BindingNormalizedNodeSerializer mappingService, SchemaContext schemaContext){
        this.mappingservice=mappingService;
        this.schemaContext=schemaContext;
        java2nnService=new YangDataTransformJava2NNServiceImpl(mappingService);
        nn2xmlService=new YangDataTransformNN2XMLServiceImpl(mappingService,schemaContext);
        yangODLnn2xml=new YangOdlNN2XMLImpl(schemaContext);
    }
    @Override
    public <T extends DataObject> String transformContrainerDataObjectToString(InstanceIdentifier<T> instanceIdentifier, String uriPath, T dataObject)
            throws Exception {
        // TODO Auto-generated method stub
        NormalizedNode nn=java2nnService.yangDataObjecttoNN(instanceIdentifier,dataObject);
        return yangODLnn2xml.yangNNtoXML(uriPath,nn);
    }
    @Override
    public <T extends Notification> String transformNotificationToString(String uriPath, T notification)
            throws Exception  {
        NormalizedNode nn= mappingservice.toNormalizedNodeNotification(notification);
        return yangODLnn2xml.yangNNtoXML(uriPath,nn);
    }
    @Override
    public <T extends DataObject> String transformRpcDataObjectToString(String uriPath, T dataObject)
            throws Exception {
        NormalizedNode nn=mappingservice.toNormalizedNodeRpcData(dataObject);
        return yangODLnn2xml.yangNNtoXML(uriPath,nn);
    }
    //for container
    public <T extends DataObject> String transformContrainerDataObjectToString(InstanceIdentifier<T> instanceIdentifier, T dataObject) {
        // TODO Auto-generated method stub
        NormalizedNode nn=java2nnService.yangDataObjecttoNN(instanceIdentifier,dataObject);
        return nn2xmlService.transformNNToString(nn);
    }

    public <T extends Notification> String transformNotificationToString(T notification) {
        NormalizedNode nn= mappingservice.toNormalizedNodeNotification(notification);
        return nn2xmlService.transformNNToString(nn);
    }

    //for rpc
    public <T extends DataObject> String transformRpcDataObjectToString(T dataObject) {
        NormalizedNode nn=mappingservice.toNormalizedNodeRpcData(dataObject);
        return nn2xmlService.transformNNToString(nn);
    }

    //for container
    @Override
    public  DataObject  transformContrainerDataObjectFromString(String uriPath, String sxml,boolean ispost) throws Exception  {
        // TODO Auto-generated method stub
    	if(ispost)
    	{
    		NormalizedNodeContext nnc=YangOdlNNC2XMLImpl.fromXML(uriPath,sxml, ispost);
    		 final InstanceIdentifierContext<?> iicontext=nnc.getInstanceIdentifierContext();
    		Map.Entry<InstanceIdentifier<?>, DataObject> temp = mappingservice.fromNormalizedNode(iicontext.getInstanceIdentifier(),nnc.getData());
    		return (DataObject)temp.getValue();
    	}
        final InstanceIdentifierContext<?> iicontext = ControllerContext.getInstance().toInstanceIdentifier(uriPath);
        NormalizedNode nn =nn2xmlService.transformNNFromString(sxml);
        Map.Entry<InstanceIdentifier<?>, DataObject> temp = mappingservice.fromNormalizedNode(iicontext.getInstanceIdentifier(),nn);
        if (null == temp) {
            return null;
        } else {
            return   temp.getValue();
        }
    }
    @Override
    public Notification transformNotificationFromString(String notficationName, String sxml) throws Exception {

        final InstanceIdentifierContext<?> iicontext = ControllerContext.getInstance().toInstanceIdentifier(notficationName);
        NormalizedNode nn = nn2xmlService.transformNotificationStringtoNN(sxml, notficationName);
        ContainerNode noti = (ContainerNode) nn;
        SchemaNode snode=iicontext.getSchemaNode() ;
        Notification obj = mappingservice.fromNormalizedNodeNotification(snode.getPath(), noti);
        return obj;
    }
        //for rpc
    @Override
    public DataObject transformRpcDataObjectFromString(String rpcName, String sxml) throws Exception {
        final InstanceIdentifierContext<?> iicontext = ControllerContext.getInstance().toInstanceIdentifier(rpcName);
        Map.Entry<DataSchemaNode, NormalizedNode>  nnentry = nn2xmlService.transformRpcNNEntryfromString(sxml,iicontext);
        ContainerNode rpc = (ContainerNode) nnentry.getValue();
        DataObject rpcdata= mappingservice.fromNormalizedNodeRpcData(nnentry.getKey().getPath(),rpc);
        return rpcdata;
    }
}
