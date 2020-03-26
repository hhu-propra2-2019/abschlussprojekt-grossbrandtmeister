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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.entities.EventRef;
import mops.rheinjug2.entities.Student;
import mops.rheinjug2.fileupload.FileService;
import mops.rheinjug2.orgamodels.DelayedSubmission;
import mops.rheinjug2.orgamodels.OrgaEvent;
import mops.rheinjug2.orgamodels.OrgaSummary;
import mops.rheinjug2.repositories.EventRepository;
import mops.rheinjug2.repositories.StudentRepository;
import org.springframework.stereotype.Service;
import org.xmlpull.v1.XmlPullParserException;

@Service
@Log4j2
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
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  public List<OrgaEvent> getEvents() {
    final List<OrgaEvent> result = new ArrayList<>();
    final List<Student> allStudents = (List<Student>) studentRepository.findAll();
    eventRepository.findAll().forEach(event -> result.add(new OrgaEvent(
        event,
        getNumberOfRegisteredStudent(event.getId(), allStudents),
        getnumberOfSubmittedSummaries(event.getId(), allStudents)
    )));
    return result.stream()
        .sorted(Comparator.comparing(OrgaEvent::getDate).reversed())
        .collect(Collectors.toList());
  }

  /**
   * Gibt Anzahl der abgegebene Zusammenfassungen eiener Veranstaltung.
   *
   * @param eventId     einer Veranstaltung
   * @param allStudents alle Studenten
   * @return Anzahl der Abgegebene Zusammenfassungen
   */
  private static int getnumberOfSubmittedSummaries(final Long eventId,
                                                   final List<Student> allStudents) {
    return (int) allStudents.stream()
        .filter(student -> student.isEventSubmitted(eventId))
        .count();
  }

  /**
   * Gibt der Anzahl der angemeldte Stundenten einer Veranstaltung.
   *
   * @param eventId     einer Veranstaltung
   * @param allStudents alle Studenten
   * @return Anzahl der Studenten
   */
  private static int getNumberOfRegisteredStudent(final Long eventId,
                                                  final List<Student> allStudents) {
    return (int) allStudents.stream()
        .filter(student -> student.isRegiteredForEvent(eventId)).count();
  }

  /**
   * Gibt alle , noch nicht bewertete Bewertunganfragen.
   *
   * @return liste alle Bewertungsanfrage.
   */
  public List<OrgaSummary> getSummaries() {
    final List<OrgaSummary> result = new ArrayList<>();
    studentRepository.findAll().forEach(student -> {
      student.getEvents()
          .stream()
          .filter(EventRef::isSubmittedAndNotAccepted)
          .forEach(notYetAcceptedSummary -> {
            try {
              result.add(new OrgaSummary(
                  notYetAcceptedSummary.getTimeOfSubmission(),
                  student,
                  getSummaryEvent(notYetAcceptedSummary.getEvent()),
                  getSummaryContentFromFileservice(student.getLogin(),
                      notYetAcceptedSummary.getEvent())
              ));
            } catch (final Exception e) {
              log.catching(e);
            }
          });
    });
    return result.stream()
        .sorted(Comparator.comparing(OrgaSummary::getTimeOfSubmission).reversed()
            .thenComparing(OrgaSummary::getSubmissionDeadline))
        .collect(Collectors.toList());
  }

  /**
   * Die Mithode dient zur Abholung von Zusammenfassung einem Student über einer Veranstaltung.
   *
   * @param studentName .
   * @param eventId     .
   * @return Inhalt der Zusammenfassung.
   * @throws IOException                .
   * @throws InvalidKeyException        .
   * @throws NoSuchAlgorithmException   .
   * @throws XmlPullParserException     .
   * @throws InvalidArgumentException   .
   * @throws InvalidResponseException   .
   * @throws InternalException          .
   * @throws NoResponseException        .
   * @throws InvalidBucketNameException .
   * @throws InsufficientDataException  .
   * @throws ErrorResponseException     .
   */
  private String getSummaryContentFromFileservice(final String studentName, final Long eventId)
      throws IOException, InvalidKeyException, NoSuchAlgorithmException,
      XmlPullParserException, InvalidArgumentException, InvalidResponseException,
      InternalException, NoResponseException, InvalidBucketNameException, InsufficientDataException,
      ErrorResponseException {
    final String fileName = studentName + "_" + eventId;
    try {
      return fileService.getContentOfFileAsString(fileName);
    } catch (final ConnectException e) {
      return "MinIO " + e.getMessage();
    }
  }

  /**
   * Die Methode gibt eine Liste alle angemeldte veranstaltungen
   * deren Zusammenfassung noch nicht abgegeben worde
   * (mögliche verspätete abgaben).
   *
   * @return liste der Abgaben, die der Orga als versätete Abgaben hochladen kann.
   */
  public List<DelayedSubmission> getDelayedSubmission() {
    final List<DelayedSubmission> result = new ArrayList<>();
    studentRepository.findAll().forEach(student -> {
      student.getEvents()
          .stream()
          .filter(EventRef::isDelayed)
          .forEach(delayedSubmision ->
              result.add(new DelayedSubmission(
                  student.getId(),
                  delayedSubmision.getEvent(),
                  student.getLogin(),
                  getSummaryEvent(delayedSubmision.getEvent()).getTitle(),
                  delayedSubmision.getDeadline(),
                  null
              ))
          );
    });
    return result.stream()
        .sorted(Comparator.comparing(DelayedSubmission::getDeadLine).reversed())
        .collect(Collectors.toList());
  }

  /**
   * Event laden.
   *
   * @param eventId Veranstaltung id.
   * @return gibt Die Veranstaltung zurueck, über die, die ZUsammenfassung geschrieben wurde.
   */
  private Event getSummaryEvent(final Long eventId) {
    return eventRepository.findById(eventId).get();
  }

  /**
   * Die methode dient zu akzeptieren von abgaben.
   *
   * @param studentId studentid
   * @param eventId   eventid
   * @return boolean ob erfolgreich als akzeptiert gespeichert wurde.
   */
  public boolean setSummaryAsAccepted(final Long studentId, final Long eventId) {
    final Optional<Student> student = studentRepository.findById(studentId);
    final Optional<Event> event = eventRepository.findById(eventId);
    if (event.isPresent() && student.isPresent()) {
      if (student.get().isRegiteredForEvent(eventId)) {
        student.get().setAccepted(true, event.get());
        studentRepository.save(student.get());
        return true;
      }
      return false;
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

  /**
   * Die Methode lädt die von Orga abgegebene Zusammenfassung auf MinIO hoch.
   *
   * @param studentId      .
   * @param eventId        .
   * @param studentName    .
   * @param summaryContent .
   * @throws IOException .
   */
  public void summaryupload(final Long studentId, final Long eventId,
                            final String studentName,
                            final String summaryContent) throws IOException {
    fileService.uploadContentConvertToMd(summaryContent, studentName + "_" + eventId);
    final Optional<Student> student = studentRepository.findById(studentId);
    final Optional<Event> event = eventRepository.findById(eventId);
    student.get().getEvents()
        .stream()
        .filter(eventRef -> eventRef.getEvent().equals(eventId))
        .forEach(eventRef -> eventRef.setSubmittedSummary(true));
    student.get().setAccepted(true, event.get());
  }

  /**
   * Gibt Verspätete abgaben einer Student.
   *
   * @param searchedName student Name
   * @return liste der verspätete Abgaben.
   */
  public List<DelayedSubmission> getDelayedSubmissionsForStudent(final String searchedName) {
    return getDelayedSubmission().stream()
        .filter(delayedSubmission ->
            searchedName.equalsIgnoreCase(delayedSubmission.getStudentName()))
        .collect(Collectors.toList());
  }

  /**
   * Gibt versätete Abgabe einer Veranstaltung.
   *
   * @param searchedName Veranstaltungsname.
   * @return .
   */
  public List<DelayedSubmission> getDelayedSubmissionsForEvent(final String searchedName) {
    return getDelayedSubmission().stream()
        .filter(delayedSubmission ->
            searchedName.equalsIgnoreCase(delayedSubmission.getEventTitle()))
        .collect(Collectors.toList());
  }
}
