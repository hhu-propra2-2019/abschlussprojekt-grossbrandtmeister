package mops.rheinjug2.entities;


import java.time.LocalDateTime;
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

  private String title;
  private String description;
  private double price;
  private LocalDateTime date;
  private String address;
  private String url;

  private String status;
  private String type;

  @Override
  public String toString() {
    return "Event{" + "id=" + id + ", title='" + title + '\'' + '}';
  }

  public boolean isOpenForSubmission() {
    LocalDateTime afterOneWeek = date.plusDays(7);
    return LocalDateTime.now().isBefore(afterOneWeek);
  }

  public boolean isUpcoming() {
    return date.isAfter(LocalDateTime.now());
  }
}
