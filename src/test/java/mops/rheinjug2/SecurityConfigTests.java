package mops.rheinjug2;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureMockMvc
public class SecurityConfigTests {

  @Autowired
  private transient MockMvc mockMvc;

  protected static final String keycloakHost = "https://keycloak.cs.hhu.de";
  protected static final String clientId = "demo";
  protected static final String realm = "MOPS";

  /**
   * Teste Zugang zu /actuator ohne Login.
   * Erwarte Status 302 Redirect zur Login Seite.
   */
  @Test
  public void anonymousClientGetsRedirected() throws Exception {
    mockMvc.perform(get("/actuator"))
        .andExpect(status().is(302));
  }

  /**
   * Teste Zugang zu /actuator mit Login und korrekter Rolle.
   * Erwarte Status 200 OK.
   */
  @Test
  public void clientWithMonitoringRoleCanAccessActuator() throws Exception {
    mockMvc.perform(get("/actuator")
        .header("Authorization", "Bearer " + getAccessToken("actuator", "actuator")))
        .andExpect(status().isOk());
  }

  /**
   * Teste Zugang zu /actuator mit Login und inkorrekter Rolle.
   * Erwarte Status 403 Forbidden.
   */
  @Test
  public void clientWithoutMonitoringRoleCanNotAccessActuator() throws Exception {
    mockMvc.perform(get("/actuator")
        .header("Authorization", "Bearer " + getAccessToken("studentin1", "studentin1")))
        .andExpect(status().isForbidden());
  }

  /**
   * Ruft einen Access Token von Keycloak für den angegeben Nutzer ab.
   *
   * @param username Nutzername für Keycloak
   * @param password Passwort für Keycloak
   * @return Gibt den Access Token zurück
   */
  protected static String getAccessToken(String username, String password) {

    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    var map = new LinkedMultiValueMap<>();
    map.add("grant_type", "password");
    map.add("client_id", clientId);
    map.add("username", username);
    map.add("password", password);

    var restTemplate = new RestTemplate();
    var token = restTemplate.postForObject(
        keycloakHost + "/auth/realms/" + realm + "/protocol/openid-connect/token",
        new HttpEntity<>(map, headers), KeyCloakToken.class);

    assert token != null;
    return token.getAccessToken();
  }

  private static class KeyCloakToken {

    private final String accessToken;

    @JsonCreator
    KeyCloakToken(@JsonProperty("access_token") String accessToken) {
      this.accessToken = accessToken;
    }

    public String getAccessToken() {
      return accessToken;
    }
  }

}
