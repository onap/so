package org.onap.so.adapters.cloudregion;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.catalog.data.repository.NetworkTechnologyReferenceRepository;


@RunWith(MockitoJUnitRunner.class)
public class CloudRegionRestImplTest {

    @Spy
    @InjectMocks
    private CloudRestImpl cloudRestImpl;

    @Mock
    private CatalogDbClient catalogDbClientMock;

    @Mock
    private AAIResourcesClient aaiResClientMock;

    @Mock
    private NetworkTechnologyReferenceRepository ntRepoMock;

    private CloudSite cloudSite = new CloudSite();

    private CloudRegion testCloudRegion = new CloudRegion();

    @Before
    public void setup() {
        cloudSite.setCloudVersion("1.0");
        cloudSite.setRegionId("region1");
        cloudSite.setCloudOwner("bob");
        Mockito.doReturn(aaiResClientMock).when(cloudRestImpl).getAaiClient();
        testCloudRegion.setCloudOwner("bob");
        testCloudRegion.setCloudRegionId("region1");
        testCloudRegion.setCloudRegionVersion("1.0");
        testCloudRegion.setInMaint(false);
        testCloudRegion.setOrchestrationDisabled(false);
        testCloudRegion.setCloudRegionVersion("1.0");
        testCloudRegion.setOwnerDefinedType("cLCP");
        testCloudRegion.setCloudType("openstack");
        testCloudRegion.setCloudZone("region1");
    }

    @Test
    public void mapCloudRegionTest() {
        CloudRegion mappedRegion = cloudRestImpl.mapCloudRegion(cloudSite);
        assertThat(mappedRegion, sameBeanAs(testCloudRegion));
    }


    @Test
    public void mapCloudRegionVersionGreaterThan3Test() {
        CloudSite cloudSite2 = new CloudSite();
        cloudSite2.setCloudVersion("3.0.1");
        cloudSite2.setRegionId("region1");
        cloudSite2.setCloudOwner("bob");

        CloudRegion mappedRegion = cloudRestImpl.mapCloudRegion(cloudSite2);
        CloudRegion testRegion2 = new CloudRegion();
        testRegion2.setCloudOwner("bob");
        testRegion2.setCloudRegionId("region1");
        testRegion2.setCloudRegionVersion("3.0.1");
        testRegion2.setInMaint(false);
        testRegion2.setOrchestrationDisabled(false);
        testRegion2.setOwnerDefinedType("cLCP");
        testRegion2.setCloudType("openstack");
        testRegion2.setCloudZone("region");
        assertThat(mappedRegion, sameBeanAs(testRegion2));
    }

    @Test
    public void createCloudRegionTest() {
        when(catalogDbClientMock.getCloudSite("region1")).thenReturn(null);
        when(catalogDbClientMock.postCloudSite(cloudSite)).thenReturn(cloudSite);
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.CLOUD_REGION, "bob", "region1");
        cloudRestImpl.createCloudRegion(cloudSite);
        ArgumentCaptor<AAIResourceUri> actualURI = ArgumentCaptor.forClass(AAIResourceUri.class);
        ArgumentCaptor<Optional<Object>> actualCloudRegion = ArgumentCaptor.forClass(Optional.class);
        verify(catalogDbClientMock, times(1)).getCloudSite("region1");
        verify(catalogDbClientMock, times(1)).postCloudSite(cloudSite);
        verify(aaiResClientMock, times(1)).createIfNotExists(Mockito.eq(uri), Mockito.any());
        verify(aaiResClientMock, times(1)).createIfNotExists(actualURI.capture(), actualCloudRegion.capture());
        assertThat((CloudRegion) actualCloudRegion.getValue().get(), sameBeanAs(testCloudRegion));
    }

    @Test
    public void updateCloudRegionTest() {
        when(catalogDbClientMock.updateCloudSite(cloudSite)).thenReturn(cloudSite);
        cloudRestImpl.updateCloudRegion(cloudSite);
        verify(catalogDbClientMock, times(1)).updateCloudSite(cloudSite);
    }

    @Test
    public void deleteCloudRegionTest() {
        doNothing().when(catalogDbClientMock).deleteCloudSite("region1");
        cloudRestImpl.deleteCloudRegion(cloudSite.getRegionId());
        verify(catalogDbClientMock, times(1)).deleteCloudSite("region1");
    }

}
