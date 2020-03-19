package mops.rheinjug2.services;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.entities.EventRef;
import mops.rheinjug2.entities.Student;
import mops.rheinjug2.fileupload.FileService;
import mops.rheinjug2.model.OrgaEvent;
import mops.rheinjug2.model.OrgaSummary;
import mops.rheinjug2.repositories.EventRepository;
import mops.rheinjug2.repositories.StudentRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrgaService {
  private final EventRepository eventRepository;
  private final StudentRepository studentRepository;
  private final FileService fileService;

  /**
   * Gibt alle events zurück.
   *
   * @return Liste alle events
   */
  public List<OrgaEvent> getEvents() {
    final List<OrgaEvent> result = new ArrayList<>();
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
  private int getnumberOfSubmition(final Long id) {
    return eventRepository.countSubmittedSummaryPerEventById(id);
  }

  /**
   * Gibt der Anzahl der angemeldte Stundenten einer Veranstaltung.
   *
   * @param id einer Veranstaltung
   * @return Anzahl der Studenten
   */
  private int getNumberOfStudent(final Long id) {
    return eventRepository.countStudentsPerEventById(id);
  }

  /**
   * Gibt alle noch nicht bewetete Zusammenfassungen.
   *
   * @return liste alle Bewertungsanfrage.
   */
  public List<OrgaSummary> getSummaries() {
    final List<OrgaSummary> result = new ArrayList<>();
    eventRepository.getSubmittedAndUnacceptedSummaries().forEach(unacceptedSummary -> {
      //try {
      result.add(
          new OrgaSummary(
              getEventRef(unacceptedSummary.getStudent(), unacceptedSummary.getEvent()),
              getStudentForSummary(unacceptedSummary.getStudent()),
              getEventForSummary(unacceptedSummary.getEvent()),
              "Hier ist eim Zusammenfassung muster"
          ));
      //fileService.getContentOfFileAsString(
      // getStudentForSummary(unacceptedSummary.getStudent()).getName()+
      // "_" + unacceptedSummary.getEvent())
      //} catch (final IOException e) {
      //  e.printStackTrace();
      //} catch (final XmlPullParserException e) {
      //  e.printStackTrace();
      //} catch (final NoSuchAlgorithmException e) {
      //  e.printStackTrace();
      //} catch (final InvalidKeyException e) {
      //  e.printStackTrace();
      //} catch (final InvalidArgumentException e) {
      //  e.printStackTrace();
      //} catch (final InvalidResponseException e) {
      //  e.printStackTrace();
      //} catch (final ErrorResponseException e) {
      //  e.printStackTrace();
      //} catch (final NoResponseException e) {
      //  e.printStackTrace();
      //} catch (final InvalidBucketNameException e) {
      //  e.printStackTrace();
      //} catch (final InsufficientDataException e) {
      //  e.printStackTrace();
      //} catch (final InternalException e) {
      //  e.printStackTrace();
      //}
    });
    return result;
  }

  /**
   * BeziehungsObjekt laden.
   *
   * @param studentId id einem Student.
   * @param eventId   id einer Veranstaltung.
   * @return Beziehungobjekt Zwischen Student und Event
   */
  private EventRef getEventRef(final Long studentId, final Long eventId) {
    return eventRepository.getEventRefByStudentIdAndEventId(studentId, eventId);
  }

  /**
   * Event laden.
   *
   * @param eventId Veranstaltung id.
   * @return gibt Die Veranstaltung zurueck, über die, die ZUsammenfassung geschrieben wurde.
   */
  private Event getEventForSummary(final Long eventId) {
    return eventRepository.getEventById(eventId);
  }

  /**
   * Student laden.
   *
   * @param studentId Student id
   * @return gibt der Student zurueck, der die Zusammenfassung geschrieben hat.
   */
  private Student getStudentForSummary(final Long studentId) {
    return studentRepository.getStudentById(studentId);
  }

  public void setSummaryAcception(final Long studentid, final Long eventid) {
    eventRepository.updateSummaryToAccepted(studentid, eventid);
  }
}
