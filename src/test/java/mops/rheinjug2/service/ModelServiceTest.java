package mops.rheinjug2.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import mops.rheinjug2.ModelService;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.entities.EventRef;
import mops.rheinjug2.entities.Student;
import mops.rheinjug2.repositories.EventRepository;
import mops.rheinjug2.repositories.StudentRepository;
import org.junit.Assert;
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
  public void testLoadStudentByLogin() {
    ModelService modelService = new ModelService(studentRepository, eventRepository);

    Student student = new Student();
    student.setLogin("testLogin1");
    studentRepository.save(student);

    Assert.assertEquals(student, modelService.loadStudentByLogin("testLogin1"));
  }

  @Test
  public void testGetAllEvents() {
    ModelService modelService = new ModelService(studentRepository, eventRepository);
    Event event1 = createAndSaveEvent("Event 1.0");
    Event event2 = createAndSaveEvent("Event 2.0");

    List<Event> events = Arrays.asList(event1, event2);

    Assert.assertEquals(events, modelService.getAllEvents());
  }

  @Test
  public void testLoadEventById() {
    ModelService modelService = new ModelService(studentRepository, eventRepository);
    Event event = createAndSaveEvent("Event 1.1");
    eventRepository.save(event);

    Assert.assertEquals(event, modelService.loadEventById(event.getId()));
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
  public void testGetAllEventsPerStudent() {
    ModelService modelService = new ModelService(studentRepository, eventRepository);

    Student student = createAndSaveStudent("testLogin5", "test5@hhu.de");
    Event event1 = createAndSaveEvent("Event 1.5");
    Event event2 = createAndSaveEvent("Event 2.5");
    student.addEvent(event1);
    student.addEvent(event2);
    studentRepository.save(student);

    List<Event> events = getEventsFromEventRef(student);

    Assert.assertTrue(compareTwoLists(events, modelService.getAllEventsPerStudent("testLogin5")));
  }

  public boolean compareTwoLists(List<Event> list1, List<Event> list2) {
    for (Event event : list1) {
      if (!list2.contains(event)) {
        return false;
      }
    }
    return true;
  }


  private List<Event> getEventsFromEventRef(Student student) {
    List<Event> events = new ArrayList<>();
    for (EventRef eventRef : student.getEvents()) {
      Optional<Event> event = eventRepository.findById(eventRef.getEvent());
      events.add(event.get());
    }
    return events;
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

}
