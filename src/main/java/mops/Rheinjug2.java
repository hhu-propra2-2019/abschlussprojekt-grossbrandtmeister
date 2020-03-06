package mops;

import mops.rheinjug2.entities.Event;
import mops.rheinjug2.entities.Student;
import mops.rheinjug2.entities.Summary;
import mops.rheinjug2.repositories.EventRepository;
import mops.rheinjug2.repositories.StudentRepository;
import mops.rheinjug2.repositories.SummaryRepository;
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
  public CommandLineRunner loadData(EventRepository repository, StudentRepository studentRepository,
                                    SummaryRepository summaryRepository) {
    return (args) -> {
      Event event = new Event();
      event.setTitle("Veranstaltung");
      repository.save(event);

      Student student = new Student();
      student.setName("Bla Ney");
      student.setEmail("jkl@hhu.de");
      studentRepository.save(student);

      event.addStudent(student);// als Transaktion!!
      studentRepository.save(student);
      repository.save(event);

      Summary summary = new Summary(student, event);
      summaryRepository.save(summary);

      Student student2 = new Student();
      student2.setName("Jo Jo");
      student2.setEmail("dehj@hhu.de");
      studentRepository.save(student2);

      event.addStudent(student2);
      repository.save(event);
      studentRepository.save(student2);

      Summary summary2 = new Summary(student2, event);
      summaryRepository.save(summary2);

      Event event2 = new Event();
      event2.setTitle("Veranstaltung2");
      repository.save(event2);

      event2.addStudent(student2);
      //Fehler beim Speichern
      //repository.save(event2);
      //studentRepository.save(student2);
    };
  }

}
