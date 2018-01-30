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

package org.openecomp.mso.openstack.beans;

import static org.assertj.core.api.Assertions.assertThat;

import com.woorea.openstack.heat.model.Stack;
import java.io.IOException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class StackInfoTest {

    private static final String STACK_NAME = "stackNameTest";
    private static final String STACK_STATUS = "CREATE_COMPLETE";
    private static final String STACK_OUTPUT_KEY = "outputKeyTest";
    private static final String STACK_OUTPUT_VALUE = "outputValueTest";
    private static final String STACK_PARAM_KEY = "paramKeyTest";
    private static final String STACK_PARAM_VALUE = "paramValueTest";

    @Test
    public void setStatusNotFoundWhenStackIsNull() {
        StackInfo stackInfo = new StackInfo(null);
        assertThat(stackInfo.getStatus()).isEqualTo(HeatStatus.NOTFOUND);
        assertThat(stackInfo.getOutputs()).isEmpty();
        assertThat(stackInfo.getParameters()).isEmpty();
    }

    @Test
    public void createObjectWhenStackStatusIsNull() {
        StackInfo stackInfo = new StackInfo(createStackWithStatus(null));
        assertThat(stackInfo.getName()).isEqualTo(STACK_NAME);
        assertThat(stackInfo.getOutputs()).isEmpty();
        assertThat(stackInfo.getStatus()).isEqualTo(HeatStatus.INIT);
        assertThat(stackInfo.getParameters()).hasSize(1).containsEntry(STACK_PARAM_KEY, STACK_PARAM_VALUE);
    }

    @Test
    public void createObjectWhenStackStatusIsFound() {
        StackInfo stackInfo = new StackInfo(createStackWithStatus(STACK_STATUS));
        assertThat(stackInfo.getName()).isEqualTo(STACK_NAME);
        assertThat(stackInfo.getOutputs()).isEmpty();
        assertThat(stackInfo.getStatus()).isEqualTo(HeatStatus.CREATED);
        assertThat(stackInfo.getParameters()).hasSize(1).containsEntry(STACK_PARAM_KEY, STACK_PARAM_VALUE);
    }

    @Test
    public void createObjectWhenStackStatusIsUnknown() {
        StackInfo stackInfo = new StackInfo(createStackWithStatus("unknownStatus"));
        assertThat(stackInfo.getName()).isEqualTo(STACK_NAME);
        assertThat(stackInfo.getOutputs()).isEmpty();
        assertThat(stackInfo.getStatus()).isEqualTo(HeatStatus.UNKNOWN);
        assertThat(stackInfo.getParameters()).hasSize(1).containsEntry(STACK_PARAM_KEY, STACK_PARAM_VALUE);
    }

    @Test
    public void createStackWhenOutputsListIsNotNull() throws IOException {
        StackInfo stackInfo = new StackInfo(createStackWithOutputs());
        assertThat(stackInfo.getOutputs()).isNotEmpty().hasSize(1);
        assertThat(stackInfo.getOutputs()).hasSize(1).containsEntry(STACK_OUTPUT_KEY, STACK_OUTPUT_VALUE);
    }

    private Stack createStackWithStatus(String stackStatus) {
        Stack stack = new Stack();
        stack.setStackName(STACK_NAME);
        stack.setStackStatus(stackStatus);
        stack.getParameters().put(STACK_PARAM_KEY, STACK_PARAM_VALUE);
        return stack;
    }

    private Stack createStackWithOutputs() throws IOException {
        String json = "{\"outputs\":[{\"output_key\" : \"" + STACK_OUTPUT_KEY + "\", \"output_value\" : \""
                + STACK_OUTPUT_VALUE + "\" }]}";
        JsonNode node = new ObjectMapper().readTree(json);
        Stack stack = new ObjectMapper().readValue(node, Stack.class);
        return stack;
    }

}
