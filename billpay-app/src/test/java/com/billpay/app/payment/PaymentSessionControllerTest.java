package com.billpay.app.payment;

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
class PaymentSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void create_returns201_forWebChannel() throws Exception {
        mockMvc.perform(post("/api/v1/payment-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sessionJson("WEB", "merchant-42")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.channel").value("WEB"))
            .andExpect(jsonPath("$.data.merchantId").value("merchant-42"))
            .andExpect(jsonPath("$.data.status").value("INITIATED"))
            .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    void create_returns201_forIvrChannel() throws Exception {
        mockMvc.perform(post("/api/v1/payment-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sessionJson("IVR", "merchant-ivr")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.channel").value("IVR"))
            .andExpect(jsonPath("$.data.merchantId").value("merchant-ivr"));
    }

    @Test
    void getById_returnsPersistedSession_afterCreate() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/payment-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sessionJson("WEB", "merchant-persist")))
            .andExpect(status().isCreated())
            .andReturn();

        String id = readJsonPath(created, "$.data.id");

        mockMvc.perform(get("/api/v1/payment-sessions/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(id))
            .andExpect(jsonPath("$.data.merchantId").value("merchant-persist"));
    }

    @Test
    void getById_returns404_whenMissing() throws Exception {
        mockMvc.perform(get("/api/v1/payment-sessions/{id}", "00000000-0000-0000-0000-000000000099"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Payment session not found"));
    }

    @Test
    void create_rejectsBlankMerchantId() throws Exception {
        mockMvc.perform(post("/api/v1/payment-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sessionJson("WEB", "")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error", containsString("merchantId")));
    }

    @Test
    void create_rejectsMerchantIdLongerThan100Characters() throws Exception {
        mockMvc.perform(post("/api/v1/payment-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sessionJson("WEB", "m".repeat(101))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error", containsString("merchantId")));
    }

    @Test
    void create_rejectsMissingChannel() throws Exception {
        mockMvc.perform(post("/api/v1/payment-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "merchantId": "merchant-42"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error", containsString("channel")));
    }

    @Test
    void create_rejectsNullChannel() throws Exception {
        mockMvc.perform(post("/api/v1/payment-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "channel": null,
                      "merchantId": "merchant-42"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error", containsString("channel")));
    }

    @Test
    void create_rejectsInvalidChannelValue() throws Exception {
        mockMvc.perform(post("/api/v1/payment-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "channel": "SMS",
                      "merchantId": "merchant-42"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Invalid request body"));
    }

    @Test
    void create_rejectsMissingMerchantId() throws Exception {
        mockMvc.perform(post("/api/v1/payment-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "channel": "WEB"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error", containsString("merchantId")));
    }

    @Test
    void create_rejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/payment-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ not-json"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Invalid request body"));
    }

    private static String sessionJson(String channel, String merchantId) {
        return """
            {
              "channel": "%s",
              "merchantId": "%s"
            }
            """.formatted(channel, merchantId);
    }

    private static String readJsonPath(MvcResult result, String path) throws Exception {
        return com.jayway.jsonpath.JsonPath.read(
            result.getResponse().getContentAsString(),
            path
        ).toString();
    }
}
