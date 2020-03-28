package mops.rheinjug2.services;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.NoResponseException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.entities.EventRef;
import mops.rheinjug2.entities.Student;
import mops.rheinjug2.fileupload.FileService;
import mops.rheinjug2.orgamodels.DelayedSubmission;
import mops.rheinjug2.orgamodels.OrgaEvent;
import mops.rheinjug2.orgamodels.OrgaSummary;
import mops.rheinjug2.repositories.EventRepository;
import mops.rheinjug2.repositories.StudentRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.xmlpull.v1.XmlPullParserException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@DisplayName("Testing OrgaService")
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class OrgaServiceTests {

  @Mock
  private transient EventRepository eventRepository;
  @Mock
  private transient StudentRepository studentRepository;
  @Mock
  private transient FileService fileService;

  private transient OrgaService orgaService;
  private transient Event event1;
  private transient Event event2;
  private transient Student student1;
  private transient Student student2;
  private transient Student student3;

  /**
   * Data für Testing Initialsieren.
   */
  @BeforeAll
  public void setUp() {
    event1 = new Event();
    event1.setId((long) 1);
    event1.setDate(LocalDateTime.now().minusDays(4));
    event1.setTitle("Event1");
    event1.setDeadline(LocalDateTime.now().plusDays(3));

    event2 = new Event();
    event2.setId((long) 2);
    event2.setDate(LocalDateTime.now().minusDays(3));
    event2.setTitle("Event2");
    event2.setDeadline(LocalDateTime.now().plusDays(3));


    student1 = new Student("student1login", "student1@email");
    student1.setName("student1");
    student1.setId((long) 1);

    student2 = new Student("student2login", "student2@email");
    student2.setName("student2");

    student3 = new Student("student3login", "student3@email");
    student3.setName("student3");
  }

  /**
   * Events Aufruf Test.
   */
  @Test
  public void eventsLoadingTest() {
    when(eventRepository.findAll()).thenReturn(List.of(event1, event2));

    orgaService = new OrgaService(eventRepository, studentRepository, fileService);
    final List<OrgaEvent> orgaEvents = orgaService.getEvents();

    Assertions.assertThat(orgaEvents).hasSize(2);
    verify(studentRepository, times(1)).findAll();
    verify(eventRepository, times(1)).findAll();
  }

  /**
   * Hier wird getestet, ob die OrgaEvents Objekte richtig erstellt worde
   * und ob diese die richtige Informationen enthalten.
   */
  @Test
  public void creatingOrgaEventObjectTest() {
    when(eventRepository.findAll()).thenReturn(List.of(event1));
    when(studentRepository.findAll()).thenReturn(List.of(student1, student2));
    //Student1->Event1 submitted
    final EventRef student1ref = mock(EventRef.class);
    when(student1ref.getEvent()).thenReturn((long) 1);
    when(student1ref.isSubmittedSummary()).thenReturn(true);
    student1.setEvents(Set.of(student1ref));
    //Student2->Event1 not submitted
    final EventRef student2ref = mock(EventRef.class);
    when(student2ref.getEvent()).thenReturn((long) 1);
    student2.setEvents(Set.of(student2ref));


    orgaService = new OrgaService(eventRepository, studentRepository, fileService);
    final List<OrgaEvent> orgaEvents = orgaService.getEvents();

    Assertions.assertThat(orgaEvents.get(0).getId()).isEqualTo(event1.getId());
    Assertions.assertThat(orgaEvents.get(0).getDate().withNano(0)
        .equals(event1.getDate().withNano(0))).isTrue();
    Assertions.assertThat(orgaEvents.get(0).getTitle()).isEqualTo(event1.getTitle());
    Assertions.assertThat(orgaEvents.get(0).getNumberOfStudent()).isEqualTo(2);
    Assertions.assertThat(orgaEvents.get(0).getNumberOfSubmition()).isEqualTo(1);
  }


  /**
   * Hier wird Der Process der Abholung von noch nicht bewertete Zusammenfassungen
   * gestestet. Dafür wird das Abgeben drei Studenten verschiedene Zusammenfaungen
   * wie folgt simuliert;
   * 1->1 , 2->1 , 3->2
   */
  @Test
  public void getSummariesTest() {
    when(studentRepository.findAll()).thenReturn(List.of(student1, student2, student3));
    when(eventRepository.findById((long) 1)).thenReturn(java.util.Optional.ofNullable(event1));
    when(eventRepository.findById((long) 2)).thenReturn(java.util.Optional.ofNullable(event2));

    //Student1->Event1 submitted
    final EventRef student1ref = mock(EventRef.class);
    when(student1ref.getEvent()).thenReturn((long) 1);
    when(student1ref.isSubmittedAndNotAccepted()).thenReturn(true);
    when(student1ref.getTimeOfSubmission()).thenReturn(LocalDateTime.now());
    student1.setEvents(Set.of(student1ref));
    //Student2->Event1 submitted
    final EventRef student2ref = mock(EventRef.class);
    when(student2ref.getEvent()).thenReturn((long) 1);
    when(student2ref.isSubmittedAndNotAccepted()).thenReturn(true);
    when(student2ref.getTimeOfSubmission()).thenReturn(LocalDateTime.now());
    student2.setEvents(Set.of(student2ref));

    //Student3->Event2  submitted
    final EventRef student3ref = mock(EventRef.class);
    when(student3ref.getEvent()).thenReturn((long) 2);
    when(student3ref.isSubmittedAndNotAccepted()).thenReturn(true);
    when(student3ref.getTimeOfSubmission()).thenReturn(LocalDateTime.now());
    student3.setEvents(Set.of(student3ref));

    orgaService = new OrgaService(eventRepository, studentRepository, fileService);
    final List<OrgaSummary> orgaSummaries = orgaService.getSummaries();

    verify(studentRepository, times(1)).findAll();

    Assertions.assertThat(orgaSummaries).hasSize(3);
    //1->1
    Assertions.assertThat(orgaSummaries.get(2).getStudentName()).isEqualTo(student1.getName());
    Assertions.assertThat(orgaSummaries.get(2).getStudentEmail()).isEqualTo(student1.getEmail());
    Assertions.assertThat(orgaSummaries.get(2).getStudentId()).isEqualTo(student1.getId());
    Assertions.assertThat(orgaSummaries.get(2).getEventTitle()).isEqualTo(event1.getTitle());
    Assertions.assertThat(orgaSummaries.get(2).getEventId()).isEqualTo(event1.getId());
    //2->1
    Assertions.assertThat(orgaSummaries.get(1).getStudentId()).isEqualTo(student2.getId());
    Assertions.assertThat(orgaSummaries.get(1).getEventId()).isEqualTo(event1.getId());
    //3->2
    Assertions.assertThat(orgaSummaries.get(0).getStudentId()).isEqualTo(student3.getId());
    Assertions.assertThat(orgaSummaries.get(0).getEventId()).isEqualTo(event2.getId());
  }

  /**
   * Hier wird getestet, ob fileservice mit dem richtigen FileName auf gerufenwird.
   *
   * @throws IOException                .
   * @throws InvalidKeyException        .
   * @throws NoSuchAlgorithmException   .
   * @throws XmlPullParserException     .
   * @throws InvalidArgumentException   .
   * @throws InvalidResponseException   .
   * @throws InternalException          .
   * @throws NoResponseException        .
   * @throws InvalidBucketNameException .
   * @throws InsufficientDataException  .
   * @throws ErrorResponseException     .
   */
  @Test
  public void callTheRightSummaryContentNameFromFileServiceTest() throws
      IOException, InvalidKeyException,
      NoSuchAlgorithmException, XmlPullParserException, InvalidArgumentException,
      InvalidResponseException, InternalException, NoResponseException, InvalidBucketNameException,
      InsufficientDataException, ErrorResponseException {
    when(studentRepository.findAll()).thenReturn(List.of(student1));
    when(eventRepository.findById((long) 1)).thenReturn(java.util.Optional.ofNullable(event1));

    //Student1->Event1 submitted
    final EventRef student1ref = mock(EventRef.class);
    when(student1ref.getEvent()).thenReturn((long) 1);
    when(student1ref.isSubmittedAndNotAccepted()).thenReturn(true);
    student1.setEvents(Set.of(student1ref));

    orgaService = new OrgaService(eventRepository, studentRepository, fileService);
    final List<OrgaSummary> orgaSummaries = orgaService.getSummaries();

    Assertions.assertThat(orgaSummaries).hasSize(1);
    verify(fileService, times(1))
        .getContentOfFileAsString(student1.getLogin() + "_" + event1.getId());
  }

  @Test
  public void getDelayedSubmissionTset() {
    //Student1->Event1 submitted
    final EventRef student1ref = mock(EventRef.class);
    when(student1ref.getEvent()).thenReturn((long) 1);
    when(student1ref.isDelayed()).thenReturn(true);
    when(student1ref.getDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
    student1.setEvents(Set.of(student1ref));
    when(studentRepository.findAll()).thenReturn(List.of(student1));
    when(eventRepository.findById((long) 1)).thenReturn(java.util.Optional.ofNullable(event1));


    orgaService = new OrgaService(eventRepository, studentRepository, fileService);
    final List<DelayedSubmission> delayedSubmissions = orgaService.getDelayedSubmission();

    verify(studentRepository, times(1)).findAll();
    Assertions.assertThat(delayedSubmissions).hasSize(1);
    //1->1
    Assertions.assertThat(delayedSubmissions.get(0).getStudentName())
        .isEqualTo(student1.getLogin());
    Assertions.assertThat(delayedSubmissions.get(0).getStudentId()).isEqualTo(student1.getId());
    Assertions.assertThat(delayedSubmissions.get(0).getEventId()).isEqualTo(1);
    Assertions.assertThat(delayedSubmissions.get(0).getEventTitle()).isEqualTo(event1.getTitle());
    Assertions.assertThat(delayedSubmissions.get(0).getDeadLine().withNano(0)
        .equals(student1ref.getDeadline().withNano(0))).isTrue();
    Assertions.assertThat(delayedSubmissions.get(0).getSummaryContent()).isEqualTo(null);

    final List<DelayedSubmission> delayedSubmissionsForStudent1 =
        orgaService.getDelayedSubmissionsForStudent("student1login");
    Assertions.assertThat(delayedSubmissionsForStudent1).hasSize(1);

    final List<DelayedSubmission> delayedSubmissionsForEvent1 =
        orgaService.getDelayedSubmissionsForEvent("Event1");
    Assertions.assertThat(delayedSubmissionsForStudent1).hasSize(1);

  }

  @Test
  public void setSummaryAsAcceptedTest() {
    when(studentRepository.findById((long) 1)).thenReturn(java.util.Optional.ofNullable(student1));
    when(eventRepository.findById((long) 1)).thenReturn(java.util.Optional.ofNullable(event1));
    //1->1
    final EventRef student1ref = mock(EventRef.class);
    when(student1ref.getEvent()).thenReturn((long) 1);
    student1.setEvents(Set.of(student1ref));

    orgaService = new OrgaService(eventRepository, studentRepository, fileService);
    final boolean result = orgaService.setSummaryAsAccepted((long) 1, (long) 1);

    verify(studentRepository, times(1)).findById((long) 1);
    verify(eventRepository, times(1)).findById((long) 1);
    verify(student1ref, times(1)).setAccepted(true);
    Assertions.assertThat(result).isTrue();
  }

  @Test
  public void summaryuploadTset() throws IOException {
    when(studentRepository.findById((long) 1)).thenReturn(java.util.Optional.ofNullable(student1));
    when(eventRepository.findById((long) 1)).thenReturn(java.util.Optional.ofNullable(event1));

    //1->1
    final EventRef student1ref = mock(EventRef.class);
    when(student1ref.getEvent()).thenReturn((long) 1);
    student1.setEvents(Set.of(student1ref));

    orgaService = new OrgaService(eventRepository, studentRepository, fileService);
    orgaService.summaryuploadStringContent((long) 1, (long) 1, "student1",
        "summaryContent");

    verify(fileService, times(1))
        .uploadContentConvertToMd("summaryContent", "student1_1");
    verify(student1ref, times(1)).setSubmittedSummary(true);
    verify(student1ref, times(1)).setAccepted(true);
    verify(studentRepository, times(1)).save(student1);

  }

  @Test
  public void callTheRightSummaryContentNameFromFileServiceWithFileTest() throws
      IOException {
    when(studentRepository.findById((long) 1)).thenReturn(Optional.ofNullable(student1));
    when(eventRepository.findById((long) 1)).thenReturn(java.util.Optional.ofNullable(event1));

    //Student1->Event1 submitted
    final EventRef student1ref = mock(EventRef.class);
    when(student1ref.getEvent()).thenReturn((long) 1);
    student1.setEvents(Set.of(student1ref));

    orgaService = new OrgaService(eventRepository, studentRepository, fileService);
    final MultipartFile file = new MockMultipartFile("file",
        "file.md", "text/plain",
        "testdata".getBytes(StandardCharsets.UTF_8));
    final boolean bol =
        orgaService.summaryuploadFileContent((long) 1, (long) 1, "student1login", file);

    Assertions.assertThat(bol).isTrue();
    verify(fileService, times(1))
        .uploadFile(file, student1.getLogin() + "_" + event1.getId());
    verify(student1ref, times(1)).setSubmittedSummary(true);
    verify(student1ref, times(1)).setAccepted(true);
    verify(studentRepository, times(1)).save(student1);
  }


}
