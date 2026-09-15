package com.example.kyocera.inventory.inventory;

import com.example.kyocera.inventory.generated.model.InventoryResponse;
import com.example.kyocera.inventory.inventory.mapper.InventoryMapper;
import com.example.kyocera.inventory.inventory.mapper.InventorySearchRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryMapper inventoryMapper;

    @Test
    void mapsSearchRowsToOpenApiGeneratedModels() {
        InventorySearchRow row = row(1001L, "ITEM001", "六角ボルト", 1L, "東京倉庫", 120, "AVAILABLE");
        when(inventoryMapper.search("ITEM001")).thenReturn(List.of(row));

        InventoryService service = new InventoryService(inventoryMapper);
        List<InventoryResponse> results = service.search(" ITEM001 ");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getInventoryId()).isEqualTo(1001L);
        assertThat(results.get(0).getItemCode()).isEqualTo("ITEM001");
        assertThat(results.get(0).getWarehouseName()).isEqualTo("東京倉庫");
        verify(inventoryMapper).search("ITEM001");
    }

    private InventorySearchRow row(Long inventoryId, String itemCode, String itemName,
                                   Long warehouseId, String warehouseName,
                                   Integer quantity, String status) {
        InventorySearchRow row = new InventorySearchRow();
        row.setInventoryId(inventoryId);
        row.setItemCode(itemCode);
        row.setItemName(itemName);
        row.setWarehouseId(warehouseId);
        row.setWarehouseName(warehouseName);
        row.setQuantity(quantity);
        row.setStatus(status);
        return row;
    }
}
