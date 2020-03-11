package mops.rheinjug2.entities;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

@Table("student_event")
@Data
class EventRef {

  private Long event;

  private boolean submittedSummary;
  private String url;
  private LocalDateTime timeOfSubmission;
  private boolean accepted;
  private boolean usedForCertificate;

  EventRef(Long event) {
    this.event = event;
  }

  boolean isSubmittedAndAcceptedButNotUsed() {
    return submittedSummary && accepted && (!usedForCertificate);
  }
}
