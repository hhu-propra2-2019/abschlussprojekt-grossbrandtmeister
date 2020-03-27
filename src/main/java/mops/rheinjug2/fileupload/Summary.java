package mops.rheinjug2.fileupload;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@AllArgsConstructor
public class Summary {

  private final String eventname;
  private final String studentname;
  private final String content;
  @DateTimeFormat(pattern = "MM.dd.yyyy")
  private final LocalDate date;
  private final Long eventId;

}