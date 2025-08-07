package org.onap.so.apihandlerinfra;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@Profile({"test"})
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "camundaEntityManagerFactory",
        transactionManagerRef = "camundaTransactionManager", basePackages = {"org.onap.so.db.camunda"})
public class EmbeddedCamundaDbConfig {
    @Bean
    MariaDB4jSpringService mariaDB4jSpringService() {
        return new MariaDB4jSpringService();
    }


    @Bean(name = "camundaDataSource")
    @ConfigurationProperties(prefix = "bpmn.datasource")
    DataSource dataSource(MariaDB4jSpringService mariaDB4jSpringService,
            @Value("${mariaDB4j.databaseName3}") String databaseName,
            @Value("${spring.datasource.username}") String datasourceUsername,
            @Value("${spring.datasource.password}") String datasourcePassword,
            @Value("${spring.datasource.driver-class-name}") String datasourceDriver) throws ManagedProcessException {
        // Create our database with default root user and no password
        mariaDB4jSpringService.getDB().createDB(databaseName);

        DBConfigurationBuilder config = mariaDB4jSpringService.getConfiguration();

        return DataSourceBuilder.create().username(datasourceUsername).password(datasourcePassword)
                .url(config.getURL(databaseName)).driverClassName(datasourceDriver).build();
    }


    @Bean(name = "camundaEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder,
            @Qualifier("camundaDataSource") DataSource dataSource) {
        return builder.dataSource(dataSource).packages("org.onap.so.db.camunda").persistenceUnit("camundabpmn").build();
    }


    @Bean(name = "camundaTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("camundaEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
