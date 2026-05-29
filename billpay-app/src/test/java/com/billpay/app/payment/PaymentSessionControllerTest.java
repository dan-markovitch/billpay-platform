package com.billpay.app.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PaymentSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void create_returns201_forWebChannel() throws Exception {
        mockMvc.perform(post("/api/v1/payment-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "channel": "WEB",
                      "merchantId": "merchant-42"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.channel").value("WEB"))
            .andExpect(jsonPath("$.data.merchantId").value("merchant-42"))
            .andExpect(jsonPath("$.data.status").value("INITIATED"));
    }

    @Test
    void create_returns201_forIvrChannel() throws Exception {
        mockMvc.perform(post("/api/v1/payment-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "channel": "IVR",
                      "merchantId": "merchant-ivr"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.channel").value("IVR"));
    }

    @Test
    void getById_returns404_whenMissing() throws Exception {
        mockMvc.perform(get("/api/v1/payment-sessions/{id}", "00000000-0000-0000-0000-000000000099"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Payment session not found"));
    }
}
