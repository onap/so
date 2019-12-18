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

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.AAI_CONTAINS_INFO_ABOUT_PNF;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_CORRELATION_ID;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.InjectExecution;
import org.onap.so.bpmn.common.scripts.ExceptionUtil;
import org.onap.so.bpmn.infrastructure.pnf.management.PnfManagement;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.serviceinstancebeans.Pnfs;
import org.onap.so.serviceinstancebeans.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation of "Check AAI for pnf_correlation_id" task in CreateAndActivatePnfResource.bpmn
 * <p>
 * Inputs: - pnfCorrelationId - String
 * <p>
 * Outputs: - aaiContainsInfoAboutPnf - local Boolean
 */
@Component
public class CheckAaiForPnfCorrelationIdDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(CheckAaiForPnfCorrelationIdDelegate.class);

    private PnfManagement pnfManagement;
    @Autowired
    ExceptionBuilder exceptionUtil;
    @Autowired
    private ExtractPojosForBB extractPojosForBB;

    @Autowired
    public void setPnfManagement(PnfManagement pnfManagement) {
        this.pnfManagement = pnfManagement;
    }

    public void doSomething(BuildingBlockExecution execution) {
        try {
            Pnf pnf = extractPojosForBB.extractByKey(execution, ResourceKey.PNF);
            logger.error(pnf.toString());
            String pnfCorrelationId = pnf.getPnfName();
            if (pnfCorrelationId == null) {
                logger.error("PnfCorrelationId is null");
                // new ExceptionUtil().buildAndThrowWorkflowException(execution, 500, PNF_CORRELATION_ID + " is not
                // set");
            }
            try {
                boolean isEntry = pnfManagement.getEntryFor(pnfCorrelationId).isPresent();
                logger.debug("AAI entry is found for pnf correlation id {}: {}", PNF_CORRELATION_ID, isEntry);
                execution.setVariable(AAI_CONTAINS_INFO_ABOUT_PNF, isEntry);
            } catch (IOException e) {
                logger.error("Exception in check AAI for pnf_correlation_id execution", e);
                // new ExceptionUtil().buildAndThrowWorkflowException(execution, 9999, e.getMessage());
            }
        } catch (BBObjectNotFoundException e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void execute(DelegateExecution execution) {
        BuildingBlockExecution buildingBlockExecution =
                (BuildingBlockExecution) execution.getVariable("gBuildingBlockExecution");
        // String pnfCorrelationId = (String) execution.getVariable(PNF_CORRELATION_ID);
        try {
            Pnfs pnf = extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.PNF);
            logger.error(pnf.toString());
            String pnfCorrelationId = pnf.getInstanceName();
            logger.error("======================BB CALLED======================");
            if (pnfCorrelationId == null) {
                new ExceptionUtil().buildAndThrowWorkflowException(execution, 500, PNF_CORRELATION_ID + " is not set");
            }
            try {
                boolean isEntry = pnfManagement.getEntryFor(pnfCorrelationId).isPresent();
                logger.debug("AAI entry is found for pnf correlation id {}: {}", PNF_CORRELATION_ID, isEntry);
                execution.setVariableLocal(AAI_CONTAINS_INFO_ABOUT_PNF, isEntry);
            } catch (IOException e) {
                logger.error("Exception in check AAI for pnf_correlation_id execution", e);
                new ExceptionUtil().buildAndThrowWorkflowException(execution, 9999, e.getMessage());
            }
        } catch (BBObjectNotFoundException e) {
            logger.error(e.toString());
        }
    }
}
