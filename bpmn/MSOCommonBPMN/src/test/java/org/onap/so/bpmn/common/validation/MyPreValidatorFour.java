package org.onap.so.bpmn.common.validation;

import java.util.Collections;
import java.util.Set;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.springframework.stereotype.Component;

@Component
public class MyPreValidatorFour implements PreBuildingBlockValidator {

	@Override
	public Set<String> forBuildingBlock() {
		return Collections.singleton("test2");
	}

	@Override
	public boolean validate(BuildingBlockExecution exeuction) {
		return true;
	}

}
