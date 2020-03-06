package mops.rheinjug2.repositories;

import mops.rheinjug2.entities.Summary;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SummaryRepository extends CrudRepository<Summary, Long> {
}
