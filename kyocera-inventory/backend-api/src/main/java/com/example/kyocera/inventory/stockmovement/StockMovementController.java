package com.example.kyocera.inventory.stockmovement;

import com.example.kyocera.inventory.generated.api.StockMovementsApi;
import com.example.kyocera.inventory.generated.model.StockMovementRequest;
import com.example.kyocera.inventory.generated.model.StockMovementResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StockMovementController implements StockMovementsApi {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @Override
    public ResponseEntity<StockMovementResponse> createStockMovement(StockMovementRequest request) {
        return ResponseEntity.ok(stockMovementService.process(request));
    }
}
