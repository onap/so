package org.onap.so.bpmn.infrastructure.adapter.cnf.tasks;

import java.io.IOException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowAction;
import org.onap.so.client.adapter.cnf.CnfAdapterClient;
import org.onap.so.client.adapter.cnf.CnfAdapterClientException;
import org.onap.so.client.adapter.cnf.entities.InstanceRequest;
import org.onap.so.client.adapter.cnf.entities.InstanceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CnfAdapter {

    @Autowired
    private CnfAdapterClient cnfAdapterClient;
    
    private static final Logger logger = LoggerFactory.getLogger(CnfAdapter.class);
    
	public void callCnfAdapter(DelegateExecution execution) throws Exception {
		try {
			final String instanceRequest = (String) execution.getVariable("instanceRequest");
			InstanceRequest request = new ObjectMapper().readValue(instanceRequest, InstanceRequest.class);
			InstanceResponse response = cnfAdapterClient.createVfModule(request);
		} catch (Exception ex) {
        	logger.error("Exception in callCnfAdapter", ex);
            throw ex;
        }
	}
}
