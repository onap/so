/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.etsi.nfvo.ns.lcm.database.config;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "entityManagerFactory",
        basePackages = {"org.onap.so.etsi.nfvo.ns.lcm.database.repository"})
public class NfvoDatabaseConfiguration {

    private static final String PERSISTENCE_UNIT = "nfvo";
    private static final String NFVO_DATA_SOURCE_QUALIFIER = "nfvoDataSource";

    @Autowired(required = false)
    private MBeanExporter mBeanExporter;

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari.nfvo")
    public HikariConfig nfvoDbConfig() {
        return new HikariConfig();
    }

    @Bean(name = NFVO_DATA_SOURCE_QUALIFIER)
    public DataSource dataSource() {
        if (mBeanExporter != null) {
            mBeanExporter.addExcludedBean(NFVO_DATA_SOURCE_QUALIFIER);
        }
        final HikariConfig hikariConfig = this.nfvoDbConfig();
        return new HikariDataSource(hikariConfig);
    }

    @Primary
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(final EntityManagerFactoryBuilder builder,
            @Qualifier(NFVO_DATA_SOURCE_QUALIFIER) final DataSource dataSource) {
        return builder.dataSource(dataSource).packages(NfvoJob.class.getPackage().getName())
                .persistenceUnit(PERSISTENCE_UNIT).build();
    }

    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") final EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
