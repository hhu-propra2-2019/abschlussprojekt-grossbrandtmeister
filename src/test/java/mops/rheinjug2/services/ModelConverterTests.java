package mops.rheinjug2.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import mops.rheinjug2.meetupcom.Event;
import mops.rheinjug2.meetupcom.MeetupCom;
import mops.rheinjug2.meetupcom.MeetupComConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MeetupCom.class, MeetupComConfiguration.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Testing conversion from meetup event to event entity")
public class ModelConverterTests {

  public static final String SINGLE_EVENT_IN_ARRAY_JSON = "[{\n"
      + "        \"created\": 1582726842000,\n"
      + "        \"duration\": 27000000,\n"
      + "        \"fee\": {\n"
      + "            \"accepts\": \"paypal\",\n"
      + "            \"amount\": 5.0,\n"
      + "            \"currency\": \"EUR\",\n"
      + "            \"description\": \"\",\n"
      + "            \"label\": \"Price\",\n"
      + "            \"required\": true\n"
      + "        },\n"
      + "        \"id\": \"269005066\",\n"
      + "        \"name\": \"EntwickelBar 6.0\",\n"
      + "        \"date_in_series_pattern\": false,\n"
      + "        \"status\": \"upcoming\",\n"
      + "        \"time\": 1599895800000,\n"
      + "        \"local_date\": \"2020-09-12\",\n"
      + "        \"local_time\": \"09:30\",\n"
      + "        \"updated\": 1582727160000,\n"
      + "        \"utc_offset\": 7200000,\n"
      + "        \"waitlist_count\": 0,\n"
      + "        \"yes_rsvp_count\": 7,\n"
      + "        \"venue\": {\n"
      + "            \"id\": 25588589,\n"
      + "            \"name\": \"Universität Düsseldorf, Gebäude 25.22 U1\",\n"
      + "            \"lat\": 0.0,\n"
      + "            \"lon\": 0.0,\n"
      + "            \"repinned\": false,\n"
      + "            \"address_1\": \"Universitätsstr. 1\",\n"
      + "            \"city\": \"Düsseldorf\",\n"
      + "            \"country\": \"de\",\n"
      + "            \"localized_country_name\": \"Germany\"\n"
      + "        },\n"
      + "        \"group\": {\n"
      + "            \"created\": 1474533027000,\n"
      + "            \"name\": \"rheinJUG\",\n"
      + "            \"id\": 20453884,\n"
      + "            \"join_mode\": \"open\",\n"
      + "            \"lat\": 51.2400016784668,\n"
      + "            \"lon\": 6.789999961853027,\n"
      + "            \"urlname\": \"rheinJUG\",\n"
      + "            \"who\": \"Mitglieder\",\n"
      + "            \"localized_location\": \"Düsseldorf, Germany\",\n"
      + "            \"state\": \"\",\n"
      + "            \"country\": \"de\",\n"
      + "            \"region\": \"en_US\",\n"
      + "            \"timezone\": \"Europe/Berlin\"\n"
      + "        },\n"
      + "        \"link\": \"https://www.meetup.com/rheinJUG/events/269005066/\",\n"
      + "        \"description\": \"<p>EntwickelBar ist eine Unconference...<a href=\\\"https://entwickelbar.github.io\\\" class=\\\"linkified\\\">https://entwickelbar.github.io</a></p> \",\n"
      + "        \"how_to_find_us\": \"Leider hat der gesamte Universitätscampus nur eine Adresse. Unter https://entwickelbar.github.io/wegbeschreibung.html findest du eine Wegbeschreibung \",\n"
      + "        \"visibility\": \"public\",\n"
      + "        \"member_pay_fee\": false\n"
      + "    }\n"
      + "]\n";
  @Autowired
  private transient RestTemplate restTemplate;

  @Autowired
  private transient MeetupCom meetupComService;

  private transient MockRestServiceServer mockServer;

  /**
   * Erstellt eine mock Rückgabe von Meetup.com in Form eines JSON.
   */
  @BeforeEach
  public void setUp() throws URISyntaxException {
    mockServer = MockRestServiceServer.createServer(restTemplate);
    mockServer.expect(ExpectedCount.once(),
        requestTo(new URI("http://api.meetup.com/rheinjug/events?no_earlier_than=1970-01-01T00:00:00.000&status=past,upcoming&desc=true")))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(SINGLE_EVENT_IN_ARRAY_JSON));
  }

  /**
   * Teste ModelConverter mit einer leeren Event Entity.
   * Erwarte ID = null und Werte wie im Mock oben.
   */
  @Test
  public void convertIntoNewEventEntity() {
    final LocalDateTime time0 = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
    final List<Event> events = meetupComService.getRheinJugEventsSince(time0);
    mockServer.verify();

    final Event meetupEvent = events.get(0);
    final mops.rheinjug2.entities.Event eventEntity
        = ModelConverter.parseMeetupEvent(meetupEvent, null);

    assertNull(eventEntity.getId());
    assertEquals("269005066", eventEntity.getMeetupId());
    assertEquals("EntwickelBar 6.0", eventEntity.getTitle());
    assertEquals("<p>EntwickelBar ist eine Unconference...<a href=\"https://entwickelbar.github.io\" class=\"linkified\">https://entwickelbar.github.io</a></p> ", eventEntity.getDescription());
    assertEquals(5.0, eventEntity.getPrice());
    assertEquals(LocalDateTime.of(2020, 9, 12, 9, 30),
        eventEntity.getDate());
    assertEquals(Duration.parse("PT7H30M"), eventEntity.getDuration()); //ISO-8601 format
    assertEquals(LocalDateTime.of(2020, 9, 19, 17, 0),
        eventEntity.getDeadline());
    assertEquals("Universitätsstr. 1, Düsseldorf", eventEntity.getAddress());
    assertEquals("Universität Düsseldorf, Gebäude 25.22 U1", eventEntity.getVenue());
    assertEquals("https://www.meetup.com/rheinJUG/events/269005066/", eventEntity.getUrl());
    assertEquals("UPCOMING", eventEntity.getStatus());
    assertEquals("EntwickelBar", eventEntity.getType());
  }

  /**
   * Teste ModelConverter mit vorhandener Event Entity.
   * Erwarte ID wie eingestellt (34) und Werte wie im Mock oben.
   */
  @Test
  public void convertIntoExistingEventEntity() {
    final LocalDateTime time0 = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
    final List<Event> events = meetupComService.getRheinJugEventsSince(time0);
    mockServer.verify();

    final Event meetupEvent = events.get(0);
    mops.rheinjug2.entities.Event eventEntity = new mops.rheinjug2.entities.Event();
    eventEntity.setId(34L);
    eventEntity = ModelConverter.parseMeetupEvent(meetupEvent, eventEntity);

    assertEquals(34, eventEntity.getId());
    assertEquals("269005066", eventEntity.getMeetupId());
    assertEquals("EntwickelBar 6.0", eventEntity.getTitle());
    assertEquals("<p>EntwickelBar ist eine Unconference...<a href=\"https://entwickelbar.github.io\" class=\"linkified\">https://entwickelbar.github.io</a></p> ", eventEntity.getDescription());
    assertEquals(5.0, eventEntity.getPrice());
    assertEquals(LocalDateTime.of(2020, 9, 12, 9, 30),
        eventEntity.getDate());
    assertEquals(Duration.parse("PT7H30M"), eventEntity.getDuration()); //ISO-8601 format
    assertEquals(LocalDateTime.of(2020, 9, 19, 17, 0),
        eventEntity.getDeadline());
    assertEquals("Universitätsstr. 1, Düsseldorf", eventEntity.getAddress());
    assertEquals("Universität Düsseldorf, Gebäude 25.22 U1", eventEntity.getVenue());
    assertEquals("https://www.meetup.com/rheinJUG/events/269005066/", eventEntity.getUrl());
    assertEquals("UPCOMING", eventEntity.getStatus());
    assertEquals("EntwickelBar", eventEntity.getType());
  }

}
