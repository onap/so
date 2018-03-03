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

package org.openecomp.mso.client;

@RunWith(JUnitParamsRunner.class)
public class ResponseExceptionMapperImplTest {

    private static final ResponseExceptionMapperImpl mapper = new ResponseExceptionMapperImpl();

    public static Object[][] statusesAndCorrespondingExceptions() {
        return new Object[][]{
                {Status.BAD_REQUEST, BadRequestException.class},
                {Status.UNAUTHORIZED, NotAuthorizedException.class},
                {Status.FORBIDDEN, ForbiddenException.class},
                {Status.NOT_FOUND, NotFoundException.class},
                {Status.METHOD_NOT_ALLOWED, NotAllowedException.class},
                {Status.NOT_ACCEPTABLE, NotAcceptableException.class},
                {Status.PRECONDITION_FAILED, PreconditionFailedException.class},
                {Status.UNSUPPORTED_MEDIA_TYPE, NotSupportedException.class},
                {Status.INTERNAL_SERVER_ERROR, InternalServerErrorException.class},
                {Status.SERVICE_UNAVAILABLE, WebApplicationException.class},
                {Status.BAD_GATEWAY, WebApplicationException.class},
        };
    }

    @Test
    @Parameters(method = "statusesAndCorrespondingExceptions")
    public void shouldThrowExceptionWhenStatusIsNotOk(Status status, Class<Exception> expectedException) {
        // given
        ClientResponseContext responseContext = createMockResponseContext(status);
        // when, then
        assertThatThrownBy(() -> mapper.filter(null, responseContext)).isInstanceOf(expectedException);
    }

    @Test
    public void shouldNotThrowExceptionWhenStatusIsOk() {
        // given
        ClientResponseContext responseContext = createMockResponseContext(Status.OK);
        // when, then
        assertThatCode(() -> mapper.filter(null, responseContext)).doesNotThrowAnyException();
    }

    @Test
    public void shouldThrowExceptionWithCustomMessageWhenResponseHasEntity() {
        // given
        ClientResponseContext responseContext = createMockResponseContext(Status.BAD_REQUEST);
        when(responseContext.hasEntity()).thenReturn(true);
        when(responseContext.getEntityStream()).thenReturn(IOUtils.toInputStream("test message", Charsets.UTF_8));
        // when, then
        assertThatThrownBy(() -> mapper.filter(null, responseContext)).isInstanceOf(BadRequestException.class)
                .hasMessage("test message");
    }

    @Test
    public void shouldThrowExceptionWithDefaultMessageWhenResponseHasNoEntity() {
        // given
        ClientResponseContext responseContext = createMockResponseContext(Status.BAD_REQUEST);
        when(responseContext.hasEntity()).thenReturn(false);
        // when, then
        assertThatThrownBy(() -> mapper.filter(null, responseContext)).isInstanceOf(BadRequestException.class)
                .hasMessage("empty message");
    }

    private static ClientResponseContext createMockResponseContext(Status status) {
        ClientResponseContext responseContext = mock(ClientResponseContext.class);
        when(responseContext.getStatusInfo()).thenReturn(status);
        when(responseContext.getStatus()).thenReturn(status.getStatusCode());
        return responseContext;
    }
}