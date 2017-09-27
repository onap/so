/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

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
