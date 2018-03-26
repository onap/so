package org.openecomp.mso.adapters.catalogdb.catalogrest;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

public class CatalogQueryExceptionTest {
    @Test
    public void catalogQueryExceptionConstructor(){
        CatalogQueryException messageCatalogQueryException = new CatalogQueryException("TestMessage");
        assertNotNull(messageCatalogQueryException.getMessage());
        assertEquals("TestMessage",messageCatalogQueryException.getMessage());

        CatalogQueryException paramsCatalogQueryException = new CatalogQueryException("TestMessage",CatalogQueryExceptionCategory.INTERNAL,true,"messageID");
        assertParams(paramsCatalogQueryException);

        CatalogQueryException defaultCatalogQueryException = new CatalogQueryException();
        defaultCatalogQueryException.setCategory(CatalogQueryExceptionCategory.INTERNAL);
        defaultCatalogQueryException.setMessage("TestMessage");
        defaultCatalogQueryException.setRolledBack(true);
        defaultCatalogQueryException.setMessageId("messageID");
        assertParams(defaultCatalogQueryException);
    }

    private void assertParams(CatalogQueryException paramsCatalogQueryException) {
        assertNotNull(paramsCatalogQueryException.getMessage());
        assertEquals("TestMessage",paramsCatalogQueryException.getMessage());
        assertNotNull(paramsCatalogQueryException.getCategory());
        assertEquals(CatalogQueryExceptionCategory.INTERNAL,paramsCatalogQueryException.getCategory());
        assertNotNull(paramsCatalogQueryException.getRolledBack());
        assertEquals(true,paramsCatalogQueryException.getRolledBack());
        assertNotNull(paramsCatalogQueryException.getMessageId());
        assertEquals("messageID",paramsCatalogQueryException.getMessageId());
    }
}
