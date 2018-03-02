package org.openecomp.mso.client.aai;

import java.util.List;
import java.util.Optional;

import org.openecomp.mso.client.aai.entities.AAIError;
import org.openecomp.mso.client.aai.entities.ServiceException;

public class AAIErrorFormatter {

	private final AAIError error;
	public AAIErrorFormatter(AAIError error) {
		this.error = error;
	}
	
	public String getMessage() {
		if (error.getRequestError() != null && 
			error.getRequestError().getServiceException() != null) {
			ServiceException serviceException = error.getRequestError().getServiceException();
			return this.fillInTemplate(serviceException.getText(), serviceException.getVariables());
		}
		
		return "no parsable error message found";
	}
	
	protected String fillInTemplate(String text, List<String> variables) {
		for (int i = 0; i < variables.size(); i++) {
			variables.set(i, this.format(variables.get(i), variables));
		}
		
		return format(text, variables);
	}
	
	protected String format(String s, List<String> variables) {
		return String.format(s.replaceAll("%(\\d+)", "%$1\\$s"), variables.toArray());
	}
}
