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

import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.onap.so.db.catalog.data.repository.VnfCustomizationRepository;
import org.onap.so.rest.catalog.beans.Service;
import org.onap.so.rest.catalog.beans.Vnf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@OpenAPIDefinition(info = @Info(title = "/v1/services/{modelUUID}/vnfs", description = "model"))
@Path("/v1/services/{modelUUID}/vnfs")
@Component
public class VnfRestImpl {

    @Autowired
    private ServiceRepository serviceRepo;

    @Autowired
    private ServiceMapper serviceMapper;

    @Autowired
    private VnfMapper vnfMapper;

    @Autowired
    private VnfCustomizationRepository vnfCustRepo;

    @GET
    @Operation(description = "Find a VNF model contained within a service", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Vnf.class)))))
    @Path("/{modelCustomizationUUID}")
    @Produces({MediaType.APPLICATION_JSON})
    @Transactional(readOnly = true)
    public Vnf findService(@PathParam("modelUUID") String serviceModelUUID,
            @PathParam("modelCustomizationUUID") String modelCustomizationUUID, @QueryParam("depth") int depth) {
        org.onap.so.db.catalog.beans.Service service = serviceRepo.findOneByModelUUID(serviceModelUUID);
        if (service.getVnfCustomizations() == null || service.getVnfCustomizations().isEmpty()) {
            throw new WebApplicationException("Vnf Not Found", 404);
        }
        List<VnfResourceCustomization> vnfCustom = service.getVnfCustomizations().stream()
                .filter(vnfCust -> vnfCust.getModelCustomizationUUID().equals(modelCustomizationUUID))
                .collect(Collectors.toList());
        if (vnfCustom.isEmpty()) {
            return null;
        } else if (vnfCustom.size() > 1) {
            throw new RuntimeException(
                    "More than one Vnf model returned with model Customization UUID: " + modelCustomizationUUID);
        }
        return serviceMapper.mapVnf(vnfCustom.get(0), depth);
    }

    @PUT
    @Operation(description = "Update a VNF model contained within a service", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Vnf.class)))))
    @Path("/{modelCustomizationUUID}")
    @Produces({MediaType.APPLICATION_JSON})
    @Transactional
    public Response findService(@PathParam("modelUUID") String serviceModelUUID,
            @PathParam("modelCustomizationUUID") String modelCustomizationUUID, Vnf vnf) {
        org.onap.so.db.catalog.beans.Service service = serviceRepo.findOneByModelUUID(serviceModelUUID);
        List<VnfResourceCustomization> vnfCustom = service.getVnfCustomizations().stream()
                .filter(vnfCust -> vnfCust.getModelCustomizationUUID().equals(modelCustomizationUUID))
                .collect(Collectors.toList());
        if (vnfCustom.isEmpty()) {
            throw new RuntimeException("No Vnf Found");
        } else if (vnfCustom.size() > 1) {
            throw new RuntimeException(
                    "More than one Vnf model returned with model Customization UUID: " + modelCustomizationUUID);
        }
        VnfResourceCustomization vnfCust = vnfMapper.mapVnf(vnfCustom.get(0), vnf);
        vnfCustRepo.save(vnfCust);
        return Response.ok().build();
    }

}

