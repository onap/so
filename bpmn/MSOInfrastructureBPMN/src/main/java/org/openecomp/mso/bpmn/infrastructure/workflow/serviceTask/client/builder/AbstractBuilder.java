package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.builder;

/**
 * Created by 10112215 on 2017/9/20.
 */
public interface AbstractBuilder<IN, OUT> {
     OUT build(IN input) throws Exception;
}
