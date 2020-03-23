package mops.rheinjug2;

import com.github.javafaker.Faker;
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
import mops.rheinjug2.services.ModelService;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DatabaseInitializer implements ServletContextInitializer {
  transient Random random = new Random();
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
  public DatabaseInitializer(final EventRepository eventRepository,
                             final StudentRepository studentRepository,
                             final ModelService modelService) {
    this.eventRepository = eventRepository;
    this.studentRepository = studentRepository;
    this.modelService = modelService;
  }

  @Override
  public void onStartup(final ServletContext servletContext) throws ServletException {
    final Faker faker = new Faker(Locale.GERMAN);
    fakeEvent(faker);
    fakeStudent(faker);
    fakeEventRef(faker);
  }

  private void fakeEventRef(final Faker faker) {
    studentRepository.findAll().forEach(student -> {
      final Long eventid = (long) faker.number().numberBetween(1, 30);
      modelService.addStudentToEvent(student.getLogin(), student.getEmail(), eventid);
      if (random.nextBoolean()) {
        modelService.submitSummary(student.getLogin(), eventid, faker.internet().url());
      }
    });
  }


  private void fakeStudent(final Faker faker) {
    IntStream.range(0, 30).forEach(value -> {
      final Student student = new Student(faker.name().firstName() + faker.number().digits(3),
          faker.internet().emailAddress());
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
      event.setDate(LocalDateTime.of(2020,
          faker.number().numberBetween(2, 5),
          faker.number().numberBetween(1, 28),
          faker.number().numberBetween(16, 20),
          faker.number().numberBetween(1, 60)));
      event.setAddress(faker.address().fullAddress());
      event.setUrl(faker.internet().url());
      event.setVenue("Universität Düsseldorf, Gebäude 25.22 U1");
      if (event.getDate().isAfter(dateNow)) {
        event.setStatus("UPCOMING");
      } else {
        event.setStatus("PAST");
      }

      if (random.nextBoolean()) {
        event.setType("EntwickelBar");
      } else {
        event.setType("Abendveranstaltung");
      }
      eventRepository.save(event);
    });
  }
}

