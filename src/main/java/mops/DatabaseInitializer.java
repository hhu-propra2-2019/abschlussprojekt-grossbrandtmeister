package mops;

import com.github.javafaker.Faker;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Random;
import java.util.stream.IntStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.repositories.EventRepository;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements ServletContextInitializer {
  transient Random random = new Random();
  transient LocalDate date1 = LocalDate.of(120, 01, 01);
  transient LocalDate date2 = LocalDate.of(120, 05, 01);
  transient LocalDateTime dateNow = LocalDateTime.now();

  final transient EventRepository eventRepository;

  public DatabaseInitializer(EventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  @Override
  public void onStartup(ServletContext servletContext) throws ServletException {
    Faker faker = new Faker(Locale.GERMAN);
    IntStream.range(0, 30).forEach(value -> {
      Event event = new Event();
      event.setTitle(faker.job().title());
      event.setDescription(faker.yoda().quote());
      event.setPrice(faker.number().randomDigit());
      event.setDate(new java.sql.Timestamp(
          faker.date().between(java.sql.Date.valueOf(date1), java.sql.Date.valueOf(date2))
              .getTime()).toLocalDateTime());
      event.setAddress(faker.address().fullAddress());
      event.setUrl(faker.internet().url());
      if (event.getDate().isBefore(dateNow)) {
        event.setStatus("PAST");
      } else {
        event.setStatus("UPCOMING");
      }

      if (random.nextBoolean()) {
        event.setType("Entwickelbar");
      } else {
        event.setType("Abendveranstaltungen");
      }
      eventRepository.save(event);
    });
  }
}

