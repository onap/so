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
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.application.PreUndeploy;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.onap.logging.filter.spring.MDCTaskDecorator;
import org.onap.so.bpmn.common.DefaultToShortClassNameBeanNameGenerator;
import org.onap.so.db.catalog.beans.Workflow;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.logger.LoggingAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @since Version 1.0
 *
 */

@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = {"org.onap"}, nameGenerator = DefaultToShortClassNameBeanNameGenerator.class,
        excludeFilters = {@Filter(type = FilterType.ANNOTATION, classes = SpringBootApplication.class)})

public class MSOInfrastructureApplication {

    private static final Logger logger = LoggerFactory.getLogger(MSOInfrastructureApplication.class);
    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private CatalogDbClient catalogDbClient;

    @Value("${mso.async.core-pool-size}")
    private int corePoolSize;

    @Value("${mso.async.max-pool-size}")
    private int maxPoolSize;

    @Value("${mso.async.queue-capacity}")
    private int queueCapacity;

    @Value("${mso.bpmn-history-ttl:14}")
    private Integer bpmnHistoryTtl;

    private static final String LOGS_DIR = "logs_dir";
    private static final String BPMN_SUFFIX = ".bpmn";
    private static final String SDC_SOURCE = "sdc";
    private static final int CANNOT_INVOKE_COMMAND = 126;


    private static void setLogsDir() {
        if (System.getProperty(LOGS_DIR) == null) {
            System.getProperties().setProperty(LOGS_DIR, "./logs/bpmn/");
        }
    }

    public static void main(String... args) {
        try {
            SpringApplication.run(MSOInfrastructureApplication.class, args);
            System.getProperties().setProperty("mso.config.path", ".");
            setLogsDir();
        } catch (Exception e) {
            logger.error("Exception has occurred during application startup. App will exit. ", e);
            System.exit(CANNOT_INVOKE_COMMAND);
        }
    }

    @PostConstruct
    public void postConstruct() {
        try {
            RepositoryService repositoryService = processEngine.getRepositoryService();
            DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
            deployCustomWorkflows(deploymentBuilder);
            setBpmnTTL(repositoryService, bpmnHistoryTtl);
        } catch (Exception e) {
            logger.warn("Unable to invoke deploymentBuilder ", e);
        }
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
        logger.debug("Attempting to deploy custom workflows");
        try {
            List<Workflow> workflows = catalogDbClient.findWorkflowBySource(SDC_SOURCE);
            if (workflows != null && !workflows.isEmpty()) {
                for (Workflow workflow : workflows) {
                    String workflowName = workflow.getName();
                    String workflowBody = workflow.getBody();
                    if (!workflowName.endsWith(BPMN_SUFFIX)) {
                        workflowName += BPMN_SUFFIX;
                    }
                    if (workflowBody != null) {
                        logger.info(LoggingAnchor.TWO, "Deploying custom workflow", workflowName);
                        deploymentBuilder.addString(workflowName, workflowBody);
                    }
                    deploymentBuilder.enableDuplicateFiltering(true);
                }
                deploymentBuilder.deploy();
            }
        } catch (Exception e) {
            logger.warn("Unable to deploy custom workflows ", e);
        }
    }

    private void setBpmnTTL(RepositoryService repositoryService, Integer ttl) {
        List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
        for (Deployment deployment : deployments) {
            List<ProcessDefinition> processDefinitions =
                    repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).list();
            for (ProcessDefinition processDefinition : processDefinitions) {
                if (!ttl.equals(processDefinition.getHistoryTimeToLive())) {
                    logger.info("Setting ttl {} for processdefinition {}", ttl, processDefinition.getName());
                    repositoryService.updateProcessDefinitionHistoryTimeToLive(processDefinition.getId(), ttl);
                }
            }
        }
    }
}
