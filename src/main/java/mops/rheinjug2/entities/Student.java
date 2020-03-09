package mops.rheinjug2.entities;


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
   * Gibt an, ob ein Student an ein irgendeiner Veranstaltung teilnimmt.
   */
  public boolean takesPartInEvents() {
    return !events.isEmpty();
  }

  /**
   * Entfernt eine Veranstaltung.
   */
  public void deleteEvent(Event event) {
    events.stream()
        .filter(x -> x.getEvent()
            .equals(event.getId())).findAny().ifPresent(eventRef -> events.remove(eventRef));
  }

}
