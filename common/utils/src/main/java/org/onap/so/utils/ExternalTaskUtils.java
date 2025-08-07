package org.onap.so.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public abstract class ExternalTaskUtils {

    private static final Logger logger = LoggerFactory.getLogger(ExternalTaskUtils.class);

    @Autowired
    protected Environment env;

    private final RetrySequenceLevel retrySequenceLevel;

    public ExternalTaskUtils() {
        this.retrySequenceLevel = RetrySequenceLevel.MEDIUM;
    }

    public ExternalTaskUtils(RetrySequenceLevel retrySequenceLevel) {
        this.retrySequenceLevel = retrySequenceLevel;
    }

    public long calculateRetryDelay(int currentRetries) {
        int retrySequence = getRetrySequence().length - currentRetries;
        return Integer.parseInt(getRetrySequence()[retrySequence]) * getRetryMutiplier();
    }

    protected Long getRetryMutiplier() {
        return Long.parseLong(env.getProperty("mso.workflow.topics.retryMultiplier", "6000"));
    }

    protected String[] getRetrySequence() {
        switch (retrySequenceLevel) {
            case SHORT:
                String[] seqShort = {"1", "1"};
                if (env.getProperty("mso.workflow.topics.retrySequence.short") != null) {
                    seqShort = env.getProperty("mso.workflow.topics.retrySequence.short", String[].class);
                }
                return seqShort;
            case MEDIUM:
                String[] seqInter = {"1", "1", "2", "3", "5"};
                if (env.getProperty("mso.workflow.topics.retrySequence.medium") != null) {
                    seqInter = env.getProperty("mso.workflow.topics.retrySequence.medium", String[].class);
                }
                return seqInter;
            case LONG:
                String[] seqLong = {"1", "1", "2", "3", "5", "8", "13", "20"};
                if (env.getProperty("mso.workflow.topics.retrySequence.long") != null) {
                    seqLong = env.getProperty("mso.workflow.topics.retrySequence", String[].class);
                }
                return seqLong;
            default:
                String[] seq = {"1"};
                return seq;
        }

    }

}
