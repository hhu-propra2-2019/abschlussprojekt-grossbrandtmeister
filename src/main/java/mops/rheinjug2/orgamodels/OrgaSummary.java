package mops.rheinjug2.orgamodels;

import java.time.LocalDateTime;
import lombok.Value;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.entities.Student;

@Value
public class OrgaSummary {
  private Long eventId;
  private String eventTitle;
  private Long studentId;
  private String studentName;
  private String studentEmail;
  private LocalDateTime timeOfSubmission;
  private String summary;
  private LocalDateTime submissionDeadline;

  /**
   * OrgaSummary ist ein Objekt, das die Zussammenfassungen, die noch zubewerten sind
   * fuer Orga-UI präsentiert.
   *
   * @param student          ein Student, der ein zusammenfassung abgegeben hat
   *                         aber noch nicht bewertet.
   * @param event            eine Veranstaltung, über die, die Zusammenfassung geschrieben wurde.
   * @param timeOfSubmission abgabezeit.
   */
  public OrgaSummary(final LocalDateTime timeOfSubmission, final Student student,
                     final Event event, final String summary) {
    eventId = event.getId();
    this.summary = summary;
    this.timeOfSubmission = timeOfSubmission;
    studentId = student.getId();
    studentName = student.getName();
    studentEmail = student.getEmail();
    eventTitle = event.getTitle();
    submissionDeadline = event.getDate().plusDays(7);
  }
}


