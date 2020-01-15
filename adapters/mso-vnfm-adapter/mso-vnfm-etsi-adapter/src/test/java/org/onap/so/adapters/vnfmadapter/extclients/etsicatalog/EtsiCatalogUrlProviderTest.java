/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.adapters.vnfmadapter.extclients.etsicatalog;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * @author Waqas Ikram (waqas.ikram@ericsson.com)
 *
 */
public class EtsiCatalogUrlProviderTest {

    private static final String DUMMY_BASE_URL = "http://localhost:80/api/vnfpkgm/v1";

    @Test
    public void testesiCatalogUrlProvider_validUrl() {
        final EtsiCatalogUrlProvider objUnderTest = new EtsiCatalogUrlProvider(DUMMY_BASE_URL);
        assertEquals(DUMMY_BASE_URL + "/vnf_packages", objUnderTest.getVnfPackagesUrl());

    }

    @Test(expected = IllegalArgumentException.class)
    public void testesiCatalogUrlProvider_nullUrl_throwException() {
        new EtsiCatalogUrlProvider(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testesiCatalogUrlProvider_emptyUrl_throwException() {
        new EtsiCatalogUrlProvider("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testesiCatalogUrlProvider_invalidUrl_throwException() {
        new EtsiCatalogUrlProvider("80/api/vnfpkgm/v1");
    }


}
