/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.infrastructure.aai.tasks;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAIPnfResources;
import org.onap.so.client.orchestration.AAIVnfResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AAIFlagTasks {
    private static final Logger logger = LoggerFactory.getLogger(AAIFlagTasks.class);


    @Autowired
    private AAIVnfResources aaiVnfResources;

    @Autowired
    private AAIPnfResources aaiPnfResources;
    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private ExtractPojosForBB extractPojosForBB;

    public void checkVnfInMaintFlag(BuildingBlockExecution execution) {
        boolean inMaint = false;
        try {
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            String vnfId = vnf.getVnfId();
            inMaint = aaiVnfResources.checkInMaintFlag(vnfId);
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
        if (inMaint) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "VNF is in maintenance in A&AI");
        }
    }

    public void modifyVnfInMaintFlag(BuildingBlockExecution execution, boolean inMaint) {
        try {
            GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);

            GenericVnf copiedGenericVnf = genericVnf.shallowCopyId();

            copiedGenericVnf.setInMaint(inMaint);
            genericVnf.setInMaint(inMaint);

            aaiVnfResources.updateObjectVnf(copiedGenericVnf);
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    public void checkPnfInMaintFlag(BuildingBlockExecution execution) {
        boolean inMaint = false;
        try {
            Pnf pnf = extractPojosForBB.extractByKey(execution, ResourceKey.PNF);
            String pnfName = pnf.getPnfName();
            inMaint = aaiPnfResources.checkInMaintFlag(pnfName);
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
        if (inMaint) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "PNF is already in maintenance in A&AI");
        }
    }

    public void modifyPnfInMaintFlag(BuildingBlockExecution execution, boolean inMaint) {
        try {
            Pnf pnf = extractPojosForBB.extractByKey(execution, ResourceKey.PNF);
            logger.info("In modifyPnfInMaintFlag pnfname: {}", pnf.getPnfName());
            Pnf copiedPnf = pnf.shallowCopyId();
            copiedPnf.setPnfName(pnf.getPnfName());

            copiedPnf.setInMaint(inMaint);
            pnf.setInMaint(inMaint);
            logger.info("In modifyPnfInMaintFlag if block pnfInMaint: {}, copiedPnfInMaint: {}", pnf.isInMaint(),
                    copiedPnf.isInMaint());
            aaiPnfResources.updateObjectPnf(copiedPnf);

        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }


    public void checkVnfClosedLoopDisabledFlag(BuildingBlockExecution execution) {
        boolean isClosedLoopDisabled = false;
        try {
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            String vnfId = vnf.getVnfId();
            isClosedLoopDisabled = aaiVnfResources.checkVnfClosedLoopDisabledFlag(vnfId);
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
        if (isClosedLoopDisabled) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "VNF Close Loop Disabled in A&AI");
        }
    }

    public void modifyVnfClosedLoopDisabledFlag(BuildingBlockExecution execution, boolean closedLoopDisabled) {
        try {
            GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);

            GenericVnf copiedGenericVnf = genericVnf.shallowCopyId();
            copiedGenericVnf.setClosedLoopDisabled(closedLoopDisabled);
            genericVnf.setClosedLoopDisabled(closedLoopDisabled);

            aaiVnfResources.updateObjectVnf(copiedGenericVnf);
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    public void checkVnfPserversLockedFlag(BuildingBlockExecution execution) {
        boolean inPserversLocked = false;
        try {
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            String vnfId = vnf.getVnfId();
            inPserversLocked = aaiVnfResources.checkVnfPserversLockedFlag(vnfId);
        } catch (Exception ex) {
            logger.warn("Exception on checking pservers: {}", ex.getMessage());
        }
        if (inPserversLocked) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "VNF PServers in Locked in A&AI");
        }
    }
}
