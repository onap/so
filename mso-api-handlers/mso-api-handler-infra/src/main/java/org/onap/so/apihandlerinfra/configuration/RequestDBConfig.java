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

package org.onap.so.apihandlerinfra.configuration;


import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "requestEntityManagerFactory",
        transactionManagerRef = "requestTransactionManager", basePackages = {"org.onap.so.db.request.data.repository"})
@Profile({"!test"})
public class RequestDBConfig {

    @Autowired(required = false)
    private MBeanExporter mBeanExporter;

    @Bean
    @ConfigurationProperties(prefix = "request.datasource.hikari")
    public HikariConfig requestDbConfig() {
        return new HikariConfig();
    }

    @Bean(name = "requestDataSource")
    public DataSource dataSource() {
        if (mBeanExporter != null) {
            mBeanExporter.addExcludedBean("requestDataSource");
        }
        HikariConfig hikariConfig = this.requestDbConfig();
        return new HikariDataSource(hikariConfig);
    }

    @Bean(name = "requestEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder,
            @Qualifier("requestDataSource") DataSource dataSource) {
        return builder.dataSource(dataSource).packages("org.onap.so.db.request.beans").persistenceUnit("requestDB")
                .build();
    }


    @Bean(name = "requestTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("requestEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
