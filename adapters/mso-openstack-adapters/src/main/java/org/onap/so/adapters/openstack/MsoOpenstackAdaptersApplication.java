/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.openstack;

import java.util.concurrent.Executor;
import org.onap.so.logging.filter.spring.MDCTaskDecorator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication(scanBasePackages = {"org.onap.so"})
@EnableAsync
@EnableScheduling
@EnableJpaRepositories({"org.onap.so.db.catalog.data.repository", "org.onap.so.db.request.data.repository"})
@EntityScan({"org.onap.so.db.catalog.beans", "org.onap.so.db.request.beans"})
public class MsoOpenstackAdaptersApplication {


    @Value("${mso.async.core-pool-size}")
    private int corePoolSize;

    @Value("${mso.async.max-pool-size}")
    private int maxPoolSize;

    @Value("${mso.async.queue-capacity}")
    private int queueCapacity;

    private static final String LOGS_DIR = "logs_dir";

    private static void setLogsDir() {
        if (System.getProperty(LOGS_DIR) == null) {
            System.getProperties().setProperty(LOGS_DIR, "./logs/openstack/");
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(MsoOpenstackAdaptersApplication.class, args);
        setLogsDir();
    }

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setTaskDecorator(new MDCTaskDecorator());
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("OpenstackAdapters-");
        executor.initialize();
        return executor;
    }
}
