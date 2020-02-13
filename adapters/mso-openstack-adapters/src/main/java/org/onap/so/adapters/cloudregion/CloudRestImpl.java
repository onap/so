package org.onap.so.adapters.cloudregion;

import java.util.Optional;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CloudRestImpl {
    private static final Logger logger = LoggerFactory.getLogger(CloudRestImpl.class);

    private AAIResourcesClient aaiClient;

    @Autowired
    private CatalogDbClient catalogDBClient;

    public void createCloudRegion(CloudSite cloudSite, String cloudOwner) throws CloudException {
        createRegionInCatalogDb(cloudSite);
        createCloudRegionInAAI(cloudSite, cloudOwner);
    }

    public void updateCloudRegion(CloudSite cloudSite, String cloudOwner) throws CloudException {
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

    protected void createCloudRegionInAAI(CloudSite cloudSite, String cloudOwner) {
        try {
            CloudRegion cloudRegion = mapCloudRegion(cloudSite, cloudOwner);
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.CLOUD_REGION,
                    cloudRegion.getCloudOwner(), cloudRegion.getCloudRegionId());
            getAaiClient().createIfNotExists(uri, Optional.of(cloudRegion));
        } catch (Exception e) {
            logger.error("Error creating cloud region in AAI", e);
            throw new CloudException("Error creating cloud region in AAI: " + e.getMessage(), e);
        }
    }

    protected void createRegionInCatalogDb(CloudSite cloudSite) throws CloudException {
        try {
            CloudSite existingCloudSite = catalogDBClient.getCloudSite(cloudSite.getRegionId());
            if (existingCloudSite == null) {
                catalogDBClient.postCloudSite(cloudSite);
            }
        } catch (Exception e) {
            logger.error("Error creating cloud site in Catalog Adapter: " + e.getMessage(), e);
            throw new CloudException("Error creating cloud site in Catalog Adapter", e);
        }
    }

    protected CloudRegion mapCloudRegion(CloudSite cloudSite, String cloudOwner) {
        CloudRegion region = new CloudRegion();
        region.setCloudOwner(cloudOwner);
        region.setCloudRegionId(cloudSite.getRegionId());
        region.setCloudRegionVersion(cloudSite.getCloudVersion());
        region.setOwnerDefinedType("cLCP");
        region.setOrchestrationDisabled(false);
        region.setComplexName("NA");
        region.setInMaint(false);
        region.setCloudType("openstack");
        return region;
    }

    protected AAIResourcesClient getAaiClient() {
        if (aaiClient == null)
            return new AAIResourcesClient();
        else
            return aaiClient;
    }

}
