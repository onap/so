package org.onap.so.bpmn.infrastructure.workflow.tasks;

import java.util.List;
import java.util.Optional;

import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.Relationships;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WorkflowActionExtractResourcesAAI {
	private static final Logger logger = LoggerFactory.getLogger(WorkflowActionExtractResourcesAAI.class);

	public Optional<Configuration> extractRelationshipsConfiguration(Relationships relationships) {
		List<AAIResultWrapper> configurations = relationships.getByType(AAIObjectType.CONFIGURATION);
		for(AAIResultWrapper configWrapper : configurations) {
			Optional<Configuration> config = configWrapper.asBean(Configuration.class);
			if(config.isPresent()){
				return config;
			}
		}
		return Optional.empty();
	}

	public Optional<Relationships> extractRelationshipsVnfc(Relationships relationships) {
		List<AAIResultWrapper> vnfcs = relationships.getByType(AAIObjectType.VNFC);
		for(AAIResultWrapper vnfcWrapper : vnfcs){
			if(vnfcWrapper.getRelationships().isPresent()){
				return vnfcWrapper.getRelationships();
			}
		}
		return Optional.empty();
	}
}
