package com.example.kyocera.inventory.inventory;

import com.example.kyocera.inventory.generated.model.InventoryResponse;
import com.example.kyocera.inventory.inventory.mapper.InventoryMapper;
import com.example.kyocera.inventory.inventory.mapper.InventorySearchRow;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryMapper inventoryMapper;

    public InventoryService(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }

    public List<InventoryResponse> search(String itemCode, Long warehouseId) {
        String normalizedItemCode = normalize(itemCode);
        return inventoryMapper.search(normalizedItemCode, warehouseId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private InventoryResponse toResponse(InventorySearchRow row) {
        return new InventoryResponse(
                row.getInventoryId(),
                row.getItemCode(),
                row.getItemName(),
                row.getWarehouseId(),
                row.getWarehouseName(),
                row.getQuantity(),
                row.getStatus()
        );
    }
}
