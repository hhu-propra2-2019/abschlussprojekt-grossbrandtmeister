package mops.rheinjug2.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import mops.rheinjug2.ModelService;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.entities.Student;
import mops.rheinjug2.repositories.EventRepository;
import mops.rheinjug2.repositories.StudentRepository;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
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

  @Test
  public void testGetAllEvents() {
    ModelService modelService = new ModelService(studentRepository, eventRepository);
    Event event1 = createAndSaveEvent("Event 1.0");
    Event event2 = createAndSaveEvent("Event 2.0");

    List<Event> events = Arrays.asList(event1, event2);

    assertThat(modelService.getAllEvents()).containsExactlyInAnyOrder(event2, event1);
  }

  @Test
  public void testAddStudentToEventIfStudentNotExistsInDatabase() {
    ModelService modelService = new ModelService(studentRepository, eventRepository);
    Event event = createAndSaveEvent("Event 1.3");
    eventRepository.save(event);
    modelService.addStudentToEvent("testLogin3", "test3@hhu.de", event.getId());

    Assert.assertEquals(1, studentRepository.count());
    System.out.println(studentRepository.findAll());
    Assert.assertEquals(1, eventRepository.countStudentsPerEventById(event.getId()));

  }

  @Test
  public void testAddStudentToEventIfStudentExistsInDatabase() {
    ModelService modelService = new ModelService(studentRepository, eventRepository);
    Student student = createAndSaveStudent("testLogin4", "test4@hhu.de");
    Event event = createAndSaveEvent("Event 1.4");
    eventRepository.save(event);
    student.addEvent(event);
    studentRepository.save(student);
    modelService.addStudentToEvent("testLogin4", "test4@hhu.de", event.getId());

    Assert.assertEquals(1, studentRepository.count());
    Assert.assertEquals(1, eventRepository.countStudentsPerEventById(event.getId()));

  }

  @Test
  public void testGetAllEventsForCP() {
    ModelService modelService = new ModelService(studentRepository, eventRepository);

    Student student = createAndSaveStudent("testLogin5", "test5@hhu.de");
    Event event1 = createAndSaveEvent("Event 5");
    Event event2 = createAndSaveEvent("Event 6");
    student.addEvent(event1);
    studentRepository.save(student);
    student.addEvent(event2);
    studentRepository.save(student);
    student.addSummary(event1);
    studentRepository.save(student);
    student.addSummary(event2);
    studentRepository.save(student);
//    eventRepository.setUsedForCertificate(true, event1.getId(), student.getId());
//    eventRepository.setUsedForCertificate(false, event2.getId(), student.getId());

    List<Event> events = Arrays.asList(event2);
    System.out.println(events);
    System.out.println(modelService.getAllEventsForCP("testLogin5"));
    //Assert.assertTrue(compareTwoLists(events, modelService.getAllEventsForCP("testLogin5")));
    assertThat(modelService.getAllEventsForCP("testLogin5")).containsExactly(event2);

  }

  @Test
  public void testGetAllEventsPerStudent() {
    Event eventUpcoming = createAndSaveEvent("Veranstaltung Java");
    eventUpcoming.setDate(LocalDateTime.of(2020, 4, 5, 13, 20));
    Event eventOpen = createAndSaveEvent("Veranstaltung Java2");
    eventOpen.setDate(LocalDateTime.now());
    Event eventPassed = createAndSaveEvent("Veranstaltung Java 3");
    eventPassed.setDate(LocalDateTime.of(2020, 1, 2, 12, 20));
    Event eventWithSubmissionNotAccepted = createAndSaveEvent("Veranstaltung Java 4");
    eventWithSubmissionNotAccepted.setDate(LocalDateTime.now());
    Event eventWithSubmissionAccepted = createAndSaveEvent("Veranstaltung Java 4");
    eventWithSubmissionAccepted.setDate(LocalDateTime.now());

    Student student = createAndSaveStudent("ll100", "ll@hhu.de");
    List<Event> events = List.of(eventOpen, eventPassed, eventUpcoming, eventWithSubmissionAccepted, eventWithSubmissionNotAccepted);
    addEventsToStudent(events, student);
    student.addSummary(eventWithSubmissionAccepted);
    student.addSummary(eventWithSubmissionNotAccepted);


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
    events.stream().forEach(x -> student.addEvent(x));
    studentRepository.save(student);
  }

}
