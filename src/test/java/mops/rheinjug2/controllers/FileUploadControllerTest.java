package mops.rheinjug2.controllers;

import static mops.rheinjug2.KeycloakTokenMock.setupTokenMock;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import io.micrometer.core.instrument.MeterRegistry;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import mops.rheinjug2.Account;
import mops.rheinjug2.fileupload.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest
class FileUploadControllerTest {
  @Autowired
  private MockMvc mvc;

  @MockBean
  private FileService fileService;

  @Autowired
  private WebApplicationContext context;

  @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
  MeterRegistry registry;

  @BeforeEach
  public void setup() {
    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  @Test
  public void testController() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("studentin");
    final Account account = new Account("name", "User@email.de", "image", roles, "givenname", "familyname");
    setupTokenMock(account);

  }

  @Test
  void testUploadMulitpartfile() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("studentin");
    final Account account = new Account("name", "User@email.de", "image", roles, "givenname", "familyname");
    setupTokenMock(account);

    final MockMultipartFile testFile = new MockMultipartFile("file",
        "file.md", "text/plain", "testdata".getBytes());

    doNothing().when(fileService).uploadContentConvertToMd(anyString(), anyString());

    mvc.perform(MockMvcRequestBuilders.multipart("/rheinjug2/student/reportsubmit")
        .file(testFile).contentType(MediaType.MULTIPART_FORM_DATA)
        .with(csrf()))
        .andExpect(status().isFound());

    verify(fileService).uploadContentConvertToMd(anyString(), anyString());
  }


  @Test
  void downloadFilebyToken() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("studentin");
    final Account account = new Account("name", "User@email.de", "image", roles, "givenname", "familyname");
    setupTokenMock(account);

    final String content = "Verusche diesen Text downzuloaden";
    final InputStream inputStream = new ByteArrayInputStream(content.getBytes(
        Charset.forName("UTF-8")));
    when(fileService.getFileInputStream(anyString())).thenReturn(inputStream);

    mvc.perform(get("/download/presentation"))
        .andExpect(status().isOk());

    inputStream.close();
  }

  @Test
  void downloadFilebyTokenAndFileMock() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("studentin");
    final Account account = new Account("name", "User@email.de", "image", roles, "givenname", "familyname");
    setupTokenMock(account);

    mvc.perform(get("/download/presentation"))
        .andExpect(status().isFound();
  }

  @Test
  void downloadPResentationforSummary() {
  }

  @Test
  void testFileUploadSummarySubmitwithvalidCsrf() throws Exception {
    mvc.perform(post("/rheinjug2/student/summarysubmit")
        .with(csrf()))
        .andExpect(status().isFound());
  }

  @Test
  void testFileUploadSummarySubmitwithoutCsrf() throws Exception {
    mvc.perform(post("/rheinjug2/student/summarysubmit")
        .with(csrf().useInvalidToken()))
        .andExpect(status().isForbidden());
  }


  @Test
  void testFileUploadReportSubmitwithoutCsrf() throws Exception {
    mvc.perform(post("/rheinjug2/student/reportsubmit")
        .with(csrf().useInvalidToken()))
        .andExpect(status().isForbidden());
  }
}