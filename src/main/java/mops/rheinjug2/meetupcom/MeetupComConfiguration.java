package mops.rheinjug2.meetupcom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MeetupComConfiguration {
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
