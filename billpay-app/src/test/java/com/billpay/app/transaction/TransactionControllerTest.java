package com.billpay.app.transaction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void create_returns201_withTransactionData() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validTransactionJson("Electric bill", "125.50")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.description").value("Electric bill"))
            .andExpect(jsonPath("$.data.amount").value(125.50))
            .andExpect(jsonPath("$.data.currency").value("USD"))
            .andExpect(jsonPath("$.data.transactionDate").value("2026-05-28"))
            .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    void getById_returnsPersistedTransaction_afterCreate() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validTransactionJson("Water bill", "42.00")))
            .andExpect(status().isCreated())
            .andReturn();

        String id = readJsonPath(created, "$.data.id");

        mockMvc.perform(get("/api/v1/transactions/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(id))
            .andExpect(jsonPath("$.data.description").value("Water bill"))
            .andExpect(jsonPath("$.data.amount").value(42.00))
            .andExpect(jsonPath("$.data.currency").value("USD"));
    }

    @Test
    void getById_returns404_whenTransactionDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/{id}", "00000000-0000-0000-0000-000000000001"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Transaction not found"));
    }

    @Test
    void list_returnsTransactionsOrderedByCreatedAt() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validTransactionJson("First", "10.00")))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validTransactionJson("Second", "20.00")))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/transactions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize(2)))
            .andExpect(jsonPath("$.data[*].description").value(containsInAnyOrder("First", "Second")));
    }

    @Test
    void create_rejectsDescriptionLongerThan50Characters() throws Exception {
        String longDescription = "x".repeat(51);

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validTransactionJson(longDescription, "10.00")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error", containsString("description")))
            .andExpect(jsonPath("$.error", containsString("50 characters")));
    }

    @Test
    void create_rejectsZeroAmount() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validTransactionJson("Zero amount", "0")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error", containsString("amount")))
            .andExpect(jsonPath("$.error", containsString("greater than zero")));
    }

    @Test
    void create_rejectsNegativeAmount() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validTransactionJson("Negative", "-5.00")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error", containsString("amount")));
    }

    @Test
    void create_rejectsFutureTransactionDate() throws Exception {
        String futureDate = LocalDate.now().plusDays(1).toString();

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "description": "Future dated",
                      "amount": 10.00,
                      "currency": "USD",
                      "transactionDate": "%s"
                    }
                    """.formatted(futureDate)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error", containsString("transactionDate")))
            .andExpect(jsonPath("$.error", containsString("future")));
    }

    @Test
    void create_rejectsInvalidCurrencyCode() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validTransactionJson("Bad currency", "10.00").replace("USD", "US")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error", containsString("currency")));
    }

    @Test
    void create_rejectsBlankDescription() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validTransactionJson("", "10.00")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error", containsString("description")));
    }

    @Test
    void create_rejectsMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void create_rejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ bad-json"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Invalid request body"));
    }

    @Test
    void getById_returns400_forInvalidUuidFormat() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/{id}", "not-a-uuid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error", containsString("Invalid value for parameter")));
    }

    private static String validTransactionJson(String description, String amount) {
        return """
            {
              "description": "%s",
              "amount": %s,
              "currency": "USD",
              "transactionDate": "2026-05-28"
            }
            """.formatted(description, amount);
    }

    private static String readJsonPath(MvcResult result, String path) throws Exception {
        return com.jayway.jsonpath.JsonPath.read(
            result.getResponse().getContentAsString(),
            path
        ).toString();
    }
}
