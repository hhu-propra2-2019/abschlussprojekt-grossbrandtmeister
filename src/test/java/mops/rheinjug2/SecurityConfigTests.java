package mops.rheinjug2;

import static com.tngtech.keycloakmock.api.TokenConfig.aTokenConfig;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tngtech.keycloakmock.junit5.KeycloakMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
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

  private static final String[] studentPages =
      {"events", "visitedevents", "creditpoints", "reportsubmit"};
  private static final String[] orgaPages =
      {"events", "creditpoints", "reports"};

  @BeforeEach
  protected void setKeycloakConfig() {
    keycloakSpringBootProperties
        .setAuthServerUrl(keycloakMockUrl);
  }

  /**
   * Teste Zugang zu /actuator ohne Login.
   * Erwarte Status 302 Found (Redirection zur Login Seite).
   */
  @Test
  public void anonymousClientGetsRedirectedFromActuator() throws Exception {
    mockMvc.perform(get("/actuator"))
        .andExpect(status().isFound());
  }

  /**
   * Teste Zugang zu /actuator mit Login und korrekter Rolle.
   * Erwarte Status 200 OK.
   */
  @Test
  public void clientWithMonitoringRoleCanAccessActuator() throws Exception {
    mockMvc.perform(get("/actuator")
        .header("Authorization",
            "Bearer " + getAccessTokenWithRole("monitoring")))
        .andExpect(status().isOk());
  }

  /**
   * Teste Zugang zu /actuator mit Login und inkorrekter Rolle.
   * Erwarte Status 403 Forbidden.
   */
  @Test
  public void clientWithoutMonitoringRoleCanNotAccessActuator() throws Exception {
    mockMvc.perform(get("/actuator")
        .header("Authorization",
            "Bearer " + getAccessTokenWithRole("studentin")))
        .andExpect(status().isForbidden());
  }

  /**
   * Teste Zugang zu /student ohne Login.
   * Erwarte Status 302 Found (Redirection zur Login Seite).
   */
  @Test
  public void anonymousClientGetsRedirectedFromStudentPages() throws Exception {
    for (String page : studentPages) {
      mockMvc.perform(get("/rheinjug2/student/" + page))
          .andExpect(status().isFound());
    }
  }

  /**
   * Teste Zugang zu /student mit Login und korrekter Rolle.
   * Erwarte Status 200 OK.
   */
  @Test
  public void clientWithStudentinRoleCanAccessStudentPages() throws Exception {
    for (String page : studentPages) {
      mockMvc.perform(get("/rheinjug2/student/" + page)
          .header("Authorization",
              "Bearer " + getAccessTokenWithRole("studentin")))
          .andExpect(status().isOk());
    }
  }

  /**
   * Teste Zugang zu /student mit Login und inkorrekter Rolle.
   * Erwarte Status 403 Forbidden.
   */
  @Test
  public void clientWithoutStudentinRoleCanNotAccessStudentPages() throws Exception {
    for (String page : studentPages) {
      mockMvc.perform(get("/rheinjug2/student/" + page)
          .header("Authorization",
              "Bearer " + getAccessTokenWithRole("monitoring")))
          .andExpect(status().isForbidden());
    }
  }

  /**
   * Teste Zugang zu /orga ohne Login.
   * Erwarte Status 302 Found (Redirection zur Login Seite).
   */
  @Test
  public void anonymousClientGetsRedirectedFromOrgaPages() throws Exception {
    for (String page : orgaPages) {
      mockMvc.perform(get("/rheinjug2/orga/" + page))
          .andExpect(status().isFound());
    }
  }

  /**
   * Teste Zugang zu /orga mit Login und korrekter Rolle.
   * Erwarte Status 200 OK.
   */
  @Test
  public void clientWithOrgaRoleCanAccessOrgaPages() throws Exception {
    for (String page : orgaPages) {
      mockMvc.perform(get("/rheinjug2/orga/" + page)
          .header("Authorization",
              "Bearer " + getAccessTokenWithRole("orga")))
          .andExpect(status().isOk());
    }
  }

  /**
   * Teste Zugang zu /orga mit Login und inkorrekter Rolle.
   * Erwarte Status 403 Forbidden.
   */
  @Test
  public void clientWithoutOrgaRoleCanNotAccessOrgaPages() throws Exception {
    for (String page : orgaPages) {
      mockMvc.perform(get("/rheinjug2/orga/" + page)
          .header("Authorization",
              "Bearer " + getAccessTokenWithRole("monitoring")))
          .andExpect(status().isForbidden());
    }
  }

  /**
   * Erzeugt einen Keycloak Access Token mit vorgegebener Rolle.
   *
   * @param role Rollenname ohne Prefix (i.e. "orga", nicht "ROLE_orga").
   * @return Gibt den Access Token zurück.
   */
  protected static String getAccessTokenWithRole(String role) {
    return keycloakMock
        .getAccessToken(aTokenConfig()
            .withRealmRole("ROLE_" + role)
            .build());
  }

}