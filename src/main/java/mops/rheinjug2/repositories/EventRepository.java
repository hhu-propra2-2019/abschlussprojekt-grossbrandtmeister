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
  @Query(value = "UPDATE student_event SET accepted = :accepted WHERE"
      + " :eventid = student_event.event AND student_event.student = :studentid")
  int setAccepted(@Param("accepted") boolean accepted,
                  @Param("eventid") Long eventid, @Param("studentid") Long studentid);

  @Query(value = "SELECT * FROM event WHERE meetup_id = :id")
  Event findEventByMeetupId(@Param("id") String id);

  @Query(value = "SELECT * FROM event WHERE status = :status")
  List<Event> findEventsByStatus(@Param("status") String status);

  @Query(value = "SELECT * FROM EVENT")
  List<Event> getAllEvents();

  @Query(value = "SELECT COUNT(*) FROM student_event "
      + "WHERE student_event.event = :id AND student_event.submitted_summary = TRUE ")
  int countSubmittedSummaryPerEventById(@Param("id") Long id);
}
