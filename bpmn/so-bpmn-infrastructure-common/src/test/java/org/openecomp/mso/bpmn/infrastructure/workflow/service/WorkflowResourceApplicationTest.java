/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.mso.bpmn.infrastructure.workflow.service;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;

public class WorkflowResourceApplicationTest {

    @Test
    public void test() throws Exception {
    	WorkflowResourceApplication workflowResourceApp = new WorkflowResourceApplication();
    	Set<Class<?>> classes = workflowResourceApp.getClasses();
    	assertEquals(0, classes.size());
    	Set<Object> singleton = workflowResourceApp.getSingletons();
    	assertEquals(3, singleton.size());
    }
}