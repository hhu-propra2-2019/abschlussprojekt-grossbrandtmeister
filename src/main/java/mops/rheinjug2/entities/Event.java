package mops.rheinjug2.entities;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("event")
@EqualsAndHashCode(exclude = {"date"})
public class Event {
  @Id
  private Long id;
  private String meetupId;

  private String title;
  private String description;
  private double price;
  private LocalDateTime date;
  private String address;
  private String venue;
  private String url;
  private String status;
  private String type;

  @Override
  public String toString() {
    return "Event{" + "id=" + id + ", title='" + title + '\'' + '}';
  }

  /**
   * Gibt an, ob für eine Veranstaltung Zusammenfassungen abgegeben
   * werden können.
   */
  public boolean isOpenForSubmission() {
    LocalDateTime afterOneWeek = date.plusDays(7);
    return LocalDateTime.now().isBefore(afterOneWeek);
  }

  /**
   * Gibt an, ob eine Veranstaltung ansteht.
   */
  public boolean isUpcoming() {
    return this.getStatus().equalsIgnoreCase("Upcoming");
  }

  public String printTime(){
    return date.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
  }

  public String printDate(){
    return date.toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
  }
}


