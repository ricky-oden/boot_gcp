package com.example.kyocera.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:1/context-test",
        "spring.datasource.hikari.initialization-fail-timeout=-1",
        "spring.sql.init.mode=never"
})
class KyoceraInventoryApplicationTest {

    @Test
    void contextLoads() {
    }
}
