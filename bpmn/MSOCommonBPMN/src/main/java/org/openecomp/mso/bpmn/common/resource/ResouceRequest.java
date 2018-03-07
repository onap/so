package org.openecomp.mso.bpmn.common.resource;

import java.util.HashMap;
import java.util.Map;

public class ResouceRequest {
    private Map<String, Object> resourceInputs = new HashMap<>();

    public void addResourceInput(String key, Object value) {
        resourceInputs.put(key, value);
    }

    public Object getValueForProperty(String key) {
        return resourceInputs.get(key);
    }
}
