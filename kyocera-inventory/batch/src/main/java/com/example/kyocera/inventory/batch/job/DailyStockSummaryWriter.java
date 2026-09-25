package com.example.kyocera.inventory.batch.job;

import com.example.kyocera.inventory.batch.domain.DailyStockSummary;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class DailyStockSummaryWriter implements ItemWriter<DailyStockSummary> {

    private final JdbcTemplate jdbcTemplate;

    public DailyStockSummaryWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void write(List<? extends DailyStockSummary> items) {
        for (DailyStockSummary item : items) {
            int updated = jdbcTemplate.update(
                    "UPDATE daily_stock_summary "
                            + "SET total_in_quantity = ?, total_out_quantity = ?, "
                            + "movement_count = ?, updated_at = ? "
                            + "WHERE business_date = ? AND item_code = ? AND warehouse_id = ?",
                    item.getTotalInQuantity(),
                    item.getTotalOutQuantity(),
                    item.getMovementCount(),
                    item.getUpdatedAt(),
                    item.getBusinessDate(),
                    item.getItemCode(),
                    item.getWarehouseId()
            );

            if (updated == 0) {
                jdbcTemplate.update(
                        "INSERT INTO daily_stock_summary ("
                                + "business_date, item_code, warehouse_id, total_in_quantity, "
                                + "total_out_quantity, movement_count, updated_at"
                                + ") VALUES (?, ?, ?, ?, ?, ?, ?)",
                        item.getBusinessDate(),
                        item.getItemCode(),
                        item.getWarehouseId(),
                        item.getTotalInQuantity(),
                        item.getTotalOutQuantity(),
                        item.getMovementCount(),
                        item.getUpdatedAt()
                );
            }
        }
    }
}
