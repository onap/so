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

package org.onap.so.apihandler.filters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.net.URI;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;
import org.junit.Test;
import org.onap.so.apihandlerinfra.BaseTest;



public class RequestUriFilterTest extends BaseTest {

    @Test
    public void filterTest() throws IOException {
        RequestUriFilter URIFilter = new RequestUriFilter();
        URI baseURI = URI.create("http://localhost:58879/");
        String requestURI = "onap/so/infra/serviceInstances/v5";

        ContainerRequestContext mockContext = mock(ContainerRequestContext.class);
        UriInfo mockInfo = mock(UriInfo.class);

        when(mockContext.getUriInfo()).thenReturn(mockInfo);
        when(mockInfo.getBaseUri()).thenReturn(baseURI);
        when(mockInfo.getPath()).thenReturn(requestURI);


        URIFilter.filter(mockContext);
        assertEquals("http://localhost:58879/onap/so/infra/serviceInstantiation/v5/serviceInstances",
                URIFilter.getRequestUri());
    }
}
