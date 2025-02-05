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

package org.onap.so.apihandlerinfra;

import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandlerinfra.exceptions.*;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;


public class ApiExceptionTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testRecipeNotFoundException() throws ApiException {
        thrown.expect(RecipeNotFoundException.class);
        thrown.expectMessage("Message rewritten");
        thrown.expect(hasProperty("httpResponseCode", is(HttpStatus.SC_NOT_FOUND)));
        thrown.expect(hasProperty("messageID", is(ErrorNumbers.SVC_BAD_PARAMETER)));
        RecipeNotFoundException testException = new RecipeNotFoundException.Builder("Test Message",
                HttpStatus.SC_NOT_FOUND, ErrorNumbers.SVC_BAD_PARAMETER).message("Message rewritten").build();
        throw testException;
    }


    @Test
    public void testBPMNFailureException() throws ApiException {
        List<String> testVariables = new LinkedList<>();
        testVariables.add("hello");
        thrown.expect(BPMNFailureException.class);
        thrown.expectMessage(startsWith("Request Failed due to BPEL error with HTTP Status ="));
        thrown.expect(hasProperty("httpResponseCode", is(HttpStatus.SC_NOT_FOUND)));
        thrown.expect(hasProperty("messageID", is(ErrorNumbers.SVC_BAD_PARAMETER)));
        thrown.expect(hasProperty("variables", sameBeanAs(testVariables)));
        BPMNFailureException testException = new BPMNFailureException.Builder("Test Message", HttpStatus.SC_NOT_FOUND,
                ErrorNumbers.SVC_BAD_PARAMETER).variables(testVariables).build();
        throw testException;
    }


    @Test
    public void testClientConnectionException() throws ApiException {
        IOException ioException = new IOException();
        thrown.expect(ClientConnectionException.class);
        thrown.expectMessage("Client from test failed to connect");
        thrown.expect(hasProperty("httpResponseCode", is(HttpStatus.SC_NOT_FOUND)));
        thrown.expect(hasProperty("messageID", is(ErrorNumbers.SVC_BAD_PARAMETER)));
        thrown.expect(hasProperty("httpResponseCode", is(HttpStatus.SC_NOT_FOUND)));
        thrown.expect(hasProperty("messageID", is(ErrorNumbers.SVC_BAD_PARAMETER)));
        thrown.expect(hasProperty("cause", sameBeanAs(ioException)));
        ClientConnectionException testException =
                new ClientConnectionException.Builder("test", HttpStatus.SC_NOT_FOUND, ErrorNumbers.SVC_BAD_PARAMETER)
                        .cause(ioException).build();
        throw testException;
    }


    @Test
    public void testDuplicateRequestException() throws ApiException {
        ErrorLoggerInfo testLog =
                new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, ErrorCode.DataError)
                        .errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
        thrown.expect(DuplicateRequestException.class);
        thrown.expectMessage(startsWith("Error: Locked instance"));
        thrown.expect(hasProperty("httpResponseCode", is(HttpStatus.SC_NOT_FOUND)));
        thrown.expect(hasProperty("messageID", is(ErrorNumbers.SVC_BAD_PARAMETER)));
        thrown.expect(hasProperty("errorLoggerInfo", sameBeanAs(testLog)));
        DuplicateRequestException testException = new DuplicateRequestException.Builder("Test1", "Test2", "Test3",
                "Test4", HttpStatus.SC_NOT_FOUND, ErrorNumbers.SVC_BAD_PARAMETER).errorInfo(testLog).build();
        throw testException;
    }


    @Test
    public void testValidateException() throws ApiException {
        thrown.expect(ValidateException.class);
        thrown.expectMessage("Test Message");
        thrown.expect(hasProperty("httpResponseCode", is(HttpStatus.SC_NOT_FOUND)));
        thrown.expect(hasProperty("messageID", is(ErrorNumbers.SVC_DETAILED_SERVICE_ERROR)));

        ValidateException testException =
                new ValidateException.Builder("Test Message", HttpStatus.SC_NOT_FOUND, ErrorNumbers.SVC_BAD_PARAMETER)
                        .messageID(ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).build();
        throw testException;
    }


    @Test
    public void testVfModuleNotFoundException() throws ApiException {
        thrown.expect(VfModuleNotFoundException.class);
        thrown.expectMessage("Test Message");
        thrown.expect(hasProperty("httpResponseCode", is(HttpStatus.SC_CONFLICT)));
        thrown.expect(hasProperty("messageID", is(ErrorNumbers.SVC_BAD_PARAMETER)));
        VfModuleNotFoundException testException =
                new VfModuleNotFoundException.Builder("Test Message", HttpStatus.SC_NOT_FOUND,
                        ErrorNumbers.SVC_BAD_PARAMETER).httpResponseCode(HttpStatus.SC_CONFLICT).build();
        throw testException;
    }


}
