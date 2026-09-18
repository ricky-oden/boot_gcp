package com.example.kyocera.inventory.stockmovement;

import com.example.kyocera.inventory.generated.model.MovementType;
import com.example.kyocera.inventory.generated.model.StockMovementRequest;
import com.example.kyocera.inventory.generated.model.StockMovementResponse;
import com.example.kyocera.inventory.stockmovement.mapper.StockMovementMapper;
import com.example.kyocera.inventory.stockmovement.mapper.StockMovementRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock
    private StockMovementMapper mapper;

    private final Clock fixedClock = Clock.fixed(
            Instant.parse("2026-09-18T01:00:00Z"), ZoneOffset.UTC);

    @Test
    void receivesStockAndWritesInventoryAndHistory() {
        StockMovementRow row = row(1001L, "ITEM001", 1L, 120);
        when(mapper.selectForUpdate("ITEM001", 1L)).thenReturn(Optional.of(row));
        when(mapper.updateQuantity(1001L, 125, java.time.OffsetDateTime.parse("2026-09-18T01:00:00Z")))
                .thenReturn(1);
        when(mapper.insertHistory(
                1001L, "IN", 5, 120, 125,
                java.time.OffsetDateTime.parse("2026-09-18T01:00:00Z")))
                .thenReturn(1);

        StockMovementService service = new StockMovementService(mapper, fixedClock);
        StockMovementResponse response = service.process(request(MovementType.IN, 5));

        assertThat(response.getPreviousQuantity()).isEqualTo(120);
        assertThat(response.getCurrentQuantity()).isEqualTo(125);
        assertThat(response.getMovementType()).isEqualTo(MovementType.IN);
        verify(mapper).updateQuantity(
                1001L, 125, java.time.OffsetDateTime.parse("2026-09-18T01:00:00Z"));
        verify(mapper).insertHistory(
                1001L, "IN", 5, 120, 125,
                java.time.OffsetDateTime.parse("2026-09-18T01:00:00Z"));
    }

    @Test
    void rejectsNonPositiveQuantityBeforeAccessingDatabase() {
        StockMovementService service = new StockMovementService(mapper, fixedClock);

        assertThatThrownBy(() -> service.process(request(MovementType.IN, 0)))
                .isInstanceOf(RequestValidationException.class)
                .hasMessageContaining("1以上");

        verify(mapper, never()).selectForUpdate("ITEM001", 1L);
    }

    @Test
    void leavesOutboundMovementAsLearnerExercise() {
        when(mapper.selectForUpdate("ITEM001", 1L))
                .thenReturn(Optional.of(row(1001L, "ITEM001", 1L, 120)));
        StockMovementService service = new StockMovementService(mapper, fixedClock);

        assertThatThrownBy(() -> service.process(request(MovementType.OUT, 5)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Exercise");

        verify(mapper, never()).updateQuantity(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.any());
    }

    private StockMovementRequest request(MovementType type, int quantity) {
        return new StockMovementRequest("ITEM001", 1L, type, quantity);
    }

    private StockMovementRow row(long id, String itemCode, long warehouseId, int quantity) {
        StockMovementRow row = new StockMovementRow();
        row.setInventoryId(id);
        row.setItemCode(itemCode);
        row.setWarehouseId(warehouseId);
        row.setQuantity(quantity);
        return row;
    }
}
