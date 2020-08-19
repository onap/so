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

package org.onap.so.db.catalog.data.repository;

import java.util.List;
import org.onap.so.db.catalog.beans.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "workflow", path = "workflow")
public interface WorkflowRepository extends JpaRepository<Workflow, Integer> {

    Workflow findByArtifactUUID(String artifactUUID);

    List<Workflow> findBySource(String source);

    List<Workflow> findByResourceTarget(String resourceTarget);

    List<Workflow> findByOperationName(String operationName);

    /**
     * Used to fetch the @{link Workflow} by the Model UUID.
     *
     * This operation is required by {@link org.onap.so.db.catalog.client.CatalogDbClient} to provide Workflow based on
     * vnf model UUID without projection.
     *
     * @param vnfResourceModelUUID UUID
     * @return List of Workflow
     */
    @Query(value = "select b.* from vnf_resource_to_workflow a join workflow b where a.WORKFLOW_ID = b.ID and a.VNF_RESOURCE_MODEL_UUID = ?1",
            nativeQuery = true)
    List<Workflow> findWorkflowByVnfModelUUID(String vnfResourceModelUUID);

    /**
     * Used to fetch the @{link Workflow} by the Pnf Model UUID.
     *
     * This operation is required by {@link org.onap.so.db.catalog.client.CatalogDbClient} to provide Workflow based on
     * pnf model UUID without projection.
     *
     * @param pnfResourceModelUUID UUID
     * @return List of Workflow
     */
    @Query(value = "select b.* from pnf_resource_to_workflow a join workflow b where a.WORKFLOW_ID = b.ID and a.PNF_RESOURCE_MODEL_UUID = ?1",
            nativeQuery = true)
    List<Workflow> findWorkflowByPnfModelUUID(String pnfResourceModelUUID);
}
