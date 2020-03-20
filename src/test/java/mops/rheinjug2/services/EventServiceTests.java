package mops.rheinjug2.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.meetupcom.MeetupCom;
import mops.rheinjug2.meetupcom.Venue;
import mops.rheinjug2.repositories.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testing EventService")
public class EventServiceTests {

  @Mock
  private transient mops.rheinjug2.meetupcom.Event meetupEvent;

  @Mock
  private transient Venue venue;

  @Mock
  private transient MeetupCom meetupComService;

  @Mock
  private transient EventRepository eventRepository;

  @InjectMocks
  private transient EventService eventService;

  @Test
  public void refreshEventsTest() {
    // Fake data for the event we get from our meetupComService
    when(meetupEvent.getId()).thenReturn("1234"); // To check if repository is called with this
    when(meetupEvent.getName()).thenReturn("Test Event");
    when(meetupEvent.getTime()).thenReturn(Instant.ofEpochMilli(0));
    when(meetupEvent.getUtcOffset()).thenReturn(Duration.ofMillis(0));
    when(meetupEvent.getVenue()).thenReturn(venue);
    when(meetupEvent.getStatus()).thenReturn(mops.rheinjug2.meetupcom.Event.Status.PAST);

    // Add that fake Event to a List that gets returned by out meetupComService
    final List<mops.rheinjug2.meetupcom.Event> eventList = new ArrayList<>();
    eventList.add(meetupEvent);
    when(meetupComService.getRheinJugEventsSince(any(LocalDateTime.class))).thenReturn(eventList);

    eventService.refreshRheinjugEvents(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC));
    verify(meetupComService, times(1))
        .getRheinJugEventsSince(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC));
    verify(eventRepository, times(1)).findEventByMeetupId("1234");
    verify(eventRepository, times(1)).save(any(Event.class));
    verify(eventRepository, times(1)).findEventsByStatus("UPCOMING");
  }

  @Test
  public void updateEventStatusTest() {
    // Create Event with test data and invalid status
    final Event testEvent = new Event();
    testEvent.setMeetupId("1234");
    testEvent.setTitle("Test Event");
    testEvent.setDate(LocalDateTime.now(ZoneId.of("Europe/Berlin")));
    testEvent.setStatus("UPCOMING");

    // Add that test Event to list that gets returned by our repository
    final List<Event> eventList = new ArrayList<>();
    eventList.add(testEvent);
    when(eventRepository.findEventsByStatus(anyString())).thenReturn(eventList);

    final ArgumentCaptor<Event> argumentCaptor = ArgumentCaptor.forClass(Event.class);
    eventService.updateStatusOfPastEvents();
    verify(eventRepository, times(1)).findEventsByStatus("UPCOMING");
    verify(eventRepository, times(1)).save(argumentCaptor.capture());
    final Event capturedEvent = argumentCaptor.getValue();
    assertEquals("PAST", capturedEvent.getStatus());
  }

}
