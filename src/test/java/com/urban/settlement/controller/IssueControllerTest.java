package com.urban.settlement.controller;

import com.urban.settlement.model.Issue;
import com.urban.settlement.model.enums.IssueStatus;
import com.urban.settlement.service.ImageClassificationService;
import com.urban.settlement.service.IssueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IssueController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
public class IssueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IssueService issueService;

    @MockBean
    private ImageClassificationService classificationService;

    @Test
    public void testGetAllIssues() throws Exception {
        Issue issue = new Issue();
        issue.setId("1");
        issue.setTitle("Test Issue");
        issue.setStatus(IssueStatus.PENDING);

        Page<Issue> issuePage = new PageImpl<>(Collections.singletonList(issue));

        given(issueService.getAllIssues(any())).willReturn(issuePage);

        mockMvc.perform(get("/api/issues")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Issue"));
    }

    @Test
    public void testGetIssueById() throws Exception {
        Issue issue = new Issue();
        issue.setId("1");
        issue.setTitle("Test Issue");

        given(issueService.getIssueById("1")).willReturn(issue);

        mockMvc.perform(get("/api/issues/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Issue"));
    }
}
