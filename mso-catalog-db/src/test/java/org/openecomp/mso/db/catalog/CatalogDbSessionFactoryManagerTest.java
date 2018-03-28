/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
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

package org.openecomp.mso.db.catalog;

import org.junit.Test;
import org.mockito.Mock;
import org.openecomp.mso.db.catalog.CatalogDbSessionFactoryManager;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;


public class CatalogDbSessionFactoryManagerTest {
    @Test
    public void testgetHibernateConfigFile() {
        CatalogDbSessionFactoryManager catalogDbSessionFactoryManager = new CatalogDbSessionFactoryManager();
        System.setProperty("mso.db", "MYSQL");
        assertNotNull(catalogDbSessionFactoryManager.getHibernateConfigFile());
    }

    @Test
    public void testgetHibernateConfigFileNonMSODB() {
        CatalogDbSessionFactoryManager catalogDbSessionFactoryManager = new CatalogDbSessionFactoryManager();
        System.setProperty("mso.db", "test");
        assertNull(catalogDbSessionFactoryManager.getHibernateConfigFile());
    }
}
