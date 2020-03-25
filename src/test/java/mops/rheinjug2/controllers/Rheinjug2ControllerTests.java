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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("Testing Rheinjug2Controller")
public class Rheinjug2ControllerTests {

  @Autowired
  private transient MockMvc mvc;

  @Autowired
  private transient WebApplicationContext context;

  @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
  MeterRegistry registry;

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
  public void accessIndexPageNoAccount() throws Exception {
    mvc.perform(get("/rheinjug2/"))
        .andExpect(status().isOk())
        .andExpect(view().name("index"));
  }

  @Test
  public void accessIndexPageNoRole() throws Exception {
    final Set<String> roles = new HashSet<>();
    final Account account = new Account("name", "User@email.de", "image", roles,
        "givenname", "familyname");
    setupTokenMock(account);

    mvc.perform(get("/rheinjug2/"))
        .andExpect(status().isOk())
        .andExpect(view().name("index"));
  }

  @Test
  public void accessIndexPageOrgaRole() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("orga");
    final Account account = new Account("name", "User@email.de", "image", roles,
        "givenname", "familyname");
    setupTokenMock(account);

    mvc.perform(get("/rheinjug2/"))
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:/rheinjug2/orga/"));
  }

  @Test
  public void accessIndexPageStudentinRole() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("studentin");
    final Account account = new Account("name", "User@email.de", "image", roles,
        "givenname", "familyname");
    setupTokenMock(account);

    mvc.perform(get("/rheinjug2/"))
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:/rheinjug2/student/"));
  }

  @Test
  public void accessIndexPageStudentinAndOrgaRole() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("studentin");
    roles.add("orga");
    final Account account = new Account("name", "User@email.de", "image", roles,
        "givenname", "familyname");
    setupTokenMock(account);

    mvc.perform(get("/rheinjug2/"))
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:/rheinjug2/orga/"));
  }

  @Test
  public void accessIndexPageOtherRole() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("actuator");
    final Account account = new Account("name", "User@email.de", "image", roles,
        "givenname", "familyname");
    setupTokenMock(account);

    mvc.perform(get("/rheinjug2/"))
        .andExpect(status().isOk())
        .andExpect(view().name("index"));
  }

  @Test
  public void accessNoMappingAndGetRedirected() throws Exception {
    mvc.perform(get("/rheinjug2"))
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:/rheinjug2/"));
  }

  @Test
  public void accessLogoutAndGetRedirected() throws Exception {
    mvc.perform(get("/rheinjug2/logout"))
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:/rheinjug2/"));
  }
}
