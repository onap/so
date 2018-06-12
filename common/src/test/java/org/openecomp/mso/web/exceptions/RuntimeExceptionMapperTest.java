package org.openecomp.mso.web.exceptions;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.AfterClass;
import org.junit.Test;
import org.openecomp.mso.logger.MsoAlarmLogger;

public class RuntimeExceptionMapperTest {

	
	@AfterClass
	public static void tearDown() {
		MsoAlarmLogger logger = new MsoAlarmLogger();
		logger.resetAppender();
	}
	
	@Test
	public void testResponse() {
		
		RuntimeExceptionMapper mapper = new RuntimeExceptionMapper();
		
		Response r = mapper.toResponse(new RuntimeException("This is the run time exception message"));
		
		assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), r.getStatus());
		assertThat(r.getEntity(), instanceOf(ExceptionResponse.class));
		assertThat(((ExceptionResponse)r.getEntity()).getMessage(), equalTo("Unexpected Internal Exception"));

	}
	
	@Test
	public void preserve404ExceptionForForwarding() {
		
		RuntimeExceptionMapper mapper = new RuntimeExceptionMapper();
		
		Response r = mapper.toResponse(new NotFoundException("not found"));
		
		assertEquals(Status.NOT_FOUND.getStatusCode(), r.getStatus());
		assertThat(r.getEntity(), is(nullValue()));
	}
	
}
