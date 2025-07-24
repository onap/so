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


public class RuntimeExceptionMapperTest {



    @Test
    public void testResponse() {

        RuntimeExceptionMapper mapper = new RuntimeExceptionMapper();

        Response r = mapper.toResponse(new RuntimeException("This is the run time exception message"));

        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), r.getStatus());
        assertThat(r.getEntity(), instanceOf(ExceptionResponse.class));
        assertThat(((ExceptionResponse) r.getEntity()).getMessage(), equalTo("Unexpected Internal Exception"));

    }

    @Test
    public void preserve404ExceptionForForwarding() {

        RuntimeExceptionMapper mapper = new RuntimeExceptionMapper();

        Response r = mapper.toResponse(new NotFoundException("not found"));

        assertEquals(Status.NOT_FOUND.getStatusCode(), r.getStatus());
        assertThat(r.getEntity(), is(nullValue()));
    }

}
