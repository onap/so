/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.apihandlerinfra.tenantisolation;

import java.io.IOException;
import java.util.List;
// import java.util.ServiceLoader;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.inject.Provider;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandlerinfra.MsoException;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.apihandlerinfra.tenantisolationbeans.Action;
import org.onap.so.apihandlerinfra.tenantisolationbeans.Distribution;
import org.onap.so.apihandlerinfra.tenantisolationbeans.Status;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.onap.so.serviceinstancebeans.RequestError;
import org.onap.so.serviceinstancebeans.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Component
@Path("/onap/so/infra/modelDistributions")
@OpenAPIDefinition(
        info = @Info(title = "/onap/so/infra/modelDistributions", description = "API Requests for Model Distributions"))
public class ModelDistributionRequest {

    private static Logger logger = LoggerFactory.getLogger(ModelDistributionRequest.class);
    @Autowired
    private Provider<TenantIsolationRunnable> tenantIsolationRunnable;
    // private TenantIsolationRunnable tenantIsolationRunnable;

    @PATCH
    @Path("/{version:[vV][1]}/distributions/{distributionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update model distribution status", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response updateModelDistributionStatus(String requestJSON, @PathParam("version") String version,
            @PathParam("distributionId") String distributionId) throws ApiException {
        Distribution distributionRequest;

        try {
            ObjectMapper mapper = new ObjectMapper();
            distributionRequest = mapper.readValue(requestJSON, Distribution.class);
        } catch (IOException e) {
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError)
                            .build();


            ValidateException validateException =
                    new ValidateException.Builder("Mapping of request to JSON object failed.  " + e.getMessage(),
                            HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                                    .errorInfo(errorLoggerInfo).build();
            throw validateException;

        }

        try {
            parse(distributionRequest);
        } catch (ValidationException e) {

            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError)
                            .build();


            ValidateException validateException =
                    new ValidateException.Builder(e.getMessage(), HttpStatus.SC_BAD_REQUEST,
                            ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo).build();
            throw validateException;
        }

        CloudOrchestrationRequest cor = new CloudOrchestrationRequest();
        cor.setDistribution(distributionRequest);
        cor.setDistributionId(distributionId);

        TenantIsolationRunnable runnable = tenantIsolationRunnable.get();
        runnable.run(Action.distributionStatus, null, cor, null);
        // tenantIsolationRunnable.run(Action.distributionStatus, null, cor, null);

        return Response.ok().build();
    }

    private void parse(Distribution distributionRequest) throws ValidationException {
        if (distributionRequest.getStatus() == null) {
            throw new ValidationException("status");
        }

        if (StringUtils.isBlank(distributionRequest.getErrorReason())
                && Status.DISTRIBUTION_COMPLETE_ERROR.equals(distributionRequest.getStatus())) {
            throw new ValidationException("errorReason");
        }
    }

    private Response buildServiceErrorResponse(int httpResponseCode, MsoException exceptionType, String text,
            String messageId, List<String> variables) throws ApiException {
        RequestError re = new RequestError();
        ServiceException se = new ServiceException();
        se.setMessageId(messageId);
        se.setText(text);
        if (variables != null) {
            for (String variable : variables) {
                se.getVariables().add(variable);
            }
        }
        re.setServiceException(se);

        String requestErrorStr;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(Include.NON_DEFAULT);
            requestErrorStr = mapper.writeValueAsString(re);
        } catch (JsonProcessingException e) {

            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_VALIDATION_ERROR, ErrorCode.DataError).build();


            ValidateException validateException =
                    new ValidateException.Builder("Mapping of request to JSON object failed.  " + e.getMessage(),
                            HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                                    .errorInfo(errorLoggerInfo).build();
            throw validateException;
        }

        return Response.status(httpResponseCode).entity(requestErrorStr).build();
    }
}
