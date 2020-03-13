package mops.rheinjug2.repositories;

import java.util.List;
import mops.rheinjug2.entities.Event;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;

@Controller
public interface EventRepository extends CrudRepository<Event, Long> {

  @Query(value = "SELECT COUNT(*) FROM student_event WHERE student_event.event = :id")
  int countStudentsPerEventById(@Param("id") Long id);

  @Query(value = "SELECT student FROM student_event WHERE student_event.event = :id")
  List<Long> findAllStudentsIdsPerEventById(@Param("id") Long id);

  @Query(value = "SELECT * FROM EVENT")
  List<Event> getAllEvents();
}
