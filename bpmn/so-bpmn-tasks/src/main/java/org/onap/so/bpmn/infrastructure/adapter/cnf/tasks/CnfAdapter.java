package org.onap.so.bpmn.infrastructure.adapter.cnf.tasks;

import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.client.adapter.cnf.CnfAdapterClient;
import org.onap.so.client.adapter.cnf.entities.InstanceRequest;
import org.onap.so.client.adapter.cnf.entities.InstanceResponse;
import org.onap.so.client.adapter.cnf.entities.Labels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CnfAdapter {

    @Autowired
    private CnfAdapterClient cnfAdapterClient;

    private static final Logger logger = LoggerFactory.getLogger(CnfAdapter.class);

    public void callCnfAdapter(DelegateExecution execution) throws Exception {
        try {
            InstanceRequest request = new InstanceRequest();
            request.setRbName("test-rbdef");
            request.setRbVersion("v1");
            request.setCloudRegion("krd");
            request.setReleaseName("VF module UUID");
            request.setProfileName("p1");
            Map<String, String> overrideValues = new HashMap<>();
            overrideValues.put("image.tag", "latest");
            overrideValues.put("dcae_collector_ip", "1.2.3.4");
            Map<String, String> labels = new HashMap<String, String>();
            labels.put("custom-label-1", "abcdef");
            request.setLabels(labels);
            request.setOverrideValues(overrideValues);
            InstanceResponse response = cnfAdapterClient.createVfModule(request);
        } catch (Exception ex) {
            logger.error("Exception in callCnfAdapter", ex);
            throw ex;
        }
    }
}
