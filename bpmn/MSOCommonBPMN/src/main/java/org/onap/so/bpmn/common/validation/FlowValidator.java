package org.onap.so.bpmn.common.validation;

import java.util.Set;

import org.onap.so.bpmn.common.BuildingBlockExecution;

public interface FlowValidator {

	/**
	 * Names of items to be validated
	 * @return
	 */
	public Set<String> forItems();
	
	/**
	 * Determines whether or not the workflow should be executed
	 * 
	 * @param execution
	 * @return
	 */
	public boolean validate(BuildingBlockExecution execution);
	
}
