package mops.rheinjug2.fileupload;

import java.time.LocalDate;
import lombok.Getter;

@Getter
public class Summary {

  private final String eventname;
  private final String studentname;
  private final String content;
  private final LocalDate date;

  public Summary(final String eventname, final String studentname, final String content, final LocalDate date) {
    this.eventname = eventname;
    this.studentname = studentname;
    this.content = content;
    this.date = date;
  }
}