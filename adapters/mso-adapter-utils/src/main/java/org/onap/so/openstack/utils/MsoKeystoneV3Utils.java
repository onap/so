package org.onap.so.openstack.utils;

import java.util.Map;

import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.openstack.beans.MsoTenant;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;
import org.onap.so.openstack.exceptions.MsoException;
import org.springframework.stereotype.Component;

@Component
public class MsoKeystoneV3Utils extends MsoTenantUtils {

	@Override
	public String createTenant(String tenantName, String cloudSiteId, Map<String, String> metadata, boolean backout)
			throws MsoException {
		throw new UnsupportedOperationException();
	}

	@Override
	public MsoTenant queryTenant(String tenantId, String cloudSiteId) throws MsoException, MsoCloudSiteNotFound {
		throw new UnsupportedOperationException();
	}

	@Override
	public MsoTenant queryTenantByName(String tenantName, String cloudSiteId)
			throws MsoException, MsoCloudSiteNotFound {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean deleteTenant(String tenantId, String cloudSiteId) throws MsoException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getKeystoneUrl(String regionId, CloudIdentity cloudIdentity) throws MsoException {
		return cloudIdentity.getIdentityUrl();
	}

}
