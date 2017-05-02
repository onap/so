/**
 *
 */
package org.openecomp.mso.cloud.servertype;

import java.util.Map;

import org.openecomp.mso.cloud.CloudIdentity;
import org.openecomp.mso.openstack.beans.MsoTenant;
import org.openecomp.mso.openstack.exceptions.MsoCloudSiteNotFound;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.utils.MsoTenantUtils;


public class NewServerTypeUtils extends MsoTenantUtils {

	/**
	 * @param msoPropID
	 */
	public NewServerTypeUtils(String msoPropID) {
		super(msoPropID);
	}

	@Override
    public String createTenant(String tenantName, String cloudSiteId, Map<String, String> metadata, boolean backout)
            throws MsoException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MsoTenant queryTenant(String tenantId, String cloudSiteId) throws MsoException, MsoCloudSiteNotFound {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MsoTenant queryTenantByName(String tenantName, String cloudSiteId)
            throws MsoException, MsoCloudSiteNotFound {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean deleteTenant(String tenantId, String cloudSiteId) throws MsoException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getKeystoneUrl(String regionId, String msoPropID, CloudIdentity cloudIdentity)
            throws MsoException {
        return msoPropID + ":" + regionId + ":NewServerTypeKeystoneURL/" + cloudIdentity.getIdentityUrl();
    }

}
