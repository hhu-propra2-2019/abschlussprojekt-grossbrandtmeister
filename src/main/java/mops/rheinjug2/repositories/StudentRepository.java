package mops.rheinjug2.repositories;

import java.util.List;
import mops.rheinjug2.entities.Student;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends CrudRepository<Student, Long> {

  @Query(value = "SELECT * FROM student s WHERE s.name = :name")
  List<Student> findByName(@Param("name") String name);

  @Query(value = "SELECT * FROM student s WHERE s.email = :email")
  Student findByEmail(@Param("email") String email);

  @Query(value = "SELECT COUNT(*) FROM student_event WHERE student_event.student = :id")
  int countEventsPerStudentById(@Param("id") Long studentId);

  @Query(value = "SELECT submitted_summary FROM student_event WHERE "
      + "student_event.student = :s_id AND student_event.event= :e_id")
  boolean getSubmittedValue(@Param("s_id") Long studentId, @Param("e_id") Long eventId);
}
