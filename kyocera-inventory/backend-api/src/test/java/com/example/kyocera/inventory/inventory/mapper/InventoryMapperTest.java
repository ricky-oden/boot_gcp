package com.example.kyocera.inventory.inventory.mapper;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest(properties = {
        "spring.sql.init.mode=never",
        "mybatis.mapper-locations=classpath:mapper/**/*.xml"
})
@Sql(scripts = {"classpath:db/test-schema.sql", "classpath:db/test-data.sql"})
class InventoryMapperTest {

    @Autowired
    private InventoryMapper inventoryMapper;

    @Test
    void returnsOnlyRowsForSpecifiedItemCode() {
        List<InventorySearchRow> results = inventoryMapper.search("ITEM001", null, null);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(InventorySearchRow::getItemCode)
                .containsOnly("ITEM001");
        assertThat(results).extracting(InventorySearchRow::getWarehouseName)
                .containsExactly("東京倉庫", "大阪倉庫");
    }

    @Test
    void returnsOnlyRowsForSpecifiedWarehouseId() {
        List<InventorySearchRow> results = inventoryMapper.search(null, null, 1L);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(InventorySearchRow::getWarehouseId)
                .containsOnly(1L);
        assertThat(results).extracting(InventorySearchRow::getWarehouseName)
                .containsExactly("東京倉庫", "東京倉庫");
    }

    @Test
    void returnOnlyRowsForSpecifiedItemName() {
        List<InventorySearchRow> results = inventoryMapper.search(null, "ナット", null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getItemName()).isEqualTo("六角ナット");
    }

    @Test
    void rerurnEscapedRowsForSpecifiedItemName() {
        List<InventorySearchRow> results = inventoryMapper.search(null, "\\%", null);

        assertThat(results).hasSize(0);
    }

    @Test
    void returnsAllRowsWhenItemCodeIsNotSpecified() {
        List<InventorySearchRow> results = inventoryMapper.search(null, null, null);

        assertThat(results).hasSize(3);
        assertThat(results).extracting(InventorySearchRow::getInventoryId)
                .containsExactly(1001L, 1002L, 1003L);
    }
}
