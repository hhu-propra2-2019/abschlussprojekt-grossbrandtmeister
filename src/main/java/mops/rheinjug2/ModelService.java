package mops.rheinjug2;


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
}
