package mops.rheinjug2.repositories;

import mops.rheinjug2.entities.Student;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends CrudRepository<Student, Long> {

  @Query(value = "SELECT * FROM student s WHERE s.login = :login")
  Student findByLogin(@Param("login") String login);

  @Query(value = "SELECT COUNT(*) FROM student_event WHERE student_event.student = :id")
  int countEventsPerStudentById(@Param("id") Long studentId);

  @Query(value = "SELECT submitted_summary FROM student_event WHERE "
      + "student_event.student = :s_id AND student_event.event= :e_id")
  boolean getSubmittedValue(@Param("s_id") Long studentId, @Param("e_id") Long eventId);

  @Query(value = "SELECT * FROM student WHERE student.id= :id")
  Student getStudentById(@Param("id") long id);

}
