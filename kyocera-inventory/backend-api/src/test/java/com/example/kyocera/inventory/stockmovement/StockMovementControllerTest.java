package com.example.kyocera.inventory.stockmovement;

import com.example.kyocera.inventory.generated.model.MovementType;
import com.example.kyocera.inventory.generated.model.StockMovementRequest;
import com.example.kyocera.inventory.generated.model.StockMovementResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockMovementController.class)
class StockMovementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockMovementService service;

    @Test
    void returnsMovementResult() throws Exception {
        when(service.process(any(StockMovementRequest.class))).thenReturn(new StockMovementResponse(
                "ITEM001", 1L, 120, 125, MovementType.IN, 5,
                OffsetDateTime.parse("2026-09-18T01:00:00Z")
        ));

        mockMvc.perform(post("/api/stock-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemCode\":\"ITEM001\",\"warehouseId\":1,"
                                + "\"movementType\":\"IN\",\"quantity\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.previousQuantity").value(120))
                .andExpect(jsonPath("$.currentQuantity").value(125))
                .andExpect(jsonPath("$.movementType").value("IN"));
    }

    @Test
    void returns400ForRequestValidationError() throws Exception {
        mockMvc.perform(post("/api/stock-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemCode\":\"ITEM001\",\"warehouseId\":1,"
                                + "\"movementType\":\"IN\",\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUEST_VALIDATION_ERROR"));
    }

    @Test
    void returns409ForBusinessRuleError() throws Exception {
        when(service.process(any(StockMovementRequest.class))).thenThrow(
                new BusinessRuleException("OUT_MOVEMENT_NOT_IMPLEMENTED", "Day4 Exercise"));

        mockMvc.perform(post("/api/stock-movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemCode\":\"ITEM001\",\"warehouseId\":1,"
                                + "\"movementType\":\"OUT\",\"quantity\":5}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("OUT_MOVEMENT_NOT_IMPLEMENTED"));
    }
}
