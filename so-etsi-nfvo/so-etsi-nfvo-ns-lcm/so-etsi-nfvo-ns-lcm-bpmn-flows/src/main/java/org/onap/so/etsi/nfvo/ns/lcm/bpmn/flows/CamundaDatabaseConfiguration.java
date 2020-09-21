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
package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows;


import static org.slf4j.LoggerFactory.getLogger;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "entityManagerFactory", transactionManagerRef = "transactionManager")
public class CamundaDatabaseConfiguration {

    private static final String CAMUNDA_DATA_SOURCE_BEAN_NAME = "camundaBpmDataSource";

    private static final Logger logger = getLogger(CamundaDatabaseConfiguration.class);

    @Autowired(required = false)
    private MBeanExporter mBeanExporter;

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari.camunda")
    public HikariConfig camundaDbConfig() {
        logger.debug("Creating Camunda HikariConfig bean ... ");
        return new HikariConfig();
    }

    @Bean(name = CAMUNDA_DATA_SOURCE_BEAN_NAME)
    public DataSource camundaDataSource() {
        if (mBeanExporter != null) {
            mBeanExporter.addExcludedBean(CAMUNDA_DATA_SOURCE_BEAN_NAME);
        }
        logger.debug("Creating Camunda HikariDataSource bean ... ");
        final HikariConfig hikariConfig = this.camundaDbConfig();
        return new HikariDataSource(hikariConfig);
    }

}
