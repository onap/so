package org.openecomp.mso.apihandlerinfra.tenantisolation.process;

import java.util.ArrayList;
import java.util.List;

import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.openecomp.mso.apihandlerinfra.tenantisolation.exceptions.TenantIsolationException;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RelatedInstanceList;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper;
import org.openecomp.mso.client.aai.objects.AAIOperationalEnvironment;
import org.openecomp.mso.client.grm.GRMClient;
import org.openecomp.mso.client.grm.beans.OperationalInfo;
import org.openecomp.mso.client.grm.beans.Property;
import org.openecomp.mso.client.grm.beans.ServiceEndPoint;
import org.openecomp.mso.client.grm.beans.ServiceEndPointList;
import org.openecomp.mso.client.grm.beans.ServiceEndPointRequest;
import org.openecomp.mso.client.grm.beans.Version;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CreateVnfOperationalEnvironment extends OperationalEnvironmentProcess {
	
	private static final String SERVICE_NAME = "CreateVnfOperationalEnvironment"; 
    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);
    private ObjectMapper mapper = new ObjectMapper();
    private GRMClient grmClient;
    
	public CreateVnfOperationalEnvironment(CloudOrchestrationRequest request, String requestId) {
		super(request, requestId);
        MsoLogger.setServiceName (getRequestId());
        MsoLogger.setLogContext(getRequestId(), getRequest().getOperationalEnvironmentId());
	}


	@Override
	public void execute() {
		try {
			msoLogger.debug("Begin of execute method in " + SERVICE_NAME);
			//Retrieve ECOMP Managing environment info in A&AI
			AAIResultWrapper aaiResultWrapper = getAaiHelper().getAaiOperationalEnvironment(getEcompManagingEnvironmentId());
			AAIOperationalEnvironment aaiEnv = mapper.readValue(aaiResultWrapper.getJson(), AAIOperationalEnvironment.class);
	
			//Find ECOMP environments in GRM
			msoLogger.debug(" Start of GRM findRunningServicesAsString");
			String searchKey = getSearchKey(aaiEnv);
			String tenantContext = getTenantContext().toUpperCase();
			String jsonResponse = getGRMClient().findRunningServicesAsString(searchKey, 1, tenantContext);
			ServiceEndPointList sel = getObjectMapper().readValue(jsonResponse, ServiceEndPointList.class);
			if(sel.getServiceEndPointList().size() == 0) {
				throw new TenantIsolationException("GRM did not find any matches for " + searchKey + " in " + tenantContext);
			}
			//Replicate end-point for VNF Operating environment in GRM
			List<ServiceEndPointRequest> serviceEndpointRequestList = buildEndPointRequestList(sel);
			int ctr = 0;
			int total = serviceEndpointRequestList.size();
			for(ServiceEndPointRequest request : serviceEndpointRequestList) {
				msoLogger.debug("Creating endpoint " + ++ctr + " of " + total + ": " + request.getServiceEndPoint().getName());
				getGRMClient().addServiceEndPoint(request);
			}
			
			//Create VNF operating in A&AI
			getAaiHelper().createOperationalEnvironment(getAaiClientObjectBuilder().buildAAIOperationalEnvironment("INACTIVE"));
			getAaiHelper().createRelationship(getRequest().getOperationalEnvironmentId(), getEcompManagingEnvironmentId());
			
			//Update request database
			getRequestDb().updateInfraSuccessCompletion("SUCCESSFULLY created VNF operational environment", getRequestId(), getRequest().getOperationalEnvironmentId()); 
		}
		catch(Exception e) {
			msoLogger.error(MessageEnum.APIH_GENERAL_EXCEPTION, "", "", "", MsoLogger.ErrorCode.DataError, e.getMessage());
			getRequestDb().updateInfraFailureCompletion(e.getMessage(), requestId, getRequest().getOperationalEnvironmentId());
		}
	}
	
	
	protected String getEcompManagingEnvironmentId() throws TenantIsolationException {
		RelatedInstanceList[] relatedInstances = getRequest().getRequestDetails().getRelatedInstanceList();
		if (relatedInstances.length > 0 && relatedInstances[0].getRelatedInstance() != null) {
			return relatedInstances[0].getRelatedInstance().getInstanceId();
		} else {
			throw new TenantIsolationException("Unable to get Managing ECOMP Environment ID, request related instance list is empty!");
		}
	}
	
	
	protected String getTenantContext() throws TenantIsolationException {
		if(!StringUtils.isEmpty(getRequest().getRequestDetails().getRequestParameters().getTenantContext())) {
			return getRequest().getRequestDetails().getRequestParameters().getTenantContext();
		}
		else {
			throw new TenantIsolationException("Tenant Context is missing from request!");
		}
	}
	
	
	private List<ServiceEndPointRequest> buildEndPointRequestList(ServiceEndPointList serviceEndPointList) throws TenantIsolationException {
		List<ServiceEndPoint> endpointList = serviceEndPointList.getServiceEndPointList();
		msoLogger.debug("Number of service endpoints from GRM: " + endpointList.size());
		List<ServiceEndPointRequest> serviceEndPointRequestList = new ArrayList<ServiceEndPointRequest>(); 
		for(ServiceEndPoint serviceEndpoint : endpointList) {
			serviceEndPointRequestList.add(buildServiceEndpoint(serviceEndpoint));
		}
		return serviceEndPointRequestList;
	}
	
	
	private ServiceEndPointRequest buildServiceEndpoint(ServiceEndPoint serviceEndpoint) throws TenantIsolationException {
		
		//@TODO: handle nulls? Put in a ServiceEndpointWrapper class which will check for nulls and flatten access to fields
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
		for(Property prop : props) {
			if(prop.getName().equalsIgnoreCase("Environment")) {
				env = prop.getValue();
			}
		}
		return env;
	}
	
	
	protected String buildServiceNameForVnf(String fqName) throws TenantIsolationException {
		// Service name format is: {tenantContext}.{workloadContext}.{serviceName}  e.g. TEST.ECOMP_PSL.Inventory
		// We need to extract the serviceName, in the above example: "Inventory"
		String[] tokens = fqName.split("[.]");
		String serviceName;
		if(tokens.length > 0) {
			serviceName = tokens[tokens.length-1];
		}
		else {
			throw new TenantIsolationException("Fully qualified service name is null.");
		}
		String tenantContext = getRequest().getRequestDetails().getRequestParameters().getTenantContext();
		String workloadContext = getRequest().getRequestDetails().getRequestParameters().getWorkloadContext();
		return tenantContext + "." + workloadContext + "." + serviceName;
	}
	

	protected GRMClient getGRMClient() {
		if(this.grmClient == null) {
			this.grmClient = new GRMClient();
		}
		return this.grmClient;
	}

	
	protected String getSearchKey(AAIOperationalEnvironment aaiEnv)  {
		return aaiEnv.getTenantContext() + "." + aaiEnv.getWorkloadContext() + ".*";
	}
	
	protected ObjectMapper getObjectMapper() {
		return mapper;
	}


	@Override
	protected String getServiceName() {
		return CreateVnfOperationalEnvironment.SERVICE_NAME;
	}


}
