package mops.rheinjug2.security.keycloak;

import static mops.rheinjug2.KeycloakTokenMock.setupMockEmptyToken;
import static mops.rheinjug2.KeycloakTokenMock.setupMockUserWithRole;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureMockMvc
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class SecurityConfigTests {

  @Autowired
  private transient MockMvc mvc;

  @Autowired
  private transient WebApplicationContext context;

  @BeforeEach
  void setUp() {
    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  /**
   * Testet den Zugriff auf die verschiedenen Mappings unseres Programms mit verschiedenen Rollen.
   * Prüft jedes Mapping mit korrekter und falscher Rolle, sowie ohne Login.
   * CsvSource Format: "role, mapping, status"
   *
   * @param role    Zu prüfende Rolle
   * @param mapping Zu prüfende Seite
   * @param status  Erwarteter Status Code
   */
  @ParameterizedTest
  @CsvSource({
      ", /rheinjug2, 302",
      ", /rheinjug2/, 200",

      "monitoring, /actuator, 200",
      "studentin, /rheinjug2/student/events, 200",
      "studentin, /rheinjug2/student/visitedevents, 200",
      "studentin, /rheinjug2/student/creditpoints, 200",
      "studentin, /rheinjug2/student/reportsubmit, 302",
      "studentin, /rheinjug2/student/reportsubmit?eventId=1, 200",
      "orga, /rheinjug2/orga/events, 200",
      "orga, /rheinjug2/orga/delayedSubmission, 200",
      "orga, /rheinjug2/orga/reports, 200",

      ", /actuator, 302",
      ", /rheinjug2/student/events, 302",
      ", /rheinjug2/student/visitedevents, 302",
      ", /rheinjug2/student/creditpoints, 302",
      ", /rheinjug2/student/reportsubmit, 302",
      ", /rheinjug2/orga/events, 302",
      ", /rheinjug2/orga/delayedSubmission, 302",
      ", /rheinjug2/orga/reports, 302",

      "invalid, /actuator, 403",
      "invalid, /rheinjug2/student/events, 403",
      "invalid, /rheinjug2/student/visitedevents, 403",
      "invalid, /rheinjug2/student/creditpoints, 403",
      "invalid, /rheinjug2/student/reportsubmit, 403",
      "invalid, /rheinjug2/orga/events, 403",
      "invalid, /rheinjug2/orga/delayedSubmission, 403",
      "invalid, /rheinjug2/orga/reports, 403"
  })
  public void userRolesProvideCorrectAccess(
      final String role, final String mapping, final int status) throws Exception {
    if (role != null) {
      setupMockUserWithRole(role);
    }
    mvc.perform(get(mapping))
        .andExpect(status().is(status));
  }

  @Test
  @Disabled("man kann den token nicht zwischen den Aufrufen wechseln")
  public void differentUsersHaveIndependentAccessRights() throws Exception {
    setupMockUserWithRole("monitoring");
    mvc.perform(get("/actuator"))
        .andExpect(status().isOk());

    setupMockEmptyToken();
    mvc.perform(get("/actuator"))
        .andExpect(status().isFound());

    setupMockUserWithRole("invalid");
    mvc.perform(get("/actuator"))
        .andExpect(status().isForbidden());
  }

}
