package mops.rheinjug2.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@EnableScheduling
@Service
@ConditionalOnProperty(prefix = "application.api-pump", name = "delay")
public class EventServiceScheduler {

  private final transient EventService eventService;

  public EventServiceScheduler(final EventService eventService) {
    this.eventService = eventService;
  }

  @PostConstruct
  public static void getAllEvents() {
    log.info("Initialized scheduler for EventService");
  }

  @Scheduled(fixedDelayString = "${application.api-pump.delay}")
  private void schedule() {
    eventService.refreshRheinjugEvents(LocalDateTime.now(ZoneId.of("Europe/Berlin")));
  }
}
