package mops.rheinjug2.entities;


import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("event")
@EqualsAndHashCode(exclude = {"date", "duration", "deadline"})
public class Event {
  @Id
  private Long id;
  private String meetupId;

  private String title;
  private String description;
  private double price;
  private LocalDateTime date;
  private Duration duration;
  private LocalDateTime deadline;
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
    return LocalDateTime.now().isBefore(deadline);
  }

  /**
   * Gibt an, ob eine Veranstaltung ansteht.
   */
  public boolean isUpcoming() {
    return getStatus().equalsIgnoreCase("Upcoming");
  }

  public String getEndDate() {
    final LocalDateTime endDateTime = date.plus(duration);
    return endDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
  }

  public String getEndTime() {
    final LocalDateTime endDateTime = date.plus(duration);
    return endDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
  }

  public String getDeadlineDate() {
    final LocalDateTime afterOneWeek = date.plusDays(7);
    return afterOneWeek.toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
  }

  public String getDeadlineTime() {
    final LocalDateTime afterOneWeek = date.plusDays(7);
    return afterOneWeek.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
  }

}


