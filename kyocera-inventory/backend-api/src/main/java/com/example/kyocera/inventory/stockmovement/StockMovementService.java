package com.example.kyocera.inventory.stockmovement;

import com.example.kyocera.inventory.generated.model.MovementType;
import com.example.kyocera.inventory.generated.model.StockMovementRequest;
import com.example.kyocera.inventory.generated.model.StockMovementResponse;
import com.example.kyocera.inventory.stockmovement.mapper.StockMovementMapper;
import com.example.kyocera.inventory.stockmovement.mapper.StockMovementRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class StockMovementService {

    private final StockMovementMapper stockMovementMapper;
    private final Clock clock;

    @Autowired
    public StockMovementService(StockMovementMapper stockMovementMapper) {
        this(stockMovementMapper, Clock.systemUTC());
    }

    StockMovementService(StockMovementMapper stockMovementMapper, Clock clock) {
        this.stockMovementMapper = stockMovementMapper;
        this.clock = clock;
    }

    @Transactional
    public StockMovementResponse process(StockMovementRequest request) {
        validateRequest(request);

        StockMovementRow inventory = stockMovementMapper
                .selectForUpdate(request.getItemCode(), request.getWarehouseId())
                .orElseThrow(() -> new BusinessRuleException(
                        "INVENTORY_NOT_FOUND",
                        "指定した商品と倉庫の在庫が見つかりません。"));

        int previousQuantity = inventory.getQuantity();
        int currentQuantity;

        if (request.getMovementType() == MovementType.OUT) {
            if (previousQuantity < request.getQuantity()) {
                throw new BusinessRuleException(
                        "INSUFFICIENT_STOCK",
                        "在庫が不足しています。");
            }
            currentQuantity = previousQuantity - request.getQuantity();
        } else {
            currentQuantity = previousQuantity + request.getQuantity();
        }

        OffsetDateTime processedAt = OffsetDateTime.now(clock).withOffsetSameInstant(ZoneOffset.UTC);

        int updated = stockMovementMapper.updateQuantity(
                inventory.getInventoryId(),
                currentQuantity,
                processedAt);
        if (updated != 1) {
            throw new IllegalStateException("在庫更新件数が1件ではありません。count=" + updated);
        }

        int inserted = stockMovementMapper.insertHistory(
                inventory.getInventoryId(),
                request.getMovementType().getValue(),
                request.getQuantity(),
                previousQuantity,
                currentQuantity,
                processedAt);
        if (inserted != 1) {
            throw new IllegalStateException("履歴登録件数が1件ではありません。count=" + inserted);
        }

        return new StockMovementResponse(
                inventory.getItemCode(),
                inventory.getWarehouseId(),
                previousQuantity,
                currentQuantity,
                request.getMovementType(),
                request.getQuantity(),
                processedAt);
    }

    private void validateRequest(StockMovementRequest request) {
        if (request == null || request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RequestValidationException("quantityは1以上を指定してください。");
        }
    }
}
