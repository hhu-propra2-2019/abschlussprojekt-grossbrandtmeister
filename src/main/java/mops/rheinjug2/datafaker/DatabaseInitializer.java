package mops.rheinjug2.datafaker;

import com.github.javafaker.Faker;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.stream.IntStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements ServletContextInitializer {
  Random random = new Random();
  Date date1 = new Date(120, 02, 12);
  Date date2 = new Date(120, 04, 12);
  LocalDateTime dateNow = LocalDateTime.now();

  @Autowired
  EventRepository eventRepository;

  @Override
  public void onStartup(ServletContext servletContext) throws ServletException {
    Faker faker = new Faker(Locale.GERMAN);
    IntStream.range(0, 30).forEach(value -> {
      Event event = new Event();
      event.setTitle(faker.job().title());
      event.setDescription(faker.yoda().quote());
      event.setPrice(faker.number().randomDigit());
      event.setDate(new java.sql.Timestamp(
          faker.date().between(date1, date2).getTime()).toLocalDateTime());
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
