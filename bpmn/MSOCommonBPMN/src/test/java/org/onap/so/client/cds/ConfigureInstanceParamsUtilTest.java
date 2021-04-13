package org.onap.so.client.cds;

import static org.junit.Assert.assertEquals;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class ConfigureInstanceParamsUtilTest {

    @Test
    public void testApplyParamsToObject() {
        List<Map<String, String>> instanceParamsList =
                List.of(Map.of("test-param-1", "value1", "test-param-2", "value2"), Map.of("test-param-3", "value3"));
        JsonObject jsonObject = new JsonObject();

        ConfigureInstanceParamsUtil.applyParamsToObject(instanceParamsList, jsonObject);

        assertEquals("value1", jsonObject.get("test-param-1").getAsString());
        assertEquals("value2", jsonObject.get("test-param-2").getAsString());
        assertEquals("value3", jsonObject.get("test-param-3").getAsString());
    }

}
