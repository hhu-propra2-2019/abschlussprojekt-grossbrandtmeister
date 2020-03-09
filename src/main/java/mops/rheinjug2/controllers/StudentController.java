package mops.rheinjug2.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rheinjug2/student")
public class StudentController {
  @GetMapping("/events")
  public String getEvents(Model m) {
    return "student_events_overview";
  }

  @GetMapping("/visitedevents")
  public String getPersonal() {
    return "personalView";
  }

  @GetMapping("/creditpoints")
  public String getCreditPoints() {
    return "credit_points_apply";
  }

  @GetMapping("/reportsubmit")
  public String reportsubmit() {
    return "report_submit";
  }
}
