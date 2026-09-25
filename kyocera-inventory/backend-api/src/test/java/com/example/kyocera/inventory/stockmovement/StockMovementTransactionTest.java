package com.example.kyocera.inventory.stockmovement;

import com.example.kyocera.inventory.generated.model.MovementType;
import com.example.kyocera.inventory.generated.model.StockMovementRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
                "spring.datasource.url=jdbc:h2:mem:stock-movement-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.sql.init.mode=never",
                "mybatis.mapper-locations=classpath:mapper/**/*.xml"
})
class StockMovementTransactionTest {

        @Autowired
        private StockMovementService service;

        @Autowired
        private JdbcTemplate jdbcTemplate;

        @Test
        @Sql(scripts = { "classpath:db/test-schema.sql", "classpath:db/test-data.sql" })
        void commitsInventoryUpdateAndHistoryInsertTogetherForInMovement() {
                service.process(new StockMovementRequest("ITEM001", 1L, MovementType.IN, 5));

                Integer quantity = jdbcTemplate.queryForObject(
                                "SELECT quantity FROM inventory WHERE inventory_id = 1001", Integer.class);
                Integer historyCount = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM stock_history WHERE inventory_id = 1001", Integer.class);

                assertThat(quantity).isEqualTo(125);
                assertThat(historyCount).isEqualTo(1);
        }

        @Test
        @Sql(scripts = { "classpath:db/test-schema.sql", "classpath:db/test-data.sql" })
        void commitsInventoryUpdateAndHistoryInsertTogetherForOutMovement() {
                service.process(new StockMovementRequest("ITEM001", 1L, MovementType.OUT, 5));

                Integer quantity = jdbcTemplate.queryForObject(
                                "SELECT quantity FROM inventory WHERE inventory_id = 1001", Integer.class);
                Integer historyCount = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM stock_history WHERE inventory_id = 1001", Integer.class);

                assertThat(quantity).isEqualTo(115);
                assertThat(historyCount).isEqualTo(1);
        }

        @Test
        @Sql(scripts = { "classpath:db/rollback-test-schema.sql", "classpath:db/test-data.sql" })
        void rollsBackInventoryWhenHistoryInsertFailsForInMovement() {
                assertThatThrownBy(() -> service.process(
                                new StockMovementRequest("ITEM001", 1L, MovementType.IN, 5)))
                                .isInstanceOf(RuntimeException.class);

                Integer quantity = jdbcTemplate.queryForObject(
                                "SELECT quantity FROM inventory WHERE inventory_id = 1001", Integer.class);
                Integer historyCount = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM stock_history", Integer.class);

                assertThat(quantity).isEqualTo(120);
                assertThat(historyCount).isZero();
        }

        @Test
        @Sql(scripts = { "classpath:db/rollback-test-schema.sql", "classpath:db/test-data.sql" })
        void rollsBackInventoryWhenHistoryInsertFailsForOutMovement() {
                assertThatThrownBy(() -> service.process(
                                new StockMovementRequest("ITEM001", 1L, MovementType.OUT, 5)))
                                .isInstanceOf(RuntimeException.class); 
                
                Integer quantity = jdbcTemplate.queryForObject(
                                "SELECT quantity FROM inventory WHERE inventory_id = 1001", Integer.class);
                Integer historyCount = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM stock_history", Integer.class);  
                
                assertThat(quantity).isEqualTo(120);
                assertThat(historyCount).isZero();
        }
}
