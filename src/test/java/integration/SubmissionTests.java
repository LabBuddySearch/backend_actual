package integration;

import lombok.RequiredArgsConstructor;
import org.example.CodeGuardApplication;
import org.example.dto.response.task.SubmissionResponse;
import org.example.entity.Status;
import org.example.exception.NotFoundException;
import org.example.facade.SubmissionFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = CodeGuardApplication.class)
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public class SubmissionTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubmissionFacade submissionFacade;

    private String validRequestJson() {
        return """
                    {
                      "language": "PYTHON",
                      "sourceCode": "print(1)"
                    }
                """;
    }

    private String invalidRequestJson() {
        return """
                    {
                      "language": "PYTHON",
                      "sourceCode": ""
                    }
                """;
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "STUDENT")
    void createSubmission_ReturnsCompilationError() throws Exception {
        SubmissionResponse response = new SubmissionResponse();
        response.setStatus(Status.COMPILATION_ERROR);
        response.setStderr("error: cannot find symbol");
        when(submissionFacade.submit(eq(1), eq("user@test.com"), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/submissions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPILATION_ERROR"))
                .andExpect(jsonPath("$.stderr").exists());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createSubmission_ReturnsRuntimeError() throws Exception {
        SubmissionResponse response = new SubmissionResponse();
        response.setStatus(Status.RUNTIME_ERROR);
        response.setStderr("NullPointerException");

        when(submissionFacade.submit(any(), any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/submissions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RUNTIME_ERROR"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createSubmission_ReturnsTimeLimitExceeded() throws Exception {
        SubmissionResponse response = new SubmissionResponse();
        response.setStatus(Status.TIME_LIMIT_EXCEEDED);

        when(submissionFacade.submit(any(), any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/submissions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("TIME_LIMIT_EXCEEDED"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createSubmission_WrongTaskId_Returns404Error() throws Exception {
        when(submissionFacade.submit(any(), any(), any()))
                .thenThrow(new NotFoundException("Task does not exist"));

        mockMvc.perform(post("/api/submissions/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createSubmission_WrongLanguage_Returns404Error() throws Exception {
        when(submissionFacade.submit(any(), any(), any()))
                .thenThrow(new NotFoundException("Язык пока не поддерживается."));

        mockMvc.perform(post("/api/submissions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createSubmission_InvalidRequest_Returns400Error() throws Exception {
        String invalidJson = "{}";

        mockMvc.perform(post("/api/submissions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSubmission_UnauthorizedUser_Returns401Error() throws Exception {
        mockMvc.perform(post("/api/submissions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void createSubmission_Teacher_Returns403() throws Exception {
        mockMvc.perform(post("/api/submissions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getSubmission_WrongRole_Returns403Error() throws Exception {
        mockMvc.perform(get("/api/submissions/teacher/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "STUDENT")
    void getSubmission_ValidUser_Returns200() throws Exception {

        when(submissionFacade.get(eq(1), eq(false), any()))
                .thenReturn(new SubmissionResponse());

        mockMvc.perform(get("/api/submissions/student/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(submissionFacade).get(eq(1), eq(false), any());
    }
}
