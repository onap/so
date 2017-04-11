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

package org.openecomp.mso.db;

import org.hibernate.cfg.Configuration;
import org.hibernate.SessionFactory;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import java.net.URL;

public abstract class HibernateUtils {

    protected SessionFactory sessionFactory;
    
    protected synchronized void initializeHibernate(URL hibernateConfigFile) {
        // Can be null, in that case, we skip the loading
        if (hibernateConfigFile != null) {
            // Already initialized, skip
            if (sessionFactory != null) {
                return;
            }
    
            Configuration conf = new Configuration().configure(hibernateConfigFile);
            ServiceRegistry sr = new StandardServiceRegistryBuilder().applySettings(conf.getProperties()).build();
            sessionFactory = conf.buildSessionFactory(sr);
        }
    }

    public SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            initializeHibernate(getHibernateConfigFile());
        }
        return sessionFactory;
    }

    protected abstract URL getHibernateConfigFile();
}
