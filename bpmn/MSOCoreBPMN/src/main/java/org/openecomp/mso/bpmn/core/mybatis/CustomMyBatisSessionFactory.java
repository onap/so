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

package org.openecomp.mso.bpmn.core.mybatis;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.interceptor.CommandContextInterceptor;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.camunda.bpm.engine.impl.interceptor.LogInterceptor;
import org.camunda.bpm.engine.impl.util.ReflectUtil;


/**
 * A special process engine that provides access to MyBatis mappings.
 * @version 1.0
 */
public class CustomMyBatisSessionFactory extends
		StandaloneProcessEngineConfiguration {

	private String resourceName;

	/**
	 * Overridden to ensure nobody ever tries to initialize this process engine
	 * in the normal way.  We are using this process engine only for MyBatis
	 * access.
	 */
	@Override
	protected void init() {
		throw new UnsupportedOperationException("init");
	}

	/**
	 * Initialize the ProcessEngineConfiguration from an existing one, just
	 * using the database settings to initialize the database / MyBatis stuff.
	 */
	public void initFromProcessEngineConfiguration(
			ProcessEngineConfigurationImpl processEngineConfiguration,
			String resourceName) {
		this.resourceName = resourceName;

		setDatabaseType(processEngineConfiguration.getDatabaseType());
		setDataSource(processEngineConfiguration.getDataSource());
		setDatabaseTablePrefix(processEngineConfiguration
				.getDatabaseTablePrefix());

		initDataSource();
		// initVariableTypes();
		initCommandContextFactory();
		initTransactionFactory();
		initTransactionContextFactory();
		initCommandExecutors();
		initSqlSessionFactory();
		initIncidentHandlers();
		initIdentityProviderSessionFactory();
		initSessionFactories();
	}

	/**
	 * In order to always open a new command context set the property
	 * "alwaysOpenNew" to true inside the CommandContextInterceptor.
	 * 
	 * If you execute the custom queries inside the process engine (for example
	 * in a service task), you have to do this.
	 */
	@Override
	protected Collection<? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequired() {
		List<CommandInterceptor> defaultCommandInterceptorsTxRequired =
            new ArrayList<>();
		defaultCommandInterceptorsTxRequired.add(new LogInterceptor());
		defaultCommandInterceptorsTxRequired.add(new CommandContextInterceptor(
				commandContextFactory, this, true));
		return defaultCommandInterceptorsTxRequired;
	}

	@Override
	protected InputStream getMyBatisXmlConfigurationSteam() {
		return ReflectUtil.getResourceAsStream(resourceName);
	}
}
