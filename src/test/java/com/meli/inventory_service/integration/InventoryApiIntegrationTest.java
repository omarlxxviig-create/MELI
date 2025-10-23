package com.meli.inventory_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meli.inventory_service.infrastructure.rest.dto.ReserveRequest;
import com.meli.inventory_service.infrastructure.rest.dto.ReserveResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class InventoryApiIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @Sql("/test-data.sql")
        public void fullReservationFlow() throws Exception {
                // 1. Create reservation
                ReserveRequest request = new ReserveRequest();
                request.setStoreId("store-1");
                request.setProductId("sku-1");
                request.setQuantity(2);
                request.setTransactionId("test-tx-1");

                MvcResult result = mockMvc.perform(post("/api/inventory/reserve")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("PENDING"))
                                .andReturn();

                // Extract reservationId from response
                ReserveResponse response = objectMapper.readValue(
                                result.getResponse().getContentAsString(),
                                ReserveResponse.class);

                // 2. Verify inventory was updated
                mockMvc.perform(get("/api/inventory"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].reservedQuantity").value(2));

                // 3. Commit reservation using extracted ID
                mockMvc.perform(post("/api/inventory/commit")
                                .param("reservationId", response.getReservationId()))
                                .andExpect(status().isOk());

                // 4. Verify final inventory state
                mockMvc.perform(get("/api/inventory"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].totalQuantity").value(98));
        }
}
