package com.example.kyocera.inventory.inventory.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InventoryMapper {

    List<InventorySearchRow> search(@Param("itemCode") String itemCode, @Param("warehouseId") Long warehouseId);
}
