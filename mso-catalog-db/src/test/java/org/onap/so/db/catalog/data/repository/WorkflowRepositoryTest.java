/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.so.db.catalog.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.onap.so.db.catalog.BaseTest;
import org.onap.so.db.catalog.beans.PnfResource;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.Workflow;
import org.onap.so.db.catalog.exceptions.NoEntityFoundException;
import org.springframework.beans.factory.annotation.Autowired;

public class WorkflowRepositoryTest extends BaseTest {

    @Autowired
    private WorkflowRepository workflowRepository;

    @Test
    public void findByArtifactUuid_ValidUuid_ExpectedOutput() throws Exception {
        Workflow workflow = workflowRepository
            .findByArtifactUUID("5b0c4322-643d-4c9f-b184-4516049e99b1");
           
        assertEquals("artifactName", "testingWorkflow", workflow.getArtifactName());
    }
    
}
