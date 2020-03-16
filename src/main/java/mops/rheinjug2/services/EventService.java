package mops.rheinjug2.services;

import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import mops.rheinjug2.meetupcom.Event;
import mops.rheinjug2.meetupcom.MeetupCom;
import mops.rheinjug2.repositories.EventRepository;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class EventService {

  private final transient MeetupCom meetupComService;
  private final transient EventRepository eventRepository;

  public EventService(final MeetupCom meetupComService, final EventRepository eventRepository) {
    this.meetupComService = meetupComService;
    this.eventRepository = eventRepository;
  }

  /**
   * Ruft Events von meetup.com ab und speichert diese in der Datenbank
   */
  public void refreshRheinjugEvents(final LocalDateTime time) {
    mops.rheinjug2.entities.Event eventEntity;
    final List<Event> meetupEvents = meetupComService.getRheinJugEventsSince(time);
    log.info("Fetched " + meetupEvents.size() + " events from meetup.com");
    for (final Event event : meetupEvents) {
      eventEntity = eventRepository.findEventByMeetupId(Long.parseLong(event.getId()));
      eventRepository.save(ModelConverter.parseMeetupEvent(event, eventEntity));
    }
  }

}
