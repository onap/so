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

package org.openecomp.mso.apihandler.common;

import java.io.IOException;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import org.openecomp.mso.logger.MsoLogger;

public class BPELRestClient extends RequestClient {

    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);

    public BPELRestClient () {
        super (CommonConstants.BPEL);
    }

    @Override
    public HttpResponse post (String bpelReqXML,
                              String requestId,
                              String requestTimeout,
                              String schemaVersion,
                              String serviceInstanceId,
                              String action) throws ClientProtocolException, IOException {
        String encryptedCredentials;
        HttpPost post = new HttpPost (url);
        msoLogger.debug ("BPEL url is: " + url);
        StringEntity input = new StringEntity (bpelReqXML);
        input.setContentType (MediaType.TEXT_XML);
        if (props != null) {
            encryptedCredentials = props.getProperty (CommonConstants.BPEL_AUTH,null);
            if (encryptedCredentials != null) {
                String userCredentials = getEncryptedPropValue (encryptedCredentials,
                                                                CommonConstants.DEFAULT_BPEL_AUTH,
                                                                CommonConstants.ENCRYPTION_KEY);
                if (userCredentials != null) {
                    post.addHeader ("Authorization",
                                    "Basic " + DatatypeConverter.printBase64Binary (userCredentials.getBytes ()));
                }
            }
        }
        post.addHeader (CommonConstants.REQUEST_ID_HEADER, requestId);
        post.addHeader (CommonConstants.REQUEST_TIMEOUT_HEADER, requestTimeout);
        post.addHeader (CommonConstants.SCHEMA_VERSION_HEADER, schemaVersion);
        if (serviceInstanceId != null) {
            post.addHeader (CommonConstants.SERVICE_INSTANCE_ID_HEADER, serviceInstanceId);
        }
        if (action != null) {
            post.addHeader (CommonConstants.ACTION_HEADER, action);
        }
        post.setEntity (input);
        HttpResponse response = client.execute (post);
        msoLogger.debug ("bpel response " + response);
        return response;
    }

    @Override
    public HttpResponse post (String bpelReqXML) {
        return null;
    }

    @Override
    public HttpResponse post(String requestId, boolean isBaseVfModule,
                             int recipeTimeout, String requestAction, String serviceInstanceId,
                             String vnfId, String vfModuleId, String volumeGroupId, String networkId, String configurationId,
                             String serviceType, String vnfType, String vfModuleType, String networkType,
                             String requestDetails) {
        return null;
    }
    
    @Override
    public HttpResponse get() {
        return null;
    }
}
