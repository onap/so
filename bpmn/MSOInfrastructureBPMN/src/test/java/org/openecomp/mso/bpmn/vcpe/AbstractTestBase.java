/*
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
package org.openecomp.mso.bpmn.vcpe;

import org.openecomp.mso.bpmn.common.WorkflowTest;

public class AbstractTestBase extends WorkflowTest {


    public static final String CUST = "SDN-ETHERNET-INTERNET";
    public static final String SVC = "123456789";
    public static final String INST = "MIS%252F1604%252F0026%252FSW_INTERNET";
    public static final String PARENT_INST = "MIS%252F1604%252F0027%252FSW_INTERNET";
    public static final String ARID = "arId-1";
    public static final String ARVERS = "1490627351232";

    public static final String DEC_INST = "MIS%2F1604%2F0026%2FSW_INTERNET";
    public static final String DEC_PARENT_INST = "MIS%2F1604%2F0027%2FSW_INTERNET";

    public static final String VAR_SUCCESS_IND = "SuccessIndicator";
    public static final String VAR_WFEX = "SavedWorkflowException1";
    public static final String VAR_RESP_CODE = "CMSO_ResponseCode";
    public static final String VAR_COMP_REQ = "CompleteMsoProcessRequest";
}
