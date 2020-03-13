package mops.rheinjug2.services;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import mops.rheinjug2.meetupcom.Event;
import mops.rheinjug2.meetupcom.MeetupCom;
import mops.rheinjug2.repositories.EventRepository;
import org.springframework.stereotype.Service;

@Service
public class EventService {

  private final transient MeetupCom meetupComService;
  private final transient EventRepository eventRepository;

  public EventService(MeetupCom meetupComService, EventRepository eventRepository) {
    this.meetupComService = meetupComService;
    this.eventRepository = eventRepository;
  }

  /**
   * Ruft Events von meetup.com ab und speichert diese in der Datenbank
   */
  public void refreshRheinjugEvents() {
    LocalDateTime time = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
    List<Event> meetupEvents = meetupComService.getRheinJugEventsSince(time);
    for (Event e : meetupEvents) {
      eventRepository.save(ModelConverter.parseMeetupEvent(e));
    }
  }

}
