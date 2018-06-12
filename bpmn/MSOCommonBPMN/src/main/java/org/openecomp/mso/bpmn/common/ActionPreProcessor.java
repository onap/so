package org.openecomp.mso.bpmn.common;


import org.camunda.bpm.engine.delegate.DelegateExecution;

/**
 * interface to be extended by the classes, where pre processing is required
 */
public interface ActionPreProcessor {

    /**
     * method to bind and return the action
     *
     * @return
     */
    String getAction();

    /**
     * method to preform certain pre processing task before BB execution
     *
     * @param execution
     * @return
     */
    boolean process(DelegateExecution execution);
}