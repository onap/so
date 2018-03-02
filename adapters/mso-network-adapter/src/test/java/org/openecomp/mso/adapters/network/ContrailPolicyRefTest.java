package org.openecomp.mso.adapters.network;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ContrailPolicyRefTest {

	@Test
	public void ContrailPolicyRef_Test()
	{
		ContrailPolicyRef ref = new ContrailPolicyRef();
		ref.populate("majorVersion 1", "minorVersion 0.02");
		String strJson = ref.toJsonString();
		assertTrue(strJson.contains("majorVersion 1"));
		assertTrue(strJson.contains("minorVersion 0.02"));
	}
	
}