package mops.rheinjug2.security.csrf;

import static com.tngtech.keycloakmock.api.TokenConfig.aTokenConfig;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
public class CsrfEnabledIntegrationTest {

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

  protected static String getOrgaAccessToken() {
    return keycloakMock
        .getAccessToken(aTokenConfig()
            .withRealmRole("ROLE_orga")
            .withEmail("orga@non.existent")
            .build());
  }

  @Test
  public void postOrgaEventsWithoutCsrf() throws Exception {
    mockMvc.perform(post("/rheinjug2/orga/events")
        .header("Authorization",
            "Bearer " + getOrgaAccessToken()))
        .andExpect(status().isForbidden());
  }

  @Test
  public void postOrgaEventsWithCsrf() throws Exception {
    mockMvc.perform(post("/rheinjug2/orga/events")
        .with(csrf())
        .header("Authorization",
            "Bearer " + getOrgaAccessToken()))
        .andExpect(status().isFound());
  }

}
