package mops.rheinjug2.model;

import lombok.Data;

/**
 * das Objekt dient zur Abholung der IDs der noch
 * nicht bewertete Zusammenfassungen.
 */
@Data
public class UnacceptedSummaryId {
  private Long student;
  private Long event;
}
