package mops.rheinjug2.entities;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import lombok.Data;

@Data
@Entity
public class Event {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private String title;
  private String description;
  private Double price;
  private LocalTime localTime;
  private LocalDate localDate;
  private URL url;
  private Duration duration;

  @Enumerated(EnumType.ORDINAL)
  private Status status;

  @Enumerated(EnumType.ORDINAL)
  private Type typ;

  private String address;

  @ManyToMany(mappedBy = "events", fetch = FetchType.EAGER)
  private final Set<Student> students = new HashSet<>();


  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "event_id")
  private final List<Summary> summaries = new ArrayList<>();

  public void addStudent(Student student) {
    students.add(student);
    student.getEvents().add(this);
  }

  public void removeStudent(Student student) {
    students.remove(student);
    student.getEvents().remove(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Event event = (Event) o;
    return id.equals(event.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  public enum Status {
    CANCELLED,
    UPCOMING,
    PAST,
    PROPOSED,
    SUGGESTED,
    DRAFT
  }

  public enum Type {
    ENTWICKELBAR,
    NORMAL
  }
}
