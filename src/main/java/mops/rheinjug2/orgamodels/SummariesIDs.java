package mops.rheinjug2.orgamodels;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * das Objekt dient zur Abholung der IDs der noch
 * nicht bewertete Zusammenfassungen.
 */
@Data
@AllArgsConstructor
public class SummariesIDs {
  private Long student;
  private Long event;
}
