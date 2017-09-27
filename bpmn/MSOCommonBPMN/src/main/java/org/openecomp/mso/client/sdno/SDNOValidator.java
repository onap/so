package org.openecomp.mso.client.sdno;

import java.io.IOException;

@FunctionalInterface
public interface SDNOValidator {
	
	void healthDiagnostic(String vnfName, String uuid) throws IOException, Exception;

}
