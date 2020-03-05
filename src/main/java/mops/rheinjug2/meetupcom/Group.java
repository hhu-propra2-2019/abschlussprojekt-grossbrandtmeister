package mops.rheinjug2.meetupcom;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Group {

  @JsonProperty("created")
  private Long created;
  @JsonProperty("name")
  private String name;
  @JsonProperty("id")
  private Long id;
  @JsonProperty("join_mode")
  private String joinMode;
  @JsonProperty("lat")
  private Double lat;
  @JsonProperty("lon")
  private Double lon;
  @JsonProperty("urlname")
  private String urlname;
  @JsonProperty("who")
  private String who;
  @JsonProperty("localized_location")
  private String localizedLocation;
  @JsonProperty("state")
  private String state;
  @JsonProperty("country")
  private String country;
  @JsonProperty("region")
  private String region;
  @JsonProperty("timezone")
  private String timezone;

}
