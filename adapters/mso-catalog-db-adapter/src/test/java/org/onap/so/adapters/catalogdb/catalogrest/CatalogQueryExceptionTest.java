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

package org.onap.so.adapters.catalogdb.catalogrest;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

public class CatalogQueryExceptionTest {
    @Test
    public void catalogQueryExceptionConstructor() {
        CatalogQueryException messageCatalogQueryException = new CatalogQueryException("TestMessage");
        assertNotNull(messageCatalogQueryException.getMessage());
        assertEquals("TestMessage", messageCatalogQueryException.getMessage());

        CatalogQueryException paramsCatalogQueryException =
                new CatalogQueryException("TestMessage", CatalogQueryExceptionCategory.INTERNAL, true, "messageID");
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
        assertEquals("TestMessage", paramsCatalogQueryException.getMessage());
        assertNotNull(paramsCatalogQueryException.getCategory());
        assertEquals(CatalogQueryExceptionCategory.INTERNAL, paramsCatalogQueryException.getCategory());
        assertNotNull(paramsCatalogQueryException.getRolledBack());
        assertEquals(true, paramsCatalogQueryException.getRolledBack());
        assertNotNull(paramsCatalogQueryException.getMessageId());
        assertEquals("messageID", paramsCatalogQueryException.getMessageId());
    }
}
