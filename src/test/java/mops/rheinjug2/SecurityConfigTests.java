package mops.rheinjug2;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureMockMvc
public class SecurityConfigTests {

  @Autowired
  private MockMvc mockMvc;

  /**
   * Test anonymous access to /actuator, expects to get redirected to Keycloak login.
   */
  @Test
  public void anAnonymousClientGetsRedirected() throws Exception {
    mockMvc.perform(get("/actuator"))
        .andExpect(status().is(302));
  }

}
