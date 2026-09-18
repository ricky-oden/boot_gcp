package com.example.kyocera.inventory.stockmovement.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

@Mapper
public interface StockMovementMapper {

    Optional<StockMovementRow> selectForUpdate(
            @Param("itemCode") String itemCode,
            @Param("warehouseId") Long warehouseId
    );

    int updateQuantity(
            @Param("inventoryId") Long inventoryId,
            @Param("quantity") Integer quantity,
            @Param("updatedAt") OffsetDateTime updatedAt
    );

    int insertHistory(
            @Param("inventoryId") Long inventoryId,
            @Param("movementType") String movementType,
            @Param("quantity") Integer quantity,
            @Param("beforeQuantity") Integer beforeQuantity,
            @Param("afterQuantity") Integer afterQuantity,
            @Param("processedAt") OffsetDateTime processedAt
    );
}
