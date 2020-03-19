package mops.rheinjug2.services;

import java.time.LocalDateTime;
import java.util.List;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.entities.Student;
import mops.rheinjug2.model.OrgaEvent;
import mops.rheinjug2.model.OrgaSummary;
import mops.rheinjug2.repositories.EventRepository;
import mops.rheinjug2.repositories.StudentRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.junit4.SpringRunner;

//import mops.rheinjug2.ModelService;

@Disabled
@DataJdbcTest
@RunWith(SpringRunner.class)
public class OrgaServiceTests {

  @Autowired
  private transient EventRepository eventRepository;
  @Autowired
  private transient StudentRepository studentRepository;

  private transient OrgaService orgaService;
  private transient ModelService modelService;

  private Student student1;
  private Student student2;
  private Student student3;
  private Event event1;
  private Event event2;


  /**
   * Services initialisieren.
   */
  @BeforeEach
  public void servicesInit() {
    orgaService = new OrgaService(eventRepository, studentRepository);
    modelService = new ModelService(studentRepository, eventRepository);
  }

  /**
   * Data zum Befüllen der DB initialisieren.
   */
  @BeforeAll
  public void dataInit() {
    student1 = new Student("student1login", "student1@email");
    student1.setName("student1");

    student2 = new Student("student2login", "student2@email");
    student2.setName("student2");

    student3 = new Student("student3login", "student3@email");
    student3.setName("student3");

    event1 = new Event();
    event1.setDate(LocalDateTime.now().minusDays(3));
    event1.setTitle("Event1");
    event1.setStatus("Status1");

    event2 = new Event();
    event2.setDate(LocalDateTime.now().minusDays(3));
    event2.setTitle("Event2");
  }

  /**
   * das Abholen der Event Objekts Testen.
   */
  @Test
  public void getEventsTest() {
    eventRepository.saveAll(List.of(event1, event2));
    System.out.println(eventRepository);
    System.out.println(eventRepository.findAll());
    final List<OrgaEvent> orgaevents = orgaService.getEvents();
    Assertions.assertThat(orgaevents).hasSize(2);
  }

  /**
   * Hier wird getestet, ob die OrgaEvents Objekte richtig erstellt worde
   * und ob diese die richtige Informationen enthalten.
   */
  @Test
  public void creatingOrgaEventObjectTest() {
    eventRepository.saveAll(List.of(event1, event2));
    studentRepository.saveAll(List.of(student1, student2, student3));
    modelService.addStudentToEvent(student1.getLogin(), student1.getEmail(), event1.getId());
    modelService.addStudentToEvent(student2.getLogin(), student2.getEmail(), event1.getId());
    modelService.submitSummary(student1.getLogin(), event1.getId(), "1->1");
    modelService.submitSummary(student2.getLogin(), event1.getId(), "2->1");

    modelService.addStudentToEvent(student1.getLogin(), student1.getEmail(), event2.getId());
    modelService.addStudentToEvent(student2.getLogin(), student2.getEmail(), event2.getId());

    final List<OrgaEvent> orgaevents = orgaService.getEvents();

    Assertions.assertThat(orgaevents).hasSize(2);
    Assertions.assertThat(orgaevents.get(0).getId()).isEqualTo(event1.getId());
    Assertions.assertThat(orgaevents.get(0).getDate().withNano(0)
        .equals(event1.getDate().withNano(0))).isTrue();
    Assertions.assertThat(orgaevents.get(0).getTitle()).isEqualTo(event1.getTitle());
    Assertions.assertThat(orgaevents.get(0).getStatus()).isEqualTo("Status1");
    Assertions.assertThat(orgaevents.get(0).getNumberOfStudent()).isEqualTo(2);
    Assertions.assertThat(orgaevents.get(0).getNumberOfSubmition()).isEqualTo(2);

    Assertions.assertThat(orgaevents.get(1).getNumberOfStudent()).isEqualTo(2);
    Assertions.assertThat(orgaevents.get(1).getNumberOfSubmition()).isEqualTo(0);
  }

  @Test
  public void getSummariesTest() {
    eventRepository.saveAll(List.of(event1, event2));
    studentRepository.saveAll(List.of(student1, student2, student3));
    modelService.addStudentToEvent(student1.getLogin(), student1.getEmail(), event1.getId());
    modelService.addStudentToEvent(student2.getLogin(), student2.getEmail(), event1.getId());
    modelService.submitSummary(student2.getLogin(), event1.getId(), "2->1");

    modelService.addStudentToEvent(student1.getLogin(), student1.getEmail(), event2.getId());
    modelService.addStudentToEvent(student2.getLogin(), student2.getEmail(), event2.getId());
    modelService.submitSummary(student1.getLogin(), event2.getId(), "1->2");

    final List<OrgaSummary> orgaSummaries = orgaService.getSummaries();

    Assertions.assertThat(orgaSummaries).hasSize(2);
    Assertions.assertThat(orgaSummaries.get(0).getEventId()).isEqualTo(event1.getId());
    Assertions.assertThat(orgaSummaries.get(0).getEventTitle()).isEqualTo(event1.getTitle());
    Assertions.assertThat(orgaSummaries.get(0).getStudentId()).isEqualTo(student2.getId());
    Assertions.assertThat(orgaSummaries.get(0).getStudentEmail()).isEqualTo(student2.getEmail());
    Assertions.assertThat(orgaSummaries.get(0).getStudentName()).isEqualTo(student2.getName());
    //Assertions.assertThat(orgaSummaries.get(0).getUrl()).isEqualTo("2->1");
  }

}
