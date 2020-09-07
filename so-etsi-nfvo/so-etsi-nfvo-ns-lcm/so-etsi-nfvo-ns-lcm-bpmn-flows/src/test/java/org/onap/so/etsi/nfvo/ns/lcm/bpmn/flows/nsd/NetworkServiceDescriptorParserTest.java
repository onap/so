package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.nsd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.TestApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class NetworkServiceDescriptorParserTest {

    private static final String VALID_ETSI_NSD_FILE = "src/test/resources/ns.csar";
    private static final String INVALID_ETSI_NSD_FILE = "src/test/resources/invalid_ns.csar";

    @Autowired
    private NetworkServiceDescriptorParser objUnderTest;

    @Test
    public void testValidEtsiNsd_ableToParserIt() throws IOException {
        final byte[] zipBytes = Files.readAllBytes(Paths.get(getAbsolutePath(VALID_ETSI_NSD_FILE)));
        final Optional<NetworkServiceDescriptor> optional = objUnderTest.parser(zipBytes);
        assertTrue(optional.isPresent());
        final NetworkServiceDescriptor actualNsd = optional.get();
        assertEquals(NetworkServiceDescriptorParser.NS_NODE_TYPE, actualNsd.getType());
        assertFalse(actualNsd.getProperties().isEmpty());

        final Map<String, Object> actualNsdProperties = actualNsd.getProperties();
        assertEquals(5, actualNsdProperties.size());
        assertEquals("ffdddc5d-a44b-45ae-8fc3-e6551cce350f", actualNsdProperties.get("descriptor_id"));
        assertEquals(5, actualNsd.getVnfs().size());

    }

    @Test
    public void testEmptyEtsiNsd_ableToParserIt() throws IOException {
        assertFalse(objUnderTest.parser(new byte[] {}).isPresent());
    }

    @Test
    public void testInvalidEtsiNsd_ableToParserIt() throws IOException {
        final byte[] zipBytes = Files.readAllBytes(Paths.get(getAbsolutePath(INVALID_ETSI_NSD_FILE)));
        assertFalse(objUnderTest.parser(zipBytes).isPresent());
    }

    private String getAbsolutePath(final String path) {
        final File file = new File(path);
        return file.getAbsolutePath();
    }

}
