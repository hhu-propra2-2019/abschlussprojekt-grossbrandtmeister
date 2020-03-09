package mops.rheinjug2.entities;


import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("event")
public class Event {
  @Id
  private Long id;

  private String title;
  private String description;
  private double price;
  private LocalTime time;
  private LocalDate date;
  private String address;
  private String url;

  private String status;
  private String type;

  @Override
  public String toString() {
    return "Event{" + "id=" + id + ", title='" + title + '\'' + '}';
  }
}
