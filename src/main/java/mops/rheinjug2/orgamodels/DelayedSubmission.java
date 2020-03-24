package mops.rheinjug2.orgamodels;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class DelayedSubmission {
  private long studentId;
  private long eventId;
  private String studentName;
  private String eventTitle;
  private String summaryContent;
}
