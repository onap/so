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

package org.openecomp.mso.bpmn.infrastructure.pnf.dmaap;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;

public class JsonUtilForCorrelationIdTest {

    private static final String JSON_EXAMPLE_WITH_CORRELATION_ID = "[\n"
            + "    {\n"
            + "        \"pnfRegistrationFields\" : {\n"
            + "        \"correlationId\" : \"corrTest1\",\n"
            + "        \"value\" : \"value1\"\n"
            + "        }\n"
            + "    },\n"
            + "    {\n"
            + "        \"pnfRegistrationFields\" : {\n"
            + "        \"correlationId\" : \"corrTest2\",\n"
            + "        \"value\" : \"value2\"\n"
            + "        }\n"
            + "    }\n"
            + "]";

    private static final String JSON_EXAMPLE_WITH_CORRELATION_ID2 = "{\"pnfRegistrationFields\":{\"correlationId\":\"corrTest3\"}}";
    private static final String JSON_EXAMPLE_WITH_CORRELATION_ID3 = "[\"\\{\\\"pnfRegistrationFields\\\":"
            + "{\\\"correlationId\\\":\\\"corrTest4\\\"}}\", \"\\{\\\"pnfRegistrationFields\\\":"
            + "{\\\"correlationId\\\":\\\"corrTest5\\\"}}\"]";
    private static final String JSON_EXAMPLE_WITH_CORRELATION_ID4 = "{\"header\":{\"key\":\"value\"}}";

    @Test
    public void parseJsonSuccessful() {
        List<String> expectedResult = JsonUtilForCorrelationId
                .parseJsonToGelAllCorrelationId(JSON_EXAMPLE_WITH_CORRELATION_ID);
        assertThat(expectedResult).containsExactly("corrTest1", "corrTest2");

        List<String> expectedResult2 = JsonUtilForCorrelationId
                .parseJsonToGelAllCorrelationId(JSON_EXAMPLE_WITH_CORRELATION_ID2);
        assertThat(expectedResult2).containsExactly("corrTest3");
    }

    @Test
    public void parseJsonWithEscapeCharacters_Successful() {
        List<String> expectedResult = JsonUtilForCorrelationId
                .parseJsonToGelAllCorrelationId(JSON_EXAMPLE_WITH_CORRELATION_ID3);
        assertThat(expectedResult).containsExactly("corrTest4", "corrTest5");
    }

    @Test
    public void parseJson_emptyListReturnedWhenNothingFound() {
        List<String> expectedResult = JsonUtilForCorrelationId
                .parseJsonToGelAllCorrelationId(JSON_EXAMPLE_WITH_CORRELATION_ID4);
        assertThat(expectedResult).isEmpty();
    }

}
