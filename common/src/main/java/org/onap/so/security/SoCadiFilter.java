/*-
 * ============LICENSE_START=======================================================
 * ONAP SO
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights
 *                             reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 * ===================================================================
 *
 */
package org.onap.so.security;

import jakarta.annotation.Priority;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import org.onap.so.security.cadi.config.Config;
import org.onap.so.security.cadi.filter.CadiFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test & aaf")
// Run right before default priority of 0 to block requests
@Priority(-1)
public class SoCadiFilter extends CadiFilter {

    protected final Logger logger = LoggerFactory.getLogger(SoCadiFilter.class);

    @Value("${mso.config.cadi.cadiLoglevel:#{null}}")
    private String cadiLoglevel;

    @Value("${mso.config.cadi.cadiKeyFile:#{null}}")
    private String cadiKeyFile;

    @Value("${mso.config.cadi.cadiTruststorePassword:#{null}}")
    private String cadiTrustStorePassword;

    @Value("${mso.config.cadi.cadiTrustStore:#{null}}")
    private String cadiTrustStore;

    @Value("${mso.config.cadi.cadiLatitude:#{null}}")
    private String cadiLatitude;

    @Value("${mso.config.cadi.cadiLongitude:#{null}}")
    private String cadiLongitude;

    @Value("${mso.config.cadi.aafEnv:#{null}}")
    private String aafEnv;

    @Value("${mso.config.cadi.aafApiVersion:#{null}}")
    private String aafApiVersion;

    @Value("${mso.config.cadi.aafRootNs:#{null}}")
    private String aafRootNs;

    @Value("${mso.config.cadi.aafId:#{null}}")
    private String aafMechId;

    @Value("${mso.config.cadi.aafPassword:#{null}}")
    private String aafMechIdPassword;

    @Value("${mso.config.cadi.aafLocateUrl:#{null}}")
    private String aafLocateUrl;

    @Value("${mso.config.cadi.aafUrl:#{null}}")
    private String aafUrl;

    @Value("${mso.config.cadi.apiEnforcement:#{null}}")
    private String apiEnforcement;

    @Value("${mso.config.cadi.userExpires:#{null}}")
    private String userExpires;

    private void checkIfNullProperty(String key, String value) {
        /*
         * When value is null, it is not defined in application.yaml set nothing in System properties
         */
        if (value != null) {
            System.setProperty(key, value);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        checkIfNullProperty(Config.CADI_LOGLEVEL, cadiLoglevel);
        checkIfNullProperty(Config.CADI_KEYFILE, cadiKeyFile);
        checkIfNullProperty(Config.CADI_TRUSTSTORE, cadiTrustStore);
        checkIfNullProperty(Config.CADI_TRUSTSTORE_PASSWORD, cadiTrustStorePassword);
        checkIfNullProperty(Config.CADI_LATITUDE, cadiLatitude);
        checkIfNullProperty(Config.CADI_LONGITUDE, cadiLongitude);
        checkIfNullProperty(Config.AAF_ENV, aafEnv);
        checkIfNullProperty(Config.AAF_API_VERSION, aafApiVersion);
        checkIfNullProperty(Config.AAF_ROOT_NS, aafRootNs);
        checkIfNullProperty(Config.AAF_APPID, aafMechId);
        checkIfNullProperty(Config.AAF_APPPASS, aafMechIdPassword);
        checkIfNullProperty(Config.AAF_LOCATE_URL, aafLocateUrl);
        checkIfNullProperty(Config.AAF_URL, aafUrl);
        checkIfNullProperty(Config.CADI_API_ENFORCEMENT, apiEnforcement);
        checkIfNullProperty(Config.AAF_USER_EXPIRES, userExpires);
        // checkIfNullProperty(AFT_ENVIRONMENT_VAR, aftEnv);
        logger.debug(" *** init Filter Config *** ");
        super.init(filterConfig);
    }


}
