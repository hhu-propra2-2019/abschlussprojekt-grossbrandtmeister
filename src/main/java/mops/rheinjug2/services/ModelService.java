package mops.rheinjug2.services;


import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
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

  /**
   * Alle Veranstaltungen zurückgeben.
   */
  public List<Event> getAllEvents() {
    final List<Event> events = (List<Event>) eventRepository.findAll();
    return events.stream().sorted(Comparator.comparing(Event::getDate)
        .reversed()).collect(Collectors.toList());
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
    if (student != null) {
      addEventsWithNoSubmission(events, student.getEventsIdsWithNoSummary());
      addNotAcceptedEvents(events, student.getEventsIdsWithSummaryNotAccepted());
      addAcceptedEvents(events, student.getEventsIdsWithSummaryAccepted());
    }
    return sortMap(events);
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
    if (student != null) {
      final Set<Long> eventsIds = student.getEventsIdsWithSummaryAcceptedNotUsed();
      return eventsIds == null ? Collections.emptyList() :
          (List<Event>) eventRepository.findAllById(eventsIds);
    }
    return Collections.emptyList();
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
   * Gibt an, ob ein Student Events gegen CP einlösen darf.
   */
  public boolean useEventsIsPossible(final String login) {
    final List<Event> events = getAllEventsForCP(login);
    for (final Event e : events) {
      if (e.getType().equalsIgnoreCase("Entwickelbar")
          || events.size() >= MAX_AMOUNT_EVENTS) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gibt an, ob ein Student schon akzeptierte Zusammenfassungen für
   * Events hat.
   */
  public boolean acceptedEventsExist(final String login) {
    final List<Event> events = getAllEventsForCP(login);
    return !events.isEmpty();
  }

  /**
   * Gibt an, ob sich ein Student für Events angemeldet hat
   * und dementsprechend in der DB gespeichert wurde.
   */
  public boolean studentExists(final String login) {
    return loadStudentByLogin(login) != null;
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

  /**
   * Ein Event von der DB holen.
   */
  public Event loadEventById(final Long eventId) {
    final Optional<Event> event = eventRepository.findById(eventId);
    return event.orElse(null);
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

  private Student loadStudentByLogin(final String login) {
    return studentRepository.findByLogin(login);
  }

  private static void addToMap(final Map<Event, SubmissionStatus> map,
                               final List<Event> events, final SubmissionStatus staus) {
    events.forEach(event -> map.put(event, staus));
  }

  private static Map<Event, SubmissionStatus> sortMap(final Map<Event, SubmissionStatus> map) {
    final Map<Event, SubmissionStatus> treeMap = new TreeMap<>(
        Comparator.comparing(Event::getDate).reversed()
    );
    treeMap.putAll(map);
    return treeMap;
  }

  private void addNotAcceptedEvents(final Map<Event, SubmissionStatus> events, final Set<Long> eventsIds) {
    final List<Event> eventsWithNotAcceptedSummary = (List<Event>) eventRepository.findAllById(eventsIds);
    addToMap(events, eventsWithNotAcceptedSummary, SubmissionStatus.SUBMITTED_NOT_ACCEPTED);
  }

  private void addAcceptedEvents(final Map<Event, SubmissionStatus> events, final Set<Long> eventsIds) {
    final List<Event> eventsWithAcceptedSummary = (List<Event>) eventRepository.findAllById(eventsIds);
    addToMap(events, eventsWithAcceptedSummary, SubmissionStatus.SUBMITTED_ACCEPTED);
  }

  private void addEventsWithNoSubmission(final Map<Event, SubmissionStatus> events, final Set<Long> eventsIds) {
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
