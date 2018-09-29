package org.onap.so.bpmn.common.validation;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Priority;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.springframework.stereotype.Component;

@Priority(1)
@Component
public class MyPreValidatorTwo implements PreBuildingBlockValidator {

	@Override
	public Set<String> forBuildingBlock() {
		return Collections.singleton("test");
	}

	@Override
	public boolean validate(BuildingBlockExecution exeuction) {
		return false;
	}

}
