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
