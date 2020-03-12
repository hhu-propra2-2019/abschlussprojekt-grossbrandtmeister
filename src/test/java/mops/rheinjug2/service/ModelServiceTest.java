package mops.rheinjug2.service;

import java.util.Arrays;
import java.util.List;
import mops.rheinjug2.ModelService;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.repositories.EventRepository;
import mops.rheinjug2.repositories.StudentRepository;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@DataJdbcTest
public class ModelServiceTest {

  @Autowired
  EventRepository eventRepository;
  @Autowired
  StudentRepository studentRepository;


  @Test
  public void testGetAllEvents() {
    ModelService modelService = new ModelService(studentRepository, eventRepository);
    Event event1 = createAndSaveEvent("Event 1");
    Event event2 = createAndSaveEvent("Event 2");

    List<Event> events = Arrays.asList(event1, event2);

    Assert.assertEquals(events, modelService.getAllEvents());
  }

  public Event createAndSaveEvent(String title) {
    Event event = new Event();
    event.setTitle(title);
    eventRepository.save(event);
    return event;
  }


}
