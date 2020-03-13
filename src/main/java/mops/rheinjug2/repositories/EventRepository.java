package mops.rheinjug2.repositories;

import java.util.List;
import mops.rheinjug2.entities.Event;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends CrudRepository<Event, Long> {

  @Query(value = "SELECT COUNT(*) FROM student_event WHERE student_event.event = :id")
  int countStudentsPerEventById(@Param("id") Long id);

  @Query(value = "SELECT student FROM student_event WHERE student_event.event = :id")
  List<Long> findAllStudentsIdsPerEventById(@Param("id") Long id);

  @Modifying
  @Query(value = "UPDATE student_event SET used_for_certificate = :used_f_c, submitted_summary = true, accepted = true WHERE"
      + ":e_id = student_event.event AND student_event.student = :s_id")
  int setUsedForCertificate(@Param("used_f_c") boolean usedForCertificate, @Param("e_id") Long e_id, @Param("s_id") Long s_id);
}
