package mops.rheinjug2;

import static com.tngtech.keycloakmock.api.TokenConfig.aTokenConfig;


import com.tngtech.keycloakmock.junit5.KeycloakMock;
import io.micrometer.core.instrument.Counter;
import mops.rheinjug2.controllers.StudentController;
import mops.rheinjug2.fileupload.FileService;
import mops.rheinjug2.services.ModelService;
import org.junit.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@WebMvcTest(StudentController.class)
@RunWith(SpringRunner.class)
public class StudentControllerTests {
  @Autowired
  private MockMvc mvc;

  @MockBean
  private ModelService modelService;

  @MockBean
  private Counter authenticatedAccess;

  @MockBean
  private FileService fileService;

  private static final int keycloakMockPort = 8000;
  private static final String keycloakMockUrl = "http://localhost:" + keycloakMockPort + "/auth";

  @RegisterExtension
  static KeycloakMock keycloakMock = new KeycloakMock(keycloakMockPort, "MOPS");


  @Test
  public void testPersonalView() {
    //when(modelService.studentExists("hh100")).
  }

  @Test
  public void testStudent_events_overview() {

    String name = keycloakMock
        .getAccessToken(aTokenConfig()
            .withRealmRole("ROLE_orga")
            .withEmail("orga@non.existent")
            .build());


  }
}
