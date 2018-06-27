/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.adapters.vnf.vmfm;


import java.util.Map;
import javax.xml.ws.Holder;
import org.openecomp.mso.entity.MsoRequest;

// ToscaResourceInstaller

/**
 * Represents the VNFM adapter functionality with clear interfaces.
 *
 * @see MsoVnfmAdapterImpl implements that #MsoVnfmAdapter API
 *
 */
public class VnfmVfModuleManager {

    /**
     *
     * @param vnfId
     * @param vfModuleId
     * @param inputs
     * @param msoRequest
     * @param rollback
     * @return the identifier of the scaling step in a given scaling aspect separated by _. Ex. myScalingAspect_3
     */
    public String createVfModule(String vnfId, String vfModuleId, Map<String,String> inputs, MsoRequest msoRequest, Rollback rollback) {
        return "myScalingAspect_3";
    }

    public void updateVfModule(String vnfId, String vfModuleId, Map<String, String> inputs, MsoRequest msoRequest, Rollback rollback) {

    }

    public void deleteVfModule(String vnfId, String vfModuleId, MsoRequest msoRequest) {

    }
}