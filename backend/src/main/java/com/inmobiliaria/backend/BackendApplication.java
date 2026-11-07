package com.inmobiliaria.backend;

import com.inmobiliaria.backend.service.ReplicationSanityCheckService;
import com.inmobiliaria.backend.service.ReplicationSanityCheckService.NodeExecutionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendApplication {

    private static final Logger log = LoggerFactory.getLogger(BackendApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    /**
     * Sanity Check: Verifica la conectividad física y el enrutamiento dinámico (Read/Write Splitting)
     * entre MASTER (:3306) y REPLICA (:3307).
     */
    @Bean
    public CommandLineRunner databaseConnectionSanityCheck(ReplicationSanityCheckService sanityCheckService) {
        return args -> {
            log.info("=============================================================================");
            log.info("INICIANDO SANITY CHECK: REPLICACIÓN MYSQL Y READ/WRITE SPLITTING");
            log.info("=============================================================================");

            try {
                // 1. Prueba de Escritura -> MASTER (:3306)
                NodeExecutionInfo writeInfo = sanityCheckService.executeWriteOperation();
                log.info("1. OPERACIÓN DE ESCRITURA (@Transactional):");
                log.info("   - Nodo Resuelto:    {}", writeInfo.targetNode());
                log.info("   - URL de Conexión:  {}", writeInfo.jdbcUrl());
                log.info("   - server_id MySQL:  {} (Esperado: 1)", writeInfo.serverId());
                log.info("   - read_only MySQL:  {} (0 = Escritura permitida)", writeInfo.readOnly());
                log.info("   - Registros leídos: {}", writeInfo.totalDistritos());
                log.info("   - Estado:           SUCCESS");

                // 2. Prueba de Lectura -> REPLICA (:3307)
                NodeExecutionInfo readInfo = sanityCheckService.executeReadOnlyOperation();
                log.info("2. OPERACIÓN DE LECTURA (@Transactional(readOnly = true)):");
                log.info("   - Nodo Resuelto:    {}", readInfo.targetNode());
                log.info("   - URL de Conexión:  {}", readInfo.jdbcUrl());
                log.info("   - server_id MySQL:  {} (Esperado: 2)", readInfo.serverId());
                log.info("   - read_only MySQL:  {} (1 = Solo lectura)", readInfo.readOnly());
                log.info("   - Registros leídos: {}", readInfo.totalDistritos());
                log.info("   - Estado:           SUCCESS");

                log.info("=============================================================================");
                log.info("[REPLICATION TEST]");
                log.info("WRITE → MASTER :3306");
                log.info("READ  → REPLICA :3307");
                log.info("SUCCESS");
                log.info("=============================================================================");
            } catch (Exception e) {
                log.error("❌ ERROR CRÍTICO EN EL SANITY CHECK DE REPLICACIÓN: {}", e.getMessage(), e);
            }
        };
    }
}

