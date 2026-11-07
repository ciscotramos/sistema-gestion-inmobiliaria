package com.inmobiliaria.backend.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de las conexiones al Master y Replica.
 * Se usa LazyConnectionDataSourceProxy para elegir la conexión
 * cuando ya se conoce el tipo de transacción.
 */
@Configuration
public class DataSourceConfig {

    @Value("${datasource.master.url}")
    private String masterUrl;

    @Value("${datasource.master.username}")
    private String masterUsername;

    @Value("${datasource.master.password}")
    private String masterPassword;

    @Value("${datasource.master.driver-class-name}")
    private String masterDriverClassName;

    @Value("${datasource.replica.url}")
    private String replicaUrl;

    @Value("${datasource.replica.username}")
    private String replicaUsername;

    @Value("${datasource.replica.password}")
    private String replicaPassword;

    @Value("${datasource.replica.driver-class-name}")
    private String replicaDriverClassName;

    @Bean(name = "masterDataSource")
    public DataSource masterDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(masterUrl);
        ds.setUsername(masterUsername);
        ds.setPassword(masterPassword);
        ds.setDriverClassName(masterDriverClassName);
        ds.setPoolName("HikariPool-Master");
        ds.setMaximumPoolSize(10);
        ds.setMinimumIdle(2);
        ds.setConnectionTimeout(30000);
        return ds;
    }

    @Bean(name = "replicaDataSource")
    public DataSource replicaDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(replicaUrl);
        ds.setUsername(replicaUsername);
        ds.setPassword(replicaPassword);
        ds.setDriverClassName(replicaDriverClassName);
        ds.setPoolName("HikariPool-Replica");
        ds.setMaximumPoolSize(10);
        ds.setMinimumIdle(2);
        ds.setConnectionTimeout(30000);
        return ds;
    }

    @Bean(name = "routingDataSource")
    public DataSource routingDataSource(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            @Qualifier("replicaDataSource") DataSource replicaDataSource) {

        RoutingDataSource routingDataSource = new RoutingDataSource();

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceType.MASTER, masterDataSource);
        targetDataSources.put(DataSourceType.REPLICA, replicaDataSource);

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(masterDataSource);
        routingDataSource.afterPropertiesSet();

        return routingDataSource;
    }

    @Primary
    @Bean(name = "dataSource")
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }
}
