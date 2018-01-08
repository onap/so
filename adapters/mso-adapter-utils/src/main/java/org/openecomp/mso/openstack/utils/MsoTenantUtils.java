/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.mso.openstack.utils;


import java.util.Map;
import org.openecomp.mso.cloud.CloudConfig;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.cloud.CloudIdentity;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.openstack.beans.MsoTenant;
import org.openecomp.mso.openstack.exceptions.MsoCloudSiteNotFound;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;

public abstract class MsoTenantUtils extends MsoCommonUtils {

    protected CloudConfigFactory cloudConfigFactory;
	protected MsoPropertiesFactory msoPropFactory;
	protected static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
	protected MsoJavaProperties msoProps;
    protected CloudConfig cloudConfig;

    public MsoTenantUtils (String msoPropID) {
    	cloudConfigFactory = new CloudConfigFactory();
    	msoPropFactory = new MsoPropertiesFactory();
    	cloudConfig = cloudConfigFactory.getCloudConfig ();

    	LOGGER.debug("msoTenantUtils:" + msoPropID);
		
    	try {
			msoProps = msoPropFactory.getMsoJavaProperties (msoPropID);
		} catch (MsoPropertiesException e) {
			LOGGER.error (MessageEnum.LOAD_PROPERTIES_FAIL, "Unknown. Mso Properties ID not found in cache: " + msoPropID, "", "", MsoLogger.ErrorCode.DataError, "Exception - Mso Properties ID not found in cache", e);
		}
    }

    public abstract String createTenant (String tenantName, String cloudSiteId, Map <String, String> metadata, boolean backout) 
    		throws MsoException;
       
    public abstract MsoTenant queryTenant (String tenantId, String cloudSiteId) 
    		throws MsoException, MsoCloudSiteNotFound;
    
    public abstract MsoTenant queryTenantByName (String tenantName, String cloudSiteId) 
    		throws MsoException, MsoCloudSiteNotFound;

    public abstract boolean deleteTenant (String tenantId, String cloudSiteId) 
    		throws MsoException;

    public abstract String getKeystoneUrl (String regionId, String msoPropID, CloudIdentity cloudIdentity)
    		throws MsoException;

}
