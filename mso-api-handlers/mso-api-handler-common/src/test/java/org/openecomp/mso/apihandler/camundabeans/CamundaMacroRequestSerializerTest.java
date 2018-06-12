package org.openecomp.mso.apihandler.camundabeans;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ANANDSAN on 4/10/2018.
 */
public class CamundaMacroRequestSerializerTest {
    @Test
    public void testWithAllParameters() throws Exception{
        String jsonRequest = CamundaMacroRequestSerializer.getJsonRequest("requestId", "action", "serviceInstanceId");
        Assert.assertNotNull(jsonRequest);
        Assert.assertEquals("{\"variables\":{\"mso-request-id\":{\"value\":\"requestId\",\"type\":\"String\"},\"gAction\":{\"value\":\"action\",\"type\":\"String\"},\"serviceInstanceId\":{\"value\":\"serviceInstanceId\",\"type\":\"String\"}}}", jsonRequest);
    }
}
