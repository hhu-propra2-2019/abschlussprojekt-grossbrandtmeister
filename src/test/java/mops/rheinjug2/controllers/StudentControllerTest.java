package mops.rheinjug2.controllers;

import static mops.rheinjug2.KeycloakTokenMock.setupMockUserWithRole;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.services.ModelService;
import mops.rheinjug2.services.SubmissionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class StudentControllerTest {
  @Autowired
  private transient MockMvc mvc;

  @MockBean
  private transient ModelService modelService;

  @Autowired
  private transient WebApplicationContext context;

  @BeforeEach
  public void setUp() {
    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  @Test
  void testGetPersonalStudent() throws Exception {
    setupMockUserWithRole("studentin");

    final Event event1 = new Event();
    event1.setDate(LocalDateTime.now());
    event1.setType("Entwickelbar");
    event1.setTitle("Entwickelbar");
    event1.setDuration(Duration.ofHours(1));
    final Map<Event, SubmissionStatus> map = new HashMap<>();
    map.put(event1, SubmissionStatus.UPCOMING);
    when(modelService.studentExists(anyString())).thenReturn(true);
    when(modelService.getAllEventsPerStudent(anyString())).thenReturn(map);

    mvc.perform(get("/rheinjug2/student/visitedevents"))
        .andExpect(status().isOk())
        .andExpect(view().name("personalView"));
  }

  @Test
  void testGetPersonalOrga() throws Exception {
    setupMockUserWithRole("orga");

    mvc.perform(get("/rheinjug2/student/visitedevents"))
        .andExpect(status().isForbidden());
  }

  @Test
  void testReportsubmitAdmissionStudent() throws Exception {
    setupMockUserWithRole("studentin");

    final Event event = new Event();
    when(modelService.getDeadline(anyString(), any())).thenReturn(LocalDateTime.MAX);
    when(modelService.loadEventById(anyLong())).thenReturn(event);

    final String eventId = "123";
    mvc.perform(get("/rheinjug2/student/reportsubmit").param("eventId", eventId))
        .andExpect(status().isOk())
        .andExpect(view().name("report_submit"));
  }

  @Test
  void testReportEventDeadlineIsPassed() throws Exception {
    setupMockUserWithRole("studentin");

    final Event event = new Event();
    when(modelService.getDeadline(anyString(), any())).thenReturn(LocalDateTime.MIN);
    when(modelService.loadEventById(anyLong())).thenReturn(event);

    final String eventId = "123";
    mvc.perform(get("/rheinjug2/student/reportsubmit").param("eventId", eventId))
        .andExpect(redirectedUrl("rheinjug2/student/visitedevents"));
  }


  @Test
  void testReportsubmitNoAdmissionOrga() throws Exception {
    setupMockUserWithRole("orga");

    final String eventId = "123";
    mvc.perform(get("/rheinjug2/student/reportsubmit").param("eventId", eventId))
        .andExpect(status().isForbidden());
  }

  @Test
  void testStudentEventsOverviewAdmissionStudent() throws Exception {
    setupMockUserWithRole("studentin");

    final Event event1 = new Event();
    final Event event2 = new Event();

    event1.setTitle("Event 1");
    event2.setTitle("Event 2");
    event1.setDate(LocalDateTime.now().plusDays(1));
    event2.setDate(LocalDateTime.now().plusDays(1));
    event1.setStatus("UPCOMING");
    event2.setStatus("UPCOMING");
    event1.setType("Entwicklelbar");
    event1.setType("Entwicklelbar");
    event1.setId((long) 1);
    event2.setId((long) 2);

    when(modelService.getAllEvents()).thenReturn(List.of(event1, event2));
    when(modelService.getAllEventIdsPerStudent(anyString())).thenReturn(List.of(event1.getId()));

    mvc.perform(get("/rheinjug2/student/events"))
        .andExpect(status().isOk()).andExpect(view()
        .name("student_events_overview"));

  }

  @Test
  void testStudentEventsOverviewNoAdmissionOrga() throws Exception {
    setupMockUserWithRole("orga");

    mvc.perform(get("/rheinjug2/student/events"))
        .andExpect(status().isForbidden());

  }

  @Test
  void testAddStudentToEventOrga() throws Exception {
    setupMockUserWithRole("orga");

    mvc.perform(post("/rheinjug2/student/events"))
        .andExpect(status().isForbidden());
  }

  @Test
  void testAddStudentToEventStudent() throws Exception {
    setupMockUserWithRole("studentin");

    final String eventId = "123";
    mvc.perform(post("/rheinjug2/student/events")
        .param("eventId", eventId)
        .with(csrf()))
        .andExpect(status().isFound())
        .andExpect(redirectedUrl("/rheinjug2/student/visitedevents"));
  }

}
