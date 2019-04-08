/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
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

package org.onap.so.openstack.mappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.onap.so.openstack.beans.HeatStatus;
import org.onap.so.openstack.beans.StackInfo;
import com.woorea.openstack.heat.model.Stack;
import com.woorea.openstack.heat.model.Stack.Output;

public class StackInfoMapper {

    private final Stack stack;
    private final Map<String, HeatStatus> heatStatusMap = new HashMap<>();

    public StackInfoMapper(Stack stack) {
        this.stack = stack;
        configureHeatStatusMap();
    }

    public StackInfo map() {
        final StackInfo info = new StackInfo();
        if (stack == null) {
            info.setStatus(HeatStatus.NOTFOUND);
        } else {
            info.setName(stack.getStackName());
            info.setCanonicalName(stack.getStackName() + "/" + stack.getId());
            info.setStatus(this.mapStatus(stack.getStackStatus()));

            info.setStatusMessage(stack.getStackStatusReason());

            Optional<Map<String, Object>> result = this.mapOutputToMap(stack.getOutputs());
            if (result.isPresent()) {
                info.setOutputs(result.get());
            }

            info.setParameters(stack.getParameters());
        }

        return info;
    }

    protected HeatStatus mapStatus(String status) {
        final HeatStatus result;
        if (status == null) {
            result = HeatStatus.INIT;
        } else {
            result = heatStatusMap.getOrDefault(status, HeatStatus.UNKNOWN);
        }

        return result;
    }

    protected Optional<Map<String, Object>> mapOutputToMap(List<Output> outputs) {
        Optional<Map<String, Object>> result = Optional.empty();
        if (outputs != null) {
            final HashMap<String, Object> map = new HashMap<>();
            for (Output output : outputs) {
                map.put(output.getOutputKey(), output.getOutputValue());
            }
            result = Optional.of(map);
        }

        return result;
    }

    private void configureHeatStatusMap() {
        heatStatusMap.put("CREATE_IN_PROGRESS", HeatStatus.BUILDING);
        heatStatusMap.put("CREATE_COMPLETE", HeatStatus.CREATED);
        heatStatusMap.put("CREATE_FAILED", HeatStatus.FAILED);
        heatStatusMap.put("DELETE_IN_PROGRESS", HeatStatus.DELETING);
        heatStatusMap.put("DELETE_COMPLETE", HeatStatus.NOTFOUND);
        heatStatusMap.put("DELETE_FAILED", HeatStatus.FAILED);
        heatStatusMap.put("UPDATE_IN_PROGRESS", HeatStatus.UPDATING);
        heatStatusMap.put("UPDATE_FAILED", HeatStatus.FAILED);
        heatStatusMap.put("UPDATE_COMPLETE", HeatStatus.UPDATED);
    }
}
