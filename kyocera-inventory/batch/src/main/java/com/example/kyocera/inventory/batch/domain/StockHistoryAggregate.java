package com.example.kyocera.inventory.batch.domain;

public class StockHistoryAggregate {

    private String itemCode;
    private Long warehouseId;
    private Integer totalInQuantity;
    private Integer totalOutQuantity;
    private Integer movementCount;

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Integer getTotalInQuantity() {
        return totalInQuantity;
    }

    public void setTotalInQuantity(Integer totalInQuantity) {
        this.totalInQuantity = totalInQuantity;
    }

    public Integer getTotalOutQuantity() {
        return totalOutQuantity;
    }

    public void setTotalOutQuantity(Integer totalOutQuantity) {
        this.totalOutQuantity = totalOutQuantity;
    }

    public Integer getMovementCount() {
        return movementCount;
    }

    public void setMovementCount(Integer movementCount) {
        this.movementCount = movementCount;
    }
}
