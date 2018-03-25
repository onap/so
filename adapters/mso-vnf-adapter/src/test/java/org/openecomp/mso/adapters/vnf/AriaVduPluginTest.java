/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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
package org.openecomp.mso.adapters.vnf;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.mso.vdu.utils.VduBlueprint;
import org.openecomp.mso.vdu.utils.VduPlugin;

import java.util.HashMap;

public class AriaVduPluginTest {

    VduPlugin vduPlugin = new AriaVduPlugin();

    @Test(expected = RuntimeException.class)
    public void instantiateVduFailedToCreateCSAR() throws Exception {
        VduBlueprint blueprint = new VduBlueprint();
        blueprint.setMainTemplateName("blueprintmain");
        vduPlugin.instantiateVdu("cloudid", "tenantid", "vduinstancename",
                new VduBlueprint(), new HashMap<>(), null, 100, true);
        Assert.assertFalse(true);
    }
}