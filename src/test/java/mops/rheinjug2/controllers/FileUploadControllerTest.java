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
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import mops.rheinjug2.Account;
import mops.rheinjug2.fileupload.FileService;
import mops.rheinjug2.services.ModelService;
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
@DisplayName("Testing FileUploadController")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class FileUploadControllerTest {
  public static final String STUDENTIN = "studentin";
  public static final String GIVENNAME = "givenname";
  public static final String FAMILYNAME = "familyname";
  public static final String USER_EMAIL_DE = "User@email.de";
  public static final String IMAGE = "image";
  public static final String FILE = "file";
  public static final String EVENT_ID = "123";
  @Autowired
  private transient MockMvc mvc;

  @MockBean
  private transient FileService fileService;

  @Autowired
  private transient WebApplicationContext context;

  @MockBean
  private transient ModelService modelService;

  @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
  transient MeterRegistry registry;

  @BeforeEach
  public void setUp() {
    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  @Test
  void testUploadMulitpartfileWithTokenName() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add(STUDENTIN);
    final String studentenname = "fancynameyeah";
    final Account account = new Account(studentenname, USER_EMAIL_DE, IMAGE, roles,
        GIVENNAME, FAMILYNAME);
    setupTokenMock(account);

    final MockMultipartFile testFile = new MockMultipartFile(FILE,
        "file.md", "text/plain", "testdata".getBytes(StandardCharsets.UTF_8));

    doNothing().when(fileService).uploadContentConvertToMd(anyString(), anyString());

    final String eventId = EVENT_ID;
    final String filename = studentenname + "_" + eventId;
    mvc.perform(MockMvcRequestBuilders.multipart("/rheinjug2/student/reportsubmit")
        .file(testFile).contentType(MediaType.MULTIPART_FORM_DATA)
        .with(csrf()).param("eventId", eventId))
        .andExpect(status().isFound())
        .andExpect(MockMvcResultMatchers.flash().attribute("message",
            "You successfully uploaded " + filename + '!'));
  }

  @Test
  void testUploadMulitpartfileWithRightCrfIsFound() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add(STUDENTIN);
    final Account account = new Account("name", USER_EMAIL_DE, IMAGE, roles,
        GIVENNAME, FAMILYNAME);
    setupTokenMock(account);

    final MockMultipartFile testFile = new MockMultipartFile(FILE,
        "file.md", "text/plain", "testdata".getBytes(StandardCharsets.UTF_8));

    doNothing().when(fileService).uploadContentConvertToMd(anyString(), anyString());

    mvc.perform(MockMvcRequestBuilders.multipart("/rheinjug2/student/reportsubmit")
        .file(testFile).contentType(MediaType.MULTIPART_FORM_DATA)
        .with(csrf()))
        .andExpect(status().isFound());
  }


  @Test
  void testSummarysubmitWithoutIdgetMessageTorReturnToVisitevents() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add(STUDENTIN);
    final Account account = new Account("name", USER_EMAIL_DE, IMAGE, roles,
        GIVENNAME, FAMILYNAME);
    setupTokenMock(account);

    mvc.perform(MockMvcRequestBuilders.multipart("/rheinjug2/student/summarysubmit")
        .with(csrf()))
        .andExpect(status().isFound())
        .andExpect(MockMvcResultMatchers.flash().attribute("message",
            "You did not choose an event.Go to your personal event side "
                + "and choose for which event you want to handle your summary in"));
  }


  @Test
  void testSummarysubmitWithEventIdIncluded() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add(STUDENTIN);
    final Account account = new Account("name", USER_EMAIL_DE, IMAGE, roles,
        GIVENNAME, FAMILYNAME);
    setupTokenMock(account);

    mvc.perform(MockMvcRequestBuilders.multipart("/rheinjug2/student/summarysubmit")
        .with(csrf()).param("eventId", EVENT_ID))
        .andExpect(status().isFound())
        .andExpect(MockMvcResultMatchers.flash().attribute("message",
            "You successfully uploaded the form !"));
  }


  @Test
  void testUploadFileExpectedViewReportsubmitWithoutEventId() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add(STUDENTIN);
    final Account account = new Account("name", USER_EMAIL_DE, IMAGE, roles,
        GIVENNAME, FAMILYNAME);
    setupTokenMock(account);

    final MockMultipartFile testFile = new MockMultipartFile(FILE,
        "file.pdf", "text/plain", "testdata"
        .getBytes(StandardCharsets.UTF_8));

    doNothing().when(fileService).uploadContentConvertToMd(anyString(), anyString());

    mvc.perform(MockMvcRequestBuilders.multipart("/rheinjug2/student/reportsubmit")
        .file(testFile).contentType(MediaType.MULTIPART_FORM_DATA)
        .with(csrf()))
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:/rheinjug2/student/reportsubmit"));
  }


  @Test
  void testUploadTestRedirectWithEventId() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add(STUDENTIN);
    final Account account = new Account("name", USER_EMAIL_DE, IMAGE, roles,
        GIVENNAME, FAMILYNAME);
    setupTokenMock(account);

    final MockMultipartFile testFile = new MockMultipartFile(FILE,
        "file.md", "text/plain", "testdata"
        .getBytes(StandardCharsets.UTF_8));

    doNothing().when(fileService).uploadContentConvertToMd(anyString(), anyString());

    final String eventId = EVENT_ID;
    mvc.perform(MockMvcRequestBuilders.multipart("/rheinjug2/student/reportsubmit")
        .file(testFile).contentType(MediaType.MULTIPART_FORM_DATA)
        .with(csrf()).param("eventId", eventId))
        .andExpect(status().isFound())
        .andExpect(view().name("redirect:/rheinjug2/student/reportsubmit?eventId="
            + eventId));
  }

  @Test
  void downloadFileByEvent_IdContenIsCorrect() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add(STUDENTIN);
    final Account account = new Account("name", USER_EMAIL_DE, IMAGE, roles,
        GIVENNAME, FAMILYNAME);
    setupTokenMock(account);

    final String content = "Versuche diesen Text downzuloaden";
    final InputStream inputStream = new ByteArrayInputStream(content.getBytes(
        StandardCharsets.UTF_8));

    when(fileService.getFileInputStream(anyString())).thenReturn(inputStream);

    final String eventId = EVENT_ID;
    final MvcResult result = mvc.perform(get("/rheinjug2/download/file")
        .param("eventId", eventId))
        .andExpect(status().isOk()).andReturn();
    final String resultcontent = result.getResponse().getContentAsString();

    assertEquals(content, resultcontent);

    inputStream.close();
  }

  @Test
  void downloadFileByvent_IdFileNameIsCorrect() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add(STUDENTIN);
    final String name = "Johanna Steiner";
    final Account account = new Account(name, USER_EMAIL_DE, IMAGE, roles,
        GIVENNAME, FAMILYNAME);
    setupTokenMock(account);

    final String content = "Versuche diesen Text downzuloaden";
    final InputStream inputStream = new ByteArrayInputStream(content.getBytes(
        StandardCharsets.UTF_8));

    when(fileService.getFileInputStream(anyString())).thenReturn(inputStream);

    final String eventId = EVENT_ID;
    final MvcResult result = mvc.perform(get("/rheinjug2/download/file")
        .param("eventId", eventId))
        .andExpect(status().isOk()).andReturn();
    final String resultcontent = result.getResponse().getHeader("Content-disposition");
    final String nameOfFile = name + "_" + eventId + ".md";

    assertEquals("attachment;filename=" + nameOfFile, resultcontent);

    inputStream.close();
  }

  @Test
  void downloadPresentationFileAndCheckContent() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add(STUDENTIN);
    final Account account = new Account("name", USER_EMAIL_DE, IMAGE, roles,
        GIVENNAME, FAMILYNAME);
    setupTokenMock(account);

    final String content = "Versuche diesen Text downzuloaden";
    final InputStream inputStream = new ByteArrayInputStream(content.getBytes(
        StandardCharsets.UTF_8));

    when(fileService.getFileInputStream(anyString())).thenReturn(inputStream);

    final MvcResult result = mvc.perform(get("/rheinjug2/download/presentation"))
        .andExpect(status().isOk()).andReturn();
    final String resultcontent = result.getResponse().getContentAsString();

    assertEquals(content, resultcontent);

    inputStream.close();
  }

  @Test
  void downloadPresentationFileCheckNameCorrect() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add(STUDENTIN);
    final Account account = new Account("name", USER_EMAIL_DE, IMAGE, roles,
        GIVENNAME, FAMILYNAME);
    setupTokenMock(account);

    final String nameOfFile = "VorlageZusammenfassung.md";
    final InputStream inputStream = new ByteArrayInputStream("content".getBytes(
        StandardCharsets.UTF_8));

    when(fileService.getFileInputStream(nameOfFile)).thenReturn(inputStream);

    final MvcResult result = mvc.perform(get("/rheinjug2/download/presentation"))
        .andExpect(status().isOk()).andReturn();
    final String resultcontent = result.getResponse().getHeader("Content-disposition");

    assertEquals("attachment;filename=" + nameOfFile, resultcontent);

    inputStream.close();
  }


  @Test
  void downloadPresentationFileStatusIsOkay() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add(STUDENTIN);
    final Account account = new Account("name", USER_EMAIL_DE, IMAGE, roles,
        GIVENNAME, FAMILYNAME);
    setupTokenMock(account);

    final InputStream testInputStream = new ByteArrayInputStream("testcontent".getBytes(
        StandardCharsets.UTF_8));

    when(fileService.getFileInputStream(anyString())).thenReturn(testInputStream);

    mvc.perform(get("/rheinjug2/download/presentation"))
        .andExpect(status().isOk());

    testInputStream.close();
  }

  @Test
  void testFileUploadSummarySubmitWithValidCsrf() throws Exception {
    mvc.perform(post("/rheinjug2/student/summarysubmit")
        .with(csrf()))
        .andExpect(status().isFound());
  }

  @Test
  void testFileUploadSummarySubmitWithInvalidCsrf() throws Exception {
    mvc.perform(post("/rheinjug2/student/summarysubmit")
        .with(csrf().useInvalidToken()))
        .andExpect(status().isForbidden());
  }

  @Test
  void testFileUploadReportSubmitWithInvalidCsrf() throws Exception {
    mvc.perform(post("/rheinjug2/student/reportsubmit")
        .with(csrf().useInvalidToken()))
        .andExpect(status().isForbidden());
  }

  @Test
  void testUploadMultitpartFileWithInvalidCrf() throws Exception {
    final Set<String> roles = new HashSet<>();
    roles.add(STUDENTIN);
    final Account account = new Account("name", USER_EMAIL_DE, IMAGE, roles,
        GIVENNAME, FAMILYNAME);
    setupTokenMock(account);

    final MockMultipartFile testFile = new MockMultipartFile(FILE,
        "file.md", "text/plain",
        "testdata".getBytes(StandardCharsets.UTF_8));

    doNothing().when(fileService).uploadContentConvertToMd(anyString(), anyString());

    mvc.perform(MockMvcRequestBuilders.multipart("/rheinjug2/student/reportsubmit")
        .file(testFile).contentType(MediaType.MULTIPART_FORM_DATA)
        .with(csrf().useInvalidToken()))
        .andExpect(status().isForbidden());
  }
}