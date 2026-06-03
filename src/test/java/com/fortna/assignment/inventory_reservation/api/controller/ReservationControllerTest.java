package com.fortna.assignment.inventory_reservation.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortna.assignment.inventory_reservation.api.dto.request.CreateReservationRequest;
import com.fortna.assignment.inventory_reservation.api.dto.request.ReservationItemRequest;
import com.fortna.assignment.inventory_reservation.application.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationService reservationService;

    @Test
    void createReservation_withBlankOrderId_returns400() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setOrderId("");
        
        ReservationItemRequest item = new ReservationItemRequest();
        item.setSku("A100");
        item.setQuantity(5);
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("orderId: Order ID is required"));
    }

    @Test
    void createReservation_withNegativeQuantity_returns400() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setOrderId("ORD-123");
        
        ReservationItemRequest item = new ReservationItemRequest();
        item.setSku("A100");
        item.setQuantity(-1);
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("items[0].quantity: Quantity must be at least 1"));
    }
}
