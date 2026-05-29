package com.billpay.app.invoice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void ingest_returns201_withReceivedStatus() throws Exception {
        mockMvc.perform(post("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "externalReference": "INV-1001",
                      "amount": 250.00,
                      "currency": "USD"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.externalReference").value("INV-1001"))
            .andExpect(jsonPath("$.data.status").value("RECEIVED"));
    }

    @Test
    void ingest_returns409_forDuplicateExternalReference() throws Exception {
        String body = """
            {
              "externalReference": "INV-DUP",
              "amount": 10.00,
              "currency": "USD"
            }
            """;

        mockMvc.perform(post("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error", containsString("already exists")));
    }

    @Test
    void getById_returns404_whenMissing() throws Exception {
        mockMvc.perform(get("/api/v1/invoices/{id}", "00000000-0000-0000-0000-000000000099"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Invoice not found"));
    }
}
