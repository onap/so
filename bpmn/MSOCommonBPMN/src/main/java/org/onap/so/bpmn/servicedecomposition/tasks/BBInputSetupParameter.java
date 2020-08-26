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


    protected CloudConfiguration getCloudConfiguration() {
        return cloudConfiguration;
    }


    protected void setCloudConfiguration(CloudConfiguration cloudConfiguration) {
        this.cloudConfiguration = cloudConfiguration;
    }


    protected ConfigurationResourceKeys getConfigurationResourceKeys() {
        return configurationResourceKeys;
    }


    protected void setConfigurationResourceKeys(ConfigurationResourceKeys configurationResourceKeys) {
        this.configurationResourceKeys = configurationResourceKeys;
    }


    protected List<Map<String, String>> getInstanceParams() {
        return instanceParams;
    }


    protected void setInstanceParams(List<Map<String, String>> instanceParams) {
        this.instanceParams = instanceParams;
    }


    protected Map<ResourceKey, String> getLookupKeyMap() {
        return lookupKeyMap;
    }


    protected void setLookupKeyMap(Map<ResourceKey, String> lookupKeyMap) {
        this.lookupKeyMap = lookupKeyMap;
    }


    protected ModelInfo getModelInfo() {
        return modelInfo;
    }


    protected void setModelInfo(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }


    protected LineOfBusiness getLineOfBusiness() {
        return lineOfBusiness;
    }


    protected void setLineOfBusiness(LineOfBusiness lineOfBusiness) {
        this.lineOfBusiness = lineOfBusiness;
    }


    protected Platform getPlatform() {
        return platform;
    }


    protected void setPlatform(Platform platform) {
        this.platform = platform;
    }


    protected RelatedInstanceList[] getRelatedInstanceList() {
        return relatedInstanceList;
    }


    protected void setRelatedInstanceList(RelatedInstanceList[] relatedInstanceList) {
        this.relatedInstanceList = relatedInstanceList;
    }


    protected RequestDetails getRequestDetails() {
        return requestDetails;
    }


    protected void setRequestDetails(RequestDetails requestDetails) {
        this.requestDetails = requestDetails;
    }


    protected Service getService() {
        return service;
    }


    protected void setService(Service service) {
        this.service = service;
    }


    protected ServiceInstance getServiceInstance() {
        return serviceInstance;
    }


    protected void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }


    protected String getBbName() {
        return bbName;
    }


    protected void setBbName(String bbName) {
        this.bbName = bbName;
    }


    protected String getInstanceGroupId() {
        return instanceGroupId;
    }


    protected void setInstanceGroupId(String instanceGroupId) {
        this.instanceGroupId = instanceGroupId;
    }


    protected String getInstanceName() {
        return instanceName;
    }


    protected void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }


    protected String getProductFamilyId() {
        return productFamilyId;
    }


    protected void setProductFamilyId(String productFamilyId) {
        this.productFamilyId = productFamilyId;
    }


    protected String getResourceId() {
        return resourceId;
    }


    protected void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }


    protected String getVnfType() {
        return vnfType;
    }


    protected void setVnfType(String vnfType) {
        this.vnfType = vnfType;
    }


    protected ExecuteBuildingBlock getExecuteBB() {
        return executeBB;
    }


    protected void setExecuteBB(ExecuteBuildingBlock executeBB) {
        this.executeBB = executeBB;
    }


    protected String getRequestAction() {
        return requestAction;
    }


    protected void setRequestAction(String requestAction) {
        this.requestAction = requestAction;
    }


    protected boolean getaLaCarte() {
        return aLaCarte;
    }

    protected void setaLaCarte(boolean aLaCarte) {
        this.aLaCarte = aLaCarte;
    }

    protected Customer getCustomer() {
        return customer;
    }

    protected void setCustomer(Customer customer) {
        this.customer = customer;
    }

    protected void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    protected String getRequestId() {
        return requestId;
    }

    protected void setConfigurationKey(String configurationKey) {
        this.configurationKey = configurationKey;
    }

    protected String getConfigurationKey() {
        return configurationKey;
    }

    protected String getKey() {
        return key;
    }

    protected void setKey(String key) {
        this.key = key;
    }

    protected String getApplicationId() {
        return applicationId;
    }

    protected void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    protected boolean getIsReplace() {
        return isReplace;
    }

    protected void setIsReplace(boolean isReplace) {
        this.isReplace = isReplace;
    }

    protected ServiceModel getServiceModel() {
        return this.serviceModel;
    }

    protected void setServiceModel(ServiceModel serviceModel) {
        this.serviceModel = serviceModel;
    }

    protected boolean getIsHelm() {
        return isHelm;
    }


    protected void setIsHelm(boolean isHelm) {
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

        protected Builder setApplicationId(String applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        protected Builder setIsReplace(boolean isReplace) {
            this.isReplace = isReplace;
            return this;
        }

        protected Builder setServiceModel(ServiceModel serviceModel) {
            this.serviceModel = serviceModel;
            return this;
        }

        protected boolean getIsHelm() {
            return isHelm;
        }


        protected void setIsHelm(boolean isHelm) {
            this.isHelm = isHelm;
        }

        public BBInputSetupParameter build() {
            return new BBInputSetupParameter(this);
        }
    }

}
