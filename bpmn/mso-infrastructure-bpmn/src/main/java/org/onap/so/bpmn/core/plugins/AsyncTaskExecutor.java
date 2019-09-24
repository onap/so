package org.onap.so.bpmn.core.plugins;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.springframework.stereotype.Component;

@Component
public class AsyncTaskExecutor extends AbstractBpmnParseListener {

    private void injectTaskExecutorExecutionListener(ActivityImpl activity) {
        activity.addListener(ExecutionListener.EVENTNAME_END, new AsyncTaskExecutorListener());
    }

    @Override
    public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
        injectTaskExecutorExecutionListener(activity);
    }
}
