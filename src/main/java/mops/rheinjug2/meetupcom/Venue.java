package mops.rheinjug2.meetupcom;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Venue {

  @JsonProperty("id")
  private Long id;
  @JsonProperty("name")
  private String name;
  @JsonProperty("lat")
  private Double lat;
  @JsonProperty("lon")
  private Double lon;
  @JsonProperty("repinned")
  private Boolean repinned;
  @JsonProperty("address_1")
  private String address1;
  @JsonProperty("city")
  private String city;
  @JsonProperty("country")
  private String country;
  @JsonProperty("localized_country_name")
  private String localizedCountryName;

}
