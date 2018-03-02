package org.openecomp.mso.client.adapter.network;

import org.openecomp.mso.adapters.nwrest.CreateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.CreateNetworkResponse;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkRequest;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkResponse;
import org.openecomp.mso.adapters.nwrest.QueryNetworkResponse;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkRequest;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkResponse;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkResponse;

public interface NetworkAdapterClient {
	
	CreateNetworkResponse createNetwork(CreateNetworkRequest req);
	
	DeleteNetworkResponse deleteNetwork(String aaiNetworkId, DeleteNetworkRequest req);
	
	RollbackNetworkResponse rollbackNetwork(String aaiNetworkId, RollbackNetworkRequest req);
	
	QueryNetworkResponse queryNetwork(String aaiNetworkId, String cloudSiteId, String tenantId, String networkStackId, boolean skipAAI, String requestId, String serviceInstanceId);
	
	UpdateNetworkResponse updateNetwork(String aaiNetworkId, UpdateNetworkRequest req);

}
