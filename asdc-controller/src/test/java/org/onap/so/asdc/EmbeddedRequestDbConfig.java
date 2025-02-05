/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.asdc;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@Profile({"test"})
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "requestEntityManagerFactory",
        transactionManagerRef = "requestTransactionManager", basePackages = {"org.onap.so.db.request.data.repository"})
public class EmbeddedRequestDbConfig {

    @Bean
    MariaDB4jSpringService mariaDB4jSpringService() {
        return new MariaDB4jSpringService();
    }


    @Bean(name = "requestDataSource")
    @ConfigurationProperties(prefix = "request.datasource")
    DataSource dataSource(MariaDB4jSpringService mariaDB4jSpringService,
            @Value("${mariaDB4j.databaseName2}") String databaseName,
            @Value("${spring.datasource.username}") String datasourceUsername,
            @Value("${spring.datasource.password}") String datasourcePassword,
            @Value("${spring.datasource.driver-class-name}") String datasourceDriver) throws ManagedProcessException {
        // Create our database with default root user and no password
        mariaDB4jSpringService.getDB().createDB(databaseName);

        DBConfigurationBuilder config = mariaDB4jSpringService.getConfiguration();

        return DataSourceBuilder.create().username(datasourceUsername).password(datasourcePassword)
                .url(config.getURL(databaseName)).driverClassName(datasourceDriver).build();
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
