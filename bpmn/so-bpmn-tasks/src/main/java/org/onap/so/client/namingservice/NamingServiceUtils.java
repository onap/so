/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.namingservice;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NamingServiceUtils {

    private static String errorMessages = "Policy name is not present, or Onap name was not generated! ";

    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private ExtractPojosForBB extractPojosForBB;

    public void checkVpnBondingService(BuildingBlockExecution execution) {
        execution.setVariable("isVpnBondingService", false);
        boolean isNamingPolicyAndOnapGeneratedNaming = false;
        checkBondingAndInfrastureVpn(execution);
        boolean isBondingAndInsfrastructureVpn = execution.getVariable("isBondingAndInsfrastructureVpn");
        if (isBondingAndInsfrastructureVpn) {
            checkNamingPolicyAndOnapGeneratedNaming(execution);
            isNamingPolicyAndOnapGeneratedNaming = execution.getVariable("isNamingPolicyAndOnapGeneratedNaming");
        }
        if (isBondingAndInsfrastructureVpn && isNamingPolicyAndOnapGeneratedNaming) {
            execution.setVariable("isVpnBondingService", true);
        }
    }

    public void checkBondingAndInfrastureVpn(BuildingBlockExecution execution) {
        execution.setVariable("isBondingAndInsfrastructureVpn", false);
        try {
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            if ("bonding".equalsIgnoreCase(serviceInstance.getModelInfoServiceInstance().getServiceType())
                    && "infrastructure-vpn"
                            .equalsIgnoreCase(serviceInstance.getModelInfoServiceInstance().getServiceRole())) {
                execution.setVariable("isBondingAndInsfrastructureVpn", true);
            }
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    public void checkNamingPolicyAndOnapGeneratedNaming(BuildingBlockExecution execution) {
        execution.setVariable("isNamingPolicyAndOnapGeneratedNaming", false);
        try {
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            if (serviceInstance.getModelInfoServiceInstance().getNamingPolicy() != null
                    && !serviceInstance.getModelInfoServiceInstance().getNamingPolicy().isEmpty()
                    && serviceInstance.getModelInfoServiceInstance().getOnapGeneratedNaming() != null
                    && serviceInstance.getModelInfoServiceInstance().getOnapGeneratedNaming() == true) {
                execution.setVariable("isNamingPolicyAndOnapGeneratedNaming", true);
            } else {
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, errorMessages);
            }
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }
}
