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

package org.onap.so.asdc;


import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
		entityManagerFactoryRef = "entityManagerFactory",
		basePackages = {"org.onap.so.db.catalog.data.repository"}
		)
@Profile({"!test"})
public class CatalogDBConfig {
	
	@Primary
	@Bean(name = "dataSource")
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	@Primary
	@Bean(name = "entityManagerFactory")
	public LocalContainerEntityManagerFactoryBean 
	entityManagerFactory(
			EntityManagerFactoryBuilder builder,
			@Qualifier("dataSource") DataSource dataSource
			) {
		return builder
				.dataSource(dataSource)
				.packages("org.onap.so.db.catalog.beans")
				.persistenceUnit("catalogDB")
				.build();
	}

	@Primary
	@Bean(name = "transactionManager")
	public PlatformTransactionManager transactionManager(
			@Qualifier("entityManagerFactory") EntityManagerFactory 
			entityManagerFactory
			) {
		return new JpaTransactionManager(entityManagerFactory);
	}

}
