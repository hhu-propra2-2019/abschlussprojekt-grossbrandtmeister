package mops.rheinjug2.meetupcom;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Event {

  public enum Status {
    CANCELLED,
    UPCOMING,
    PAST,
    PROPOSED,
    SUGGESTED,
    DRAFT;

    @JsonCreator
    static Status fromValue(final String value) {
      if ("cancelled".equalsIgnoreCase(value)) {
        return CANCELLED;
      } else if ("upcoming".equalsIgnoreCase(value)) {
        return UPCOMING;
      } else if ("past".equalsIgnoreCase(value)) {
        return PAST;
      } else if ("proposed".equalsIgnoreCase(value)) {
        return PROPOSED;
      } else if ("suggested".equalsIgnoreCase(value)) {
        return SUGGESTED;
      } else if ("draft".equalsIgnoreCase(value)) {
        return DRAFT;
      }

      return null;
    }
  }

  @JsonProperty("created")
  private Long created;
  @JsonProperty("duration")
  private Long duration;
  @JsonProperty("fee")
  private Fee fee;
  @JsonProperty("id")
  private String id;
  @JsonProperty("name")
  private String name;
  @JsonProperty("date_in_series_pattern")
  private Boolean dateInSeriesPattern;
  @JsonProperty("status")
  private Status status;
  @JsonProperty("time")
  private Long time;
  @JsonProperty("local_date")
  private String localDate;
  @JsonProperty("local_time")
  private String localTime;
  @JsonProperty("updated")
  private Long updated;
  @JsonProperty("utc_offset")
  private Long utcOffset;
  @JsonProperty("waitlist_count")
  private Long waitlistCount;
  @JsonProperty("yes_rsvp_count")
  private Long yesRsvpCount;
  @JsonProperty("maybe_rsvpcount")
  private Integer maybeRsvpCount;
  @JsonProperty("rsvp_limit")
  private Integer rsvpLimit;
  @JsonProperty("venue")
  private Venue venue;
  @JsonProperty("group")
  private Group group;
  @JsonProperty("link")
  private URL link;
  @JsonProperty("description")
  private String description;
  @JsonProperty("how_to_find_us")
  private String howToFindUs;
  @JsonProperty("visibility")
  private String visibility;
  @JsonProperty("member_pay_fee")
  private Boolean memberPayFee;

  public Instant getCreated() {
    return Instant.ofEpochMilli(created);
  }

  public Duration getDuration() {
    return Duration.ofMillis(duration);
  }

  public Instant getTime() {
    return Instant.ofEpochMilli(time);
  }

  public Instant getUpdated() {
    return Instant.ofEpochMilli(updated);
  }

  public Duration getUtcOffset() {
    return Duration.ofMillis(utcOffset);
  }

  public String getLink() {
    return link.toString();
  }

}
