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

package org.onap.so.bpmn.common;

import java.util.List;
import java.util.concurrent.Executor;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.PreUndeploy;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.onap.so.bpmn.common.DefaultToShortClassNameBeanNameGenerator;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @since Version 1.0
 *
 */

@SpringBootApplication
@EnableProcessApplication("MSO Common Application")
@EnableAsync
@ComponentScan(basePackages = { "org.onap" }, nameGenerator = DefaultToShortClassNameBeanNameGenerator.class, excludeFilters = {
				@Filter(type = FilterType.ANNOTATION, classes = SpringBootApplication.class)})
public class MSOCommonApplication {

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL,
			MSOCommonApplication.class);

	@Value("${mso.async.core-pool-size}")
	private int corePoolSize;

	@Value("${mso.async.max-pool-size}")
	private int maxPoolSize;

	@Value("${mso.async.queue-capacity}")
	private int queueCapacity;

	private static final String LOGS_DIR = "logs_dir";


	private static void setLogsDir() {
		if (System.getProperty(LOGS_DIR) == null) {
			System.getProperties().setProperty(LOGS_DIR, "./logs/bpmn/");
		}
	}

	public static void main(String... args) {
		SpringApplication.run(MSOCommonApplication.class, args);
		System.getProperties().setProperty("mso.config.path", ".");
		setLogsDir();
	}

	@PostDeploy
	public void postDeploy(ProcessEngine processEngineInstance) {
		long startTime = System.currentTimeMillis();

		msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
				"Post deployment complete...");
	}

	@PreUndeploy
	public void cleanup(ProcessEngine processEngine, ProcessApplicationInfo processApplicationInfo,
			List<ProcessEngine> processEngines) {
		long startTime = System.currentTimeMillis();

		msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
				"Pre Undeploy complete...");

	}

	@Bean
	public Executor asyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		executor.setCorePoolSize(corePoolSize);
		executor.setMaxPoolSize(maxPoolSize);
		executor.setQueueCapacity(queueCapacity);
		executor.setThreadNamePrefix("Camunda-");
		executor.initialize();
		return executor;
	}
}