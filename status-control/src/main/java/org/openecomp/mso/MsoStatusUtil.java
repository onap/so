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

package org.openecomp.mso;


import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.SiteStatus;
import org.openecomp.mso.requestsdb.RequestsDatabase;

public class MsoStatusUtil {

    private MsoLogger logger = MsoLogger.getMsoLogger (MsoLogger.Catalog.GENERAL);

    public boolean getSiteStatus (String siteName) {
        // Query DB for the value
       
        try {
            SiteStatus siteStatus = (RequestsDatabase.getInstance()).getSiteStatus(siteName);
            if (null != siteStatus) {
                return siteStatus.getStatus();
            } else {
                // If status not present in the DB, by default the site is on, thus return true
                return true;
            }
        } catch (Exception e) {
            logger.error (MessageEnum.GENERAL_EXCEPTION, "", "getSiteStatus", MsoLogger.ErrorCode.DataError, "Exception in getting the site status", e);
        }

        return false;
    }
}
