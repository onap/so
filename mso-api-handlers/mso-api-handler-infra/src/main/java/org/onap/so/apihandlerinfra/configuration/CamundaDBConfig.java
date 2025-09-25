package org.onap.so.apihandlerinfra.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "camundaEntityManagerFactory",
        transactionManagerRef = "camundaTransactionManager", basePackages = {"org.onap.so.db.camunda.client"})
@Profile({"!test"})
public class CamundaDBConfig {
    protected static Logger logger = LoggerFactory.getLogger(CamundaDBConfig.class);

    @Autowired(required = false)
    private MBeanExporter mBeanExporter;

    @Bean
    @ConfigurationProperties(prefix = "bpmn.datasource.hikari")
    public HikariConfig hikariDBConfig() {
        return new HikariConfig();
    }

    @Bean(name = "camundaDataSource")
    public DataSource dataSource() {
        logger.info("***** in CamundaDBConfig ****");
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
