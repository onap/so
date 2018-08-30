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

package org.onap.so.logging.jaxrs.filter;


import org.apache.commons.io.IOUtils;
import org.onap.so.logger.MsoLogger;
import org.onap.so.utils.TargetEntity;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import java.io.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Component
public class JaxRsClientLogging implements ClientRequestFilter,ClientResponseFilter {
	
	private static MsoLogger logger = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA,JaxRsClientLogging.class);

	private TargetEntity targetEntity;

	public void setTargetService(TargetEntity targetEntity){
	    this.targetEntity = targetEntity;
    }

	@Override
	public void filter(ClientRequestContext clientRequest) {
        try{
            MultivaluedMap<String, Object> headers = clientRequest.getHeaders();
            
            
            Instant instant = Instant.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX" )
                .withLocale( Locale.US )
                .withZone( ZoneId.systemDefault() );

            String requestId = MDC.get(MsoLogger.REQUEST_ID);
            if(requestId == null || requestId.isEmpty()){
                requestId = UUID.randomUUID().toString();
                logger.warnSimple(clientRequest.getUri().getPath(),"Could not Find Request ID Generating New One");
            }

			MDC.put(MsoLogger.METRIC_BEGIN_TIME, formatter.format(instant));
			MDC.put(MsoLogger.METRIC_START_TIME, String.valueOf(System.currentTimeMillis()));
            MDC.put(MsoLogger.REQUEST_ID,requestId);	
            MDC.put(MsoLogger.TARGETSERVICENAME, clientRequest.getUri().toString());
        } catch (Exception e) {
			logger.warnSimple("Error in incoming JAX-RS Inteceptor", e);
		}
	}	


	@Override
	public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {

        try {
			Instant instant = Instant.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX" )
			        .withLocale( Locale.US )
			        .withZone( ZoneId.systemDefault() );
			String startTime= MDC.get(MsoLogger.METRIC_START_TIME);

			long elapsedTime = System.currentTimeMillis()-Long.parseLong(startTime);
			String statusCode;
			if(Response.Status.Family.familyOf(responseContext.getStatus()).equals(Response.Status.Family.SUCCESSFUL)){		
			    statusCode=MsoLogger.COMPLETE;
			}else{							
				statusCode=MsoLogger.StatusCode.ERROR.toString();				
			}
			MultivaluedMap<String, String> headers = responseContext.getHeaders();

			String partnerName =  headers.getFirst(MsoLogger.HEADER_FROM_APP_ID );
			if(partnerName == null || partnerName.isEmpty())
				partnerName="UNKNOWN";
			MDC.put(MsoLogger.RESPONSEDESC,getStringFromInputStream(responseContext));
			MDC.put(MsoLogger.STATUSCODE, statusCode);
			MDC.put(MsoLogger.RESPONSECODE,String.valueOf(responseContext.getStatus()));
			MDC.put(MsoLogger.METRIC_TIMER, String.valueOf(elapsedTime));
			MDC.put(MsoLogger.METRIC_END_TIME,formatter.format(instant));
			MDC.put(MsoLogger.PARTNERNAME,partnerName);	
            MDC.put(MsoLogger.TARGETENTITY, targetEntity.toString());
			logger.recordMetricEvent();			
		} catch ( Exception e) {
			logger.warnSimple("Error in outgoing JAX-RS Inteceptor", e);
		}
	}

    private static String getStringFromInputStream(ClientResponseContext clientResponseContext) {

	    InputStream is = clientResponseContext.getEntityStream();
	    ByteArrayOutputStream boas = new ByteArrayOutputStream();

        try {
            IOUtils.copy(is,boas);
            InputStream copiedStream = new ByteArrayInputStream(boas.toByteArray());
            clientResponseContext.setEntityStream(copiedStream);
            return boas.toString();

        } catch (IOException e) {
            logger.warnSimple("Failed to read response body", e);
        }
        return "Unable to read input stream";
    }
}
