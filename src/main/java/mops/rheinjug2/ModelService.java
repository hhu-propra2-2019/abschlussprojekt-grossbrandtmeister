package mops.rheinjug2;


import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.repositories.EventRepository;
import mops.rheinjug2.repositories.StudentRepository;
import org.springframework.stereotype.Service;

@Service
public class ModelService {
  private final StudentRepository studentRepository;
  private final EventRepository eventRepository;

  public ModelService(StudentRepository studentRepository, EventRepository eventRepository) {
    this.studentRepository = studentRepository;
    this.eventRepository = eventRepository;
  }

  public List<Event> getAllEvents() {
    return StreamSupport.stream(eventRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
  }

  private boolean studentIsPersisted(String login) {
    return studentRepository.existsByLogin(login);
  }

}
