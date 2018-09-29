package org.onap.so.bpmn.common.validation;

import java.util.Set;

import org.onap.so.bpmn.common.BuildingBlockExecution;

public interface BuildingBlockValidator {

	
	/**
	 * Name of the building block to be validated
	 * @return
	 */
	public Set<String> forBuildingBlock();
	
	/**
	 * Determines whether or not the building block should be executed
	 * 
	 * @param execution
	 * @return
	 */
	public boolean validate(BuildingBlockExecution execution);
	
}
