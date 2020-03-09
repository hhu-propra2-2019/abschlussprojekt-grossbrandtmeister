package mops.rheinjug2.entities;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

@Table("student_event")
@Data
class EventRef {

  private Long event;

  EventRef(Long event) {
    this.event = event;
  }
}
