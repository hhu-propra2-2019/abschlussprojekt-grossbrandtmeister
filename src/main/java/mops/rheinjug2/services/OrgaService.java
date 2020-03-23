package mops.rheinjug2.services;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.NoResponseException;
import java.io.IOException;
import java.net.ConnectException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.entities.Student;
import mops.rheinjug2.fileupload.FileService;
import mops.rheinjug2.orgamodels.DelayedSubmission;
import mops.rheinjug2.orgamodels.OrgaEvent;
import mops.rheinjug2.orgamodels.OrgaSummary;
import mops.rheinjug2.orgamodels.SummariesIDs;
import mops.rheinjug2.repositories.EventRepository;
import mops.rheinjug2.repositories.StudentRepository;
import org.springframework.stereotype.Service;
import org.xmlpull.v1.XmlPullParserException;

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
    return result.stream()
        .sorted(Comparator.comparing(OrgaEvent::getDate).reversed())
        .collect(Collectors.toList());
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
      try {
        result.add(
            new OrgaSummary(
                getSummarySubmissionDate(unacceptedSummary.getStudent(),
                    unacceptedSummary.getEvent()),
                getSummaryStudent(unacceptedSummary.getStudent()),
                getSummaryEvent(unacceptedSummary.getEvent()),
                getSummaryContentFromFileservice(unacceptedSummary.getStudent(),
                    unacceptedSummary.getEvent())
            ));
      } catch (final Exception e) {
        e.printStackTrace();
      }
    });
    return result.stream()
        .sorted(Comparator.comparing(OrgaSummary::getTimeOfSubmission).reversed()
            .thenComparing(OrgaSummary::getSubmissionDeadline))
        .collect(Collectors.toList());
  }

  private String getSummaryContentFromFileservice(final Long studentid, final Long eventid)
      throws IOException, InvalidKeyException, NoSuchAlgorithmException,
      XmlPullParserException, InvalidArgumentException, InvalidResponseException,
      InternalException, NoResponseException, InvalidBucketNameException, InsufficientDataException,
      ErrorResponseException {
    final String fileName = getSummaryStudent(studentid).getName() + "_" + eventid;
    try {
      return fileService.getContentOfFileAsString(fileName);
    } catch (final ConnectException e) {
      return "MinIO " + e.getMessage();
    }
  }

  /**
   * Die Methode gibt eine Liste alle angemeldte veranstaltungen
   * deren Zusammenfassung noch nicht abgegeben worde.
   *
   * @return liste der Veranstaltungen.
   */
  public List<DelayedSubmission> getDelayedSubmission() {
    final List<DelayedSubmission> result = new ArrayList<>();
    eventRepository.getUnSubmittedSummaries().forEach(summariesIDs -> {
      if (summaryIsDelayd(summariesIDs.getEvent())) {
        result.add(new DelayedSubmission(
                summariesIDs.getStudent(),
                summariesIDs.getEvent(),
                getSummaryStudent(summariesIDs.getStudent()).getName(),
                getSummaryEvent(summariesIDs.getEvent()).getTitle()
            )
        );
      }
    });
    return result;
  }

  /**
   * Oberprüfen, ob die Abgabe zeit überschritten wurde.
   *
   * @param eventId id einer Veranstaltung
   * @return boolean
   */
  private boolean summaryIsDelayd(final Long eventId) {
    final LocalDateTime submissionDeadline = getSummaryEvent(eventId).getDate().plusDays(7);
    return submissionDeadline.isAfter(LocalDateTime.now());
  }

  /**
   * BeziehungsObjekt laden.
   *
   * @param studentId id einem Student.
   * @param eventId   id einer Veranstaltung.
   * @return Beziehungobjekt Zwischen Student und Event
   */
  private LocalDateTime getSummarySubmissionDate(final Long studentId,
                                                 final Long eventId) {
    return eventRepository.getEventRefByStudentIdAndEventId(studentId,
        eventId).getTimeOfSubmission();
  }

  /**
   * Event laden.
   *
   * @param eventId Veranstaltung id.
   * @return gibt Die Veranstaltung zurueck, über die, die ZUsammenfassung geschrieben wurde.
   */
  private Event getSummaryEvent(final Long eventId) {
    return eventRepository.getEventById(eventId);
  }

  /**
   * Student laden.
   *
   * @param studentId Student id
   * @return gibt der Student zurueck, der die Zusammenfassung geschrieben hat.
   */
  private Student getSummaryStudent(final Long studentId) {
    return studentRepository.getStudentById(studentId);
  }

  public void setSummaryAcception(final Long studentid, final Long eventid) {
    eventRepository.updateSummaryToAccepted(studentid, eventid);
  }

  /**
   * Oberprügen, ob der Student an der Veranstaltung angemeldet ist.
   *
   * @param studentid studentid
   * @param eventid   eventid
   * @return true, wenn ein der student an der Veranstaltung angemeldet ist.
   */
  public boolean summaryExist(final Long studentid, final Long eventid) {
    final Optional<SummariesIDs> summariesIDsOption =
        eventRepository.checkSummary(studentid, eventid);
    if (summariesIDsOption.isPresent()) {
      return true;
    }
    return false;
  }

  /**
   * Gibt der Anzahl alle Bewertungsanfragen.
   *
   * @return Anzahl der Anfragen
   */
  public int getnumberOfEvaluationRequests() {
    return eventRepository.getNumberOfSubmittedAndUnacceptedSummaries();
  }
}
