package org.openecomp.mso.requestsdb;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class RequestDatabaseTest {

	@Test
	public void testCheckVnfIdStatus() {
		RequestsDatabase reqDb = mock(RequestsDatabase.class);
		when(reqDb.checkVnfIdStatus(any(String.class))).thenReturn(new InfraActiveRequests());
		
		InfraActiveRequests response = reqDb.checkVnfIdStatus("123456");
		assertNotNull(response);
	}
}
