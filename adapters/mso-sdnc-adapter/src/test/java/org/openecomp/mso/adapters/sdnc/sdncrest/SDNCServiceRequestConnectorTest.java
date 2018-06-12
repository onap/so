package org.openecomp.mso.adapters.sdnc.sdncrest;

import org.junit.Test;
import org.openecomp.mso.adapters.sdnc.FileUtil;
import org.openecomp.mso.adapters.sdncrest.SDNCResponseCommon;

import static org.junit.Assert.assertNotNull;

public class SDNCServiceRequestConnectorTest {

    @Test
    public void parseResponseContentTest() throws Exception {

        String content = FileUtil.readResourceFile("SdncServiceResponse.xml");
        SDNCResponseCommon responseCommon = SDNCServiceRequestConnector.parseResponseContent(content);

        assertNotNull(responseCommon);
    }
}