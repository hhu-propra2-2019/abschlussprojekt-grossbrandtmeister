package mops.rheinjug2.meetupcom;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Fee {

  @JsonProperty("accepts")
  private String accepts;
  @JsonProperty("amount")
  private Double amount;
  @JsonProperty("currency")
  private String currency;
  @JsonProperty("description")
  private String description;
  @JsonProperty("label")
  private String label;
  @JsonProperty("required")
  private Boolean required;

}
