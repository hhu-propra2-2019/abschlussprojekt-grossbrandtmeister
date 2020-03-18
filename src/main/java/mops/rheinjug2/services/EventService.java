package mops.rheinjug2.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import mops.rheinjug2.meetupcom.Event;
import mops.rheinjug2.meetupcom.MeetupCom;
import mops.rheinjug2.repositories.EventRepository;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class EventService {

  private final transient MeetupCom meetupComService;
  private final transient EventRepository eventRepository;

  public EventService(final MeetupCom meetupComService, final EventRepository eventRepository) {
    this.meetupComService = meetupComService;
    this.eventRepository = eventRepository;
  }

  /**
   * Ruft einmalig alle Events von meetup.com ab, wenn unsere Anwendung startet.
   */
  @PostConstruct
  public void getAllEvents() {
    log.info("Get all available rheinjug events");
    refreshRheinjugEvents(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC));
  }

  /**
   * Ruft Events von meetup.com ab und speichert diese in der Datenbank
   */
  public void refreshRheinjugEvents(final LocalDateTime time) {
    try {
      mops.rheinjug2.entities.Event eventEntity;
      final List<Event> meetupEvents = meetupComService.getRheinJugEventsSince(time);
      log.info("Fetched " + meetupEvents.size() + " events from meetup.com");
      for (final Event event : meetupEvents) {
        eventEntity = eventRepository.findEventByMeetupId(event.getId());
        eventRepository.save(ModelConverter.parseMeetupEvent(event, eventEntity));
      }
      updateStatusOfPastEvents();
    } catch (final Exception e) {
      log.error("Could not get fetch events from meetup.com. " + e.getMessage());
    }
  }

  /**
   * Prüft ob UPCOMING Events bereits vorbei sind und setzt diese auf PAST.
   */
  public void updateStatusOfPastEvents() {
    int invalidStatusCount = 0;
    final List<mops.rheinjug2.entities.Event> upcomingEvents =
        eventRepository.findEventsByStatus("UPCOMING");

    for (final mops.rheinjug2.entities.Event event : upcomingEvents) {
      if (event.getDate().isBefore(LocalDateTime.now(ZoneId.of("Europe/Berlin")))) {
        invalidStatusCount++;
        log.debug("Event '" + event.getTitle() + "' has invalid status");
        event.setStatus("PAST");
        eventRepository.save(event);
      }
    }
    log.info("Fixed " + invalidStatusCount + " Events with invalid status");
  }

}
