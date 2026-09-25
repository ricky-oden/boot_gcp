package com.example.kyocera.inventory.batch.domain;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class DailyStockSummary {

    private final LocalDate businessDate;
    private final String itemCode;
    private final Long warehouseId;
    private final Integer totalInQuantity;
    private final Integer totalOutQuantity;
    private final Integer movementCount;
    private final OffsetDateTime updatedAt;

    public DailyStockSummary(LocalDate businessDate, String itemCode, Long warehouseId,
                             Integer totalInQuantity, Integer totalOutQuantity,
                             Integer movementCount, OffsetDateTime updatedAt) {
        this.businessDate = businessDate;
        this.itemCode = itemCode;
        this.warehouseId = warehouseId;
        this.totalInQuantity = totalInQuantity;
        this.totalOutQuantity = totalOutQuantity;
        this.movementCount = movementCount;
        this.updatedAt = updatedAt;
    }

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public String getItemCode() {
        return itemCode;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public Integer getTotalInQuantity() {
        return totalInQuantity;
    }

    public Integer getTotalOutQuantity() {
        return totalOutQuantity;
    }

    public Integer getMovementCount() {
        return movementCount;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
