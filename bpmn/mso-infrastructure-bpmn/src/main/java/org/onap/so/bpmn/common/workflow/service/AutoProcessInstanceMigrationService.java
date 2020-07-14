package org.onap.so.bpmn.common.workflow.service;

import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanExecutionBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class AutoProcessInstanceMigrationService {

    private static Logger logger = LoggerFactory.getLogger(AutoProcessInstanceMigrationService.class);

    @Autowired
    private Environment env;
    @Autowired
    protected ProcessEngine processEngine;

    @Value("${migration.autoMigrationEnabled:false}")
    private boolean autoMigrationEnabled;

    private RuntimeService runtimeService;
    private RepositoryService repositoryService;
    private List<String> processDefinitionKeys;

    @EventListener(ApplicationReadyEvent.class)
    protected void executeAutoProcessInstanceMigrations() {
        if (autoMigrationEnabled) {
            runtimeService = processEngine.getRuntimeService();
            repositoryService = processEngine.getRepositoryService();
            for (ProcessDefinition definition : getProcessDefinitions()) {
                for (ProcessDefinition procDefOld : getOldProcessDefinitions(definition.getKey(),
                        definition.getVersion())) {
                    migrate(procDefOld.getId(), definition.getId());
                }
            }
        }
    }

    protected List<ProcessDefinition> getProcessDefinitions() {

        List<ProcessDefinition> processDefinitions = new ArrayList<ProcessDefinition>();
        processDefinitionKeys = env.getProperty("migration.processDefinitionKeys", List.class, new ArrayList<String>());
        for (String key : processDefinitionKeys) {
            processDefinitions.add(repositoryService.createProcessDefinitionQuery().processDefinitionKey(key)
                    .latestVersion().singleResult());
        }
        return processDefinitions;
    }

    private void migrate(String sourceProcessDefinitionId, String targetProcessDefinitionId) {
        MigrationPlan migrationPlan =
                runtimeService.createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
                        .mapEqualActivities().updateEventTriggers().build();
        List<String> activityIds = new ArrayList<>();

        for (MigrationInstruction instruction : migrationPlan.getInstructions()) {
            activityIds.add(instruction.getSourceActivityId());
        }
        for (String activityId : activityIds) {
            ProcessInstanceQuery activeProcessInstancesQuery = runtimeService.createProcessInstanceQuery()
                    .processDefinitionId(sourceProcessDefinitionId).activityIdIn(activityId).active();
            if (!activeProcessInstancesQuery.list().isEmpty()) {
                logger.info("Migrating {} process instance(s) from {} to {}",
                        Long.valueOf(activeProcessInstancesQuery.count()), sourceProcessDefinitionId,
                        targetProcessDefinitionId);
                MigrationPlanExecutionBuilder migration =
                        runtimeService.newMigration(migrationPlan).processInstanceQuery(activeProcessInstancesQuery);
                migration.executeAsync();
            }
        }
        suspendEmptyProcessDefinition(sourceProcessDefinitionId);
    }

    private void suspendEmptyProcessDefinition(String sourceProcessDefinitionId) {
        List<ProcessInstance> activeProcessInstances = runtimeService.createProcessInstanceQuery()
                .processDefinitionId(sourceProcessDefinitionId).active().list();
        if (activeProcessInstances.isEmpty()) {
            repositoryService.suspendProcessDefinitionById(sourceProcessDefinitionId);
        } else {
            logger.info("Unable to migrate {} process instance(s) from {}",
                    Integer.valueOf(activeProcessInstances.size()), sourceProcessDefinitionId);
        }
    }

    protected List<ProcessDefinition> getOldProcessDefinitions(String key, int version) {
        List<ProcessDefinition> processDefinitions =
                repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).list();
        List<ProcessDefinition> oldProcessDefinitions = new ArrayList<>();
        for (ProcessDefinition processDef : processDefinitions) {
            if (!processDef.isSuspended() && (processDef.getVersion() != version)) {
                oldProcessDefinitions.add(processDef);
            }
        }
        return oldProcessDefinitions;
    }
}
