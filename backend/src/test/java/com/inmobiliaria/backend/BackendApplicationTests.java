package com.inmobiliaria.backend;

import com.inmobiliaria.backend.service.ReplicationSanityCheckService;
import com.inmobiliaria.backend.service.ReplicationSanityCheckService.NodeExecutionInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BackendApplicationTests {

    @Autowired
    private ReplicationSanityCheckService sanityCheckService;

    @Test
    void contextLoads() {
        assertNotNull(sanityCheckService);
    }

    @Test
    @DisplayName("Debe enrutar escritura hacia MASTER (:3306, server_id=1, read_only=0)")
    void testWriteRoutingToMaster() {
        NodeExecutionInfo writeInfo = sanityCheckService.executeWriteOperation();
        assertNotNull(writeInfo);
        assertEquals("MASTER", writeInfo.targetNode());
        assertEquals(1, writeInfo.serverId());
        assertEquals(0, writeInfo.readOnly());
        assertTrue(writeInfo.jdbcUrl().contains("3306"));
        assertTrue(writeInfo.success());
    }

    @Test
    @DisplayName("Debe enrutar lectura hacia REPLICA (:3307, server_id=2, read_only=1)")
    void testReadRoutingToReplica() {
        NodeExecutionInfo readInfo = sanityCheckService.executeReadOnlyOperation();
        assertNotNull(readInfo);
        assertEquals("REPLICA", readInfo.targetNode());
        assertEquals(2, readInfo.serverId());
        assertEquals(1, readInfo.readOnly());
        assertTrue(readInfo.jdbcUrl().contains("3307"));
        assertTrue(readInfo.success());
    }
}

