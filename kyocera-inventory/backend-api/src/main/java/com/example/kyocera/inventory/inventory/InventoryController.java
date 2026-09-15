package com.example.kyocera.inventory.inventory;

import com.example.kyocera.inventory.generated.api.InventoriesApi;
import com.example.kyocera.inventory.generated.model.InventoryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class InventoryController implements InventoriesApi {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public ResponseEntity<List<InventoryResponse>> searchInventories(String itemCode) {
        return ResponseEntity.ok(inventoryService.search(itemCode));
    }
}
