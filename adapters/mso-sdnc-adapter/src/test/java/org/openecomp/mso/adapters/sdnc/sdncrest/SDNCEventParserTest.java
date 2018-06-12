package org.openecomp.mso.adapters.sdnc.sdncrest;

import org.junit.Test;
import org.openecomp.mso.adapters.sdncrest.SDNCEvent;

import java.text.ParseException;

import static org.junit.Assert.*;

public class SDNCEventParserTest {

    @Test
    public void parseTest() throws ParseException {

        String content = "<dummy><configuration-event>" +
                "<event-type>test</event-type>" +
                "<event-correlator-type>test</event-correlator-type>" +
                "<event-correlator>123</event-correlator>" +
                "<event-parameters><event-parameter>" +
                "<tag-name>test</tag-name>" +
                "<tag-value>test</tag-value></event-parameter></event-parameters>" +
                "</configuration-event></dummy>";

        SDNCEvent sdncEvent = SDNCEventParser.parse(content);
        assertNotNull(sdncEvent);
    }
}