/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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
package org.openecomp.mso.adapters.vfc.model;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

public class NsParametersTest {
    // TODO: following test case is done for coverage
    // later it should be modified for proper test.
    NsParameters nsParameters = new NsParameters();

    @Test
    public void getLocationConstraints() throws Exception {
        nsParameters.getLocationConstraints();
    }

    @Test
    public void setLocationConstraints() throws Exception {
        nsParameters.setLocationConstraints(Collections.emptyList());
    }

    @Test
    public void getAdditionalParamForNs() throws Exception {
        nsParameters.getAdditionalParamForNs();
    }

    @Test
    public void setAdditionalParamForNs() throws Exception {
        nsParameters.setAdditionalParamForNs(new HashMap<>());
    }

}