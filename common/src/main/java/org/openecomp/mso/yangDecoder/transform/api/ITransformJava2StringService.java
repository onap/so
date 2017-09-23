package org.openecomp.mso.yangDecoder.transform.api;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;

/**
 * Created by Administrator on 2017/3/21.
 */
public interface ITransformJava2StringService {
	//following function encode 
    <T extends DataObject>
    String transformContrainerDataObjectToString(InstanceIdentifier<T> instanceIdentifier, String uriPath, T dataObject)
            throws Exception;
    <T extends Notification>
    String transformNotificationToString(String uriPath, T notification)  throws Exception;
    <T extends DataObject>
    String transformRpcDataObjectToString(String uriPath, T dataObject)  throws Exception;

    //following function decode
    //for container
    DataObject  transformContrainerDataObjectFromString(String uriPath, String sxml, boolean ispost) throws Exception;
    //notification
    Notification transformNotificationFromString(String notficationName, String sxml) throws Exception;
    //for rpc
    DataObject transformRpcDataObjectFromString(String rpcName, String sxml) throws Exception;
}
