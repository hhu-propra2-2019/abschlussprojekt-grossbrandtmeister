package mops.rheinjug2.security.keycloak;

import static com.tngtech.keycloakmock.api.TokenConfig.aTokenConfig;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tngtech.keycloakmock.junit5.KeycloakMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureMockMvc
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class SecurityConfigTests {

  @Autowired
  private transient MockMvc mockMvc;

  private static final int keycloakMockPort = 8000;
  private static final String keycloakMockUrl = "http://localhost:" + keycloakMockPort + "/auth";
  @RegisterExtension
  static KeycloakMock keycloakMock = new KeycloakMock(keycloakMockPort, "MOPS");
  @Autowired
  private transient KeycloakSpringBootProperties keycloakSpringBootProperties;

  @BeforeEach
  protected void setKeycloakConfig() {
    keycloakSpringBootProperties
        .setAuthServerUrl(keycloakMockUrl);
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
      "studentin, /rheinjug2/student/reportsubmit/123, 404",
      "orga, /rheinjug2/orga/events, 200",
      "orga, /rheinjug2/orga/reports, 200",

      ", /actuator, 302",
      ", /rheinjug2/student/events, 302",
      ", /rheinjug2/student/visitedevents, 302",
      ", /rheinjug2/student/creditpoints, 302",
      ", /rheinjug2/student/reportsubmit/123, 302",
      ", /rheinjug2/orga/events, 302",
      ", /rheinjug2/orga/reports, 302",

      "invalid, /actuator, 403",
      "invalid, /rheinjug2/student/events, 403",
      "invalid, /rheinjug2/student/visitedevents, 403",
      "invalid, /rheinjug2/student/creditpoints, 403",
      "invalid, /rheinjug2/student/reportsubmit/123, 403",
      "invalid, /rheinjug2/orga/events, 403",
      "invalid, /rheinjug2/orga/reports, 403"
  })
  public void userRolesProvideCorrectAccess(
      final String role, final String mapping, final int status) throws Exception {

    if (role == null) {
      mockMvc.perform(get(mapping))
          .andExpect(status().is(status));
    } else {
      mockMvc.perform(get(mapping)
          .header("Authorization",
              "Bearer " + getAccessTokenWithRole(role)))
          .andExpect(status().is(status));
    }
  }

  @Test
  public void differentUsersHaveIndependentAccessRights() throws Exception {
    mockMvc.perform(get("/actuator")
        .header("Authorization",
            "Bearer " + getAccessTokenWithRole("monitoring")))
        .andExpect(status().isOk());
    mockMvc.perform(get("/actuator"))
        .andExpect(status().isFound());
    mockMvc.perform(get("/actuator")
        .header("Authorization",
            "Bearer " + getAccessTokenWithRole("invalid")))
        .andExpect(status().isForbidden());
  }

  /**
   * Erzeugt einen Keycloak Access Token mit vorgegebener Rolle.
   *
   * @param role Rollenname ohne Prefix (i.e. "orga", nicht "ROLE_orga").
   * @return Gibt den Access Token zurück.
   */
  protected static String getAccessTokenWithRole(final String role) {
    return keycloakMock
        .getAccessToken(aTokenConfig()
            .withRealmRole("ROLE_" + role)
            .withEmail(role + "@non.existent")
            .build());
  }

}
