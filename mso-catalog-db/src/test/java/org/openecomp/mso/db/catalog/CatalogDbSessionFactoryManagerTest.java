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
