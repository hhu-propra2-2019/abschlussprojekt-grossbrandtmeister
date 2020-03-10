package mops;

import mops.rheinjug2.entities.Event;
import mops.rheinjug2.entities.Student;
import mops.rheinjug2.repositories.EventRepository;
import mops.rheinjug2.repositories.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Rheinjug2 {

  public static void main(String[] args) {
    SpringApplication.run(Rheinjug2.class, args);
  }

  @Bean
  public CommandLineRunner loadData(EventRepository repository, StudentRepository studentRepository) {
    return (args) -> {
      Event event = new Event();
      event.setTitle("Vers");
      repository.save(event);
      Student s = new Student();
      s.setName("Lo Lo");
      s.setEmail("nid@hhu.de");
      s.addEvent(event);
      studentRepository.save(s);
      s.addSummary(event);
      studentRepository.save(s);
    };
  }


}
