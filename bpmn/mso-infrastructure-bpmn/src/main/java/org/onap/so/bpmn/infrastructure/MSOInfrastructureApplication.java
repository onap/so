/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.infrastructure;

import java.util.List;
import java.util.concurrent.Executor;
import com.google.common.base.Strings;
import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.PreUndeploy;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.onap.so.bpmn.common.DefaultToShortClassNameBeanNameGenerator;
import org.onap.so.db.catalog.beans.Workflow;
import org.onap.so.db.catalog.data.repository.WorkflowRepository;
import org.onap.so.logging.jaxrs.filter.MDCTaskDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @since Version 1.0
 *
 */

@SpringBootApplication
@EnableAsync
@EnableJpaRepositories("org.onap.so.db.catalog.data.repository")
@EntityScan({"org.onap.so.db.catalog.beans"})
@ComponentScan(basePackages = {"org.onap"}, nameGenerator = DefaultToShortClassNameBeanNameGenerator.class,
        excludeFilters = {@Filter(type = FilterType.ANNOTATION, classes = SpringBootApplication.class)})

public class MSOInfrastructureApplication {

    private static final Logger logger = LoggerFactory.getLogger(MSOInfrastructureApplication.class);

    @Autowired
    private WorkflowRepository workflowRepository;

    @Value("${mso.async.core-pool-size}")
    private int corePoolSize;

    @Value("${mso.async.max-pool-size}")
    private int maxPoolSize;

    @Value("${mso.async.queue-capacity}")
    private int queueCapacity;

    private static final String LOGS_DIR = "logs_dir";
    private static final String BPMN_SUFFIX = ".bpmn";


    private static void setLogsDir() {
        if (System.getProperty(LOGS_DIR) == null) {
            System.getProperties().setProperty(LOGS_DIR, "./logs/bpmn/");
        }
    }

    public static void main(String... args) {
        SpringApplication.run(MSOInfrastructureApplication.class, args);
        System.getProperties().setProperty("mso.config.path", ".");
        setLogsDir();
    }

    @PostDeploy
    public void postDeploy(ProcessEngine processEngineInstance) {
        DeploymentBuilder deploymentBuilder = processEngineInstance.getRepositoryService().createDeployment();
        deployCustomWorkflows(deploymentBuilder);
    }

    @PreUndeploy
    public void cleanup(ProcessEngine processEngine, ProcessApplicationInfo processApplicationInfo,
            List<ProcessEngine> processEngines) {}

    @Bean
    @Primary
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setTaskDecorator(new MDCTaskDecorator());
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("Camunda-");
        executor.initialize();
        return executor;
    }

    public void deployCustomWorkflows(DeploymentBuilder deploymentBuilder) {
        if (workflowRepository == null) {
            return;
        }
        List<Workflow> workflows = workflowRepository.findAll();
        if (workflows != null && workflows.size() != 0) {
            for (Workflow workflow : workflows) {
                String workflowName = workflow.getName();
                String workflowBody = workflow.getBody();
                if (!workflowName.endsWith(BPMN_SUFFIX)) {
                    workflowName += BPMN_SUFFIX;
                }
                if (workflowBody != null) {
                    logger.info(Strings.repeat("{} ", 2), "Deploying custom workflow", workflowName);
                    deploymentBuilder.addString(workflowName, workflowBody);
                }
            }
            deploymentBuilder.deploy();
        }
    }
}
