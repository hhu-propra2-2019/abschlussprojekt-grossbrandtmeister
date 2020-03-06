package mops.rheinjug2.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Summary {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne
  private Student student;

  @ManyToOne
  private Event event;

  private Boolean approved = false;
  private Boolean usedForCertificate = false;

  /**
   * Erstellt eine Zusammenfassung.
   */
  public Summary(Student student, Event event) {
    //TODO: ein Student darf zu einer Veranstaltung maximal eine Summary abgeben
    this.student = student;
    this.event = event;
    student.getSummaries().add(this);
    event.getSummaries().add(this);
  }
}
