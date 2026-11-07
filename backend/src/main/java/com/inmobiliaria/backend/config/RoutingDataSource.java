package com.inmobiliaria.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Define a qué base de datos se dirige cada operación.
 * Las consultas de solo lectura van a la Replica
 * y las operaciones de escritura van al Master.
 */
public class RoutingDataSource extends AbstractRoutingDataSource {

    private static final Logger log = LoggerFactory.getLogger(RoutingDataSource.class);

    @Override
    protected Object determineCurrentLookupKey() {
        boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        DataSourceType target = isReadOnly ? DataSourceType.REPLICA : DataSourceType.MASTER;
        log.debug("RoutingDataSource determinó el nodo objetivo: {} (readOnly = {})", target, isReadOnly);
        return target;
    }
}
