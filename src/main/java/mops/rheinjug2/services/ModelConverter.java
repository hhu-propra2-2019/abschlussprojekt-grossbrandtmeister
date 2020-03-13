package mops.rheinjug2.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import mops.rheinjug2.meetupcom.Event;
import org.springframework.stereotype.Service;

@Service
public class ModelConverter {

  static mops.rheinjug2.entities.Event parseMeetupEvent(Event meetupEvent) {

    LocalDateTime eventTime = LocalDateTime.ofInstant(meetupEvent.getTime(), ZoneId.ofOffset(
        "UTC", ZoneOffset.ofHoursMinutes(
            meetupEvent.getUtcOffset().toHoursPart(), meetupEvent.getUtcOffset().toMinutesPart())));

    mops.rheinjug2.entities.Event event = new mops.rheinjug2.entities.Event();

    event.setMeetupId(meetupEvent.getId());
    event.setTitle(meetupEvent.getName());
    event.setDescription(meetupEvent.getDescription());
    event.setPrice(meetupEvent.getFee() == null ? 0.0 : meetupEvent.getFee().getAmount());
    event.setDate(eventTime);
    event.setAddress(meetupEvent.getVenue().getAddress1());
    event.setUrl(meetupEvent.getLink().toString());
    event.setStatus(meetupEvent.getStatus().name());
    event.setType(meetupEvent.getName().toLowerCase().contains("entwickelbar") ? "EntwickelBar" : "Normal");

    return event;
  }

}
