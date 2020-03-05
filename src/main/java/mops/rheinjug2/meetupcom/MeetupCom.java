package mops.rheinjug2.meetupcom;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public final class MeetupCom {

  /**
   * Holt die Liste der RheinJUG-Veranstaltungen von api.meetup.com.
   */
  Event[] getRheinJugEventsSince(Calendar calendar) {
    String dateIso = asIso8601String(calendar.getTime());

    var url = String.format("http://api.meetup.com/rheinjug/events?no_earlier_than=%s&status=past,upcoming&desc=true", dateIso);

    var restTemplate = new RestTemplate();
    ResponseEntity<Event[]> response = restTemplate.getForEntity(url, Event[].class);
    return response.getBody();
  }

  /**
   * Konvertiert ein Date-Objekt in ein String
   * im ISO 8601 format("2019-06-01T00:00:00.000")
   */
  private String asIso8601String(Date date) {
    var sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ROOT);
    return sdf.format(date);
  }

}
