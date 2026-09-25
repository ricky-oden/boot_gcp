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
        when(inventoryMapper.search("ITEM001", null, null)).thenReturn(List.of(row));

        InventoryService service = new InventoryService(inventoryMapper);
        List<InventoryResponse> results = service.search(" ITEM001 ", null, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getInventoryId()).isEqualTo(1001L);
        assertThat(results.get(0).getItemCode()).isEqualTo("ITEM001");
        assertThat(results.get(0).getWarehouseName()).isEqualTo("東京倉庫");
        verify(inventoryMapper).search("ITEM001", null, null);
    }

    @Test
    void mapsSearchRowsToOpenApiGeneratedModels2() {
        InventorySearchRow row = row(1001L, "ITEM001", "六角ボルト", 1L, "東京倉庫", 120, "AVAILABLE");
        when(inventoryMapper.search(null, null, 1L)).thenReturn(List.of(row));

        InventoryService service = new InventoryService(inventoryMapper);
        List<InventoryResponse> results = service.search(null, null, 1L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getInventoryId()).isEqualTo(1001L);
        assertThat(results.get(0).getItemCode()).isEqualTo("ITEM001");
        assertThat(results.get(0).getWarehouseId()).isEqualTo(1L);
        assertThat(results.get(0).getWarehouseName()).isEqualTo("東京倉庫");
        verify(inventoryMapper).search(null, null, 1L);
    }

    @Test
    void mapsSearchRowsToOpenApiGeneratedModels3() {
        InventorySearchRow row = row(1003L, "ITEM002", "六角ナット", 1L, "東京倉庫", 75, "AVAILABLE");
        when(inventoryMapper.search(null, "ナット", null)).thenReturn(List.of(row));

        InventoryService service = new InventoryService(inventoryMapper);
        List<InventoryResponse> results = service.search(null, "ナット", null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getInventoryId()).isEqualTo(1003L);
        assertThat(results.get(0).getItemCode()).isEqualTo("ITEM002");
        assertThat(results.get(0).getWarehouseId()).isEqualTo(1L);
        assertThat(results.get(0).getWarehouseName()).isEqualTo("東京倉庫");
        verify(inventoryMapper).search(null, "ナット", null);
    }

    @Test
    void searchEscapesSpecialCharactersInItemName() {
        InventorySearchRow row = row(1004L, "ITEM003", "特殊%文字_テ\\スト", 2L, "大阪倉庫", 50, "AVAILABLE");
        when(inventoryMapper.search(null, "特殊\\%文字\\_テ\\\\スト", null)).thenReturn(List.of(row));

        InventoryService service = new InventoryService(inventoryMapper);
        List<InventoryResponse> results = service.search(null, "特殊%文字_テ\\スト", null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getInventoryId()).isEqualTo(1004L);
        assertThat(results.get(0).getItemCode()).isEqualTo("ITEM003");
        assertThat(results.get(0).getWarehouseId()).isEqualTo(2L);
        assertThat(results.get(0).getWarehouseName()).isEqualTo("大阪倉庫");
        verify(inventoryMapper).search(null, "特殊\\%文字\\_テ\\\\スト", null);
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
