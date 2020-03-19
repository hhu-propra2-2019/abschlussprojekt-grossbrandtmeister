package mops.rheinjug2.services;


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
  private static final int MAX_AMOUNT_EVENTS = 3;

  private final transient StudentRepository studentRepository;
  private final transient EventRepository eventRepository;

  public ModelService(final StudentRepository studentRepository,
                      final EventRepository eventRepository) {
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
  public Student addStudentToEvent(final String login, final String email, final Long eventId) {
    final Event event = loadEventById(eventId);
    final Student student = loadStudentByLogin(login);
    if (student == null) {
      final Student newStudent = new Student(login, email);
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
  public Map<Event, SubmissionStatus> getAllEventsPerStudent(final String login) {
    final Student student = loadStudentByLogin(login);
    final Map<Event, SubmissionStatus> events = new HashMap<>();

    addEventsWithNoSubmission(events, student.getEventsIdsWithNoSummary());
    addNotAcceptedEvents(events, student.getEventsIdsWithSummaryNotAccepted());
    addAcceptedEvents(events, student.getEventsIdsWithSummaryAccepted());
    return events;
  }


  /**
   * Ein Student gibt eine Zusammenfassung ab.
   */
  public Student submitSummary(final String login, final Long eventId, final String url) {
    final Student student = loadStudentByLogin(login);
    final Event event = loadEventById(eventId);
    student.addSummary(event, url);
    studentRepository.save(student);
    return student;
  }

  /**
   * Alle Veranstaltungen zurückgeben, die ein Student noch nicht
   * für CPs verbraucht hat.
   */
  public List<Event> getAllEventsForCP(final String login) {
    final Student student = loadStudentByLogin(login);
    final Set<Long> eventsIds = student.getEventsIdsWithSummaryAcceptedNotUsed();
    return (List<Event>) eventRepository.findAllById(eventsIds);
  }

  /**
   * Veranstaltungen werden für CPs verbraucht, wenn möglich.
   */
  public boolean useEventsForCertificate(final String login) {
    final Student student = loadStudentByLogin(login);
    final List<Event> events = getAllEventsForCP(login);
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
  public Student acceptSummary(final Long eventId, final String login) {
    final Event event = loadEventById(eventId);
    final Student student = loadStudentByLogin(login);
    student.setAccepted(true, event);
    studentRepository.save(student);
    return student;
  }

  private boolean useForEntwickelbar(final Student student, final List<Event> events) {
    for (final Event e : events) {
      if (e.getType().equalsIgnoreCase("Entwickelbar")) {
        student.useEventsForCP(List.of(e));
        studentRepository.save(student);
        return true;
      }
    }
    return false;
  }

  private Event loadEventById(final Long eventId) {
    final Optional<Event> event = eventRepository.findById(eventId);
    return event.get();
  }

  private Student loadStudentByLogin(final String login) {
    return studentRepository.findByLogin(login);
  }

  private static void addToMap(final Map<Event, SubmissionStatus> map,
                               final List<Event> events, final SubmissionStatus staus) {
    events.forEach(event -> map.put(event, staus));
  }

  private void addNotAcceptedEvents(final Map<Event, SubmissionStatus> events,
                                    final Set<Long> eventsIds) {
    final var eventsWithNotAcceptedSummary = (List<Event>) eventRepository.findAllById(eventsIds);
    addToMap(events, eventsWithNotAcceptedSummary, SubmissionStatus.SUBMITTED_NOT_ACCEPTED);
  }

  private void addAcceptedEvents(final Map<Event, SubmissionStatus> events,
                                 final Set<Long> eventsIds) {
    final var eventsWithAcceptedSummary = (List<Event>) eventRepository.findAllById(eventsIds);
    addToMap(events, eventsWithAcceptedSummary, SubmissionStatus.SUBMITTED_ACCEPTED);
  }

  private void addEventsWithNoSubmission(final Map<Event, SubmissionStatus> events,
                                         final Set<Long> eventsIds) {
    final List<Event> eventsWithNoSummary = (List<Event>) eventRepository.findAllById(eventsIds);
    for (final Event e : eventsWithNoSummary) {
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
