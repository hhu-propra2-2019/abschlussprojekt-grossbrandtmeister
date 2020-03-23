package mops.rheinjug2.orgamodels;


import java.time.LocalDateTime;
import lombok.Value;
import mops.rheinjug2.entities.Event;

@Value
public class OrgaEvent {
  private Long id;
  private String title;
  private LocalDateTime date;
  private int numberOfStudent;
  private int numberOfSubmition;
  private String status;
  private LocalDateTime submissionDeadline;

  /**
   * OrgaEvent ist ein Objekt, das Event Objekt mit zusätzliche Variablen, die
   * fuer Orga-UI notwindig sind.
   *
   * @param event             ein Event
   * @param numberOfStudent   Anzahl der Stundenten, die fuer Diese Verantaltung angemeldet sind.
   * @param numberOfSubmition Anzahl der abgegebene Zusammenfassungen.
   */
  public OrgaEvent(final Event event, final int numberOfStudent, final int numberOfSubmition) {
    id = event.getId();
    title = event.getTitle();
    date = event.getDate();
    status = event.getStatus();
    this.numberOfStudent = numberOfStudent;
    this.numberOfSubmition = numberOfSubmition;
    submissionDeadline = event.getDeadline();
  }
}
