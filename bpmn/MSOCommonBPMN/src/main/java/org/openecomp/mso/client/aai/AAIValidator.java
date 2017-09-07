package org.openecomp.mso.client.aai;

import java.io.IOException;

public interface AAIValidator {
	
	boolean isPhysicalServerLocked(String hostName, String transactionLoggingUuid) throws IOException;
	
	boolean isVNFLocked(String vnfId, String transactionLoggingUuid) throws IOException, Exception;
	

}