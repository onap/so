/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.rest;

import javax.net.ssl.SSLException;

import org.apache.http.conn.ssl.AbstractVerifier;
import org.onap.so.logger.MsoLogger;

/**
 * @version 1.0
 * Place holder to validate host name, for now just invokes the super class method
 *
 */
public class HostNameVerifier extends AbstractVerifier {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, HostNameVerifier.class);
    
    public final void verify(
            final String host,
            final String[] cns,
            final String[] subjectAlts) throws SSLException {
    	try {
    		verify(host, cns, subjectAlts, true);
    	} catch (SSLException sex) {
    	    LOGGER.debug("Exception:", sex);
    	}
    }
	
}
