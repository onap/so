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

package org.openecomp.mso.logger;


import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;


/**
 * This class will attempt to initialize MSO log4j when part of a web application.
 * It will look for the logback configuration file logback.xml in the 
 * following order:
 * 1. In an init-param "log.configuration" in web.xml
 * 2. TODO: In a property "log.configuration" in an "application.properties" file
 * 3. In the default location "/etc/ecomp/mso/config"
 * 4. Using the log4j default (check system property log.configuration or
 *    just look on the classpath for logback.xml)
 * 
 *
 */
@WebListener
public class MsoLogInitializer implements ServletContextListener
{
	private static String DEFAULT_LOG4J_PROPERTIES_FILE = "/etc/ecomp/mso/config/logback.xml";
	private static String prefixMsoPropertiesPath = System.getProperty("mso.config.path");
	static  {
		if (prefixMsoPropertiesPath == null) {
			prefixMsoPropertiesPath = "/";
		} else if (!(prefixMsoPropertiesPath.charAt(prefixMsoPropertiesPath.length() - 1) == '/')) {
			prefixMsoPropertiesPath = prefixMsoPropertiesPath + "/";
		}
	}
	
	public MsoLogInitializer () {
	}


	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// Nothing to do...
	}

	@Override
	public void contextInitialized(ServletContextEvent event)
	{
		String logPropertiesFile = null;
		try {
			// Look first in the init-parameters
			String initParam = event.getServletContext().getInitParameter("log.configuration");
			if (initParam != null && fileIsReadable(prefixMsoPropertiesPath + initParam)) {
				logPropertiesFile = prefixMsoPropertiesPath + initParam;
			}
			else if (fileIsReadable(DEFAULT_LOG4J_PROPERTIES_FILE)) {
				logPropertiesFile = DEFAULT_LOG4J_PROPERTIES_FILE;
			}
			
			if (logPropertiesFile != null) {
				// Initialize loggers with the specified file.  If no such file was
				// specified, will use the default Log4j resolution.
				LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory(); 
				JoranConfigurator jc = new JoranConfigurator(); 
				jc.setContext(context); 		
				context.reset(); 
				jc.doConfigure(logPropertiesFile);
				// Try it out
				MsoLogger initLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL);
				initLogger.info(MessageEnum.INIT_LOGGER, logPropertiesFile, "", "");
			}
		} catch (Exception e) {
			MsoLogger initLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL);
			initLogger.error (MessageEnum.INIT_LOGGER_FAIL, "", "", MsoLogger.ErrorCode.UnknownError, "", e);
		}
	}
	
	private boolean fileIsReadable (String filePath) {
		File f = new File(filePath);
		if (f.exists() && f.canRead())
			return true;
		return false;
	}
}
