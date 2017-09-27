package org.openecomp.mso.client.sdno;

import java.io.IOException;

@FunctionalInterface annotation
public interface SDNOValidator {
	
	void healthDiagnostic(String vnfName, String uuid) throws IOException, Exception;

}
