package mops.rheinjug2.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * das Objekt dient zur Abholung der IDs der noch
 * nicht bewertete Zusammenfassungen.
 */
@Data
@AllArgsConstructor
public class UnacceptedSummaryId {
  private Long student;
  private Long event;
}
