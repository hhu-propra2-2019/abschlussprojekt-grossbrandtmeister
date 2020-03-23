package mops.rheinjug2.controllers;

import static mops.rheinjug2.KeycloakTokenMock.setupTokenMock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


import io.micrometer.core.instrument.MeterRegistry;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import mops.rheinjug2.Account;
import mops.rheinjug2.fileupload.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@AutoConfigureMockMvc
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

  @DisplayName("Test token Name")
  @Test
  void testUploadMulitpartfileWithTokenName() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("studentin");
    final String studentenname = "fancynameyeah";
    final Account account = new Account(studentenname, "User@email.de", "image", roles,
        "givenname", "familyname");
    setupTokenMock(account);

    final MockMultipartFile testFile = new MockMultipartFile("file",
        "file.md", "text/plain", "testdata".getBytes());

    doNothing().when(fileService).uploadContentConvertToMd(anyString(), anyString());

    final String eventId = "123";
    final String filename = studentenname + "_" + eventId;
    mvc.perform(MockMvcRequestBuilders.multipart("/rheinjug2/student/reportsubmit")
        .file(testFile).contentType(MediaType.MULTIPART_FORM_DATA)
        .with(csrf()).param("eventId", eventId))
        .andExpect(status().isFound())
        .andExpect(MockMvcResultMatchers.flash().attribute("message",
            "You successfully uploaded " + filename + '!'));
  }

  @DisplayName("Multiplipartfile Upload works with correct crsf token")
  @Test
  void testUploadMulitpartfile() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("studentin");
    final Account account = new Account("name", "User@email.de", "image", roles,
        "givenname", "familyname");
    setupTokenMock(account);

    final MockMultipartFile testFile = new MockMultipartFile("file",
        "file.md", "text/plain", "testdata".getBytes());

    doNothing().when(fileService).uploadContentConvertToMd(anyString(), anyString());

    mvc.perform(MockMvcRequestBuilders.multipart("/rheinjug2/student/reportsubmit")
        .file(testFile).contentType(MediaType.MULTIPART_FORM_DATA)
        .with(csrf()))
        .andExpect(status().isFound());
  }

  @DisplayName("Test if summarysubmit in report_submit.html works")
  @Test
  void testSummarysubmitWithoutIdgetMessageTorReturnToVisitevents() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("studentin");
    final Account account = new Account("name", "User@email.de", "image", roles,
        "givenname", "familyname");
    setupTokenMock(account);

    mvc.perform(MockMvcRequestBuilders.multipart("/rheinjug2/student/summarysubmit")
        .with(csrf()))
        .andExpect(status().isFound())
        .andExpect(MockMvcResultMatchers.flash().attribute("message",
            "You did not choose an event.Go to your personal event side "
                + "and choose which event you want to give your summary"));
  }

  @DisplayName("Test if summarysubmit in report_submit.html works")
  @Test
  void testSummarysubmitWithEventIdIncluded() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("studentin");
    final Account account = new Account("name", "User@email.de", "image", roles,
        "givenname", "familyname");
    setupTokenMock(account);

    mvc.perform(MockMvcRequestBuilders.multipart("/rheinjug2/student/summarysubmit")
        .with(csrf()).param("eventId", "123"))
        .andExpect(status().isFound())
        .andExpect(MockMvcResultMatchers.flash().attribute("message",
            "You successfully uploaded the form !"));
  }

  @DisplayName("Redirect to view works, still need to check attributes!")
  @Test
  void testUploadTestExpectedViewReportsubmitWithoutEventId() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("studentin");
    final Account account = new Account("name", "User@email.de", "image", roles,
        "givenname", "familyname");
    setupTokenMock(account);

    final MockMultipartFile testFile = new MockMultipartFile("file",
        "file.pdf", "text/plain", "testdata".getBytes());

    doNothing().when(fileService).uploadContentConvertToMd(anyString(), anyString());

    mvc.perform(MockMvcRequestBuilders.multipart("/rheinjug2/student/reportsubmit")
        .file(testFile).contentType(MediaType.MULTIPART_FORM_DATA)
        .with(csrf()))
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:/rheinjug2/student/reportsubmit"));
  }

  @DisplayName("Redirect to view report_submit with correct eventId passed")
  @Test
  void testUploadTestRedirectWithEventId() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("studentin");
    final Account account = new Account("name", "User@email.de", "image", roles,
        "givenname", "familyname");
    setupTokenMock(account);

    final MockMultipartFile testFile = new MockMultipartFile("file",
        "file.md", "text/plain", "testdata".getBytes());

    doNothing().when(fileService).uploadContentConvertToMd(anyString(), anyString());

    final String eventId = "123";
    mvc.perform(MockMvcRequestBuilders.multipart("/rheinjug2/student/reportsubmit")
        .file(testFile).contentType(MediaType.MULTIPART_FORM_DATA)
        .with(csrf()).param("eventId", eventId))
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:/rheinjug2/student/reportsubmit?eventId=" + eventId));
  }

  @DisplayName("Content of Downloaded Presentfile is Correct")
  @Test
  void downloadPresentationFileAndCheckContent() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("studentin");
    final Account account = new Account("name", "User@email.de", "image", roles,
        "givenname", "familyname");
    setupTokenMock(account);

    final String content = "Versuche diesen Text downzuloaden";
    final InputStream inputStream = new ByteArrayInputStream(content.getBytes(
        Charset.forName("UTF-8")));

    when(fileService.getFileInputStream(anyString())).thenReturn(inputStream);

    final MvcResult result = mvc.perform(get("/rheinjug2/download/presentation"))
        .andExpect(status().isOk()).andReturn();
    final String resultcontent = result.getResponse().getContentAsString();

    assertEquals(content, resultcontent);

    inputStream.close();
  }

  @Test
  @DisplayName("Name of Downloaded Presentfile is Correct")
  void downloadPresentationFileAndCheckIfNameISCorrect() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("studentin");
    final Account account = new Account("name", "User@email.de", "image", roles, "givenname", "familyname");
    setupTokenMock(account);

    final String nameOfFile = "VorlageZusammenfassung.md";
    final InputStream inputStream = new ByteArrayInputStream("content".getBytes(
        Charset.forName("UTF-8")));

    when(fileService.getFileInputStream(nameOfFile)).thenReturn(inputStream);

    final MvcResult result = mvc.perform(get("/rheinjug2/download/presentation"))
        .andExpect(status().isOk()).andReturn();
    final String resultcontent = result.getResponse().getHeader("Content-disposition");

    assertEquals("attachment;filename=" + nameOfFile, resultcontent);

    inputStream.close();
  }

  @DisplayName("Able to download Presentation File")
  @Test
  void downloadPresentationFileStatusIsOkay() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("studentin");
    final Account account = new Account("name", "User@email.de", "image", roles,
        "givenname", "familyname");
    setupTokenMock(account);

    final InputStream testInputStream = new ByteArrayInputStream("testcontent".getBytes(
        Charset.forName("UTF-8")));
    when(fileService.getFileInputStream(anyString())).thenReturn(testInputStream);

    mvc.perform(get("/rheinjug2/download/presentation"))
        .andExpect(status().isOk());
    testInputStream.close();
  }

  @DisplayName("Just with right token, you have access to Uploading Files in Summarysubmit")
  @Test
  void testFileUploadSummarySubmitWithValidCsrf() throws Exception {
    mvc.perform(post("/rheinjug2/student/summarysubmit")
        .with(csrf()))
        .andExpect(status().isFound());
  }

  @DisplayName("Wrong token, no access to Uploading Files in Summarysubmit")
  @Test
  void testFileUploadSummarySubmitWithInvalidCsrf() throws Exception {
    mvc.perform(post("/rheinjug2/student/summarysubmit")
        .with(csrf().useInvalidToken()))
        .andExpect(status().isForbidden());
  }

  @DisplayName("Wrong token, no access to Uploading Files in Reportsubmit")
  @Test
  void testFileUploadReportSubmitWithInvalidCsrf() throws Exception {
    mvc.perform(post("/rheinjug2/student/reportsubmit")
        .with(csrf().useInvalidToken()))
        .andExpect(status().isForbidden());
  }

  @DisplayName("Multiplipartfile Upload works with correct crsf token")
  @Test
  void testUploadMulitpartfileWithInvalidCrf() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add("studentin");
    final Account account = new Account("name", "User@email.de", "image", roles,
        "givenname", "familyname");
    setupTokenMock(account);

    final MockMultipartFile testFile = new MockMultipartFile("file",
        "file.md", "text/plain", "testdata".getBytes());

    doNothing().when(fileService).uploadContentConvertToMd(anyString(), anyString());

    mvc.perform(MockMvcRequestBuilders.multipart("/rheinjug2/student/reportsubmit")
        .file(testFile).contentType(MediaType.MULTIPART_FORM_DATA)
        .with(csrf().useInvalidToken()))
        .andExpect(status().isForbidden());
  }
}