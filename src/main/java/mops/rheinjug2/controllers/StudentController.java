package mops.rheinjug2.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDate;
import mops.rheinjug2.AccountCreator;
import mops.rheinjug2.fileupload.FileService;
import mops.rheinjug2.fileupload.Summary;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Secured({"ROLE_studentin"})
@RequestMapping("/rheinjug2/student")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class StudentController {

  private final transient Counter authenticatedAccess;

  transient FileService fileService;

  public StudentController(MeterRegistry registry, FileService fileService) {
    this.fileService = fileService;
    authenticatedAccess = registry.counter("access.authenticated");
  }

  /**
   * Event Übersicht für Studenten.
   */
  @GetMapping("/events")
  public String getEvents(KeycloakAuthenticationToken token, Model model) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    return "student_events_overview";
  }

  /**
   * Übersicht der Events für die der aktuelle Student angemeldet war/ist.
   */
  @GetMapping("/visitedevents")
  public String getPersonal(KeycloakAuthenticationToken token, Model model) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    return "personalView";
  }

  /**
   * Formular zur Einreichung der Zusammenfassung.
   * Das Summary-Objekt muss noch auf die Datenbank angepasst werden.
   */
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  @GetMapping("/reportsubmit")
  public String reportsubmit(KeycloakAuthenticationToken token, Model model) {
    LocalDate today = LocalDate.now();
    final String eventname = "das coolste Event";
    String content;
    try {
      content = fileService.getContentOfFileAsString("VorlageZusammenfassung.md");
      content = content.isEmpty()
          ? "Vorlage momentan nicht vorhanden. Schreib hier deinen Code hinein." : content;
    } catch (Exception e) {
      content = "Vorlage momentan nicht vorhanden. Schreib hier deinen Code hinein.";
    }
    final String student = "Hannah Hengelbrock";
    Summary summary = new Summary(eventname, student, content, today);
    model.addAttribute("summary", summary);
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    return "report_submit";
  }
}
