package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import java.util.Optional;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ControllerActorSelection {
	private static final Logger logger = LoggerFactory.getLogger(ControllerActorSelection.class);
	public static final String CONTROLLER_ACTOR = "controller_actor";
	@Autowired
	private ExceptionBuilder exceptionUtil;
	@Autowired
	private CatalogDbClient catalogDbClient;
	@Autowired
	private ExtractPojosForBB extractPojosForBB;

	public void setControllerActor(BuildingBlockExecution execution) {
		try {
			GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
			String modleUuid = genericVnf.getModelInfoGenericVnf().getModelCustomizationUuid();
			VnfResourceCustomization vnfResourceCustomization = catalogDbClient
					.getVnfResourceCustomizationByModelCustomizationUUID(modleUuid);

			// Fetching Controller Actor at VNF level
			String controller_actor = Optional.ofNullable(vnfResourceCustomization.getControllerActor()).orElse("NULL");
			execution.setVariable(CONTROLLER_ACTOR, controller_actor);

		} catch (Exception ex) {
			logger.error("An exception occurred when fetching Controller Actor from Service ", ex);
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);

		}
	}

}
