package mops.rheinjug2;

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
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DatabaseInitializer implements ServletContextInitializer {
  transient Random random = new Random();
  transient LocalDate date1 = LocalDate.of(2020, 01, 01);
  transient LocalDate date2 = LocalDate.of(2020, 05, 01);
  transient LocalDateTime dateNow = LocalDateTime.now();

  transient EventRepository eventRepository;
  transient StudentRepository studentRepository;
  transient ModelService modelService;


  /**
   * DB mit daten zum Tseten füllen.
   *
   * @param eventRepository   event Repo
   * @param studentRepository student Repo
   * @param modelService      model service
   */
  public DatabaseInitializer(EventRepository eventRepository,
                             StudentRepository studentRepository,
                             ModelService modelService) {
    this.eventRepository = eventRepository;
    this.studentRepository = studentRepository;
    this.modelService = modelService;
  }

  @Override
  public void onStartup(ServletContext servletContext) throws ServletException {
    Faker faker = new Faker(Locale.GERMAN);
    fakeEvent(faker);
    fakeStudent(faker);
    fakerEventRef(faker);
  }

  private void fakerEventRef(Faker faker) {
    studentRepository.findAll().forEach(student -> {
      Long eventid = (long) faker.number().numberBetween(1, 30);
      modelService.addStudentToEvent(student.getLogin(), student.getEmail(), eventid);
      if (random.nextBoolean()) {
        modelService.submitSummary(student.getLogin(), eventid, faker.internet().url());
      }
    });
  }


  private void fakeStudent(Faker faker) {
    IntStream.range(0, 30).forEach(value -> {
      Student student = new Student(faker.name().firstName() + faker.number().digits(3),
          faker.internet().emailAddress());
      student.setName(faker.name().firstName());
      studentRepository.save(student);
    });
  }

  private void fakeEvent(Faker faker) {
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

