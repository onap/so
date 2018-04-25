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


import javax.ejb.EJB;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.cloud.CloudConfigIdentityMapper;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

/**
 * This class will attempt to initialize Cloud Config when part of a web application.
 * 
 *
 *
 */
@WebListener
public class CloudConfigInitializer implements ServletContextListener
{

	private CloudConfigFactory cloudConfigFactory=new CloudConfigFactory();

	public CloudConfigInitializer () {
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// Nothing to do...
	}


	@Override
	public void contextInitialized(ServletContextEvent event)
	{

		// Note - this logger may be before or after MSO Logging configuration applied
		MsoLogger initLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL);
		try {
			// Look first in the init-parameters
			String msoPropConfigParam = event.getServletContext().getInitParameter("mso.cloud_config.configuration");

			String[] configFileSplit = msoPropConfigParam.split(",");
			for (String msoPropConfig:configFileSplit) {
				String[] msoPropDecoded = msoPropConfig.split("=");

				try {
					cloudConfigFactory.initializeCloudConfig(msoPropDecoded[0], Integer.valueOf(msoPropDecoded[1]));
					initLogger.info(MessageEnum.RA_CONFIG_LOAD, msoPropDecoded[0], "", "");
					initLogger.debug("Mso properties successfully loaded:"+msoPropDecoded[0]+"(Timer(mins):"+Integer.valueOf(msoPropDecoded[1]));
				} catch (NumberFormatException ne) {
					initLogger.error(MessageEnum.RA_CONFIG_EXC, msoPropDecoded[0] + ". MSO Properties failed due to conversion error (in web.xml file)", "", "", MsoLogger.ErrorCode.DataError, "MSO Properties failed due to conversion error (in web.xml file)", ne);
				}
			}

			// Second, obtain class name that will register all mappings
			String msoMapperClassParam = event.getServletContext().getInitParameter("mso.cloud_config.mapper.class");
			if (msoMapperClassParam != null) {
			        Class<?> mapperClass = Class.forName(msoMapperClassParam);
        			if (CloudConfigIdentityMapper.class.isAssignableFrom(mapperClass)) {
        				((CloudConfigIdentityMapper)mapperClass.newInstance()).registerAllMappings();
        				initLogger.info(MessageEnum.RA_CONFIG_LOAD,msoMapperClassParam+"(Openstack authentication mapper class)","","");
        			} else {
        			    initLogger.info(MessageEnum.RA_CONFIG_LOAD,msoMapperClassParam+"(Openstack authentication mapper class not an implementation of CloudConfigIdentityMapper)","","");
        			}
			} else {
			    initLogger.info(MessageEnum.RA_CONFIG_LOAD,"Openstack authentication mapper class not specified in web.xml (ONLY core authentication mechanisms will be loaded)","","");
			}

		}
		catch (Exception e) {
			initLogger.error(MessageEnum.RA_CONFIG_EXC,  "Unknown. MSO Properties failed to initialize completely", "", "", MsoLogger.ErrorCode.AvailabilityError, "Exception - MSO Properties failed to initialize completely", e);
		}
	}
}
