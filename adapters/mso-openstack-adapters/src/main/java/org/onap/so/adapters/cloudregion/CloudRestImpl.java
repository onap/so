package org.onap.so.adapters.cloudregion;

import java.util.List;
import java.util.Optional;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.aai.domain.yang.Complex;
import org.onap.aai.domain.yang.NetworkTechnologies;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.NetworkTechnologyReference;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.catalog.data.repository.NetworkTechnologyReferenceRepository;
import org.onap.so.db.catalog.utils.MavenLikeVersioning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CloudRestImpl {
    private static final Logger logger = LoggerFactory.getLogger(CloudRestImpl.class);

    private AAIResourcesClient aaiClient;

    @Autowired
    private NetworkTechnologyReferenceRepository ctrRepo;

    @Autowired
    private CatalogDbClient catalogDBClient;

    public void createCloudRegion(CloudSite cloudSite) throws CloudException {
        createRegionInCatalogDb(cloudSite);
        createCloudRegionInAAI(cloudSite);
    }

    public void updateCloudRegion(CloudSite cloudSite) throws CloudException {
        updateRegionInCatalogDb(cloudSite);
    }

    protected void updateRegionInCatalogDb(CloudSite cloudSite) {
        try {
            catalogDBClient.updateCloudSite(cloudSite);
        } catch (Exception e) {
            logger.error("Error updating cloud region in catalogdb", e);
            throw new CloudException("Error updating cloud region in Catalog: " + e.getMessage(), e);
        }
    }

    public void deleteCloudRegion(String cloudRegionId) throws CloudException {
        try {
            catalogDBClient.deleteCloudSite(cloudRegionId);
        } catch (Exception e) {
            logger.error("Error deleting cloud region in catalogdb", e);
            throw new CloudException("Error deleting cloud region in Catalog: " + e.getMessage(), e);
        }
    }

    protected void createCloudRegionInAAI(CloudSite cloudSite) {
        try {
            CloudRegion cloudRegion = mapCloudRegion(cloudSite);
            Optional<Complex> complex = retrieveComplex(cloudSite);
            if (complex.isPresent()) {
                cloudRegion.setComplexName(complex.get().getComplexName());
            }
            AAIResourceUri cloudRegionURI = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure()
                    .cloudRegion(cloudRegion.getCloudOwner(), cloudRegion.getCloudRegionId()));
            getAaiClient().createIfNotExists(cloudRegionURI, Optional.of(cloudRegion));
            if (complex.isPresent()) {
                AAIResourceUri complexURI = AAIUriFactory
                        .createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().complex(cloudSite.getClli()));
                getAaiClient().connect(cloudRegionURI, complexURI);
            }
            createCloudRegionNetworkTechnologyRelationship(cloudSite, cloudRegionURI);
        } catch (Exception e) {
            logger.error("Error creating cloud region in AAI", e);
            throw new CloudException("Error creating cloud region in AAI: " + e.getMessage(), e);
        }
    }

    protected void createCloudRegionNetworkTechnologyRelationship(CloudSite cloudSite, AAIResourceUri cloudRegionURI) {
        List<NetworkTechnologyReference> listOfNetworkTech = ctrRepo.findAllByCloudOwner(cloudSite.getCloudOwner());
        listOfNetworkTech.stream().forEach(tech -> linkCloudAndTechnology(tech.getNetworkTechnology(), cloudRegionURI));
    }

    protected Optional<Complex> retrieveComplex(CloudSite cloudSite) {
        AAIResourceUri complexURI = AAIUriFactory
                .createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().complex(cloudSite.getClli()));
        return getAaiClient().get(Complex.class, complexURI);
    }

    protected void linkCloudAndTechnology(String networkTechnologyName, AAIResourceUri cloudRegionURI) {
        AAIPluralResourceUri technologyPluralUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().networkTechnologies())
                        .queryParam("network-technology-name", networkTechnologyName);
        Optional<NetworkTechnologies> networkTechnology =
                getAaiClient().get(NetworkTechnologies.class, technologyPluralUri);
        if (networkTechnology.isPresent()) {
            AAIResourceUri networkTechnologyURI =
                    AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().networkTechnology(
                            networkTechnology.get().getNetworkTechnology().get(0).getNetworkTechnologyId()));
            getAaiClient().connect(cloudRegionURI, networkTechnologyURI);
        }
    }

    protected void createRegionInCatalogDb(CloudSite cloudSite) throws CloudException {
        try {
            CloudSite existingCloudSite = catalogDBClient.getCloudSite(cloudSite.getRegionId());
            if (existingCloudSite == null) {
                catalogDBClient.postCloudSite(cloudSite);
            }
        } catch (Exception e) {
            logger.error("Error creating cloud site in Catalog Adapter: {}", e.getMessage(), e);
            throw new CloudException("Error creating cloud site in Catalog Adapter", e);
        }
    }

    protected CloudRegion mapCloudRegion(CloudSite cloudSite) {
        CloudRegion region = new CloudRegion();
        region.setCloudOwner(cloudSite.getCloudOwner());
        region.setCloudRegionId(cloudSite.getRegionId());
        region.setCloudRegionVersion(cloudSite.getCloudVersion());
        region.setOwnerDefinedType("cLCP");
        region.setCloudType("openstack");
        MavenLikeVersioning cloudVersion = new MavenLikeVersioning();
        cloudVersion.setVersion(cloudSite.getCloudVersion());
        if (cloudVersion.isMoreRecentThan("3.0")) {
            region.setCloudZone(cloudSite.getRegionId().substring(0, cloudSite.getRegionId().length() - 1));
        } else {
            region.setCloudZone(cloudSite.getRegionId());
        }
        region.setOrchestrationDisabled(false);
        region.setInMaint(false);
        return region;
    }

    protected AAIResourcesClient getAaiClient() {
        if (aaiClient == null)
            return new AAIResourcesClient();
        else
            return aaiClient;
    }

}
