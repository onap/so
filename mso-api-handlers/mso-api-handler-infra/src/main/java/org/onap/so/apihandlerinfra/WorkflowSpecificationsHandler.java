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
package org.onap.so.apihandlerinfra;

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;

import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;

import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.apihandlerinfra.workflowspecificationbeans.WorkflowSpecifications;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.MessageEnum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("onap/so/infra/workflowSpecifications")
@Api(value="onap/so/infra/workflowSpecifications",description="Queries of Workflow Specifications")
@Component
public class WorkflowSpecificationsHandler {

    @Autowired
	private ResponseBuilder builder;
	
    @Path("/{version:[vV]1}/workflows")
    @GET
    @ApiOperation(value="Finds Workflow Specifications",response=Response.class)
    @Transactional
    public Response queryFilters (@QueryParam("vnfModelVersionId") String vnfModelVersionId,
    								@PathParam("version") String version) throws Exception {
    	    	
    	String apiVersion = version.substring(1);
		
		ObjectMapper mapper1 = new ObjectMapper();		
		mapper1.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		//Replace with Catalog DB Query
		WorkflowSpecifications workflowSpecifications = mapper1.readValue(new String(Files.readAllBytes(Paths.get("src/test/resources/__files/WorkflowSpecifications.json"))), WorkflowSpecifications.class);
       
		String jsonResponse = null;
		try {
			ObjectMapper mapper = new ObjectMapper();			
			jsonResponse = mapper.writeValueAsString(workflowSpecifications);
		}
		catch (JsonProcessingException e) {
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError).build();
			ValidateException validateException = new ValidateException.Builder("Mapping of request to JSON object failed : " + e.getMessage(),
					HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo).build();
			throw validateException;
		}
		
		return builder.buildResponse(HttpStatus.SC_OK, "", jsonResponse, apiVersion);
    }    
}
