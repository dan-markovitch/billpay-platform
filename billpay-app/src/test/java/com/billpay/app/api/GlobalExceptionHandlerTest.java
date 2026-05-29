package com.billpay.app.api;

import com.billpay.common.api.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ValidationProbeController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void validationErrors_return400_withErrorEnvelope() throws Exception {
        mockMvc.perform(post("/api/v1/_probe/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("name: must not be blank"));
    }

    @Test
    void apiException_returnsStatus_withErrorEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/_probe/api-error"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Resource not found"));
    }

    @Test
    void malformedJson_returns400_withErrorEnvelope() throws Exception {
        mockMvc.perform(post("/api/v1/_probe/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ not valid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Invalid request body"));
    }
}
