package mops.rheinjug2.controllers;

import static mops.rheinjug2.KeycloakTokenMock.setupMockUserWithRole;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import mops.rheinjug2.entities.Event;
import mops.rheinjug2.services.ModelService;
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
  void testReportsubmitAdmisionStudent() throws Exception {
    setupMockUserWithRole("studentin");

    final Event event = new Event();
    when(modelService.loadEventById(anyLong())).thenReturn(event);

    final String eventId = "123";
    mvc.perform(get("/rheinjug2/student/reportsubmit").param("eventId", eventId))
        .andExpect(status().isOk())
        .andExpect(view().name("report_submit"));
  }

  @Test
  void testReportsubmitNoAdmisionOrga() throws Exception {
    setupMockUserWithRole("orga");

    final String eventId = "123";
    mvc.perform(get("/rheinjug2/student/reportsubmit").param("eventId", eventId))
        .andExpect(status().isForbidden());
  }
}