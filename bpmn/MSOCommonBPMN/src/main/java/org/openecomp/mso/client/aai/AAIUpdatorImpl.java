package org.openecomp.mso.client.aai;

import org.springframework.beans.factory.annotation.Autowired;

public class AAIUpdatorImpl implements AAIUpdator {
	
	@Autowired
	protected AAIRestClient client;
	
	public AAIRestClient getClient() {
		return client;
	}


	public void setClient(AAIRestClient client) {
		this.client = client;
	}

	@Override
	public void updateVnfToLocked(String vnfId, String uuid) throws Exception {
		client.updateMaintenceFlagVnfId(vnfId, true, uuid);
	}

	@Override
	public void updateVnfToUnLocked(String vnfId, String uuid) throws Exception {
		client.updateMaintenceFlagVnfId(vnfId, false, uuid);
	}

}
