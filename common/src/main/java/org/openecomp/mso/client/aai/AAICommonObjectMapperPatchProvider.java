package org.openecomp.mso.client.aai;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class AAICommonObjectMapperPatchProvider extends AAICommonObjectMapperProvider {

	
	public AAICommonObjectMapperPatchProvider() {
		super();
		EmptyStringToNullSerializer sp = new EmptyStringToNullSerializer(); 
		SimpleModule emptyStringModule = new SimpleModule();
		emptyStringModule.addSerializer(String.class, sp);
		mapper.registerModule(emptyStringModule);
	}
}
