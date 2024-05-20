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

package org.onap.so.apihandlerinfra.tenantisolation;

import java.net.MalformedURLException;
import java.net.URL;
import jakarta.ws.rs.core.MediaType;
import org.onap.so.client.grm.GRMProperties;
import org.onap.so.spring.SpringContextHelper;
import org.springframework.context.ApplicationContext;

public class GrmClientPropertiesImpl implements GRMProperties {

    private String grmEndpoint;
    private String grmAuth;
    private String grmKey;

    public GrmClientPropertiesImpl() {
        ApplicationContext context = SpringContextHelper.getAppContext();
        grmEndpoint = context.getEnvironment().getProperty("mso.grm.endpoint");
        grmAuth = context.getEnvironment().getProperty("mso.grm.auth");
        grmKey = context.getEnvironment().getProperty("mso.msoKey");
    }

    @Override
    public URL getEndpoint() throws MalformedURLException {
        return new URL(grmEndpoint);
    }

    @Override
    public String getSystemName() {
        return "MSO";
    }

    @Override
    public String getDefaultVersion() {
        return "v1";
    }

    @Override
    public String getAuth() {
        return grmAuth;
    }

    @Override
    public String getKey() {
        return grmKey;
    }

    @Override
    public String getContentType() {
        return MediaType.APPLICATION_JSON;
    }

}
