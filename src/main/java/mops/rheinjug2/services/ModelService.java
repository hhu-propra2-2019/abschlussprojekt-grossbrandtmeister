package mops.rheinjug2.services;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import mops.rheinjug2.Account;
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

  public ModelService(StudentRepository studentRepository,
                      EventRepository eventRepository) {
    this.studentRepository = studentRepository;
    this.eventRepository = eventRepository;
  }

  /**
   * Alle Veranstaltungen zurückgeben.
   */
  public List<Event> getAllEvents() {
    List<Event> events = (List<Event>) eventRepository.findAll();
    return events.stream().sorted(Comparator.comparing(Event::getDate)
        .reversed()).collect(Collectors.toList());
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
    if (student != null) {
      addEventsWithNoSubmission(events, student.getEventsIdsWithNoSummary());
      addNotAcceptedEvents(events, student.getEventsIdsWithSummaryNotAccepted());
      addAcceptedEvents(events, student.getEventsIdsWithSummaryAccepted());
    }
    return sortMap(events);
  }

  /**
   * Alle Ids der Veranstaltungen, für die sich ein Student angemeldet hat,
   * werden zurückgegeben.
   */
  public List<Long> getAllEventIdsPerStudent(Account account) {
    Map<Event, SubmissionStatus> getAllEventsPerStudent = getAllEventsPerStudent(account.getName());
    List<Long> eventsFromStudent = new ArrayList<>();
    for (Event s : getAllEventsPerStudent.keySet()) {
      eventsFromStudent.add(s.getId());
    }
    return eventsFromStudent;
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
    if (student != null) {
      Set<Long> eventsIds = student.getEventsIdsWithSummaryAcceptedNotUsed();
      return eventsIds == null ? Collections.emptyList() :
          (List<Event>) eventRepository.findAllById(eventsIds);
    }
    return Collections.emptyList();
  }

  /**
   * Es wird geprüft ob Veranstaltungen für CP eingereicht werden können.
   */
  public boolean checkEventsForCertificate(String login) {
    List<Event> events = getAllEventsForCP(login);
    if (checkForEntwickelbar(events)) {
      return true;
    } else if (events.size() < MAX_AMOUNT_EVENTS) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Gibt die Veranstaltung(en) zurück die für CPs eingelöst werden.
   */
  public List<Event> getEventsForCertificate(String login) {
    List<Event> usableEvents = new ArrayList<>();
    List<Event> events = getAllEventsForCP(login);
    if (checkForEntwickelbar(events)) {
      usableEvents.addAll(getEntwickelbarForCP(events));
      return usableEvents;
    } else if (events.size() < MAX_AMOUNT_EVENTS) {
      return usableEvents;
    } else {
      usableEvents.addAll(events.subList(0, 3));
      return usableEvents;
    }
  }

  private static boolean checkForEntwickelbar(List<Event> events) {
    for (Event e : events) {
      if (e.getType().equalsIgnoreCase("Entwickelbar")) {
        return true;
      }
    }
    return false;
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
   * Gibt an, ob ein Student Events gegen CP einlösen darf.
   */
  public boolean useEventsIsPossible(String login) {
    List<Event> events = getAllEventsForCP(login);
    for (Event e : events) {
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
  public boolean acceptedEventsExist(String login) {
    List<Event> events = getAllEventsForCP(login);
    return !events.isEmpty();
  }

  /**
   * Gibt an, ob sich ein Student für Events angemeldet hat
   * und dementsprechend in der DB gespeichert wurde.
   */
  public boolean studentExists(String login) {
    return loadStudentByLogin(login) != null;
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

  /**
   * Ein Event von der DB holen.
   */
  public Event loadEventById(Long eventId) {
    Optional<Event> event = eventRepository.findById(eventId);
    return event.orElse(null);
  }


  private static List<Event> getEntwickelbarForCP(List<Event> events) {
    List<Event> entwickelBar = new ArrayList<>();
    for (Event e : events) {
      if (e.getType().equalsIgnoreCase("Entwickelbar")) {
        entwickelBar.add(e);
        return entwickelBar;
      }
    }
    return entwickelBar;
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

  public Student loadStudentByLogin(String login) {
    return studentRepository.findByLogin(login);
  }

  private static void addToMap(Map<Event, SubmissionStatus> map,
                               List<Event> events, SubmissionStatus staus) {
    events.forEach(event -> map.put(event, staus));
  }

  private static Map<Event, SubmissionStatus> sortMap(Map<Event, SubmissionStatus> map) {
    Map<Event, SubmissionStatus> treeMap = new TreeMap<>(
        Comparator.comparing(Event::getDate).reversed()
    );
    treeMap.putAll(map);
    return treeMap;
  }

  private void addNotAcceptedEvents(Map<Event, SubmissionStatus> events,
                                    Set<Long> eventsIds) {
    var eventsWithNotAcceptedSummary = (List<Event>) eventRepository.findAllById(eventsIds);
    addToMap(events, eventsWithNotAcceptedSummary, SubmissionStatus.SUBMITTED_NOT_ACCEPTED);
  }

  private void addAcceptedEvents(Map<Event, SubmissionStatus> events,
                                 Set<Long> eventsIds) {
    var eventsWithAcceptedSummary = (List<Event>) eventRepository.findAllById(eventsIds);
    addToMap(events, eventsWithAcceptedSummary, SubmissionStatus.SUBMITTED_ACCEPTED);
  }

  private void addEventsWithNoSubmission(Map<Event, SubmissionStatus> events,
                                         Set<Long> eventsIds) {
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
