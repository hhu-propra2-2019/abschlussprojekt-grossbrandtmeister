package mops.rheinjug2.entities;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

@Table("student_event")
@Data
public class EventRef {

  private Long event;

  private boolean submittedSummary;
  private String url;
  private LocalDateTime timeOfSubmission;
  private LocalDateTime deadline;
  private boolean accepted;
  private boolean usedForCertificate;

  EventRef(final Long event, final LocalDateTime deadline) {
    this.event = event;
    this.deadline = deadline;
  }

  /**
   * Gibt an, ob für eine Veranstaltung eine Zusammenfassung abgegeben
   * werden kann.
   */
  public boolean isOpenForSubmission() {
    return LocalDateTime.now().isBefore(deadline);
  }

  boolean isSubmittedAndAcceptedButNotUsed() {
    return submittedSummary && accepted && (!usedForCertificate);
  }

  boolean isSubmittedAndNotAccepted() {
    return submittedSummary && !accepted;
  }

  boolean isSubmittedAndAccepted() {
    return submittedSummary && accepted;
  }
}
