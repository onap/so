/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom AG Intellectual Property. All rights reserved.
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

package org.onap.aaiclient.client;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.onap.aai.domain.yang.Complex;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aaiclient.client.api.AAIResultWrapper;
import org.onap.aaiclient.client.api.Relationships;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

public class ResultWrapperTest {
    private final static String AAI_JSON_FILE_LOCATION = "src/test/resources/__files/";
    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JaxbAnnotationModule());
    }

    @Test
    public void getByTypeTests() throws IOException {
        final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "e2e-complex.json")));

        Complex complex = mapper.readValue(content, Complex.class);
        AAIResultWrapper<Complex> resultWrapper = new AAIResultWrapper<Complex>(complex);
        Relationships relationships = resultWrapper.getRelationships();

        List<Relationship> result = relationships.getByType(Types.SERVICE_INSTANCE);
        assertTrue(result.stream().allMatch(relationship -> relationship.getRelatedTo().equals(Types.VCE.getName())));
    }
}
