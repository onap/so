package org.openecomp.mso.client.sdn.common;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openecomp.mso.BaseTest;
import org.openecomp.mso.client.exception.BadResponseException;
import org.openecomp.mso.client.exception.MapperException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;


public class SdnCommonTasksTest{

    
    SdnCommonTasks sdnCommonTasks = new SdnCommonTasks();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void buildJsonRequestTest() throws MapperException {
        String jsonStr = sdnCommonTasks.buildJsonRequest("");
        Assert.assertNotNull(jsonStr);
    }

    @Test
    public void buildJsonRequestTestException() throws MapperException {
        expectedException.expect(MapperException.class);
        sdnCommonTasks.buildJsonRequest(new Object());
    }

    @Test
    public void getHttpHeadersTest() {
        Assert.assertNotNull(sdnCommonTasks.getHttpHeaders(""));
    }

    @Test
    public void validateSDNResponseTest() throws BadResponseException {
        LinkedHashMap responseMap = new LinkedHashMap();
        responseMap.put("response-code", "0");
        responseMap.put("response-message", "success");
        Assert.assertNotNull(sdnCommonTasks.validateSDNResponse(responseMap));
    }

    @Test
    public void validateSDNResponseTestException() throws BadResponseException {
        expectedException.expect(BadResponseException.class);
        LinkedHashMap responseMap = new LinkedHashMap();
        Assert.assertNotNull(sdnCommonTasks.validateSDNResponse(responseMap));
    }

    @Test
    public void validateSDNResponseTestRespCodeNot200() throws BadResponseException {
        expectedException.expect(BadResponseException.class);
        LinkedHashMap responseMap = new LinkedHashMap();
        responseMap.put("response-code", "300");
        responseMap.put("response-message", "Failed");
        Assert.assertNotNull(sdnCommonTasks.validateSDNResponse(responseMap));
    }

}