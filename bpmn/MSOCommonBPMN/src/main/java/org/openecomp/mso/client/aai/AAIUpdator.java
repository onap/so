package org.openecomp.mso.client.aai;

import java.io.IOException;

public interface AAIUpdator {
	
	void updateVnfToLocked(String vnfName, String uuid) throws IOException, Exception;
	
	void updateVnfToUnLocked(String vnfName, String uuid) throws IOException, Exception;

}
