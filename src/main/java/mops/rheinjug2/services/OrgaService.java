package mops.rheinjug2.services;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import mops.rheinjug2.model.OrgaEvent;
import mops.rheinjug2.repositories.EventRepository;
import mops.rheinjug2.repositories.StudentRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrgaService {
  EventRepository eventRepository;
  StudentRepository studentRepository;

  /**
   * Gibt alle events zurück.
   *
   * @return Liste alle events
   */
  public List<OrgaEvent> getEvents() {
    List<OrgaEvent> result = new ArrayList<>();
    eventRepository.findAll().forEach(event -> result.add(new OrgaEvent(event,
        getNumberOfStudent(event.getId()),
        getnumberOfSubmition(event.getId())
    )));
    return result;
  }

  /**
   * Gibt Anzahl der abgegebene Zusammenfassungen eiener Veranstaltung.
   *
   * @param id einer Veranstaltung
   * @return Anzahl der Abgegebene Zusammenfassungen
   */
  private int getnumberOfSubmition(Long id) {
    return eventRepository.countStudentsPerEventById(id);
  }

  /**
   * Gibt der Anzahl der angemeldte Stundenten einer Veranstaltung.
   *
   * @param id einer Veranstaltung
   * @return Anzahl der Studenten
   */
  private int getNumberOfStudent(Long id) {
    return eventRepository.countSubmittedSummaryPerEventById(id);
  }
}
