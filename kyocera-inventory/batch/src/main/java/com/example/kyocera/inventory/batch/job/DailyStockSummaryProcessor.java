package com.example.kyocera.inventory.batch.job;

import com.example.kyocera.inventory.batch.domain.DailyStockSummary;
import com.example.kyocera.inventory.batch.domain.StockHistoryAggregate;
import org.springframework.batch.item.ItemProcessor;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DailyStockSummaryProcessor
        implements ItemProcessor<StockHistoryAggregate, DailyStockSummary> {

    private final LocalDate businessDate;
    private final String failOnItemCode;
    private final Clock clock;

    public DailyStockSummaryProcessor(LocalDate businessDate, String failOnItemCode) {
        this(businessDate, failOnItemCode, Clock.systemUTC());
    }

    DailyStockSummaryProcessor(LocalDate businessDate, String failOnItemCode, Clock clock) {
        this.businessDate = businessDate;
        this.failOnItemCode = failOnItemCode;
        this.clock = clock;
    }

    @Override
    public DailyStockSummary process(StockHistoryAggregate item) {
        if (item.getItemCode().equals(failOnItemCode)) {
            throw new IllegalStateException(
                    "学習用の意図的Batch失敗: itemCode=" + item.getItemCode());
        }

        return new DailyStockSummary(
                businessDate,
                item.getItemCode(),
                item.getWarehouseId(),
                item.getTotalInQuantity(),
                item.getTotalOutQuantity(),
                item.getMovementCount(),
                OffsetDateTime.now(clock).withOffsetSameInstant(ZoneOffset.UTC)
        );
    }
}
