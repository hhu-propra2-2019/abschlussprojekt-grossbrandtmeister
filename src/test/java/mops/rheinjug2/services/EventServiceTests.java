package mops.rheinjug2.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.repositories.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EventServiceTests {

  @Spy
  @InjectMocks
  EventService eventService;

  @Mock
  EventRepository eventRepository;

  /**
   * Setzt ein Event mit ungültigem Status auf.
   */
  @BeforeEach
  public void setUp() {
    final Event event = new Event();
    event.setTitle("Test Event");
    event.setStatus("UPCOMING");
    event.setDate(LocalDateTime.now(ZoneId.of("Europe/Berlin")));

    final List<Event> events = new ArrayList<>();
    events.add(event);

    when(eventRepository.findEventsByStatus(anyString())).thenReturn(events);
    when(eventRepository.save(any(Event.class)))
        .thenAnswer(invocation -> invocation.getArguments()[0]);
  }

  @Test
  public void test() {
    eventService.updateStatusOfPastEvents();
  }

}
