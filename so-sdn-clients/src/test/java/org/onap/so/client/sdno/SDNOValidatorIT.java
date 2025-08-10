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

package org.onap.so.client.sdno;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.client.dmaap.Consumer;
import org.onap.so.client.dmaap.exceptions.DMaaPConsumerFailure;
import org.onap.so.client.exceptions.SDNOException;
import org.onap.so.client.sdno.beans.SDNO;
import org.onap.so.client.sdno.dmaap.SDNOHealthCheckDmaapConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;



public class SDNOValidatorIT extends BaseIntegrationTest {


    @Mock
    private Consumer mrConsumer;
    private SDNOHealthCheckDmaapConsumer dmaapConsumer;
    private final String fileLocation = "src/test/resources/__files/sdno/client/";
    private final String uuid = "xyz123";
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUpTests() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    public void success() throws IOException, Exception {
        dmaapConsumer = spy(new SDNOHealthCheckDmaapConsumer());
        when(dmaapConsumer.getConsumer()).thenReturn(mrConsumer);
        when(mrConsumer.fetch())
                .thenReturn(Arrays.asList(new String[] {getJson("response.json"), getJson("output-success.json")}));

        SDNOValidatorImpl validator = new SDNOValidatorImpl();
        SDNOValidatorImpl spy = spy(validator);
        when(dmaapConsumer.getRequestId()).thenReturn("xyz123");
        doReturn(dmaapConsumer).when(spy).getConsumer(any(String.class));
        boolean result = spy.pollForResponse("xyz123");
        assertEquals("result is true", result, true);
    }

    @Test
    public void failure() throws IOException, Exception {
        dmaapConsumer = spy(new SDNOHealthCheckDmaapConsumer());

        when(dmaapConsumer.getConsumer()).thenReturn(mrConsumer);
        when(mrConsumer.fetch())
                .thenReturn(Arrays.asList(new String[] {getJson("response.json"), getJson("output-failure.json")}));

        SDNOValidatorImpl validator = new SDNOValidatorImpl();
        SDNOValidatorImpl spy = spy(validator);
        when(dmaapConsumer.getRequestId()).thenReturn("xyz123");
        doReturn(dmaapConsumer).when(spy).getConsumer(any(String.class));
        thrown.expect(SDNOException.class);
        thrown.expectMessage(containsString("my error message"));
        boolean result = spy.pollForResponse("xyz123");

    }

    @Ignore
    @Test
    public void run() throws Exception {
        SDNOValidatorImpl validator = new SDNOValidatorImpl();
        UUID uuid = UUID.randomUUID();
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("test");
        vnf.setIpv4OamAddress("1.2.3.4");
        vnf.setNfRole("VPE");
        SDNO request = validator.buildRequestDiagnostic(vnf, uuid, "mechid");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);
        validator.submitRequest(json);
        thrown.expect(DMaaPConsumerFailure.class);
        boolean result = validator.pollForResponse(uuid.toString());
        System.out.println(json);

    }

    private String getJson(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileLocation + filename)));
    }
}
