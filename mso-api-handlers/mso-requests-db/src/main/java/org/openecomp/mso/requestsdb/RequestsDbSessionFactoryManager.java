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
package org.openecomp.mso.requestsdb;

import org.openecomp.mso.db.AbstractSessionFactoryManager;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import java.net.URL;

public class RequestsDbSessionFactoryManager extends AbstractSessionFactoryManager {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.APIH);

    @Override
    protected URL getHibernateConfigFile() {
        try {

            if ("MYSQL".equals (System.getProperty ("mso.db")) || "MARIADB".equals(System.getProperty("mso.db"))) {
                return this.getClass().getClassLoader().getResource("hibernate-requests-core-mysql.cfg.xml");
            } else {
                   LOGGER.error (MessageEnum.APIH_DB_ACCESS_EXC_REASON, "DB Connection not specified to the JVM,choose either:-Dmso.db=MARIADB, -Dmso.db=MYSQL or -Dmso.container=AJSC", "", "", MsoLogger.ErrorCode.DataError , "DB Connection not specified to the JVM,choose either:-Dmso.db=MARIADB, -Dmso.db=MYSQL or -Dmso.container=AJSC");
                   return null;
               }
           } catch (Exception ex) {
               LOGGER.error (MessageEnum.APIH_DB_ACCESS_EXC_REASON, ex.getMessage (), "", "", MsoLogger.ErrorCode.DataError , "Problem in getting DB connection type", ex);
               return null;
           }

    }
}