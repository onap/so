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

package org.onap.so.client;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.UnsupportedEncodingException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class ResponseExceptionMapperImplTest {

    private static final ResponseExceptionMapperImpl mapper = new ResponseExceptionMapperImpl();

    public static Object[][] statusesAndCorrespondingExceptions() {
        return new Object[][] {{Status.BAD_REQUEST, BadRequestException.class},
                {Status.UNAUTHORIZED, NotAuthorizedException.class}, {Status.FORBIDDEN, ForbiddenException.class},
                {Status.NOT_FOUND, NotFoundException.class}, {Status.METHOD_NOT_ALLOWED, NotAllowedException.class},
                {Status.NOT_ACCEPTABLE, NotAcceptableException.class},
                {Status.PRECONDITION_FAILED, PreconditionFailedException.class},
                {Status.UNSUPPORTED_MEDIA_TYPE, NotSupportedException.class},
                {Status.INTERNAL_SERVER_ERROR, InternalServerErrorException.class},
                {Status.SERVICE_UNAVAILABLE, WebApplicationException.class},
                {Status.BAD_GATEWAY, WebApplicationException.class},};
    }

    @Rule
    public ExpectedException expectedExceptionTest = ExpectedException.none();

    @Test
    @Parameters(method = "statusesAndCorrespondingExceptions")
    public void shouldThrowExceptionWhenStatusIsNotOk(Status status, Class<Exception> expectedException) {
        // given
        Response response = createMockResponse(status);
        // when, then
        expectedExceptionTest.expect(expectedException);
        mapper.map(response);
    }

    @Test
    public void shouldNotThrowExceptionWhenStatusIsOk() {
        // given
        Response response = createMockResponse(Status.OK);
        // when, then
        expectedExceptionTest.none();
        mapper.map(response);
    }

    @Test
    public void shouldThrowExceptionWithCustomMessageWhenResponseHasEntity() throws UnsupportedEncodingException {
        // given
        Response response = createMockResponse(Status.BAD_REQUEST);
        when(response.hasEntity()).thenReturn(true);
        when(response.readEntity(String.class)).thenReturn("test message");

        expectedExceptionTest.expect(BadRequestException.class);
        expectedExceptionTest.expectMessage("test message");
        mapper.map(response);
    }

    @Test
    public void shouldThrowExceptionWithDefaultMessageWhenResponseHasNoEntity() {
        // given
        Response response = createMockResponse(Status.BAD_REQUEST);
        when(response.hasEntity()).thenReturn(false);
        // when, then
        expectedExceptionTest.expect(BadRequestException.class);
        expectedExceptionTest.expectMessage("");
        mapper.map(response);
    }

    private static Response createMockResponse(Status status) {
        Response responseContext = mock(Response.class);
        when(responseContext.getStatusInfo()).thenReturn(status);
        when(responseContext.getStatus()).thenReturn(status.getStatusCode());
        return responseContext;
    }
}
