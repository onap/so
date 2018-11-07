/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Nokia.
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

package org.onap.so.bpmn.infrastructure.pnf.dmaap;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;

public class JsonUtilForCorrelationIdTest {

    private static final String JSON_EXAMPLE_WITH_CORRELATION_ID = "[{\"correlationId\": \"corrTest1\","
            + "\"key1\":\"value1\"},{\"correlationId\": \"corrTest2\",\"key2\":\"value2\"}]";

    private static final String JSON_WITH_ONE_CORRELATION_ID = "[{\"correlationId\":\"corrTest3\"}]";

    private static final String JSON_WITH_TWO_CORRELATION_ID_AND_ESCAPED_CHARACTERS =
            "[\"{\\\"correlationId\\\":\\\"corrTest4\\\"}\", \"{\\\"correlationId\\\":\\\"corrTest5\\\"}\"]";

    private static final String JSON_WITH_NO_CORRELATION_ID = "[{\"key1\":\"value1\"}]";

    @Test
    public void parseJsonSuccessful() {
        List<String> expectedResult = JsonUtilForCorrelationId
                .parseJsonToGelAllCorrelationId(JSON_EXAMPLE_WITH_CORRELATION_ID);
        assertThat(expectedResult).containsExactly("corrTest1", "corrTest2");

        List<String> expectedResult2 = JsonUtilForCorrelationId
                .parseJsonToGelAllCorrelationId(JSON_WITH_ONE_CORRELATION_ID);
        assertThat(expectedResult2).containsExactly("corrTest3");
    }

    @Test
    public void parseJsonWithEscapeCharacters_Successful() {
        List<String> expectedResult = JsonUtilForCorrelationId
                .parseJsonToGelAllCorrelationId(JSON_WITH_TWO_CORRELATION_ID_AND_ESCAPED_CHARACTERS);
        assertThat(expectedResult).containsExactly("corrTest4", "corrTest5");
    }

    @Test
    public void parseJson_emptyListReturnedWhenNothingFound() {
        List<String> expectedResult = JsonUtilForCorrelationId
                .parseJsonToGelAllCorrelationId(JSON_WITH_NO_CORRELATION_ID);
        assertThat(expectedResult).isEmpty();
    }

}
