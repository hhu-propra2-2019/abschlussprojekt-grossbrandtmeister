package mops.rheinjug2.services;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.entities.EventRef;
import mops.rheinjug2.entities.Student;
import mops.rheinjug2.model.OrgaEvent;
import mops.rheinjug2.model.OrgaSummary;
import mops.rheinjug2.repositories.EventRepository;
import mops.rheinjug2.repositories.StudentRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrgaService {
  EventRepository eventRepository;
  StudentRepository studentRepository;

  /**
   * Ggit ibt alle events zurück.
   *
   * @return Liste alle events
   */
  public List<OrgaEvent> getEvents() {
    List<OrgaEvent> result = new ArrayList<>();
    eventRepository.findAll().forEach(event -> result.add(new OrgaEvent(event,
        getNumberOfStudent(event.getId()),
        getnumberOfSubmition(event.getId())
    )));
    return result;
  }

  /**
   * Gibt Anzahl der abgegebene Zusammenfassungen eiener Veranstaltung.
   *
   * @param id einer Veranstaltung
   * @return Anzahl der Abgegebene Zusammenfassungen
   */
  private int getnumberOfSubmition(Long id) {
    return eventRepository.countSubmittedSummaryPerEventById(id);
  }

  /**
   * Gibt der Anzahl der angemeldte Stundenten einer Veranstaltung.
   *
   * @param id einer Veranstaltung
   * @return Anzahl der Studenten
   */
  private int getNumberOfStudent(Long id) {
    return eventRepository.countStudentsPerEventById(id);
  }

  /**
   * Gibt alle noch nicht bewetete Zusammenfassungen.
   *
   * @return liste alle Bewertungsanfrage.
   */
  public List<OrgaSummary> getSummaries() {
    List<OrgaSummary> result = new ArrayList<>();
    eventRepository.getSubmittedAndUnacceptedSummaries().forEach(unacceptrdSummary ->
        result.add(new OrgaSummary(
            getEventRef(unacceptrdSummary.getStudent(), unacceptrdSummary.getEvent()),
            getStudentForSummary(unacceptrdSummary.getStudent()),
            getEventForSummary(unacceptrdSummary.getEvent())
        ))
    );
    return result;
  }

  /**
   * BeziehungsObjekt laden.
   *
   * @param studentId id einem Student.
   * @param eventId   id einer Veranstaltung.
   * @return Beziehungobjekt Zwischen Student und Event
   */
  private EventRef getEventRef(Long studentId, Long eventId) {
    return eventRepository.getEventRefByStudentIdAndEventId(studentId, eventId);
  }

  /**
   * Event laden.
   *
   * @param eventId Veranstaltung id.
   * @return gibt Die Veranstaltung zurueck, über die, die ZUsammenfassung geschrieben wurde.
   */
  private Event getEventForSummary(Long eventId) {
    return eventRepository.getEventById(eventId);
  }

  /**
   * Student laden.
   *
   * @param studentId Student id
   * @return gibt der Student zurueck, der die Zusammenfassung geschrieben hat.
   */
  private Student getStudentForSummary(Long studentId) {
    return studentRepository.getStudentById(studentId);
  }

  public void setSummaryAcception(Long studentid, Long eventid) {
    eventRepository.updateSummarytoaccepted(studentid, eventid);
  }
}
