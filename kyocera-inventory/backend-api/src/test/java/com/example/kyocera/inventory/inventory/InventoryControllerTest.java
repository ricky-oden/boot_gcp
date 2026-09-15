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
        when(inventoryService.search("ITEM001")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/inventories").param("itemCode", "ITEM001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].inventoryId").value(1001))
                .andExpect(jsonPath("$[0].itemCode").value("ITEM001"))
                .andExpect(jsonPath("$[0].itemName").value("六角ボルト"))
                .andExpect(jsonPath("$[0].warehouseName").value("東京倉庫"));

        verify(inventoryService).search("ITEM001");
    }
}
