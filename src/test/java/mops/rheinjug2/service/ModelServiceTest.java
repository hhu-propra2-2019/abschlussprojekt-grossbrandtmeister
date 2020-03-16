package mops.rheinjug2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import mops.rheinjug2.ModelService;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.entities.Student;
import mops.rheinjug2.repositories.EventRepository;
import mops.rheinjug2.repositories.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@DataJdbcTest
public class ModelServiceTest {

  @Autowired
  private transient EventRepository eventRepository;
  @Autowired
  private transient StudentRepository studentRepository;

  private ModelService modelService;

  @BeforeEach
  public void init() {
    modelService = new ModelService(studentRepository, eventRepository);
  }

  @Test
  public void testGetAllEvents() {
    Event event1 = createAndSaveEvent("Event 1.0");
    Event event2 = createAndSaveEvent("Event 2.0");
    List<Event> allEvents = modelService.getAllEvents();
    assertThat(allEvents).containsExactlyInAnyOrder(event2, event1);
  }

  @Test
  public void testAddStudentToEventIfStudentNotExistsInDatabase() {
    Event event = createAndSaveEvent("Event 1.3");
    Student savedStudent = modelService.addStudentToEvent("testLogin3", "test3@hhu.de", event.getId());
    assertThat(savedStudent.getEventsIds()).containsExactly(event.getId());
  }

  @Test
  public void testAddStudentToEventIfStudentExistsInDatabase() {
    Student student = createAndSaveStudent("testLogin4", "test4@hhu.de");
    Event event = createAndSaveEvent("Event 1.4");
    Student savedStudent = modelService.addStudentToEvent("testLogin4", "test4@hhu.de", event.getId());
    assertThat(savedStudent.getEventsIds()).containsExactly(event.getId());
  }

  @Test
  public void testGetAllEventsForCP() {
    Student student = createAndSaveStudent("testLogin5", "test5@hhu.de");
    Event event1 = createAndSaveEvent("Event 5");
    Event event2 = createAndSaveEvent("Event 6");
    event1.setDate(LocalDateTime.now());
    event2.setDate(LocalDateTime.now());
    eventRepository.save(event1);
    eventRepository.save(event2);
    student.addEvent(event1);
    student.addEvent(event2);
    student.addSummary(event1);
    student.addSummary(event2);
    student.useEventsForCP(List.of(event1));
    studentRepository.save(student);
    modelService.acceptSummary(event2.getId(), student.getLogin());

    assertThat(modelService.getAllEventsForCP("testLogin5")).containsExactly(event2);

  }

  @Test
  public void testSubmit() {
    Student student = createAndSaveStudent("testLogin5", "test5@hhu.de");
    Event event1 = createAndSaveEvent("Event 5");
    event1.setDate(LocalDateTime.now());
    eventRepository.save(event1);

    student.addEvent(event1);

    studentRepository.save(student);
    System.out.println(studentRepository.findAll());
    Student s = modelService.submitSummary("testLogin5", event1.getId());
    System.out.println(s);
  }

  @Test
  public void testGetAllEventsPerStudent() {
    Event eventUpcoming = createAndSaveEvent("Veranstaltung Java");
    eventUpcoming.setDate(LocalDateTime.now().plusDays(1));
    eventRepository.save(eventUpcoming);

    Event eventOpen = createAndSaveEvent("Veranstaltung Java2");
    eventOpen.setDate(LocalDateTime.now());
    eventRepository.save(eventOpen);

    Event eventPassed = createAndSaveEvent("Veranstaltung Java 3");
    eventPassed.setDate(LocalDateTime.of(2020, 1, 2, 12, 20));
    eventRepository.save(eventPassed);

    Event eventWithSubmissionNotAccepted = createAndSaveEvent("Veranstaltung Java 4");
    eventWithSubmissionNotAccepted.setDate(LocalDateTime.now());
    eventRepository.save(eventWithSubmissionNotAccepted);

    Event eventWithSubmissionAccepted = createAndSaveEvent("Veranstaltung Java 4");
    eventWithSubmissionAccepted.setDate(LocalDateTime.now());
    eventRepository.save(eventWithSubmissionAccepted);
    
    Student student = createAndSaveStudent("ll100", "ll@hhu.de");
    List<Event> events = List.of(eventOpen, eventPassed, eventUpcoming, eventWithSubmissionAccepted, eventWithSubmissionNotAccepted);
    addEventsToStudent(events, student);
    student.addSummary(eventWithSubmissionAccepted);
    student.addSummary(eventWithSubmissionNotAccepted);
    studentRepository.save(student);
    Student savedStudent = modelService.acceptSummary(eventWithSubmissionAccepted.getId(), "ll100");

    Map<Event, ModelService.SubmissionStatus> allEvents = modelService.getAllEventsPerStudent(student.getLogin());

    assertThat(allEvents).containsOnly(entry(eventUpcoming, ModelService.SubmissionStatus.UPCOMING),
        entry(eventOpen, ModelService.SubmissionStatus.OPEN_FOR_SUBMISSION),
        entry(eventPassed, ModelService.SubmissionStatus.NO_SUBMISSION),
        entry(eventWithSubmissionAccepted, ModelService.SubmissionStatus.SUBMITTED_ACCEPTED),
        entry(eventWithSubmissionNotAccepted, ModelService.SubmissionStatus.SUBMITTED_NOT_ACCEPTED));

  }

  @AfterEach
  public void cleanUpEach() {
    eventRepository.deleteAll();
    studentRepository.deleteAll();
  }

  private Student createAndSaveStudent(String login, String email) {
    Student student = new Student(login, email);
    studentRepository.save(student);
    return student;
  }

  private Event createAndSaveEvent(String title) {
    Event event = new Event();
    event.setTitle(title);
    eventRepository.save(event);
    return event;
  }

  private void addEventsToStudent(List<Event> events, Student student) {
    events.forEach(student::addEvent);
    studentRepository.save(student);
  }

}
