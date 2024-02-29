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

package org.onap.so.bpmn.infrastructure.pnf.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class JsonUtilForPnfCorrelationIdTest {
    private static final List<String> LIST_EXAMPLE_WITH_PNF_CORRELATION_ID = new ArrayList<>();
    private static final List<String> LIST_WITH_ONE_PNF_CORRELATION_ID = new ArrayList<>();
    private static final List<String> LIST_WITH_TWO_PNF_CORRELATION_ID_AND_ESCAPED_CHARACTERS = new ArrayList<>();
    private static final List<String> LIST_WITH_NO_PNF_CORRELATION_ID = new ArrayList<>();

    static {
        LIST_EXAMPLE_WITH_PNF_CORRELATION_ID.add("{\"correlationId\": \"corrTest1\",\"key1\":\"value1\"}");
        LIST_EXAMPLE_WITH_PNF_CORRELATION_ID.add("{\"correlationId\": \"corrTest2\",\"key2\":\"value2\"}");
        LIST_WITH_ONE_PNF_CORRELATION_ID.add("{\"correlationId\":\"corrTest3\"}");
        LIST_WITH_TWO_PNF_CORRELATION_ID_AND_ESCAPED_CHARACTERS.add("\"{\\\"correlationId\\\":\\\"corrTest4\\\"}\"");
        LIST_WITH_TWO_PNF_CORRELATION_ID_AND_ESCAPED_CHARACTERS.add("\"{\\\"correlationId\\\":\\\"corrTest5\\\"}\"");
        LIST_WITH_NO_PNF_CORRELATION_ID.add("{\"key1\":\"value1\"}");
    }

    @Test
    public void parseJsonSuccessful() {
        List<String> expectedResult =
                JsonUtilForPnfCorrelationId.parseJsonToGelAllPnfCorrelationId(LIST_EXAMPLE_WITH_PNF_CORRELATION_ID);
        assertThat(expectedResult).containsExactly("corrTest1", "corrTest2");

        List<String> expectedResult2 =
                JsonUtilForPnfCorrelationId.parseJsonToGelAllPnfCorrelationId(LIST_WITH_ONE_PNF_CORRELATION_ID);
        assertThat(expectedResult2).containsExactly("corrTest3");
    }

    @Test
    public void parseJsonWithEscapeCharacters_Successful() {
        List<String> expectedResult = JsonUtilForPnfCorrelationId
                .parseJsonToGelAllPnfCorrelationId(LIST_WITH_TWO_PNF_CORRELATION_ID_AND_ESCAPED_CHARACTERS);
        assertThat(expectedResult).containsExactly("corrTest4", "corrTest5");
    }

    @Test
    public void parseJson_emptyListReturnedWhenNothingFound() {
        List<String> expectedResult =
                JsonUtilForPnfCorrelationId.parseJsonToGelAllPnfCorrelationId(LIST_WITH_NO_PNF_CORRELATION_ID);
        assertThat(expectedResult).isEmpty();
    }

    @Test
    public void shouldReturnEmptyListWhenInputIsNull() {
        assertThat(JsonUtilForPnfCorrelationId.parseJsonToGelAllPnfCorrelationId(null)).isEmpty();
    }

    @Test
    public void shouldReturnEmptyListWhenInputIsEmpty() {
        assertThat(JsonUtilForPnfCorrelationId.parseJsonToGelAllPnfCorrelationId(null)).isEmpty();
    }
}
