package org.openecomp.mso.client.aai;

import org.openecomp.mso.client.aai.entities.Configuration;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;
import org.openecomp.mso.serviceinstancebeans.RequestDetails;

public class AAIConfigurationClient {

	private AAIResourcesClient aaiClient;

	private static final String ORCHESTRATION_STATUS = "PendingCreate";

	public AAIConfigurationClient() {
		aaiClient = new AAIResourcesClient();
	}

	public void createConfiguration(RequestDetails requestDetails, String configurationId, String configurationType,
			String configurationSubType) {
		Configuration payload = new Configuration();
		payload.setConfigurationId(configurationId);
		payload.setConfigurationType(configurationType);
		payload.setConfigurationSubType(configurationSubType);
		payload.setModelInvariantId(requestDetails.getModelInfo().getModelInvariantId());
		payload.setModelVersionId(requestDetails.getModelInfo().getModelVersionId());
		payload.setOrchestrationStatus(ORCHESTRATION_STATUS);
		payload.setOperationalStatus("");
		AAIResourceUri uri = getConfigurationURI(payload.getConfigurationId());
		payload.setConfigurationSelflink(uri.build().getPath());
		payload.setModelCustomizationId(requestDetails.getModelInfo().getModelCustomizationId());

		aaiClient.create(uri, payload);
	}

	public void deleteConfiguration(String uuid) {
		aaiClient.delete(getConfigurationURI(uuid));
	}

	public void updateOrchestrationStatus(String uuid, String payload) {
		aaiClient.update(getConfigurationURI(uuid), payload);
	}

	public Configuration getConfiguration(String uuid) {
		return aaiClient.get(Configuration.class, getConfigurationURI(uuid));
	}

	public boolean configurationExists(String uuid) {
		return aaiClient.exists(getConfigurationURI(uuid));
	}

	public AAIResourceUri getConfigurationURI(String uuid) {
		return AAIUriFactory.createResourceUri(AAIObjectType.CONFIGURATION, uuid);
	}
}
