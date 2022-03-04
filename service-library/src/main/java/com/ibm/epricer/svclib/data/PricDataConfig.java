package com.ibm.epricer.svclib.data;

import javax.sql.DataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@Conditional(PricDbCondition.class)
@EnableJpaRepositories(
        basePackages = PricDataConfig.BASE_PACKAGE, 
        entityManagerFactoryRef = PricDataConfig.EM_FACTORY,
        transactionManagerRef = PricDataConfig.TRANS_MANAGER,
        repositoryBaseClass = EpricerCustomJpaRepositoryImpl.class
)
@ImportResource("classpath:database-pric-advice.xml")

class PricDataConfig extends CommonDataConfig {

    static final String TRANS_MANAGER = "transManagerPric";
    static final String EM_FACTORY = "emfPric";
    static final String BASE_PACKAGE = "com.ibm.epricer.data.pric";
    static final String CONFIIG_PROP_PREFIX = "epricer.datasource.pric";
    static final String PU_NAME = "pu-pric";

    @Bean(name = "pricDataSource")
    @ConfigurationProperties(prefix = CONFIIG_PROP_PREFIX)
    DataSource dataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean(name = EM_FACTORY)
    LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean factory = entityManagerFactoryTemplate();
        factory.setDataSource(dataSource());
        factory.setPackagesToScan(BASE_PACKAGE);
        factory.setPersistenceUnitName(PU_NAME);
        return factory;
    }

    @Bean(name = TRANS_MANAGER)
    PlatformTransactionManager transactionManager() { // EntityManagerFactory entityManagerFactory
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return txManager;
    }
}


class PricDbCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return DatabaseSupportChecker.isPricEnabled();
    }
}
