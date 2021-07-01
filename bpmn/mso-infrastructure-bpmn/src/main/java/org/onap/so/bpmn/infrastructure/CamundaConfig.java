package org.onap.so.bpmn.infrastructure;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.spring.application.SpringServletProcessApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaConfig {

    @Bean
    public SpringServletProcessApplication springServletProcessApplication() {
        return new SpringServletProcessApplication();
    }

    @Bean
    public ProcessEngineService processEngineService() {
        return BpmPlatform.getProcessEngineService();
    }

    @Bean
    public ProcessEngine processEngine(ProcessEngineService processEngineService) {
        return processEngineService.getDefaultProcessEngine();
    }

    @Bean
    public RepositoryService repositoryService(ProcessEngine processEngine) {
        return processEngine.getRepositoryService();
    }

    @Bean
    public RuntimeService runtimeService(ProcessEngine processEngine) {
        return processEngine.getRuntimeService();
    }
}
