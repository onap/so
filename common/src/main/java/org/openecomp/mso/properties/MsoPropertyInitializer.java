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

package org.openecomp.mso.properties;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

/**
 * This class will attempt to initialize MSO Properties when part of a web application.
 * It will look for the configuration file mso.properties in the
 * following order:
 * 1. In an init-param "mso.configuration" in web.xml
 * 2. In a system property "mso.configuration"
 * 3. In the default location "/etc/ecomp/mso/config/mso.properties"
 *
 * If all else fails, the MSO Properties will go uninitialized, and will
 * attempt to use the default constructors within the MsoProperties class.
 *
 *
 */
@WebListener
public class MsoPropertyInitializer implements ServletContextListener
{

	private MsoPropertiesFactory msoPropertiesFactory=new MsoPropertiesFactory();

	public MsoPropertyInitializer () {
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
			String msoPropConfigParam = event.getServletContext().getInitParameter("mso.configuration");
			if (msoPropConfigParam != null && !msoPropConfigParam.isEmpty() ) {
				String[] configFileSplit = msoPropConfigParam.split(",");
				for (String msoPropConfig:configFileSplit) {
					String[] msoPropDecoded = msoPropConfig.split("=");
	
					try {
						msoPropertiesFactory.initializeMsoProperties(msoPropDecoded[0], msoPropDecoded[1]);
						initLogger.info(MessageEnum.LOAD_PROPERTIES_SUC, msoPropDecoded[1], "", "");
						initLogger.debug("Mso properties successfully loaded:"+msoPropDecoded[1]+",ID:"+msoPropDecoded[0]+")");
					} catch (MsoPropertiesException e) {
						initLogger.error(MessageEnum.LOAD_PROPERTIES_FAIL, msoPropDecoded[1] + ". MSO Properties failed due to an mso properties exception", "", "", MsoLogger.ErrorCode.DataError, "Error in contextInitialized", e);
					}
				}
			}
		}
		catch (Exception e) {
			initLogger.error(MessageEnum.LOAD_PROPERTIES_FAIL, "Unknown. MSO Properties failed to initialize completely", "", "", MsoLogger.ErrorCode.DataError, "", e);
		}
	}
}
