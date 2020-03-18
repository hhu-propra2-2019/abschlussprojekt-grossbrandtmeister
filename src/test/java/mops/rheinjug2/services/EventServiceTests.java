package mops.rheinjug2.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.repositories.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class})
//@ContextConfiguration(classes = {MeetupCom.class, MeetupComConfiguration.class})
@DisplayName("Testing EventService")
public class EventServiceTests {

  @Mock
  private transient EventRepository eventRepository;

  @InjectMocks
  private transient EventService eventService;

  /**
   * Setzt ein Event mit ungültigem Status auf.
   */
  @BeforeEach
  public void setUp() {
    final Event testEvent = new Event();
    testEvent.setMeetupId("1234");
    testEvent.setTitle("Test Event");
    testEvent.setDate(LocalDateTime.now(ZoneId.of("Europe/Berlin")));
    testEvent.setStatus("UPCOMING");

    final List<Event> eventList = new ArrayList<>();
    eventList.add(testEvent);

    when(eventRepository.findEventsByStatus(anyString())).thenReturn(eventList);
  }

  @Test
  public void updateEventStatusTest() {
    final ArgumentCaptor<Event> argumentCaptor = ArgumentCaptor.forClass(Event.class);
    eventService.updateStatusOfPastEvents();
    verify(eventRepository, times(1)).findEventsByStatus("UPCOMING");
    verify(eventRepository, times(1)).save(argumentCaptor.capture());
    final Event capturedEvent = argumentCaptor.getValue();
    assertEquals("PAST", capturedEvent.getStatus());
  }

}
