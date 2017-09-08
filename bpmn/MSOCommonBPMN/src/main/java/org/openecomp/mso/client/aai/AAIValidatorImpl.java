package org.openecomp.mso.client.aai;

import java.io.IOException;
import java.util.List;

import org.openecomp.aai.domain.yang.GenericVnf;
import org.openecomp.aai.domain.yang.Pserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class AAIValidatorImpl implements AAIValidator {


	@Autowired
	protected AAIRestClient client;
	
	public AAIRestClient getClient() {
		return client;
	}


	public void setClient(AAIRestClient client) {
		this.client = client;
	}

	@Override
	public boolean isPhysicalServerLocked(String vnfId, String transactionLoggingUuid) throws IOException {
		List<Pserver> pservers;
		boolean isLocked = false;
		pservers = client.getPhysicalServerByVnfId(vnfId, transactionLoggingUuid);
		for (Pserver pserver : pservers)
			if (pserver.isInMaint())
				isLocked = true;
		
		return isLocked;
	}

	@Override
	public boolean isVNFLocked(String vnfId, String transactionLoggingUuid) throws Exception {
		boolean isLocked = false;
		GenericVnf genericVnf = client.getVnfByName(vnfId, transactionLoggingUuid);
		if (genericVnf.isInMaint())
			isLocked = true;

		return isLocked;
	}

}
