package mops.rheinjug2.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import mops.rheinjug2.AccountCreator;
import mops.rheinjug2.fileupload.FileService;
import mops.rheinjug2.fileupload.Summary;
import org.keycloak.KeycloakPrincipal;
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
   * Die EventId muss später durch die richtige Id aus der Datenbank ersetzt werden.
   */
  @GetMapping("/visitedevents")
  public String getPersonal(KeycloakAuthenticationToken token, Model model) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    final Long eventId = 123L;
    model.addAttribute("eventId", eventId);
    authenticatedAccess.increment();
    return "personalView";
  }

  /**
   * Formular zur Einreichung der Zusammenfassung.
   * Das Summary-Objekt muss noch auf die Angaben des jeweiligen Events aus
   * der Datenbank angepasst werden.
   */
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  @GetMapping("/reportsubmit")
  public String reportsubmit(KeycloakAuthenticationToken token, Model model,
                             Long eventId) {
    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    String student = principal.getName();
    LocalDateTime today = LocalDateTime.now();
    final String eventname = "cooler Event Name";

    String content;
    try {
      content = fileService.getContentOfFileAsString("VorlageZusammenfassung.md");
      content = content.isEmpty()
          ? "Vorlage momentan nicht vorhanden. Schreib hier deinen Code hinein." : content;
    } catch (Exception e) {
      content = "Vorlage momentan nicht vorhanden. Schreib hier deinen Code hinein.";
    }
    Summary summary = new Summary(eventname, student, content, today, eventId);
    model.addAttribute("summary", summary);
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    return "report_submit";
  }
}
