package org.openecomp.mso.client.sdno;

import java.io.IOException;

public interface SDNOValidator {
	
	void healthDiagnostic(String vnfName, String uuid) throws IOException, Exception;

}
