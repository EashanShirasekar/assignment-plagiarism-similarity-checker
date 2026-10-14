package com.example.plagiarismchecker.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies the complete workflow described in the project brief actually
 * works end-to-end over real HTTP requests, against an in-memory H2
 * database (see application-test.properties): a teacher creates an
 * assignment, two students submit near-identical text, the teacher runs
 * the analysis, and the result comes back correctly flagged as HIGH
 * similarity with a non-empty comparison view.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EndToEndWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullPlagiarismCheckWorkflowWorksEndToEnd() throws Exception {
        String teacherToken = registerAndGetToken("Dr. Test Teacher", "e2e.teacher@college.edu", "password123", "TEACHER");
        String student1Token = registerAndGetToken("E2E Student One", "e2e.student1@college.edu", "password123", "STUDENT");
        String student2Token = registerAndGetToken("E2E Student Two", "e2e.student2@college.edu", "password123", "STUDENT");

        // Teacher creates an assignment
        String deadline = LocalDateTime.now().plusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Map<String, Object> assignmentPayload = new LinkedHashMap<>();
        assignmentPayload.put("title", "E2E Test Assignment");
        assignmentPayload.put("description", "Explain recursion with an example.");
        assignmentPayload.put("deadline", deadline);
        String assignmentBody = objectMapper.writeValueAsString(assignmentPayload);

        String assignmentJson = mockMvc.perform(post("/api/assignments")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType("application/json")
                        .content(assignmentBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long assignmentId = objectMapper.readTree(assignmentJson).get("id").asLong();

        // Both students submit near-identical text
        String sharedText = "Recursion is when a function calls itself to solve a smaller version of the same problem.";
        submitAnswer(student1Token, assignmentId, sharedText);
        submitAnswer(student2Token, assignmentId, sharedText);

        // Teacher runs the analysis
        String analyzeJson = mockMvc.perform(post("/api/analysis/assignments/" + assignmentId)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode results = objectMapper.readTree(analyzeJson);
        assertThat(results.isArray()).isTrue();
        assertThat(results.size()).isEqualTo(1);
        assertThat(results.get(0).get("similarityPercentage").asDouble()).isGreaterThan(90.0);
        assertThat(results.get(0).get("similarityLevel").asText()).isEqualTo("HIGH");
        assertThat(results.get(0).get("flagged").asBoolean()).isTrue();

        long resultId = results.get(0).get("id").asLong();

        // Fetching stored results works too
        mockMvc.perform(get("/api/analysis/assignments/" + assignmentId)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].similarityLevel").value("HIGH"));

        // Comparison view returns both submissions and a disclaimer
        mockMvc.perform(get("/api/analysis/" + resultId + "/comparison")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content1").isNotEmpty())
                .andExpect(jsonPath("$.content2").isNotEmpty())
                .andExpect(jsonPath("$.disclaimer").isNotEmpty());

        // A student cannot see another student's raw submission list for the assignment
        mockMvc.perform(get("/api/assignments/" + assignmentId + "/submissions")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isForbidden());
    }

    private String registerAndGetToken(String name, String email, String password, String role) throws Exception {
        String body = String.format(
                "{\"name\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"role\":\"%s\"}",
                name, email, password, role);

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }

    private void submitAnswer(String studentToken, long assignmentId, String content) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("assignmentId", assignmentId);
        payload.put("content", content);
        String body = objectMapper.writeValueAsString(payload);

        mockMvc.perform(post("/api/submissions")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated());
    }
}
