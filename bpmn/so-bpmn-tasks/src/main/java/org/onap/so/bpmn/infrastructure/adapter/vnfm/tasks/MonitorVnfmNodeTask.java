/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Ericsson. All rights reserved.
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
package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks;

import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.CREATE_VNF_NODE_STATUS;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.DELETE_VNF_NODE_STATUS;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.VNF_ASSIGNED;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.VNF_CREATED;
import static org.onap.so.bpmn.servicedecomposition.entities.ResourceKey.GENERIC_VNF_ID;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 
 * @author Lathishbabu Ganesan (lathishbabu.ganesan@est.tech)
 *
 */
@Component
public class MonitorVnfmNodeTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorVnfmNodeTask.class);

  private final ExtractPojosForBB extractPojosForBB;
  private final ExceptionBuilder exceptionUtil;

  @Autowired
  public MonitorVnfmNodeTask(final ExtractPojosForBB extractPojosForBB, final ExceptionBuilder exceptionUtil) {
    this.exceptionUtil = exceptionUtil;
    this.extractPojosForBB = extractPojosForBB;
  }

  /**
   * Check the final status of vnf in A&AI
   * 
   * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
   */
  public void getNodeStatus(final BuildingBlockExecution execution) {
    try {
      LOGGER.debug("Executing getNodeStatus  ...");
      final GenericVnf vnf = extractPojosForBB.extractByKey(execution, GENERIC_VNF_ID);
      String orchestrationStatus = vnf.getOrchestrationStatus();
      LOGGER.debug("Orchestration Status in AAI {}", orchestrationStatus);
      execution.setVariable(CREATE_VNF_NODE_STATUS, VNF_CREATED.equalsIgnoreCase(orchestrationStatus));
      execution.setVariable(DELETE_VNF_NODE_STATUS, VNF_ASSIGNED.equalsIgnoreCase(orchestrationStatus));
    } catch (final Exception exception) {
      LOGGER.error("Unable to get vnf from AAI", exception);
      exceptionUtil.buildAndThrowWorkflowException(execution, 1220, exception);
    }
  }

  /**
   * Log and throw exception on timeout for job status
   * 
   * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
   */
  public void timeOutLogFailue(final BuildingBlockExecution execution) {
    final String message = "Node operation time out";
    LOGGER.error(message);
    exceptionUtil.buildAndThrowWorkflowException(execution, 1221, message);
  }
}
