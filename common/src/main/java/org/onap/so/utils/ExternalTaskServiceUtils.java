package org.onap.so.utils;

import java.security.GeneralSecurityException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
        return new BasicAuthProvider(env.getRequiredProperty("mso.config.cadi.aafId"), auth);
    }

    protected String getAuth() throws Exception {
        try {
            return CryptoUtils.decrypt(env.getRequiredProperty("mso.auth"), env.getRequiredProperty("mso.msoKey"));
        } catch (IllegalStateException | GeneralSecurityException e) {
            logger.error("Error Decrypting Password", e);
            throw new Exception("Cannot load password");
        }
    }

    public int getMaxClients() {
        return Integer.parseInt(env.getProperty("workflow.topics.maxClients", "10"));
    }

    public Long getLockDuration() {
        Long lockDuration = Long.parseLong(env.getProperty("mso.audit.lock-time", "60000"));
        return lockDuration;
    }

    @ScheduledLogging
    @Scheduled(fixedDelay = 30000)
    public void checkAllClientsActive() {
        getClients().stream().filter(client -> !client.isActive()).forEach(ExternalTaskClient::start);
    }

    protected Set<ExternalTaskClient> getClients() {
        return taskClients;
    }

}
