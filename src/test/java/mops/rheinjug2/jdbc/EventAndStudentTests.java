package mops.rheinjug2.jdbc;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDateTime;
import java.util.List;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.entities.Student;
import mops.rheinjug2.repositories.EventRepository;
import mops.rheinjug2.repositories.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJdbcTest
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class EventAndStudentTests {

  @Autowired
  private transient EventRepository eventRepository;

  @Autowired
  private transient StudentRepository studentRepository;

  @Test
  public void testOneEventOneStudent() {
    Event event = createAndSaveEvent("Veranstaltung");
    Student student = createAndSaveStudent("sk100", "sk@hhu.de");

    addStudentToEvent(event, student);
    Student savedStudent = studentRepository.findByLogin("sk100");
    var events = eventRepository.findAllById(savedStudent.getEventsIds());

    assertThat(events).containsExactly(event);
  }

  @Test
  public void testOneEventTwoStudents() {
    Event event = createAndSaveEvent("Veranstaltung");
    Student student1 = createAndSaveStudent("sk100", "sk@hhu.de");
    Student student2 = createAndSaveStudent("pk100", "pk@hhu.de");

    addStudentsToEvent(event, List.of(student1, student2));

    assertThat(eventRepository.countStudentsPerEventById(event.getId())).isEqualTo(2);
    assertThat(studentRepository.countEventsPerStudentById(student1.getId())).isEqualTo(1);
  }

  @Test
  public void testTwoEventsWithTwoStudentsEach() {
    Event event1 = createAndSaveEvent("Veranstaltung A");
    Event event2 = createAndSaveEvent("Veranstaltung B");
    Student student1 = createAndSaveStudent("sk100", "sk@hhu.de");
    Student student2 = createAndSaveStudent("pk100", "pk@hhu.de");
    Student student3 = createAndSaveStudent("ak100", "ak@hhu.de");
    Student student4 = createAndSaveStudent("lk100", "lk@hhu.de");

    addStudentsToEvent(event1, List.of(student1, student2));
    addStudentsToEvent(event2, List.of(student3, student4));

    assertThat(eventRepository.countStudentsPerEventById(event1.getId())).isEqualTo(2);
    assertThat(eventRepository.countStudentsPerEventById(event2.getId())).isEqualTo(2);
  }

  @Test
  public void testDeleteOneEventWithTwoStudents() {
    Event event = createAndSaveEvent("Veranstaltung Java");
    Student student1 = createAndSaveStudent("sk100", "sk@hhu.de");
    Student student2 = createAndSaveStudent("pk100", "pk@hhu.de");

    addStudentsToEvent(event, List.of(student1, student2));
    List<Long> studentIds = eventRepository.findAllStudentsIdsPerEventById(event.getId());
    eventRepository.delete(event);
    deleteEventForAllStudents(event, studentIds);

    assertThat(eventRepository.count()).isZero();
    assertThat(studentRepository.countEventsPerStudentById(student1.getId())).isZero();
  }

  @Test
  public void testTwoEventsWithTheSameStudentDeleted() {
    Event event1 = createAndSaveEvent("Veranstaltung A");
    Event event2 = createAndSaveEvent("Veranstaltung B");
    Student student = createAndSaveStudent("sk100", "sk@hhu.de");

    addStudentToEvent(event1, student);
    addStudentToEvent(event2, student);
    studentRepository.delete(student);

    assertThat(eventRepository.countStudentsPerEventById(event1.getId())).isEqualTo(0);
    assertThat(eventRepository.countStudentsPerEventById(event2.getId())).isEqualTo(0);
    assertThat(studentRepository.countEventsPerStudentById(student.getId())).isEqualTo(0);
  }

  @Test
  public void testOneStudentAddsSummaryToEvent() {
    Event event = createAndSaveEvent("Veranstaltung");
    event.setDate(LocalDateTime.now());
    eventRepository.save(event);
    Student student = createAndSaveStudent("ax100", "ax@hhu.de");
    student.addEvent(event);
    student.addSummary(event);
    studentRepository.save(student);

    assertThat(studentRepository.getSubmittedValue(student.getId(), event.getId())).isEqualTo(true);
  }

  @Test
  public void testOneStudentAddsSummaryToTwoEvents() {
    Event event = createAndSaveEvent("Veranstaltung A");
    event.setDate(LocalDateTime.now());
    eventRepository.save(event);
    Event event2 = createAndSaveEvent("Veranstaltung B");
    event2.setDate(LocalDateTime.now());
    eventRepository.save(event2);
    Student student = createAndSaveStudent("ax100", "ax@hhu.de");
    student.addEvent(event);
    student.addEvent(event2);

    student.addSummary(event);
    student.addSummary(event2);
    studentRepository.save(student);

    assertThat(studentRepository.getSubmittedValue(student.getId(), event.getId())).isTrue();
    assertThat(studentRepository.getSubmittedValue(student.getId(), event2.getId())).isTrue();
  }

  @Test
  public void testDeleteOneStudent() {
    Student student = createAndSaveStudent("Sarah K", "sk@hhu.de");
    studentRepository.delete(student);

    List<Student> students = (List<Student>) studentRepository.findAll();

    assertFalse(students.stream().anyMatch(item -> student.getId().equals(item.getId())));
  }

  @Test
  public void testDeleteOneEvent() {
    Event event2 = createAndSaveEvent("Veranstaltung");

    eventRepository.delete(event2);
    List<Event> events = (List<Event>) eventRepository.findAll();

    assertFalse(events.stream().anyMatch(item -> event2.getId().equals(item.getId())));
  }

  @AfterEach
  public void cleanUpEach() {
    eventRepository.deleteAll();
    studentRepository.deleteAll();
  }

  private void addStudentToEvent(Event event, Student student) {
    student.addEvent(event);
    studentRepository.save(student);
  }

  private void addStudentsToEvent(Event event, List<Student> students) {
    students.forEach(x -> x.addEvent(event));
    studentRepository.saveAll(students);
  }

  private Student createAndSaveStudent(String login, String email) {
    Student s = new Student(login, email);
    studentRepository.save(s);
    return s;
  }

  private Event createAndSaveEvent(String title) {
    Event event = new Event();
    event.setTitle(title);
    eventRepository.save(event);
    return event;
  }

  private void deleteEventForAllStudents(Event event, List<Long> studentIds) {
    var students = studentRepository.findAllById(studentIds);
    students.forEach(student -> student.deleteEvent(event));
    studentRepository.saveAll(students);
  }
}
