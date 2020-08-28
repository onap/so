/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Wipro Limited. All rights reserved.
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

package org.onap.so.adapters.oof.utils;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.DatatypeConverter;
import org.onap.so.adapters.oof.constants.Constants;
import org.onap.so.utils.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class OofUtils {
    private static Logger logger = LoggerFactory.getLogger(OofUtils.class);

    @Autowired
    private Environment env;

    /**
     * @param messageEventName
     * @param correlator
     * @return
     */
    public String getCamundaMsgUrl(String messageEventName, String correlator) {
        System.out.println(env);
        String camundaMsgUrl = new StringBuilder(env.getRequiredProperty(Constants.WORKFLOW_MESSAGE_ENPOINT))
                .append("/").append(messageEventName).append("/").append(correlator).toString();
        return camundaMsgUrl;
    }

    /**
     * @return
     */
    public HttpHeaders getCamundaHeaders() {
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(MediaType.ALL);
        headers.setAccept(acceptableMediaTypes);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, addAuthorizationHeader(env.getRequiredProperty(Constants.CAMUNDA_AUTH),
                env.getRequiredProperty(Constants.MSO_KEY)));
        return headers;
    }

    /**
     * @param auth
     * @param msoKey
     * @return
     */
    protected String addAuthorizationHeader(String auth, String msoKey) {
        String basicAuth = null;
        try {
            String userCredentials = CryptoUtils.decrypt(auth, msoKey);
            if (userCredentials != null) {
                basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userCredentials.getBytes());
            }
        } catch (GeneralSecurityException e) {
            logger.error("Security exception", e);
        }
        return basicAuth;
    }

    /**
     * @return
     * @throws Exception
     */
    public HttpHeaders getOofHttpHeaders() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(acceptableMediaTypes);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * @param apiPath
     * @return
     */
    public String getOofurl(String apiPath) {
        return new StringBuilder(env.getRequiredProperty(Constants.OOF_ENDPOINT)).append(apiPath).toString();
    }


}
