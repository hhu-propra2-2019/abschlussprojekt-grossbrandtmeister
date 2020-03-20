package mops.rheinjug2.controllers;

import static mops.rheinjug2.KeycloakTokenMock.setupTokenMock;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashSet;
import java.util.Set;
import mops.rheinjug2.Account;
import mops.rheinjug2.fileupload.FileService;
import org.apache.catalina.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class StudentControllerTest {
  @Autowired
  private MockMvc mvc;

  @MockBean
  private FileService fileService;

  @Autowired
  private WebApplicationContext context;

  @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
  MeterRegistry registry;

  @BeforeEach
  public void setup() {
    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  @Test
  void testReportsubmitAdmisionStudent() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("studentin");
    final Account account = new Account("name", "User@email.de", "image", roles, "givenname", "familyname");
    setupTokenMock(account);

    mvc.perform(get("/rheinjug2/student/reportsubmit"))
        .andExpect(status().isOk())
        .andExpect(view().name("report_submit"));
  }

  @Test
  void testReportsubmitNoAdmisionOrga() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("orga");
    final Account account = new Account("name", "User@email.de", "image", roles, "givenname", "familyname");
    setupTokenMock(account);

    mvc.perform(get("/rheinjug2/student/reportsubmit"))
        .andExpect(status().isForbidden());
  }

}