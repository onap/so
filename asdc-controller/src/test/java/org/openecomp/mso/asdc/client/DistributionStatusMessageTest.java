package org.openecomp.mso.asdc.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.onap.sdc.utils.DistributionStatusEnum;

public class DistributionStatusMessageTest {
	
	@Test
	public void distributionStatusMessageTest() {
		String artifactUrl = "artifactUrl";
		String consumerId = "consumerId";
		String distributionId = "distributionId";
		Long timestamp = 123L;
		DistributionStatusMessage distributionStatusMessage = new DistributionStatusMessage(
				artifactUrl, consumerId, distributionId, DistributionStatusEnum.DEPLOY_OK, timestamp);
		
		assertEquals(artifactUrl, distributionStatusMessage.getArtifactURL());
		assertEquals(consumerId, distributionStatusMessage.getConsumerID());
		assertEquals(distributionId, distributionStatusMessage.getDistributionID());
		assertEquals(DistributionStatusEnum.DEPLOY_OK, distributionStatusMessage.getStatus());
		assertEquals(123L, distributionStatusMessage.getTimestamp());
	}
}
