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

package org.onap.so.client.aai;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.core.Response;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.client.RestClientSSL;
import org.onap.so.client.graphinventory.GraphInventoryPatchConverter;
import org.onap.so.client.graphinventory.exceptions.GraphInventoryPatchDepthExceededException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class AAIRestClientTest {

    @Mock
    private AAIProperties props;

    private ObjectMapper mapper = new AAICommonObjectMapperProvider().getMapper();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void failPatchOnComplexObject() throws URISyntaxException {
        AAIRestClient client = new AAIRestClient(props, new URI(""));
        this.thrown.expect(GraphInventoryPatchDepthExceededException.class);
        this.thrown.expectMessage(containsString("Object exceeds allowed depth for update action"));
        client.patch(
                "{ \"hello\" : \"world\", \"nestedSimple\" : [\"value1\" , \"value2\"], \"relationship-list\" : [{\"key\" : \"value\"}], \"nested\" : { \"key\" : \"value\" }}");
    }

    @Test
    public void verifyPatchValidation() throws URISyntaxException {
        AAIRestClient client = new AAIRestClient(props, new URI(""));
        AAIRestClient spy = spy(client);
        GraphInventoryPatchConverter patchValidatorMock = mock(GraphInventoryPatchConverter.class);
        doReturn(patchValidatorMock).when(spy).getPatchConverter();
        String payload = "{}";
        doReturn(Response.ok().build()).when(spy).method(eq("PATCH"), any());
        spy.patch(payload);
        verify(patchValidatorMock, times(1)).convertPatchFormat(eq((Object) payload));
    }
}
