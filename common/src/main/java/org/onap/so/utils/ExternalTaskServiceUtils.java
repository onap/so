package org.onap.so.utils;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.camunda.bpm.client.interceptor.auth.BasicAuthProvider;
import org.onap.logging.filter.base.ScheduledLogging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;



@Component
public class ExternalTaskServiceUtils {

    @Autowired
    public Environment env;


    private static final long DEFAULT_LOCK_DURATION_LONG = 2700000;
    private static final long DEFAULT_LOCK_DURATION_MEDIUM = 900000;
    private static final long DEFAULT_LOCK_DURATION_SHORT = 300000;

    private static final String LOCK_DURATION_LONG = "mso.workflow.topics.lockDurationLong";
    private static final String LOCK_DURATION_MEDIUM = "mso.workflow.topics.lockDurationMedium";
    private static final String LOCK_DURATION_SHORT = "mso.workflow.topics.lockDurationShort";

    protected Set<ExternalTaskClient> taskClients = ConcurrentHashMap.newKeySet();

    private static final Logger logger = LoggerFactory.getLogger(ExternalTaskServiceUtils.class);

    public ExternalTaskClient createExternalTaskClient() throws Exception {
        String auth = getAuth();
        ClientRequestInterceptor interceptor = createClientInterceptor(auth);
        ExternalTaskClient client =
                ExternalTaskClient.create().baseUrl(env.getRequiredProperty("mso.workflow.endpoint")).maxTasks(1)
                        .addInterceptor(interceptor).asyncResponseTimeout(120000).build();
        taskClients.add(client);
        return client;
    }

    protected ClientRequestInterceptor createClientInterceptor(String auth) {
        return new BasicAuthProvider(env.getRequiredProperty("mso.auth.user"), auth);
    }

    protected String getAuth() throws Exception {
        try {
            return CryptoUtils.decrypt(env.getRequiredProperty("mso.auth.password"), env.getRequiredProperty("mso.msoKey"));
        } catch (IllegalStateException | GeneralSecurityException e) {
            logger.error("Error Decrypting Password", e);
            throw new Exception("Cannot load password");
        }
    }

    public int getMaxClients() {
        return Integer.parseInt(env.getProperty("workflow.topics.maxClients", "10"));
    }

    @ScheduledLogging
    @Scheduled(fixedDelay = 30000)
    public void checkAllClientsActive() {
        try {
            List<ExternalTaskClient> inactiveClients =
                    getClients().stream().filter(client -> !client.isActive()).collect(Collectors.toList());
            inactiveClients.forEach(c -> {
                c.start();
            });
        } catch (Exception e) {
            logger.error("Exception occured in checkAllClientsActive", e);
        }

    }

    protected Set<ExternalTaskClient> getClients() {
        return taskClients;
    }

    public long getLockDurationLong() {
        return env.getProperty(LOCK_DURATION_LONG, Long.class, new Long(DEFAULT_LOCK_DURATION_LONG));
    }

    public long getLockDurationMedium() {
        return env.getProperty(LOCK_DURATION_MEDIUM, Long.class, new Long(DEFAULT_LOCK_DURATION_MEDIUM));
    }

    public long getLockDurationShort() {
        return env.getProperty(LOCK_DURATION_SHORT, Long.class, new Long(DEFAULT_LOCK_DURATION_SHORT));
    }

}
