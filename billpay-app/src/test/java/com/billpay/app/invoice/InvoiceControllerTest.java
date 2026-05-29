package com.billpay.app.invoice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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
    void ingest_returns201_withFullInvoiceData() throws Exception {
        mockMvc.perform(post("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validInvoiceJson("INV-1001", "250.00", "USD")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.externalReference").value("INV-1001"))
            .andExpect(jsonPath("$.data.amount").value(250.00))
            .andExpect(jsonPath("$.data.currency").value("USD"))
            .andExpect(jsonPath("$.data.status").value("RECEIVED"))
            .andExpect(jsonPath("$.data.receivedAt").exists());
    }

    @Test
    void getById_returnsPersistedInvoice_afterIngest() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validInvoiceJson("INV-GET", "99.99", "EUR")))
            .andExpect(status().isCreated())
            .andReturn();

        String id = readJsonPath(created, "$.data.id");

        mockMvc.perform(get("/api/v1/invoices/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(id))
            .andExpect(jsonPath("$.data.externalReference").value("INV-GET"))
            .andExpect(jsonPath("$.data.amount").value(99.99))
            .andExpect(jsonPath("$.data.currency").value("EUR"));
    }

    @Test
    void ingest_returns409_forDuplicateExternalReference() throws Exception {
        String body = validInvoiceJson("INV-DUP", "10.00", "USD");

        mockMvc.perform(post("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error", containsString("already exists")));
    }

    @Test
    void getById_returns404_whenMissing() throws Exception {
        mockMvc.perform(get("/api/v1/invoices/{id}", "00000000-0000-0000-0000-000000000099"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Invoice not found"));
    }

    @Test
    void ingest_rejectsBlankExternalReference() throws Exception {
        mockMvc.perform(post("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validInvoiceJson("", "10.00", "USD")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error", containsString("externalReference")));
    }

    @Test
    void ingest_rejectsExternalReferenceLongerThan100Characters() throws Exception {
        mockMvc.perform(post("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validInvoiceJson("r".repeat(101), "10.00", "USD")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error", containsString("externalReference")));
    }

    @Test
    void ingest_rejectsZeroAmount() throws Exception {
        mockMvc.perform(post("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validInvoiceJson("INV-ZERO", "0", "USD")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error", containsString("amount")))
            .andExpect(jsonPath("$.error", containsString("greater than zero")));
    }

    @Test
    void ingest_rejectsNegativeAmount() throws Exception {
        mockMvc.perform(post("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validInvoiceJson("INV-NEG", "-1.00", "USD")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error", containsString("amount")));
    }

    @Test
    void ingest_rejectsInvalidCurrencyCode() throws Exception {
        mockMvc.perform(post("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validInvoiceJson("INV-BAD", "10.00", "usd")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error", containsString("currency")));
    }

    @Test
    void ingest_rejectsMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void ingest_rejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Invalid request body"));
    }

    private static String validInvoiceJson(String externalReference, String amount, String currency) {
        return """
            {
              "externalReference": "%s",
              "amount": %s,
              "currency": "%s"
            }
            """.formatted(externalReference, amount, currency);
    }

    private static String readJsonPath(MvcResult result, String path) throws Exception {
        return com.jayway.jsonpath.JsonPath.read(
            result.getResponse().getContentAsString(),
            path
        ).toString();
    }
}
