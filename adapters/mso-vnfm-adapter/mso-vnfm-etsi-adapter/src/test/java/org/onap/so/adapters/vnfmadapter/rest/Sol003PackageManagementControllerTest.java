package org.onap.so.adapters.vnfmadapter.rest;


import static org.junit.Assert.assertEquals;
import static org.onap.so.client.RestTemplateConfig.CONFIGURABLE_REST_TEMPLATE;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.vnfmadapter.VnfmAdapterApplication;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.InlineResponse2001;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = VnfmAdapterApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class Sol003PackageManagementControllerTest {

    private static final String vnfPackageId = "myVnfPackageId";
    private static final String artifactPath = "myArtifactPath";

    @Autowired
    @Qualifier(CONFIGURABLE_REST_TEMPLATE)
    private RestTemplate testRestTemplate;
    private MockRestServiceServer mockRestServer;

    @Autowired
    private Sol003PackageManagementController controller;

    @Before
    public void setUp() throws Exception {
        mockRestServer = MockRestServiceServer.bindTo(testRestTemplate).build();
    }

    @Test
    public void getVnfPackages() throws URISyntaxException, InterruptedException {
        final ResponseEntity<List<InlineResponse2001>> response = controller.getVnfPackages();
        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }

    @Test
    public void getVnfPackage() throws URISyntaxException, InterruptedException {
        final ResponseEntity<InlineResponse2001> response = controller.getVnfPackage(vnfPackageId);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }

    @Test
    public void getVnfPackageVnfd() throws URISyntaxException, InterruptedException {
        final ResponseEntity<byte[]> response = controller.getVnfPackageVnfd(vnfPackageId);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }

    @Test
    public void getVnfPackageContents() throws URISyntaxException, InterruptedException {
        final ResponseEntity<byte[]> response = controller.getVnfPackageContent(vnfPackageId);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }

    @Test
    public void getVnfPackageArtifact() throws URISyntaxException, InterruptedException {
        final ResponseEntity<byte[]> response = controller.getVnfPackageArtifact(vnfPackageId, artifactPath);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }

}
