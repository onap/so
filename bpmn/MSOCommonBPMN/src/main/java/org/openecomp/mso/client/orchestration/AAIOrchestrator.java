package org.openecomp.mso.client.orchestration;

import java.util.Optional;
import java.util.logging.Logger;

import org.modelmapper.ModelMapper;
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition;
import org.openecomp.mso.client.aai.AAIResourcesClient;
import org.openecomp.mso.client.aai.entities.AAIEntityObject;
import org.openecomp.mso.client.aai.objects.AAIOwningEntity;
import org.openecomp.mso.client.aai.objects.AAIProject;
import org.openecomp.mso.client.aai.objects.AAIServiceInstance;

public class AAIOrchestrator {
	
	private static Logger LOGGER = Logger.getLogger("AAIOrchestrator");
	
	public void createServiceInstance(ServiceDecomposition serviceDecomp) {
		try{
			ModelMapper modelMapper = new ModelMapper();
			AAIEntityObject serviceInstance = modelMapper.map(serviceDecomp.getServiceInstance(), AAIServiceInstance.class);
			AAIResourcesClient aaiRC = this.getClient();
			aaiRC.createIfNotExists(serviceInstance.getUri(), Optional.of(serviceInstance));
		}catch(Exception ex) {
			String msg = "Failed to create service instance in A&AI.";
			throw new IllegalStateException(msg);
		}
	}
	
	public void deleteServiceInstance(ServiceDecomposition serviceDecomp) {
		try{
			ModelMapper modelMapper = new ModelMapper();
			AAIEntityObject serviceInstance = modelMapper.map(serviceDecomp.getServiceInstance(), AAIServiceInstance.class);
			AAIResourcesClient aaiRC = this.getClient();
			aaiRC.delete(serviceInstance.getUri());
		} catch (Exception ex) {
			String msg = "Failed to delete service instance in A&AI.";
			throw new IllegalStateException(msg);
		}
	}
	
	public void createProject(ServiceDecomposition serviceDecomp) {
		try{
			ModelMapper modelMapper = new ModelMapper();
			AAIEntityObject project = modelMapper.map(serviceDecomp.getProject(), AAIProject.class);
			AAIResourcesClient aaiRC = this.getClient();
			aaiRC.createIfNotExists(project.getUri(), Optional.of(project));
		}catch(Exception ex) {
			String msg = "Failed to create project in A&AI.";
			throw new IllegalStateException(msg);		}
	}
	
	public void createProjectandConnectServiceInstance(ServiceDecomposition serviceDecomp) {
		try{
			ModelMapper modelMapper = new ModelMapper();
			AAIEntityObject project = modelMapper.map(serviceDecomp.getProject(), AAIProject.class);
			AAIEntityObject serviceInstance = modelMapper.map(serviceDecomp.getServiceInstance(), AAIServiceInstance.class);
			AAIResourcesClient aaiRC = this.getClient();
			aaiRC.createIfNotExists(project.getUri(), Optional.of(project)).connect(project.getUri(), serviceInstance.getUri());
		} catch(Exception ex) {
			String msg = "Failed to create project and connect service instance in A&AI.";
			throw new IllegalStateException(msg);
		}
	}
	
	public void createOwningEntity(ServiceDecomposition serviceDecomp) {
		try{
			ModelMapper modelMapper = new ModelMapper();
			AAIEntityObject owningEntity = modelMapper.map(serviceDecomp.getOwningEntity(), AAIOwningEntity.class);
			AAIResourcesClient aaiRC = this.getClient();
			aaiRC.createIfNotExists(owningEntity.getUri(), Optional.of(owningEntity));
		}catch(Exception ex) {
			String msg = "Failed to create owning entity in A&AI.";
			throw new IllegalStateException(msg);
		}
	}
	
	public void createOwningEntityandConnectServiceInstance(ServiceDecomposition serviceDecomp) {
		try{
			ModelMapper modelMapper = new ModelMapper();
			AAIEntityObject owningEntity = modelMapper.map(serviceDecomp.getOwningEntity(), AAIOwningEntity.class);
			AAIEntityObject serviceInstance = modelMapper.map(serviceDecomp.getServiceInstance(), AAIServiceInstance.class);
			AAIResourcesClient aaiRC = this.getClient();
			aaiRC.createIfNotExists(owningEntity.getUri(), Optional.of(owningEntity)).connect(owningEntity.getUri(), serviceInstance.getUri());
		}catch(Exception ex) {
			String msg = "Failed to create owning entity and connect service instance in A&AI.";
			throw new IllegalStateException(msg);		}
	}
	
	protected AAIResourcesClient getClient() {
		return new AAIResourcesClient();
	}

}
