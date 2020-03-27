package mops.rheinjug2.controllers;

import static mops.rheinjug2.KeycloakTokenMock.setupMockUserWithRole;
import static mops.rheinjug2.KeycloakTokenMock.setupTokenMock;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mops.rheinjug2.Account;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.entities.Student;
import mops.rheinjug2.orgamodels.DelayedSubmission;
import mops.rheinjug2.orgamodels.OrgaEvent;
import mops.rheinjug2.orgamodels.OrgaSummary;
import mops.rheinjug2.services.OrgaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testing OrgaController")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class OrgaControllerTest {
  @Autowired
  private transient MockMvc mvc;

  @Autowired
  private transient WebApplicationContext context;

  @MockBean
  private transient OrgaService orgaService;
  @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
  MeterRegistry registry;

  private static final String BASE_URL = "/rheinjug2/orga";

  /**
   * Setzt den mvc mock auf.
   */
  @BeforeEach
  public void setUp() {
    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  @Test
  public void accessOrgaPagesNoAccount() throws Exception {
    mvc.perform(get(BASE_URL))
        .andExpect(status().isFound());
    mvc.perform(get(BASE_URL + "/"))
        .andExpect(status().isFound());
    mvc.perform(get(BASE_URL + "/events"))
        .andExpect(status().isFound());
    mvc.perform(get(BASE_URL + "/delayedSubmission"))
        .andExpect(status().isFound());
    mvc.perform(post(BASE_URL + "/summaryaccepting"))
        .andExpect(status().isForbidden());
    mvc.perform(post(BASE_URL + "/summaryupload"))
        .andExpect(status().isForbidden());
    mvc.perform(get(BASE_URL + "/reports"))
        .andExpect(status().isFound());
    mvc.perform(post(BASE_URL + "/searchstudent"))
        .andExpect(status().isForbidden());
    mvc.perform(post(BASE_URL + "/searchevent"))
        .andExpect(status().isForbidden());

  }

  @Test
  public void accessOrgaPageNoRole() throws Exception {
    final Set<String> roles = new HashSet<>();
    final Account account = new Account("NAME", "USER_EMAIL_DE", "IMAGE", roles,
        "GIVENNAME", "FAMILYNAME");
    setupTokenMock(account);

    mvc.perform(get(BASE_URL))
        .andExpect(status().isForbidden());
    mvc.perform(get(BASE_URL + "/"))
        .andExpect(status().isForbidden());
    mvc.perform(get(BASE_URL + "/events"))
        .andExpect(status().isForbidden());
    mvc.perform(get(BASE_URL + "/delayedSubmission"))
        .andExpect(status().isForbidden());
    mvc.perform(post(BASE_URL + "/summaryaccepting"))
        .andExpect(status().isForbidden());
    mvc.perform(post(BASE_URL + "/summaryupload"))
        .andExpect(status().isForbidden());
    mvc.perform(get(BASE_URL + "/reports"))
        .andExpect(status().isForbidden());
    mvc.perform(post(BASE_URL + "/searchstudent"))
        .andExpect(status().isForbidden());
    mvc.perform(post(BASE_URL + "/searchevent"))
        .andExpect(status().isForbidden());
    mvc.perform(post(BASE_URL + "/events"))
        .andExpect(status().isForbidden());
  }

  @Test
  public void accessOrgaPageStudentRole() throws Exception {
    setupMockUserWithRole("studentin");

    mvc.perform(get(BASE_URL))
        .andExpect(status().isForbidden());
    mvc.perform(get(BASE_URL + "/"))
        .andExpect(status().isForbidden());
    mvc.perform(get(BASE_URL + "/events"))
        .andExpect(status().isForbidden());
    mvc.perform(get(BASE_URL + "/delayedSubmission"))
        .andExpect(status().isForbidden());
    mvc.perform(post(BASE_URL + "/summaryaccepting"))
        .andExpect(status().isForbidden());
    mvc.perform(post(BASE_URL + "/summaryupload"))
        .andExpect(status().isForbidden());
    mvc.perform(get(BASE_URL + "/reports"))
        .andExpect(status().isForbidden());
    mvc.perform(post(BASE_URL + "/searchstudent"))
        .andExpect(status().isForbidden());
    mvc.perform(post(BASE_URL + "/searchevent"))
        .andExpect(status().isForbidden());
    mvc.perform(post(BASE_URL + "/events"))
        .andExpect(status().isForbidden());
  }


  @Test
  public void rgaGetEventsTest() throws Exception {
    setupMockUserWithRole("orga");
    final Event event = new Event();
    event.setDeadline(LocalDateTime.now().plusDays(1));
    event.setStatus("PAST");
    final List<OrgaEvent> orgaEvents = List.of(new OrgaEvent(event, 1, 1));
    when(orgaService.getEvents()).thenReturn(orgaEvents);
    when(orgaService.getnumberOfEvaluationRequests()).thenReturn(1);

    mvc.perform(get(BASE_URL + "/events"))
        .andExpect(status().isOk())
        .andExpect(view().name("orga_events_overview"))
        .andExpect(MockMvcResultMatchers.model()
            .attribute("events", orgaEvents))
        .andExpect(MockMvcResultMatchers.model()
            .attribute("numberOfEvaluationRequests", 1));
  }

  @Test
  public void getDelayedSubmissionTest() throws Exception {
    setupMockUserWithRole("orga");
    when(orgaService.getnumberOfEvaluationRequests()).thenReturn(1);
    mvc.perform(get(BASE_URL + "/delayedSubmission"))
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.model()
            .attribute("numberOfEvaluationRequests", 1));
  }

  @Test
  @Disabled
  public void summaryacceptingTest() throws Exception {
    setupMockUserWithRole("orga");
    when(orgaService.setSummaryAsAccepted((long) 1, (long) 1)).thenReturn(true);

    mvc.perform(post(BASE_URL + "/summaryaccepting")
        .param("eventid", "1")
        .param("studentid", "1")
        .with(csrf()))
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:/rheinjug2/orga/reports"));
    verify(orgaService, times(1)).getnumberOfEvaluationRequests();
  }

  @Test
  @Disabled
  public void summaryuploadTest() throws Exception {
    setupMockUserWithRole("orga");

    final LocalDateTime date = LocalDateTime.now().minusDays(1);
    doNothing().when(orgaService).summaryuploadStringContent(
        anyLong(), anyLong(), anyString(), anyString());

    mvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/summaryupload")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .param("studentId", "1")
        .param("eventId", "2")
        .param("studentName", "xfgdfg")
        .param("eventTitle", "dfgdfg")
        .param("deadLine", String.valueOf(date))
        .param("summaryContent", "dfgdfg")
        .with(csrf()))
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:/rheinjug2/orga/delayedSubmission"));
  }

  @Test
  @Disabled
  public void searchstudentTest() throws Exception {
    setupMockUserWithRole("orga");
    when(orgaService.getnumberOfEvaluationRequests()).thenReturn(1);
    final DelayedSubmission delayedSubmission = new DelayedSubmission(
        1, 2, "name", "4",
        LocalDateTime.now(), "6");
    when(orgaService.getDelayedSubmissionsForStudent("name"))
        .thenReturn(List.of(delayedSubmission));

    mvc.perform(post(BASE_URL + "/summaryupload")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .with(csrf())
        .param("searchedName", "name"))
        .andExpect(status().isFound());
  }

  @Test
  public void getReportsTest() throws Exception {
    setupMockUserWithRole("orga");
    when(orgaService.getnumberOfEvaluationRequests()).thenReturn(1);
    final Event event = new Event();
    event.setDeadline(LocalDateTime.now().plusDays(1));
    final Student student = new Student("", "");
    final OrgaSummary orgaSummary = new OrgaSummary(LocalDateTime.now(), student,
        event, "summary");
    when(orgaService.getSummaries()).thenReturn(List.of(orgaSummary));


    mvc.perform(get(BASE_URL + "/reports"))
        .andExpect(status().isOk())
        .andExpect(view().name("orga_reports_overview"))
        .andExpect(MockMvcResultMatchers.model()
            .attribute("summaries", List.of(orgaSummary)))
        .andExpect(MockMvcResultMatchers.model()
            .attribute("numberOfEvaluationRequests", 1));
  }
}

