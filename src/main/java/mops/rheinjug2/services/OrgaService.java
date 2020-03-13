package mops.rheinjug2.services;

import java.util.List;
import lombok.AllArgsConstructor;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.repositories.EventRepository;
import mops.rheinjug2.repositories.StudentRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrgaService {
  EventRepository eventRepository;
  StudentRepository studentRepository;

  /**
   * @return
   */
  public List<Event> getEvents() {
    return eventRepository.getAllEvents();
  }
}
