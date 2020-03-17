package mops.rheinjug2.meetupcom;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public final class MeetupCom {
  private RestTemplate restTemplate;

  @Autowired
  public MeetupCom(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  /**
   * Holt die Liste der RheinJUG-Veranstaltungen von api.meetup.com.
   */
  List<Event> getRheinJugEventsSince(LocalDateTime localDateTime) {
    String dateIso = asIso8601String(localDateTime);

    var url = String.format("http://api.meetup.com/rheinjug/events?no_earlier_than=%s&status=past,upcoming&desc=true", dateIso);

    ResponseEntity<Event[]> response = restTemplate.getForEntity(url, Event[].class);
    return Arrays.asList(Objects.requireNonNull(response.getBody()));
  }

  /**
   * Konvertiert ein Date-Objekt in ein String
   * im ISO 8601 format("2019-06-01T00:00:00.000")
   */
  private static String asIso8601String(LocalDateTime localDateTime) {
    var dtf = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        .withLocale(Locale.ROOT)
        .withZone(ZoneId.of("UTC"));

    return dtf.format(localDateTime);
  }

}
