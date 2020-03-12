package mops.rheinjug2.entities;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("student")
@NoArgsConstructor
public class Student {
  @Id
  private Long id;

  private String login;
  private String name;
  private String email;

  private Set<EventRef> events = new HashSet<>();

  public Student(String login, String email) {
    this.login = login;
    this.email = email;
  }

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

  public Event findEventByEventRef(Long id) {
    return null;
  }

  /**
   * Entfernt eine Veranstaltung.
   */
  public void deleteEvent(Event event) {
    EventRef ref = findEventRef(event);
    events.remove(ref);
  }

  /**
   * Eine Zusammenfassung hinzufügen.
   */
  public void addSummary(Event event) {
    EventRef ref = findEventRef(event);
    // Frist prüfen zuerst
    if (!ref.isSubmittedSummary()) {
      ref.setSubmittedSummary(true);
      ref.setTimeOfSubmission(LocalDateTime.now());
    }
  }

  /**
   * Gibt alle IDs der Veranstaltungen mit Zusammenfassungen, die akzeptiert, aber
   * nicht für einen Scchein verwendet wurden.
   */
  public Set<Long> getEventsIdsWithSummaryAcceptedNotUsed() {
    return events.stream().filter(EventRef::isSubmittedAndAcceptedButNotUsed)
        .map(EventRef::getEvent).collect(Collectors.toSet());
  }

  /**
   * Gibt alle IDs der Veranstaltungen mit Zusammenfassungen.
   */
  public Set<Long> getEventsIdsWithSummary() {
    return events.stream().filter(EventRef::isSubmittedSummary)
        .map(EventRef::getEvent).collect(Collectors.toSet());
  }

  private EventRef findEventRef(Event event) {
    return events.stream().filter(x -> x.getEvent()
        .equals(event.getId())).findAny().get();
  }


}
