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
@Conditional(PrefDbCondition.class)
@EnableJpaRepositories(
        basePackages = PrefDataConfig.BASE_PACKAGE, 
        entityManagerFactoryRef = PrefDataConfig.EM_FACTORY,
        transactionManagerRef = PrefDataConfig.TRANS_MANAGER,
        repositoryBaseClass = EpricerCustomJpaRepositoryImpl.class
)
@ImportResource("classpath:database-pref-advice.xml")

class PrefDataConfig extends CommonDataConfig {

    static final String TRANS_MANAGER = "transManagerPref";
    static final String EM_FACTORY = "emfPref";
    static final String BASE_PACKAGE = "com.ibm.epricer.data.pref";
    static final String CONFIIG_PROP_PREFIX = "epricer.datasource.pref";
    static final String PU_NAME = "pu-pref";

    @Bean(name = "prefDataSource")
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


class PrefDbCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return DatabaseSupportChecker.isPrefEnabled();
    }
}
