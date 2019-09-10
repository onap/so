package org.onap.so.utils;

import org.camunda.bpm.client.task.ExternalTask;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public abstract class ExternalTaskUtils {

    @Autowired
    Environment env;

    private static final Logger logger = LoggerFactory.getLogger(ExternalTaskUtils.class);

    public long calculateRetryDelay(int currentRetries) {
        int retrySequence = getRetrySequence().length - currentRetries;
        return Integer.parseInt(getRetrySequence()[retrySequence]) * getRetryMutiplier();
    }

    protected Long getRetryMutiplier() {
        return Long.parseLong(env.getProperty("mso.workflow.topics.retryMultiplier", "6000"));
    }

    protected String[] getRetrySequence() {
        String[] seq = {"1", "1", "2", "3", "5", "8", "13", "20"};
        if (env.getProperty("mso.workflow.topics.retrySequence") != null) {
            seq = env.getProperty("mso.workflow.topics.retrySequence", String[].class);
        }
        return seq;
    }
}
