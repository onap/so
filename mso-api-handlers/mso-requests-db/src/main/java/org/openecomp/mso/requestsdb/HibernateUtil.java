/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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


import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

@SuppressWarnings("deprecation")
public class HibernateUtil {

    //private static SessionFactory SESSION_FACTORY;
    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);
    
    private static final String hibernateFilename = "hibernate-requests-";
    
    private static SessionFactory SESSION_FACTORY;

    static {
        try {

            if (System.getProperty("mso.db") != null) {
                if ("container-managed".equals(System.getProperty("mso.db").toLowerCase())) {
                    LOGGER.debug("SessionFactory will be created by the container");
                } else {
                    SESSION_FACTORY = new Configuration()
                            .configure(hibernateFilename + System.getProperty("mso.db").toLowerCase() + ".cfg.xml")
                            .buildSessionFactory();
                }
            } else {
                LOGGER.error(MessageEnum.APIH_DB_ACCESS_EXC_REASON,
                        "MSO DB Connection type not specified to the JVM,you must specify a value to the -Dmso.db param: -Dmso.db=mysql or -Dmso.db=container-managed",
                        "", "", MsoLogger.ErrorCode.DataError,
                        "MSO DB Connection type not specified to the JVM,you must specify a value to the -Dmso.db param: -Dmso.db=mysql or -Dmso.db=container-managed");
            }
        } catch (Exception ex) {
            LOGGER.error(MessageEnum.APIH_DB_ACCESS_EXC_REASON, ex.getMessage(), "", "", MsoLogger.ErrorCode.DataError,
                    "Problem in getting DB connection type", ex);
            throw ex;
        }
    }

    public static SessionFactory getSessionFactory () {
        return SESSION_FACTORY;
    }

    private HibernateUtil () {
        // Avoid creation of an instance
    }
}
