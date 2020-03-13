package mops.rheinjug2.entities;


import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("event")
public class Event {
  @Id
  private Long id;
  private String meetupId;

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

  public LocalDateTime getSubmissionDeadline() {
    return date.plusDays(7);
  }
}