/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.web.exceptions;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.onap.so.logger.MsoAlarmLogger;
import org.onap.so.logger.MsoLogger;

public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

	private static MsoLogger logger = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL, RuntimeExceptionMapper.class);
	private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger();
	@Override
	public Response toResponse(RuntimeException exception) {
		
		if (exception instanceof NotFoundException) {
			return Response.status(Status.NOT_FOUND).build();
		} else {
			alarmLogger.sendAlarm("MsoApplicationError", MsoAlarmLogger.CRITICAL, exception.getMessage());
			logger.error(exception);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ExceptionResponse("Unexpected Internal Exception")).build();
		}
	}
}
