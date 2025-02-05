/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Bell Canada
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

package org.onap.so.adapters.catalogdb.rest;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.onap.so.db.catalog.beans.BuildingBlockRollback;
import org.onap.so.db.catalog.data.repository.BuildingBlockRollbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@OpenAPIDefinition(info = @Info(title = "/v1", description = "model"))
@Path("/v1/buildingBlockRollback")
@Component
public class BuildingBlockRollbackRestImpl {

    @Autowired
    private BuildingBlockRollbackRepository bbRollbackRepo;

    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Transactional(readOnly = true)
    public BuildingBlockRollback findService(@PathParam("id") Integer id) {
        return bbRollbackRepo.findOneById(id);
    }

    @GET
    @Operation(description = "Look up BuildingBlock Rollback List", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = BuildingBlockRollback.class)))))
    @Produces({MediaType.APPLICATION_JSON})
    @Transactional(readOnly = true)
    public List<BuildingBlockRollback> getBBRollbackList() {
        return bbRollbackRepo.findAll();
    }
}
