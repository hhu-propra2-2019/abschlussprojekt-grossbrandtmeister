package mops.rheinjug2;

import io.micrometer.core.instrument.Counter;
import mops.rheinjug2.controllers.StudentController;
import mops.rheinjug2.fileupload.FileService;
import mops.rheinjug2.services.ModelService;
import org.junit.Test;
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

  @Test
  public void testPersonalView() {
    //when(modelService.studentExists("hh100")).
  }
}
