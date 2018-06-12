package org.openecomp.mso.web.exceptions;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;

public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

	private static MsoLogger logger = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL, RuntimeExceptionMapper.class);
	private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger();
	@Override
	public Response toResponse(RuntimeException exception) {
		
		logger.error(exception);
		alarmLogger.sendAlarm("MsoApplicationError", MsoAlarmLogger.CRITICAL, exception.getMessage());
		if (exception instanceof NotFoundException) {
			return Response.status(Status.NOT_FOUND).build();
		} else {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ExceptionResponse("Unexpected Internal Exception")).build();
		}
	}
}
