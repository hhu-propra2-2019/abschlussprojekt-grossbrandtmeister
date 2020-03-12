package mops.rheinjug2;


import java.util.List;
import java.util.Optional;
import java.util.Set;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.entities.Student;
import mops.rheinjug2.repositories.EventRepository;
import mops.rheinjug2.repositories.StudentRepository;
import org.springframework.stereotype.Service;

@Service
public class ModelService {
  private final transient StudentRepository studentRepository;
  private final transient EventRepository eventRepository;

  public ModelService(StudentRepository studentRepository, EventRepository eventRepository) {
    this.studentRepository = studentRepository;
    this.eventRepository = eventRepository;
  }

  public List<Event> getAllEvents() {
    return (List<Event>) eventRepository.findAll();
  }

  public void addStudentToEvent(String login, String email, Long eventId) {
    Event event = loadEventById(eventId);
    Student student = loadStudentByLogin(login);
    if (student == null) {
      Student newStudent = new Student(login, email);
      newStudent.addEvent(event);
      studentRepository.save(newStudent);
    } else {
      student.addEvent(event);
      studentRepository.save(student);
    }
  }

  public List<Event> getAllEventsPerStudent(String login) {
    Student student = loadStudentByLogin(login);
    Set<Long> eventsIds = student.getEventsIds();
    return (List<Event>) eventRepository.findAllById(eventsIds);
  }

  public Event loadEventById(Long eventId) {
    Optional<Event> event = eventRepository.findById(eventId);
    return event.get();
  }

  public Student loadStudentByLogin(String login) {
    return studentRepository.findByLogin(login);
  }

}
