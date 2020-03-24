package mops.rheinjug2.controllers;

import static mops.rheinjug2.KeycloakTokenMock.setupTokenMock;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mops.rheinjug2.Account;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.fileupload.FileService;
import mops.rheinjug2.services.ModelService;
import mops.rheinjug2.services.SubmissionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
class StudentControllerTest {
  @Autowired
  private transient MockMvc mvc;

  @MockBean
  private transient FileService fileService;

  @MockBean
  private transient ModelService modelService;

  @Autowired
  private transient WebApplicationContext context;

  @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
  MeterRegistry registry;

  @BeforeEach
  public void setUp() {
    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  @Test
  void testGetPersonalStudent() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("studentin");
    final Account account = new Account("name", "User@email.de", "image", roles,
        "givenname", "familyname");
    setupTokenMock(account);

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
    final Set<String> roles = new HashSet<>();
    roles.add("orga");
    final Account account = new Account("name", "User@email.de", "image", roles,
        "givenname", "familyname");
    setupTokenMock(account);

    mvc.perform(get("/rheinjug2/student/visitedevents"))
        .andExpect(status().isForbidden());
  }

  @Test
  void testReportsubmitAdmissionStudent() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("studentin");
    final Account account = new Account("name", "User@email.de", "image", roles,
        "givenname", "familyname");
    setupTokenMock(account);

    final Event event = new Event();
    when(modelService.loadEventById(anyLong())).thenReturn(event);

    final String eventId = "123";
    mvc.perform(get("/rheinjug2/student/reportsubmit").param("eventId", eventId))
        .andExpect(status().isOk())
        .andExpect(view().name("report_submit"));
  }

  @Test
  void testReportsubmitNoAdmissionOrga() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("orga");
    final Account account = new Account("name", "User@email.de", "image", roles,
        "givenname", "familyname");
    setupTokenMock(account);

    final String eventId = "123";
    mvc.perform(get("/rheinjug2/student/reportsubmit").param("eventId", eventId))
        .andExpect(status().isForbidden());
  }

  @Test
  void testStudentEventsOverviewAdmissionStudent() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("studentin");
    final Account account = new Account("name", "User@email.de", "image", roles,
        "givenname", "familyname");
    setupTokenMock(account);

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
}
