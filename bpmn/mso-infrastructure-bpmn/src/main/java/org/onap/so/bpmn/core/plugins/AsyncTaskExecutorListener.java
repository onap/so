package org.onap.so.bpmn.core.plugins;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.onap.so.bpmn.common.listener.validation.WorkflowActionListenerRunner;
import org.onap.so.spring.SpringContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncTaskExecutorListener implements ExecutionListener {
    private final Logger logger = LoggerFactory.getLogger(AsyncTaskExecutorListener.class);


    private WorkflowActionListenerRunner listenerRunner;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        if (!isBlank(execution.getCurrentActivityName())) {
            try {
                String id = execution.getId();
                if (id != null) {
                    RepositoryService repositoryService = execution.getProcessEngineServices().getRepositoryService();
                    String processName = repositoryService.createProcessDefinitionQuery()
                            .processDefinitionId(execution.getProcessDefinitionId()).singleResult().getName();
                    logger.info("ProcessName : {}", processName);
                    if (processName != null) {
                        listenerRunner =
                                SpringContextHelper.getAppContext().getBean(WorkflowActionListenerRunner.class);
                        listenerRunner.executeAsyncListeners(processName, execution, ExecutionListener.EVENTNAME_END);
                    }
                }
            } catch (Exception e) {
                logger.error("Error occured in executing Complete Task Listeners", e);
            }
        }
    }

    private boolean isBlank(Object object) {
        return object == null || "".equals(object.toString().trim());
    }
}
