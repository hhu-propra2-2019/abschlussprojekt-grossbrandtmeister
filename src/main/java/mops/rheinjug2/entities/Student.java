package mops.rheinjug2.entities;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("student")
public class Student {
  @Id
  private Long id;

  private String login;
  private String name;
  private String email;

  private Set<EventRef> events = new HashSet<>();

  /**
   * Event hinzufügen.
   */
  public void addEvent(Event event) {
    events.add(new EventRef(event.getId()));
  }

  /**
   * Gibt alle Id's der Veranstaltungen eines Students zurück.
   */
  public Set<Long> getEventsIds() {
    return events.stream().map(EventRef::getEvent).collect(Collectors.toSet());
  }

  /**
   * Entfernt eine Veranstaltung.
   */
  public void deleteEvent(Event event) {
    EventRef ref = findEventRef(event);
    events.remove(ref);
  }

  // Frist prüfen zuerst
  public void addSummary(Event event) {
    EventRef ref = findEventRef(event);
    if (!ref.isSubmittedSummary()) {
      ref.setSubmittedSummary(true);
      ref.setTimeSubmission(LocalTime.now());
      ref.setDateSubmission(LocalDate.now());
    }
  }

  public Set<Long> getEventsIdsWithSummaryAcceptedNotUsed() {
    return events.stream().filter(EventRef::isSubmittedAndAcceptedButNotUsed)
        .map(EventRef::getEvent).collect(Collectors.toSet());
  }

  private EventRef findEventRef(Event event) {
    return events.stream().filter(x -> x.getEvent()
        .equals(event.getId())).findAny().get();
  }

}
