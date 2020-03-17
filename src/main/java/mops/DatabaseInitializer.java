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
import mops.rheinjug2.entities.Student;
import mops.rheinjug2.repositories.EventRepository;
import mops.rheinjug2.repositories.StudentRepository;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements ServletContextInitializer {
  transient Random random = new Random();
  transient LocalDate date1 = LocalDate.of(2020, 01, 01);
  transient LocalDate date2 = LocalDate.of(2020, 05, 01);
  transient LocalDateTime dateNow = LocalDateTime.now();

  transient EventRepository eventRepository;
  transient StudentRepository studentRepository;


  public DatabaseInitializer(
      final EventRepository eventRepository, final StudentRepository studentRepository) {
    this.eventRepository = eventRepository;
    this.studentRepository = studentRepository;
  }

  @Override
  public void onStartup(final ServletContext servletContext) throws ServletException {
    final Faker faker = new Faker(Locale.GERMAN);
    //fakeEvent(faker);
    fakeStudent(faker);
  }


  private void fakeStudent(final Faker faker) {
    IntStream.range(0, 30).forEach(value -> {
      final Student student = new Student();
      student.setLogin(faker.name().firstName() + faker.number().digits(3));
      student.setEmail(faker.internet().emailAddress());
      student.setName(faker.name().firstName());
      studentRepository.save(student);
    });
  }

  private void fakeEvent(final Faker faker) {
    IntStream.range(0, 30).forEach(value -> {
      final Event event = new Event();
      event.setTitle(faker.job().title());
      event.setDescription(faker.yoda().quote());
      event.setPrice(faker.number().randomDigit());
      event.setDate(new java.sql.Timestamp(
          faker.date().between(java.sql.Date.valueOf(date1), java.sql.Date.valueOf(date2))
              .getTime()).toLocalDateTime());
      event.setAddress(faker.address().fullAddress());
      event.setUrl(faker.internet().url());
      if (event.getDate().isBefore(dateNow)) {
        event.setStatus("UPCOMING");
      } else {
        event.setStatus("PAST");
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

