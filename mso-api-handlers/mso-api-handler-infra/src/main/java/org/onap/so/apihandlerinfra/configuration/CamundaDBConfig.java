/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG.
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
package org.onap.so.apihandlerinfra.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.lang.Nullable;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "camundaEntityManagerFactory",
        transactionManagerRef = "camundaTransactionManager", basePackages = {"org.onap.so.db.camunda.client"})
@Profile({"!test"})
@Slf4j
public class CamundaDBConfig {

    private final MBeanExporter mBeanExporter;

    public CamundaDBConfig(@Nullable MBeanExporter mBeanExporter) {
        this.mBeanExporter = mBeanExporter;
    }

    @Bean
    @ConfigurationProperties(prefix = "bpmn.datasource.hikari")
    public HikariConfig hikariDBConfig() {
        return new HikariConfig();
    }

    @Bean(name = "camundaDataSource")
    public DataSource dataSource() {
        if (mBeanExporter != null) {
            mBeanExporter.addExcludedBean("camundaDataSource");
        }
        HikariConfig hikariConfig = this.hikariDBConfig();
        return new HikariDataSource(hikariConfig);
    }

    @Bean(name = "camundaEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder,
            @Qualifier("camundaDataSource") DataSource dataSource) {
        return builder.dataSource(dataSource).packages("org.onap.so.db.camunda.client").persistenceUnit("camundabpmn")
                .build();
    }

    @Bean(name = "camundaTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("camundaEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
