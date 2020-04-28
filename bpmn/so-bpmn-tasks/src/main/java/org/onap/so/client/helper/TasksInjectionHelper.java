package org.onap.so.client.helper;

import org.onap.so.client.sdno.SDNOValidator;
import org.onap.so.client.sdno.SDNOValidatorImpl;
import org.springframework.stereotype.Component;

@Component
public class TasksInjectionHelper {



    public SDNOValidator getSdnoValidator() {
        return new SDNOValidatorImpl();
    }

}
