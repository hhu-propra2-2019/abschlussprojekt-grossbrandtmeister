package mops.rheinjug2.security.csrf;

import static mops.rheinjug2.KeycloakTokenMock.setupMockUserWithRole;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
public class CsrfEnabledIntegrationTest {

  @Autowired
  private transient MockMvc mvc;

  @Autowired
  WebApplicationContext context;

  @BeforeEach
  public void setup() {
    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  @Test
  public void postOrgaEventsWithoutCsrf() throws Exception {
    setupMockUserWithRole("orga");

    mvc.perform(post("/rheinjug2/orga/events"))
        .andExpect(status().isForbidden());
  }

  @Test
  public void postOrgaEventsWithCsrf() throws Exception {
    setupMockUserWithRole("orga");

    mvc.perform(post("/rheinjug2/orga/events")
        .with(csrf()))
        .andExpect(status().isFound());
  }

}
