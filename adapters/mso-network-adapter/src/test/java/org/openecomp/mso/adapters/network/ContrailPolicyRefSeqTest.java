package org.openecomp.mso.adapters.network;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ContrailPolicyRefSeqTest {
	@Test
	public void ContrailPolicyRefSeqJson_Test()
	{
		ContrailPolicyRefSeq cprs = new ContrailPolicyRefSeq("majorVersion 1","minorVersion 0.02");
		assertTrue(cprs.toString().contains("majorVersion 1"));
		assertTrue(cprs.toString().contains("minorVersion 0.02"));
	}
	
}
