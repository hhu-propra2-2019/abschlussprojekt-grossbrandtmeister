package mops.rheinjug2.entities;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

@Table("student_event")
@Data
class EventRef {

  private Long event;

  private boolean submittedSummary;
  private LocalTime timeSubmission;
  private LocalDate dateSubmission;
  private boolean accepted;
  private boolean usedForCertificate;

  EventRef(Long event) {
    this.event = event;
  }
}
