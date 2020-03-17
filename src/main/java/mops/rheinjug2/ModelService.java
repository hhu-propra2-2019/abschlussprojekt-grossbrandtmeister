package mops.rheinjug2;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.entities.Student;
import mops.rheinjug2.repositories.EventRepository;
import mops.rheinjug2.repositories.StudentRepository;
import org.springframework.stereotype.Service;

@Service
public class ModelService {
  private final static int MAX_AMOUNT_EVENTS = 3;

  private final transient StudentRepository studentRepository;
  private final transient EventRepository eventRepository;

  public ModelService(StudentRepository studentRepository, EventRepository eventRepository) {
    this.studentRepository = studentRepository;
    this.eventRepository = eventRepository;
  }

  public enum SubmissionStatus {
    UPCOMING, OPEN_FOR_SUBMISSION, NO_SUBMISSION, SUBMITTED_NOT_ACCEPTED, SUBMITTED_ACCEPTED
  }

  /**
   * Alle Veranstaltungen zurückgeben.
   */
  public List<Event> getAllEvents() {
    return (List<Event>) eventRepository.findAll();
  }

  /**
   * Ein Student zu einer Veranstaltung hinzufügen.
   */
  public Student addStudentToEvent(String login, String email, Long eventId) {
    Event event = loadEventById(eventId);
    Student student = loadStudentByLogin(login);
    if (student == null) {
      Student newStudent = new Student(login, email);
      newStudent.addEvent(event);
      studentRepository.save(newStudent);
      return newStudent;
    } else {
      student.addEvent(event);
      studentRepository.save(student);
      return student;
    }
  }

  /**
   * Alle Veranstaltungen, für die sich ein Student angemeldet hat,
   * mit dem entsprechenden Status zurückgeben.
   */
  public Map<Event, SubmissionStatus> getAllEventsPerStudent(String login) {
    Student student = loadStudentByLogin(login);
    Map<Event, SubmissionStatus> events = new HashMap<>();

    addEventsWithNoSubmission(events, student.getEventsIdsWithNoSummary());
    addNotAcceptedEvents(events, student.getEventsIdsWithSummaryNotAccepted());
    addAcceptedEvents(events, student.getEventsIdsWithSummaryAccepted());
    return events;
  }


  /**
   * Ein Student gibt eine Zusammenfassung ab.
   */
  public Student submitSummary(String login, Long eventId) {
    Student student = loadStudentByLogin(login);
    Event event = loadEventById(eventId);
    student.addSummary(event);
    studentRepository.save(student);
    return student;
  }

  /**
   * Alle Veranstaltungen zurückgeben, die ein Student noch nicht
   * für CPs verbraucht hat.
   */
  public List<Event> getAllEventsForCP(String login) {
    Student student = loadStudentByLogin(login);
    Set<Long> eventsIds = student.getEventsIdsWithSummaryAcceptedNotUsed();
    return (List<Event>) eventRepository.findAllById(eventsIds);
  }

  /**
   * Veranstaltungen werden für CPs verbraucht, wenn möglich.
   */
  public boolean useEventsForCertificate(String login) {
    Student student = loadStudentByLogin(login);
    List<Event> events = getAllEventsForCP(login);
    if (useForEntwickelbar(student, events)) {
      return true;
    } else if (events.size() < MAX_AMOUNT_EVENTS) {
      return false;
    } else {
      student.useEventsForCP(events.subList(0, 3));
      studentRepository.save(student);
      return true;
    }
  }

  /**
   * Eine Zusammenfassung wird akzeptiert.
   */
  public Student acceptSummary(Long eventId, String login) {
    Event event = loadEventById(eventId);
    Student student = loadStudentByLogin(login);
    student.setAccepted(true, event);
    studentRepository.save(student);
    return student;
  }

  private boolean useForEntwickelbar(Student student, List<Event> events) {
    for (Event e : events) {
      if (e.getType().equalsIgnoreCase("Entwickelbar")) {
        student.useEventsForCP(List.of(e));
        studentRepository.save(student);
        return true;
      }
    }
    return false;
  }

  private Event loadEventById(Long eventId) {
    Optional<Event> event = eventRepository.findById(eventId);
    return event.get();
  }

  private Student loadStudentByLogin(String login) {
    return studentRepository.findByLogin(login);
  }

  private static void addToMap(Map<Event, SubmissionStatus> map,
                               List<Event> events, SubmissionStatus staus) {
    events.forEach(event -> map.put(event, staus));
  }

  private void addNotAcceptedEvents(Map<Event, SubmissionStatus> events, Set<Long> eventsIds) {
    List<Event> eventsWithNotAcceptedSummary = (List<Event>) eventRepository.findAllById(eventsIds);
    addToMap(events, eventsWithNotAcceptedSummary, SubmissionStatus.SUBMITTED_NOT_ACCEPTED);
  }

  private void addAcceptedEvents(Map<Event, SubmissionStatus> events, Set<Long> eventsIds) {
    List<Event> eventsWithAcceptedSummary = (List<Event>) eventRepository.findAllById(eventsIds);
    addToMap(events, eventsWithAcceptedSummary, SubmissionStatus.SUBMITTED_ACCEPTED);
  }

  private void addEventsWithNoSubmission(Map<Event, SubmissionStatus> events, Set<Long> eventsIds) {
    List<Event> eventsWithNoSummary = (List<Event>) eventRepository.findAllById(eventsIds);
    for (Event e : eventsWithNoSummary) {
      if (e.isUpcoming()) {
        addToMap(events, List.of(e), SubmissionStatus.UPCOMING);
      } else if (e.isOpenForSubmission()) {
        addToMap(events, List.of(e), SubmissionStatus.OPEN_FOR_SUBMISSION);
      } else {
        addToMap(events, List.of(e), SubmissionStatus.NO_SUBMISSION);
      }
    }
  }

}
