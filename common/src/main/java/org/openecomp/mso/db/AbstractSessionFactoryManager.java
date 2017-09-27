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

package org.openecomp.mso.db;

import org.hibernate.cfg.Configuration;
import org.hibernate.SessionFactory;
import org.hibernate.service.ServiceRegistry;
import org.openecomp.mso.properties.MsoDatabaseException;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractSessionFactoryManager {

    protected static Map<String, SessionFactory> sessionFactories = new ConcurrentHashMap<>();

    protected synchronized SessionFactory initializeSessionFactory(URL hibernateConfigFile)
            throws MsoDatabaseException {
        try {
            if (hibernateConfigFile != null) {
                SessionFactory tempFactory = sessionFactories.get(hibernateConfigFile.getPath());
                // Already initialized, skip
                if (tempFactory != null) {
                    return tempFactory;
                }

                Configuration conf = new Configuration().configure(hibernateConfigFile);
                ServiceRegistry sr = new StandardServiceRegistryBuilder().applySettings(conf.getProperties()).build();
                tempFactory = conf.buildSessionFactory(sr);
                if (tempFactory == null) {
                    throw new MsoDatabaseException(
                            "SessionFactory can't be initialized, method buildSessionFactory returned null !");
                }
                sessionFactories.put(hibernateConfigFile.getPath(), tempFactory);
                return tempFactory;
            } else {
                throw new MsoDatabaseException(
                        "HibernateConfigFile provided is null, therefore Hibernate can't be initialized !");
            }
        } catch (Exception e) {
            throw new MsoDatabaseException("Exception occurred during the SessionFactory Build", e);
        }
    }

    public SessionFactory getSessionFactory() throws MsoDatabaseException {
        URL hibernateConfigFile = getHibernateConfigFile();
        SessionFactory factory = sessionFactories.get(hibernateConfigFile.getPath());
        if (factory == null) {
            factory = initializeSessionFactory(hibernateConfigFile);
        }
        return factory;
    }

    protected abstract URL getHibernateConfigFile();
}
