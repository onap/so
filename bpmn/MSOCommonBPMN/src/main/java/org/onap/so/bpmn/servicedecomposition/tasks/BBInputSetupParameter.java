package org.onap.so.bpmn.servicedecomposition.tasks;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ConfigurationResourceKeys;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.entities.ServiceModel;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.LineOfBusiness;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.Platform;
import org.onap.so.serviceinstancebeans.RelatedInstanceList;
import org.onap.so.serviceinstancebeans.RequestDetails;

public class BBInputSetupParameter {
    private CloudConfiguration cloudConfiguration;
    private ConfigurationResourceKeys configurationResourceKeys;
    private List<Map<String, String>> instanceParams;
    private Map<ResourceKey, String> lookupKeyMap;
    private ModelInfo modelInfo;
    private LineOfBusiness lineOfBusiness;
    private Platform platform;
    private RelatedInstanceList[] relatedInstanceList;
    private RequestDetails requestDetails;
    private Service service;
    private ServiceInstance serviceInstance;
    private String bbName;
    private String instanceGroupId;
    private String instanceName;
    private String productFamilyId;
    private String resourceId;
    private String vnfType;
    private ExecuteBuildingBlock executeBB;
    private String requestAction;
    private boolean aLaCarte;
    private Customer customer;
    private String requestId;
    private String configurationKey;
    private String key;
    private String applicationId;
    private boolean isReplace;
    private ServiceModel serviceModel;
    private boolean isHelm;

    private BBInputSetupParameter(Builder builder) {
        this.cloudConfiguration = builder.cloudConfiguration;
        this.configurationResourceKeys = builder.configurationResourceKeys;
        this.instanceParams = builder.instanceParams;
        this.lookupKeyMap = builder.lookupKeyMap;
        this.modelInfo = builder.modelInfo;
        this.lineOfBusiness = builder.lineOfBusiness;
        this.platform = builder.platform;
        this.relatedInstanceList = builder.relatedInstanceList;
        this.requestDetails = builder.requestDetails;
        this.service = builder.service;
        this.serviceInstance = builder.serviceInstance;
        this.bbName = builder.bbName;
        this.instanceGroupId = builder.instanceGroupId;
        this.instanceName = builder.instanceName;
        this.productFamilyId = builder.productFamilyId;
        this.resourceId = builder.resourceId;
        this.vnfType = builder.vnfType;
        this.executeBB = builder.executeBB;
        this.requestAction = builder.requestAction;
        this.aLaCarte = builder.aLaCarte;
        this.customer = builder.customer;
        this.requestId = builder.requestId;
        this.configurationKey = builder.configurationKey;
        this.key = builder.key;
        this.applicationId = builder.applicationId;
        this.isReplace = builder.isReplace;
        this.serviceModel = builder.serviceModel;
        this.isHelm = builder.isHelm;
    }


    public CloudConfiguration getCloudConfiguration() {
        return cloudConfiguration;
    }


    public void setCloudConfiguration(CloudConfiguration cloudConfiguration) {
        this.cloudConfiguration = cloudConfiguration;
    }


    public ConfigurationResourceKeys getConfigurationResourceKeys() {
        return configurationResourceKeys;
    }


    public void setConfigurationResourceKeys(ConfigurationResourceKeys configurationResourceKeys) {
        this.configurationResourceKeys = configurationResourceKeys;
    }


    public List<Map<String, String>> getInstanceParams() {
        return instanceParams;
    }


    public void setInstanceParams(List<Map<String, String>> instanceParams) {
        this.instanceParams = instanceParams;
    }


    public Map<ResourceKey, String> getLookupKeyMap() {
        return lookupKeyMap;
    }


    public void setLookupKeyMap(Map<ResourceKey, String> lookupKeyMap) {
        this.lookupKeyMap = lookupKeyMap;
    }


    public ModelInfo getModelInfo() {
        return modelInfo;
    }


    public void setModelInfo(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }


    public LineOfBusiness getLineOfBusiness() {
        return lineOfBusiness;
    }


    public void setLineOfBusiness(LineOfBusiness lineOfBusiness) {
        this.lineOfBusiness = lineOfBusiness;
    }


    public Platform getPlatform() {
        return platform;
    }


    public void setPlatform(Platform platform) {
        this.platform = platform;
    }


    public RelatedInstanceList[] getRelatedInstanceList() {
        return relatedInstanceList;
    }


    public void setRelatedInstanceList(RelatedInstanceList[] relatedInstanceList) {
        this.relatedInstanceList = relatedInstanceList;
    }


    public RequestDetails getRequestDetails() {
        return requestDetails;
    }


    public void setRequestDetails(RequestDetails requestDetails) {
        this.requestDetails = requestDetails;
    }


    public Service getService() {
        return service;
    }


    public void setService(Service service) {
        this.service = service;
    }


    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }


    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }


    public String getBbName() {
        return bbName;
    }


    public void setBbName(String bbName) {
        this.bbName = bbName;
    }


    public String getInstanceGroupId() {
        return instanceGroupId;
    }


    public void setInstanceGroupId(String instanceGroupId) {
        this.instanceGroupId = instanceGroupId;
    }


    public String getInstanceName() {
        return instanceName;
    }


    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }


    public String getProductFamilyId() {
        return productFamilyId;
    }


    public void setProductFamilyId(String productFamilyId) {
        this.productFamilyId = productFamilyId;
    }


    public String getResourceId() {
        return resourceId;
    }


    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }


    public String getVnfType() {
        return vnfType;
    }


    public void setVnfType(String vnfType) {
        this.vnfType = vnfType;
    }


    public ExecuteBuildingBlock getExecuteBB() {
        return executeBB;
    }


    public void setExecuteBB(ExecuteBuildingBlock executeBB) {
        this.executeBB = executeBB;
    }


    public String getRequestAction() {
        return requestAction;
    }


    public void setRequestAction(String requestAction) {
        this.requestAction = requestAction;
    }


    public boolean getaLaCarte() {
        return aLaCarte;
    }

    public void setaLaCarte(boolean aLaCarte) {
        this.aLaCarte = aLaCarte;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setConfigurationKey(String configurationKey) {
        this.configurationKey = configurationKey;
    }

    public String getConfigurationKey() {
        return configurationKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public boolean getIsReplace() {
        return isReplace;
    }

    public void setIsReplace(boolean isReplace) {
        this.isReplace = isReplace;
    }

    public ServiceModel getServiceModel() {
        return this.serviceModel;
    }

    public void setServiceModel(ServiceModel serviceModel) {
        this.serviceModel = serviceModel;
    }

    public boolean getIsHelm() {
        return isHelm;
    }


    public void setIsHelm(boolean isHelm) {
        this.isHelm = isHelm;
    }

    public static class Builder {
        private CloudConfiguration cloudConfiguration;
        private ConfigurationResourceKeys configurationResourceKeys;
        private List<Map<String, String>> instanceParams = Collections.emptyList();
        private Map<ResourceKey, String> lookupKeyMap = Collections.emptyMap();
        private ModelInfo modelInfo;
        private LineOfBusiness lineOfBusiness;
        private Platform platform;
        private RelatedInstanceList[] relatedInstanceList;
        private RequestDetails requestDetails;
        private Service service;
        private ServiceInstance serviceInstance;
        private String bbName;
        private String instanceGroupId;
        private String instanceName;
        private String productFamilyId;
        private String resourceId;
        private String vnfType;
        private ExecuteBuildingBlock executeBB;
        private String requestAction;
        private boolean aLaCarte;
        private Customer customer;
        private String requestId;
        private String configurationKey;
        private String key;
        private String applicationId;
        private boolean isReplace;
        private ServiceModel serviceModel;
        private boolean isHelm;

        public Builder setCloudConfiguration(CloudConfiguration cloudConfiguration) {
            this.cloudConfiguration = cloudConfiguration;
            return this;
        }

        public Builder setConfigurationResourceKeys(ConfigurationResourceKeys configurationResourceKeys) {
            this.configurationResourceKeys = configurationResourceKeys;
            return this;
        }

        public Builder setInstanceParams(List<Map<String, String>> instanceParams) {
            this.instanceParams = instanceParams;
            return this;
        }

        public Builder setLookupKeyMap(Map<ResourceKey, String> lookupKeyMap) {
            this.lookupKeyMap = lookupKeyMap;
            return this;
        }

        public Builder setModelInfo(ModelInfo modelInfo) {
            this.modelInfo = modelInfo;
            return this;
        }

        public Builder setLineOfBusiness(LineOfBusiness lineOfBusiness) {
            this.lineOfBusiness = lineOfBusiness;
            return this;
        }

        public Builder setPlatform(Platform platform) {
            this.platform = platform;
            return this;
        }

        public Builder setRelatedInstanceList(RelatedInstanceList[] relatedInstanceList) {
            this.relatedInstanceList = relatedInstanceList;
            return this;
        }

        public Builder setRequestDetails(RequestDetails requestDetails) {
            this.requestDetails = requestDetails;
            return this;
        }

        public Builder setService(Service service) {
            this.service = service;
            return this;
        }

        public Builder setServiceInstance(ServiceInstance serviceInstance) {
            this.serviceInstance = serviceInstance;
            return this;
        }

        public Builder setBbName(String bbName) {
            this.bbName = bbName;
            return this;
        }

        public Builder setInstanceGroupId(String instanceGroupId) {
            this.instanceGroupId = instanceGroupId;
            return this;
        }

        public Builder setInstanceName(String instanceName) {
            this.instanceName = instanceName;
            return this;
        }

        public Builder setProductFamilyId(String productFamilyId) {
            this.productFamilyId = productFamilyId;
            return this;
        }

        public Builder setResourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public Builder setVnfType(String vnfType) {
            this.vnfType = vnfType;
            return this;
        }

        public Builder setExecuteBB(ExecuteBuildingBlock executeBB) {
            this.executeBB = executeBB;
            return this;
        }

        public Builder setRequestAction(String requestAction) {
            this.requestAction = requestAction;
            return this;
        }

        public Builder setALaCarte(boolean aLaCarte) {
            this.aLaCarte = aLaCarte;
            return this;
        }

        public Builder setCustomer(Customer customer) {
            this.customer = customer;
            return this;
        }

        public Builder setRequestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder setConfigurationKey(String configurationKey) {
            this.configurationKey = configurationKey;
            return this;
        }

        public Builder setKey(String key) {
            this.key = key;
            return this;
        }

        public Builder setApplicationId(String applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        public Builder setIsReplace(boolean isReplace) {
            this.isReplace = isReplace;
            return this;
        }

        public Builder setServiceModel(ServiceModel serviceModel) {
            this.serviceModel = serviceModel;
            return this;
        }

        public boolean getIsHelm() {
            return isHelm;
        }


        public void setIsHelm(boolean isHelm) {
            this.isHelm = isHelm;
        }

        public BBInputSetupParameter build() {
            return new BBInputSetupParameter(this);
        }
    }

}
