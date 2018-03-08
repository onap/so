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

package org.openecomp.mso.db.catalog.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openecomp.mso.db.catalog.beans.AllottedResourceCustomization;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.db.catalog.beans.ServiceMacroHolder;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;

/**
 */

public class ServiceMacroHolderTest {

    @Test
    public final void serviceMacroHolderDataTest() {
        ServiceMacroHolder serviceMacroHolder = new ServiceMacroHolder();
        assertTrue(serviceMacroHolder.getService() == null);
        serviceMacroHolder.addVnfResource(new VnfResource());
        serviceMacroHolder.addVnfResourceCustomizations(new VnfResourceCustomization());
        serviceMacroHolder.addNetworkResourceCustomization(new NetworkResourceCustomization());
        serviceMacroHolder.addAllottedResourceCustomization(new AllottedResourceCustomization());
        assertTrue(serviceMacroHolder != null);
    }

}
