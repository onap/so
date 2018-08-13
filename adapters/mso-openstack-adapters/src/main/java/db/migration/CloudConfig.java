package db.migration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.CloudifyManager;

import java.util.HashMap;
import java.util.Map;

/**
 * @deprecated
 * This class is introduced as deprecated as its only purpose is for migration of cloud config data. It shouldnt be used elsewhere.
 */

@Deprecated
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudConfig {
    @JsonProperty("identity_services")
    private Map<String, CloudIdentity> identityServices = new HashMap<>();

    @JsonProperty("cloud_sites")
    private Map<String, CloudSite> cloudSites = new HashMap<>();

    @JsonProperty("cloudify_managers")
    private Map<String, CloudifyManager> cloudifyManagers = new HashMap<>();


    public Map<String, CloudIdentity> getIdentityServices() {
        return identityServices;
    }

    public void setIdentityServices(Map<String, CloudIdentity> identityServices) {
        this.identityServices = identityServices;
    }

    public Map<String, CloudSite> getCloudSites() {
        return cloudSites;
    }

    public void setCloudSites(Map<String, CloudSite> cloudSites) {
        this.cloudSites = cloudSites;
    }

    public Map<String, CloudifyManager> getCloudifyManagers() {
        return cloudifyManagers;
    }

    public void setCloudifyManagers(Map<String, CloudifyManager> cloudifyManagers) {
        this.cloudifyManagers = cloudifyManagers;
    }

    public void populateId(){
        for (Map.Entry<String, CloudIdentity> entry : identityServices.entrySet()) {
            entry.getValue().setId(entry.getKey());
        }

        for (Map.Entry <String, CloudSite> entry : cloudSites.entrySet()) {
            entry.getValue().setId(entry.getKey());
        }

        for (Map.Entry<String, CloudifyManager> entry : cloudifyManagers.entrySet()) {
            entry.getValue().setId(entry.getKey());
        }
    }
}
