package org.openecomp.mso.bpmn.common;

import org.openecomp.mso.client.aai.AAICommonObjectMapperProvider;
import org.openecomp.mso.client.aai.AAIResourcesClient;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper;
import org.openecomp.mso.client.sdno.SDNOValidator;
import org.openecomp.mso.client.sdno.SDNOValidatorImpl;
import org.springframework.stereotype.Component;

/*
 * This object is intended to be a helper for acquiring classes
 * that cannot be acquired via Spring injection.
 * 
 * It brings two benefits:
 * 
 * 1) Enforces acquisition of a new copy of these classes every
 *    time to help with picking up properties files changes, etc
 * 2) The classes are exposed in such a way that mocks of them can
 *    still be injected when testing the Spring objects that use
 *    them 
 */

@Component
public class InjectionHelper {
	public AAIResourcesClient getAaiClient() {
		return new AAIResourcesClient();
	}
	
	public SDNOValidator getSdnoValidator() {
		return new SDNOValidatorImpl();
	}

	public AAICommonObjectMapperProvider getAaiCommonObjectMapperProvider() {
		return new AAICommonObjectMapperProvider();
	}
	
	public AAIResultWrapper getAaiResultWrapper(String json) {
		return new AAIResultWrapper(json);
	}
}
