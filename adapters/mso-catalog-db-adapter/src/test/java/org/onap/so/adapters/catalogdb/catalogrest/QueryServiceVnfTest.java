package org.onap.so.adapters.catalogdb.catalogrest;

import static org.junit.Assert.assertEquals;
import org.json.JSONException;
import org.junit.Test;


public class QueryServiceVnfTest extends QueryServiceVnfs {

    private QueryServiceVnfs queryServiceVnf = new QueryServiceVnfs();

    private final String invalidJSON =
            "\"{\\\\\\\"nf_function\\\\\\\":\\\\\\\"DO_STUFF\\\\\\\",\\\"_image_name\\\\\\\":\\\\\\\"test_image\\\"";

    private final String validJSON = "\"{\"nf_function\":\"DO_STUFF\",\"image_name\":\"test_image\"}";

    @Test
    public void test_IsValidJsonTrue() throws JSONException {
        boolean isValidJson = queryServiceVnf.isJSONValid(validJSON);
        assertEquals(true, isValidJson);
    }

    @Test
    public void test_IsValidJsonFalse() throws JSONException {
        boolean isValidJson = queryServiceVnf.isJSONValid(invalidJSON);
        assertEquals(false, isValidJson);
    }

    @Test
    public void test_IsValidJsonNull() throws JSONException {
        boolean isValidJson = queryServiceVnf.isJSONValid(null);
        assertEquals(false, isValidJson);
    }


}
