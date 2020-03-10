package mops.rheinjug2.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/rheinjug2/orga")
@Controller
public class OrgaController {

  @GetMapping("/events")
  public String getEvents() {
    return "orga_events_overview";
  }

  @GetMapping("/creditpoints")
  public String getCreditpoins() {
    return "orga_creditpoints";
  }

  @GetMapping("/reports")
  public String getReports() {
    return "orga_reports_overview";
  }
}
