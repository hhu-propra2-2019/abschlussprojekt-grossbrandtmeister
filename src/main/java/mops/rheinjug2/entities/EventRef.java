package mops.rheinjug2.entities;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

@Table("student_event")
@Data
public class EventRef {

  private Long event;

  private boolean submittedSummary;
  private LocalDateTime timeOfSubmission;
  private boolean accepted;
  private boolean usedForCertificate;

  EventRef(final Long event) {
    this.event = event;
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
