package mops.rheinjug2.meetupcom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeAll;
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
@ContextConfiguration(classes = {MeetupComConfiguration.class, MeetupCom.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Testing api.meetup.com")
class MeetupComTests {

  @Autowired
  private transient RestTemplate restTemplate;
  @Autowired
  private transient MeetupCom meetupcom;

  private transient MockRestServiceServer mockServer;

  @BeforeAll
  public void init() {
    mockServer = MockRestServiceServer.createServer(restTemplate);
  }

  @Test
  @DisplayName("a JSON string converts correctly to a MeetupCom.Event object")
  public void givenMockingIsDoneByMockRestServiceServer_whenGetIsCalled_thenReturnsMockedObject()
      throws URISyntaxException {
    mockServer.expect(ExpectedCount.once(),
        requestTo(new URI("http://api.meetup.com/rheinjug/events?no_earlier_than=1970-01-01T00:00:00.000&status=past,upcoming&desc=true")))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body("[{\n"
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
                + "]\n"));

    final var time0 = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
    final var events = meetupcom.getRheinJugEventsSince(time0);
    mockServer.verify();

    assertNotNull(events, "events is null");
    assertEquals(events.size(), 1, "events size isn't 1");
    final var event = events.get(0);
    // only check properties we are interested in
    assertEquals(event.getId(), "269005066",
        "Id isn't as expected");
    assertEquals(event.getName(), "EntwickelBar 6.0",
        "Name isn't as expected");
    assertEquals(event.getTime(), Instant.ofEpochMilli(1599895800000L),
        "Time isn't as expected");
    assertEquals(event.getDuration(), Duration.ofMillis(27000000L),
        "Duration isn't as expected");
  }
}
