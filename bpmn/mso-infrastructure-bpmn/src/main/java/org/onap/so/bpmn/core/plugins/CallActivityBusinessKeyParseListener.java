package org.onap.so.bpmn.core.plugins;

import org.camunda.bpm.engine.impl.bpmn.behavior.CallableElementActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.model.CallableElement;
import org.camunda.bpm.engine.impl.el.ElValueProvider;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.springframework.stereotype.Component;


@Component
public class CallActivityBusinessKeyParseListener extends AbstractBpmnParseListener {

    @Override
    public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
        ExpressionManager expressionManager = Context.getProcessEngineConfiguration().getExpressionManager();
        Expression expression = expressionManager.createExpression("#{execution.processBusinessKey}");
        ElValueProvider p = new ElValueProvider(expression);
        CallableElementActivityBehavior callableElementActivityBehavior =
                (CallableElementActivityBehavior) activity.getActivityBehavior();
        CallableElement callableElement = (CallableElement) callableElementActivityBehavior.getCallableElement();
        callableElement.setBusinessKeyValueProvider(p);
        callableElementActivityBehavior.setCallableElement(callableElement);
    }
}
