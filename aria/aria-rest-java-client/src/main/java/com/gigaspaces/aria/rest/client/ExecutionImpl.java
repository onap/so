/*
 * ============LICENSE_START===================================================
 * Copyright (c) 2017 Cloudify.co.  All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END====================================================
*/
package com.gigaspaces.aria.rest.client;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by DeWayne on 7/17/2017.
 */
public class ExecutionImpl implements Execution {
    @JsonProperty("execution_id")
    int execution_id;
    @JsonProperty("workflow_name")
    String workflow_name;
    @JsonProperty("service_template_name")
    String service_template_name;
    @JsonProperty("service_name")
    String service_name;
    String status;

    public int getExecutionId() {
        return execution_id;
    }
    public String getWorkflowName() {
        return workflow_name;
    }

    public String getServiceTemplateName() {
        return service_template_name;
    }

    public String getServiceName() {
        return service_name;
    }

    public String getStatus() {
        return status;
    }
}
