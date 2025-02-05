/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.apihandlerinfra.tenantisolation.process;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.onap.aai.domain.yang.OperationalEnvironment;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.onap.so.apihandlerinfra.tenantisolation.exceptions.TenantIsolationException;
import org.onap.so.apihandlerinfra.tenantisolation.helpers.AAIClientHelper;
import org.onap.so.apihandlerinfra.tenantisolation.helpers.AAIClientObjectBuilder;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RelatedInstanceList;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.grm.GRMClient;
import org.onap.so.client.grm.beans.OperationalInfo;
import org.onap.so.client.grm.beans.Property;
import org.onap.so.client.grm.beans.ServiceEndPoint;
import org.onap.so.client.grm.beans.ServiceEndPointList;
import org.onap.so.client.grm.beans.ServiceEndPointRequest;
import org.onap.so.client.grm.beans.Version;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.onap.so.requestsdb.RequestsDBHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CreateVnfOperationalEnvironment {

    private static Logger logger = LoggerFactory.getLogger(CreateVnfOperationalEnvironment.class);
    protected CloudOrchestrationRequest request;

    @Autowired
    private AAIClientObjectBuilder aaiClientObjectBuilder;
    @Autowired
    private AAIClientHelper aaiHelper;
    @Autowired
    private RequestsDBHelper requestDb;
    private GRMClient grmClient;

    public void execute(String requestId, CloudOrchestrationRequest request) throws ApiException {
        try {
            setRequest(request);
            ObjectMapper objectMapper = new ObjectMapper();
            AAIResultWrapper aaiResultWrapper = aaiHelper.getAaiOperationalEnvironment(getEcompManagingEnvironmentId());
            if (aaiResultWrapper.isEmpty()) {
                throw new NotFoundException(getEcompManagingEnvironmentId() + " not found in A&AI");
            }
            OperationalEnvironment aaiEnv = aaiResultWrapper.asBean(OperationalEnvironment.class).get();

            // Find ECOMP environments in GRM
            logger.debug(" Start of GRM findRunningServicesAsString");
            String searchKey = getSearchKey(aaiEnv);
            String tenantContext = getTenantContext().toUpperCase();
            String jsonResponse = getGrmClient().findRunningServicesAsString(searchKey, 1, tenantContext);
            ServiceEndPointList sel = objectMapper.readValue(jsonResponse, ServiceEndPointList.class);
            if (sel.getServiceEndPointList().size() == 0) {
                throw new TenantIsolationException(
                        "GRM did not find any matches for " + searchKey + " in " + tenantContext);
            }

            // Replicate end-point for VNF Operating environment in GRM
            List<ServiceEndPointRequest> serviceEndpointRequestList = buildEndPointRequestList(sel);
            int ctr = 0;
            int total = serviceEndpointRequestList.size();
            for (ServiceEndPointRequest requestList : serviceEndpointRequestList) {
                logger.debug("Creating endpoint " + ++ctr + " of " + total + ": "
                        + requestList.getServiceEndPoint().getName());
                getGrmClient().addServiceEndPoint(requestList);
            }

            // Create VNF operating in A&AI
            aaiHelper.createOperationalEnvironment(
                    aaiClientObjectBuilder.buildAAIOperationalEnvironment("INACTIVE", request));
            aaiHelper.createRelationship(request.getOperationalEnvironmentId(), getEcompManagingEnvironmentId());

            // Update request database
            requestDb.updateInfraSuccessCompletion("SUCCESSFULLY created VNF operational environment", requestId,
                    request.getOperationalEnvironmentId());

        } catch (Exception e) {
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_GENERAL_EXCEPTION, ErrorCode.DataError).build();


            ValidateException validateException =
                    new ValidateException.Builder(e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR,
                            ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e).errorInfo(errorLoggerInfo).build();

            throw validateException;
        }
    }


    protected String getEcompManagingEnvironmentId() throws TenantIsolationException {
        RelatedInstanceList[] relatedInstances = request.getRequestDetails().getRelatedInstanceList();
        if (relatedInstances.length > 0 && relatedInstances[0].getRelatedInstance() != null) {
            return relatedInstances[0].getRelatedInstance().getInstanceId();
        } else {
            return null;
        }
    }


    protected String getTenantContext() throws TenantIsolationException {
        if (!StringUtils.isEmpty(request.getRequestDetails().getRequestParameters().getTenantContext())) {
            return request.getRequestDetails().getRequestParameters().getTenantContext();
        } else {
            throw new TenantIsolationException("Tenant Context is missing from request!");
        }
    }


    private List<ServiceEndPointRequest> buildEndPointRequestList(ServiceEndPointList serviceEndPointList)
            throws TenantIsolationException {
        List<ServiceEndPoint> endpointList = serviceEndPointList.getServiceEndPointList();
        logger.debug("Number of service endpoints from GRM: {}", endpointList.size());
        List<ServiceEndPointRequest> serviceEndPointRequestList = new ArrayList<>();
        for (ServiceEndPoint serviceEndpoint : endpointList) {
            serviceEndPointRequestList.add(buildServiceEndpoint(serviceEndpoint));
        }
        return serviceEndPointRequestList;
    }


    private ServiceEndPointRequest buildServiceEndpoint(ServiceEndPoint serviceEndpoint)
            throws TenantIsolationException {

        // @TODO: handle nulls? Put in a ServiceEndpointWrapper class which will check for nulls and flatten access to
        // fields
        Version ver = new Version();
        ver.setMajor(serviceEndpoint.getVersion().getMajor());
        ver.setMinor(serviceEndpoint.getVersion().getMinor());
        ver.setPatch(serviceEndpoint.getVersion().getPatch());

        ServiceEndPoint endpoint = new ServiceEndPoint();
        endpoint.setName(buildServiceNameForVnf(serviceEndpoint.getName()));

        endpoint.setVersion(ver);
        endpoint.setHostAddress(serviceEndpoint.getHostAddress());
        endpoint.setListenPort(serviceEndpoint.getListenPort());
        endpoint.setLatitude(serviceEndpoint.getLatitude());
        endpoint.setLongitude(serviceEndpoint.getLongitude());
        endpoint.setContextPath(serviceEndpoint.getContextPath());
        endpoint.setRouteOffer(serviceEndpoint.getRouteOffer());

        OperationalInfo operInfo = new OperationalInfo();
        operInfo.setCreatedBy(serviceEndpoint.getOperationalInfo().getCreatedBy());
        operInfo.setUpdatedBy(serviceEndpoint.getOperationalInfo().getUpdatedBy());

        endpoint.setOperationalInfo(operInfo);
        endpoint.setProperties(serviceEndpoint.getProperties());

        String env = getEnvironmentName(serviceEndpoint.getProperties());

        ServiceEndPointRequest serviceEndPontRequest = new ServiceEndPointRequest();
        serviceEndPontRequest.setEnv(env);
        serviceEndPontRequest.setServiceEndPoint(endpoint);

        return serviceEndPontRequest;
    }


    protected String getEnvironmentName(List<Property> props) {
        String env = "";
        for (Property prop : props) {
            if (prop.getName().equalsIgnoreCase("Environment")) {
                env = prop.getValue();
            }
        }
        return env;
    }


    protected String buildServiceNameForVnf(String fqName) throws TenantIsolationException {
        // Service name format is: {tenantContext}.{workloadContext}.{serviceName} e.g. TEST.ECOMP_PSL.Inventory
        // We need to extract the serviceName, in the above example: "Inventory"
        String[] tokens = fqName.split("[.]");
        String serviceName;
        if (tokens.length > 0) {
            serviceName = tokens[tokens.length - 1];
        } else {
            throw new TenantIsolationException("Fully qualified service name is null.");
        }
        String tenantContext = request.getRequestDetails().getRequestParameters().getTenantContext();
        String workloadContext = request.getRequestDetails().getRequestParameters().getWorkloadContext();
        return tenantContext + "." + workloadContext + "." + serviceName;
    }

    protected String getSearchKey(OperationalEnvironment aaiEnv) {
        return aaiEnv.getTenantContext() + "." + aaiEnv.getWorkloadContext() + ".*";
    }

    public void setRequest(CloudOrchestrationRequest request) {
        this.request = request;
    }

    private GRMClient getGrmClient() {
        if (grmClient == null) {
            this.grmClient = new GRMClient();
        }

        return grmClient;
    }
}
