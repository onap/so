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

package org.onap.so.apihandler.recipe;

import java.io.IOException;

import org.onap.so.logger.MsoLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;



public class CamundaClientErrorHandler implements ResponseErrorHandler{
	
	 private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.APIH, CamundaClientErrorHandler.class);
	
	  @Override
	  public void handleError(ClientHttpResponse response) throws IOException {
		    
			msoLogger.debug(response.getBody().toString());
			//msoLogger.recordMetricEvent(startTime, MsoLogger.StatusCode.ERROR,
		//			MsoLogger.ResponseCode.CommunicationError, e.getMessage(), "BPMN", fullURL, null);
	  }

	  @Override
	  public boolean hasError(ClientHttpResponse response) throws IOException {
		        HttpStatus.Series series = response.getStatusCode().series();
		        return (HttpStatus.Series.CLIENT_ERROR.equals(series)
		                || HttpStatus.Series.SERVER_ERROR.equals(series));
	}
	    
}
