package org.openecomp.mso.client.aai;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.openecomp.aai.domain.yang.GenericVnf;
import org.openecomp.aai.domain.yang.Pserver;
import org.openecomp.aai.domain.yang.Pservers;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface AAIRestClient {
	
	Pservers getPhysicalServers(String hostName, String uuid);
	
	List<Pserver> getPhysicalServerByVnfId(String vnfId, String transactionLoggingUuid) throws UnsupportedEncodingException, JsonParseException, JsonMappingException, IOException;
	
	void updateMaintenceFlag(String vnfId,boolean inMaint, String transactionLoggingUuid) throws Exception;

	void updateMaintenceFlagVnfId(String vnfId, boolean inMaint, String transactionLoggingUuid) throws Exception;
	
	GenericVnf getVnfByName(String vnfId,  String transactionLoggingUuid) throws Exception;
}
