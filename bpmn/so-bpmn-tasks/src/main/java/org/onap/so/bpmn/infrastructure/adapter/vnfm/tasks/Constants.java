/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

import static com.google.common.collect.Sets.newHashSet;
import java.util.Set;
import org.onap.vnfmadapter.v1.model.OperationStateEnum;
import org.onap.vnfmadapter.v1.model.OperationStatusRetrievalStatusEnum;

/**
 * @author waqas.ikram@est.tech
 */
public class Constants {

  public static final String CREATE_VNF_REQUEST_PARAM_NAME = "createVnfRequest";
  public static final String CREATE_VNF_RESPONSE_PARAM_NAME = "createVnfResponse";
  public static final String INPUT_PARAMETER = "inputParameter";
  public static final String DELETE_VNF_RESPONSE_PARAM_NAME = "deleteVnfResponse";


  public static final String DOT = ".";
  public static final String UNDERSCORE = "_";
  public static final String SPACE = "\\s+";

  public static final String VNFM_ADAPTER_DEFAULT_URL = "http://so-vnfm-adapter.onap:9092/so/vnfm-adapter/v1/";
  public static final String VNFM_ADAPTER_DEFAULT_AUTH = "Basic dm5mbTpwYXNzd29yZDEk";

  public static final String FORWARD_SLASH = "/";
  public static final String PRELOAD_VNFS_URL = "/restconf/config/VNF-API:preload-vnfs/vnf-preload-list/";


  public static final Set<OperationStateEnum> OPERATION_FINISHED_STATES =
      newHashSet(OperationStateEnum.COMPLETED, OperationStateEnum.FAILED, OperationStateEnum.ROLLED_BACK);

  public static final Set<OperationStatusRetrievalStatusEnum> OPERATION_RETRIEVAL_STATES = newHashSet(
      OperationStatusRetrievalStatusEnum.STATUS_FOUND, OperationStatusRetrievalStatusEnum.WAITING_FOR_STATUS);

  public static final String OPERATION_STATUS_PARAM_NAME = "operationStatus";

  private Constants() {}
}
