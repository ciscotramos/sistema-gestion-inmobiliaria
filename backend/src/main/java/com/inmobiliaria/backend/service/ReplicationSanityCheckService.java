package com.inmobiliaria.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Servicio para verificar el enrutamiento dinámico (Read/Write Splitting)
 * entre el nodo Maestro (3306) y el nodo Réplica (3307).
 */
@Service
public class ReplicationSanityCheckService {

    private static final Logger log = LoggerFactory.getLogger(ReplicationSanityCheckService.class);
    private final DataSource dataSource;

    public ReplicationSanityCheckService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public record NodeExecutionInfo(
            String operationType,
            String targetNode,
            String jdbcUrl,
            int serverId,
            int readOnly,
            int totalDistritos,
            boolean success
    ) {}

    /**
     * Operación transaccional de escritura:
     * Por defecto (@Transactional sin readOnly=true), debe dirigirse al MASTER (puerto 3306, server_id=1).
     */
    @Transactional
    public NodeExecutionInfo executeWriteOperation() {
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            String url = conn.getMetaData().getURL();

            int serverId = -1;
            int readOnly = -1;
            int count = 0;

            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("SELECT @@server_id, @@read_only")) {
                    if (rs.next()) {
                        serverId = rs.getInt(1);
                        readOnly = rs.getInt(2);
                    }
                }
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ubicaciones_distritos")) {
                    if (rs.next()) {
                        count = rs.getInt(1);
                    }
                }
            }

            return new NodeExecutionInfo(
                    "ESCRITURA (@Transactional)",
                    "MASTER",
                    url,
                    serverId,
                    readOnly,
                    count,
                    true
            );
        } catch (Exception e) {
            log.error("Error al ejecutar operación de escritura en MASTER: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        }
    }

    /**
     * Operación transaccional de solo lectura:
     * Al estar anotada con @Transactional(readOnly = true), debe dirigirse a la REPLICA (puerto 3307, server_id=2).
     */
    @Transactional(readOnly = true)
    public NodeExecutionInfo executeReadOnlyOperation() {
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            String url = conn.getMetaData().getURL();

            int serverId = -1;
            int readOnly = -1;
            int count = 0;

            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("SELECT @@server_id, @@read_only")) {
                    if (rs.next()) {
                        serverId = rs.getInt(1);
                        readOnly = rs.getInt(2);
                    }
                }
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ubicaciones_distritos")) {
                    if (rs.next()) {
                        count = rs.getInt(1);
                    }
                }
            }

            return new NodeExecutionInfo(
                    "LECTURA (@Transactional(readOnly = true))",
                    "REPLICA",
                    url,
                    serverId,
                    readOnly,
                    count,
                    true
            );
        } catch (Exception e) {
            log.error("Error al ejecutar operación de lectura en REPLICA: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        }
    }
}
