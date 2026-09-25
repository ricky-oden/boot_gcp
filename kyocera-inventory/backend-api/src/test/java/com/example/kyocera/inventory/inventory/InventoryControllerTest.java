package com.example.kyocera.inventory.inventory;

import com.example.kyocera.inventory.generated.model.InventoryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Test
    void returnsInventoriesFilteredByItemCode() throws Exception {
        InventoryResponse response = new InventoryResponse(
                1001L, "ITEM001", "六角ボルト", 1L, "東京倉庫", 120, "AVAILABLE");
        when(inventoryService.search("ITEM001", null, null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/inventories").param("itemCode", "ITEM001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].inventoryId").value(1001))
                .andExpect(jsonPath("$[0].itemCode").value("ITEM001"))
                .andExpect(jsonPath("$[0].itemName").value("六角ボルト"))
                .andExpect(jsonPath("$[0].warehouseName").value("東京倉庫"));

        verify(inventoryService).search("ITEM001", null, null);
    }

    @Test
    void returnsInventoriesFilteredByWarehouseId() throws Exception {
        InventoryResponse response = new InventoryResponse(
                1001L, "ITEM001", "六角ボルト", 1L, "東京倉庫", 120, "AVAILABLE");
        when(inventoryService.search(null, null, 1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/inventories").param("warehouseId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].inventoryId").value(1001))
                .andExpect(jsonPath("$[0].itemCode").value("ITEM001"))
                .andExpect(jsonPath("$[0].itemName").value("六角ボルト"))
                .andExpect(jsonPath("$[0].warehouseId").value(1L))
                .andExpect(jsonPath("$[0].warehouseName").value("東京倉庫"));

        verify(inventoryService).search(null, null, 1L);
    }

    @Test
    void returnsInventoriesFilteredByItemName() throws Exception {
        InventoryResponse response = new InventoryResponse(
                1003L, "ITEM002", "六角ナット", 1L, "東京倉庫", 75, "AVAILABLE");
        when(inventoryService.search(null, "ナット", null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/inventories").param("itemName", "ナット"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].inventoryId").value(1003))
                .andExpect(jsonPath("$[0].itemCode").value("ITEM002"))
                .andExpect(jsonPath("$[0].itemName").value("六角ナット"))
                .andExpect(jsonPath("$[0].warehouseId").value(1L))
                .andExpect(jsonPath("$[0].warehouseName").value("東京倉庫"));

        verify(inventoryService).search(null, "ナット", null);
    }
}
