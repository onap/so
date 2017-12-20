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
package org.openecomp.mso.apihandlerinfra.utils;

import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesFactory;

public class MsoPropertiesUtils {

    private static final String MSO_PROP_APIHANDLER_INFRA = "MSO_PROP_APIHANDLER_INFRA";

    private static MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory ();

    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);

    private static boolean noProperties = true;

    private MsoPropertiesUtils() {
    }

    public static synchronized MsoJavaProperties loadMsoProperties () {
        MsoJavaProperties msoProperties;
        try {
            msoProperties = msoPropertiesFactory.getMsoJavaProperties (MSO_PROP_APIHANDLER_INFRA);
        } catch (Exception e) {
            msoLogger.error (MessageEnum.APIH_LOAD_PROPERTIES_FAIL, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.DataError, "Exception when loading MSO Properties", e);
            return null;
        }

        if (msoProperties != null && msoProperties.size () > 0) {
        	noProperties = false;
            msoLogger.info (MessageEnum.APIH_PROPERTY_LOAD_SUC, "", "");
            return msoProperties;
        } else {
            msoLogger.error (MessageEnum.APIH_NO_PROPERTIES, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.DataError, "No MSO APIH_INFRA Properties found");
            return null;
        }
    }

    public static synchronized boolean getNoPropertiesState() {
    	return noProperties;
    }
}
