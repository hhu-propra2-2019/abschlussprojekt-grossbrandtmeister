package mops.rheinjug2.services;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.entities.EventRef;
import mops.rheinjug2.entities.Student;
import mops.rheinjug2.fileupload.FileService;
import mops.rheinjug2.orgamodels.OrgaEvent;
import mops.rheinjug2.orgamodels.OrgaSummary;
import mops.rheinjug2.orgamodels.SummariesIDs;
import mops.rheinjug2.repositories.EventRepository;
import mops.rheinjug2.repositories.StudentRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
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
    event1.setDate(LocalDateTime.now().minusDays(3));
    event1.setTitle("Event1");

    event2 = new Event();
    event2.setId((long) 2);
    event2.setDate(LocalDateTime.now().minusDays(3));
    event1.setTitle("Event2");

    student1 = new Student("student1login", "student1@email");
    student1.setName("student1");

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
    verify(eventRepository, times(1)).countStudentsPerEventById(event1.getId());
    verify(eventRepository, times(1)).countStudentsPerEventById(event2.getId());
    verify(eventRepository, times(1)).countSubmittedSummaryPerEventById(event1.getId());
  }

  /**
   * Hier wird getestet, ob die OrgaEvents Objekte richtig erstellt worde
   * und ob diese die richtige Informationen enthalten.
   */
  @Test
  public void creatingOrgaEventObjectTest() {
    when(eventRepository.findAll()).thenReturn(List.of(event1));
    when(eventRepository.countStudentsPerEventById((long) 1)).thenReturn(10);
    when(eventRepository.countSubmittedSummaryPerEventById((long) 1)).thenReturn(5);

    orgaService = new OrgaService(eventRepository, studentRepository, fileService);
    final List<OrgaEvent> orgaEvents = orgaService.getEvents();

    Assertions.assertThat(orgaEvents.get(0).getId()).isEqualTo(event1.getId());
    Assertions.assertThat(orgaEvents.get(0).getDate().withNano(0)
        .equals(event1.getDate().withNano(0))).isTrue();
    Assertions.assertThat(orgaEvents.get(0).getTitle()).isEqualTo(event1.getTitle());
    Assertions.assertThat(orgaEvents.get(0).getNumberOfStudent()).isEqualTo(10);
    Assertions.assertThat(orgaEvents.get(0).getNumberOfSubmition()).isEqualTo(5);
  }


  /**
   * Hier wird Der Process der Abholung von noch nicht bewertete Zusammenfassungen
   * gestestet. Dafür wird das Abgeben drei Studenten verschiedene Zusammenfaungen
   * wie folgt simuliert;
   * 1->1 , 2->1 , 3->2
   */
  @Test
  public void getSummariesTest() {
    final SummariesIDs student1_event1 = new SummariesIDs((long) 1, (long) 1);
    final SummariesIDs student2_event1 = new SummariesIDs((long) 2, (long) 1);
    final SummariesIDs student3_event2 = new SummariesIDs((long) 3, (long) 2);
    when(eventRepository.getSubmittedAndUnacceptedSummaries()).thenReturn(List.of(
        student1_event1, student2_event1, student3_event2));
    when(studentRepository.getStudentById(1)).thenReturn(student1);
    when(studentRepository.getStudentById(2)).thenReturn(student2);
    when(studentRepository.getStudentById(3)).thenReturn(student3);
    when(eventRepository.getEventById((long) 1)).thenReturn(event1);
    when(eventRepository.getEventById((long) 2)).thenReturn(event2);
    final EventRef ref = mock(EventRef.class);
    when(ref.getTimeOfSubmission()).thenReturn(LocalDateTime.now());
    when(eventRepository.getEventRefByStudentIdAndEventId(anyLong(), anyLong())).thenReturn(ref);

    orgaService = new OrgaService(eventRepository, studentRepository, fileService);
    final List<OrgaSummary> orgaSummaries = orgaService.getSummaries();

    verify(studentRepository, times(1)).getStudentById(1);
    verify(studentRepository, times(1)).getStudentById(2);
    verify(studentRepository, times(1)).getStudentById(3);
    verify(eventRepository, times(1)).getEventById((long) 2);
    verify(eventRepository, times(2)).getEventById((long) 1);

    Assertions.assertThat(orgaSummaries).hasSize(3);
    //1->1
    Assertions.assertThat(orgaSummaries.get(0).getStudentName()).isEqualTo(student1.getName());
    Assertions.assertThat(orgaSummaries.get(0).getStudentEmail()).isEqualTo(student1.getEmail());
    Assertions.assertThat(orgaSummaries.get(0).getStudentId()).isEqualTo(student1.getId());
    Assertions.assertThat(orgaSummaries.get(0).getEventTitle()).isEqualTo(event1.getTitle());
    Assertions.assertThat(orgaSummaries.get(0).getEventId()).isEqualTo(event1.getId());
    //2->1
    Assertions.assertThat(orgaSummaries.get(1).getStudentId()).isEqualTo(student2.getId());
    Assertions.assertThat(orgaSummaries.get(1).getEventId()).isEqualTo(event1.getId());
    //3->2
    Assertions.assertThat(orgaSummaries.get(2).getStudentId()).isEqualTo(student3.getId());
    Assertions.assertThat(orgaSummaries.get(2).getEventId()).isEqualTo(event2.getId());

  }

  @Test
  @Disabled("bis die verknüpfung mit MinIO richtig konfiguriert ist.")
  public void getTheRightSummaryContentFromFileServiceTest() {

  }

}
