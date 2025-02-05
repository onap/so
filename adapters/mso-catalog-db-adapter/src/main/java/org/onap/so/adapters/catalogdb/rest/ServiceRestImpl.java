/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.catalogdb.rest;

import java.util.ArrayList;
import java.util.List;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.onap.so.rest.catalog.beans.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;


@OpenAPIDefinition(info = @Info(title = "/v1", description = "model"))
@Path("/v1/services")
@Component
public class ServiceRestImpl {

    @Autowired
    private ServiceRepository serviceRepo;

    @Autowired
    private ServiceMapper serviceMapper;


    @GET
    @Path("/{modelUUID}")
    @Produces({MediaType.APPLICATION_JSON})
    @Transactional(readOnly = true)
    public Service findService(@PathParam("modelUUID") String modelUUID, @QueryParam("depth") int depth) {
        org.onap.so.db.catalog.beans.Service service = serviceRepo.findOneByModelUUID(modelUUID);
        if (service == null) {
            throw new CatalogEntityNotFoundException("Unable to find Service " + modelUUID);
        }
        return serviceMapper.mapService(service, depth);
    }

    @GET
    @Operation(description = "Find Service Models", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Service.class)))))
    @Produces({MediaType.APPLICATION_JSON})
    @Transactional(readOnly = true)
    public List<Service> queryServices(
            @Parameter(description = "modelName", required = false) @QueryParam("modelName") String modelName,
            @Parameter(description = "distributionStatus",
                    required = false) @QueryParam("distributionStatus") String distributionStatus,
            @Parameter(description = "depth", required = false) @QueryParam("depth") int depth) {
        List<Service> services = new ArrayList<>();
        List<org.onap.so.db.catalog.beans.Service> serviceFromDB;
        if (!Strings.isNullOrEmpty(modelName) && !Strings.isNullOrEmpty(distributionStatus)) {
            serviceFromDB = serviceRepo.findByModelNameAndDistrobutionStatus(modelName, distributionStatus);
        } else if (!Strings.isNullOrEmpty(modelName)) {
            serviceFromDB = serviceRepo.findByModelName(modelName);
        } else {
            serviceFromDB = serviceRepo.findAll();
        }
        serviceFromDB.stream().forEach(serviceDB -> services.add(serviceMapper.mapService(serviceDB, depth)));
        return services;
    }
}
