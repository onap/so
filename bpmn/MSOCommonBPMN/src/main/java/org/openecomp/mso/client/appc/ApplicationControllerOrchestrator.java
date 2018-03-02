package org.openecomp.mso.client.appc;

import java.util.Optional;

import org.openecomp.mso.client.appc.ApplicationControllerSupport.StatusCategory;

import org.onap.appc.client.lcm.model.Action;
import org.onap.appc.client.lcm.model.ActionIdentifiers;
import org.onap.appc.client.lcm.model.Payload;
import org.onap.appc.client.lcm.model.Status;

public class ApplicationControllerOrchestrator {

	public Status vnfCommand(Action action, String requestId, String vnfId, Optional<String> request) throws ApplicationControllerOrchestratorException {
		ApplicationControllerClient client = new ApplicationControllerClient();
		Status status;
		ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
		actionIdentifiers.setVnfId(vnfId);
		Payload payload = null;
		if (request.isPresent()) {
			payload = new Payload(request.get());
		}
		status = client.runCommand(action, actionIdentifiers, payload, requestId);
		if (ApplicationControllerSupport.getCategoryOf(status).equals(StatusCategory.ERROR)) {
			throw new ApplicationControllerOrchestratorException(status.getMessage(), status.getCode());
		} else {
			return status;
		}
	}
}
