package mops.rheinjug2.controllers;

import static mops.rheinjug2.KeycloakTokenMock.setupTokenMock;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


import java.util.HashSet;
import java.util.Set;
import mops.rheinjug2.Account;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.fileupload.FileService;
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
  private transient FileService fileService;

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
  void testReportsubmitNoAdmisionOrga() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("orga");

    final Account account = new Account("name", "User@email.de", "image", roles,
        "givenname", "familyname");
    setupTokenMock(account);

    final String eventId = "123";
    mvc.perform(get("/rheinjug2/student/reportsubmit").param("eventId", eventId))
        .andExpect(status().isForbidden());
  }
}