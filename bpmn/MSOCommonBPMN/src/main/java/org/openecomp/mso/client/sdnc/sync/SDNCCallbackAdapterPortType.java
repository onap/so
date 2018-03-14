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

package org.openecomp.mso.client.sdnc.sync;


import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;



//SDNCAdapter to BPEL Async response WEB Service
@WebService(targetNamespace = "http://org.openecomp/workflow/sdnc/adapter/callback/wsdl/v1", name = "SDNCCallbackAdapterPortType")
@XmlSeeAlso({ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface SDNCCallbackAdapterPortType {

    @WebResult(name = "SDNCAdapterResponse", targetNamespace = "http://org.openecomp/workflow/sdnc/adapter/schema/v1", partName = "SDNCAdapterCallbackResponse")
    @WebMethod(operationName = "SDNCAdapterCallback")
    public SDNCAdapterResponse sdncAdapterCallback(
        @WebParam(partName = "SDNCAdapterCallbackRequest", name = "SDNCAdapterCallbackRequest", targetNamespace = "http://org.openecomp/workflow/sdnc/adapter/schema/v1")
        SDNCAdapterCallbackRequest sdncAdapterCallbackRequest
    );
}
