package mops.rheinjug2.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rheinjug2/student")
public class StudentController {
  @GetMapping("/events")
  public String getEvents() {
    return "student_events_overview";
  }
}
