package mops.rheinjug2.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import java.time.ZoneId;
import mops.rheinjug2.Account;
import mops.rheinjug2.AccountCreator;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.fileupload.FileService;
import mops.rheinjug2.fileupload.Summary;
import mops.rheinjug2.services.ModelService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@Secured({"ROLE_studentin"})
@RequestMapping("/rheinjug2/student")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class StudentController {
  
  private final transient Counter authenticatedAccess;
  private final transient ModelService modelService;
  
  
  transient FileService fileService;
  
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public StudentController(final MeterRegistry registry, final FileService fileService,
                           final ModelService modelService) {
    this.fileService = fileService;
    this.modelService = modelService;
    authenticatedAccess = registry.counter("access.authenticated");
  }
  
  @GetMapping("/")
  public String studentBase() {
    return "redirect:/rheinjug2/student/events/";
  }
  
  /**
   * Event Übersicht für Studenten.
   */
  @GetMapping("/events")
  public String getEvents(final KeycloakAuthenticationToken token, final Model model) {
    final Account account = AccountCreator.createAccountFromPrincipal(token);
    model.addAttribute("account", account);
    model.addAttribute("events", modelService.getAllEvents());
    model.addAttribute("studentRegisteredForEvent",
        modelService.getAllEventIdsPerStudent(account.getName()));
    authenticatedAccess.increment();
    return "student_events_overview";
  }
  
  /**
   * Übersicht der Events für die der aktuelle Student angemeldet war/ist.
   * Die EventId muss später durch die richtige Id aus der Datenbank ersetzt werden.
   */
  @GetMapping("/visitedevents")
  public String getPersonal(final KeycloakAuthenticationToken token, final Model model) {
    final Account account = AccountCreator.createAccountFromPrincipal(token);
    model.addAttribute("account", account);
    model.addAttribute("exists", modelService.studentExists(account.getName()));
    model.addAttribute("studentEvents", modelService.getAllEventsPerStudent(account.getName()));
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
  public String reportSubmit(final KeycloakAuthenticationToken token, final Model model,
                             final Long eventId) {
    if (eventId == null) {
      return "redirect:rheinjug2/student/visitedevents";
    }
    final LocalDateTime today = LocalDateTime.now(ZoneId.of("Europe/Berlin"));
    final Account account = AccountCreator.createAccountFromPrincipal(token);
    final Event event = modelService.loadEventById(eventId);
    if (event == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event ID not found!");
    }
    final String eventname = event.getTitle();
    String content;
    try {
      content = fileService.getContentOfFileAsString("VorlageZusammenfassung.md");
      content = content.isEmpty()
          ? "Vorlage momentan nicht vorhanden. Schreib hier deinen Code hinein." : content;
    } catch (final Exception e) {
      content = "Vorlage momentan nicht vorhanden. Schreib hier deinen Code hinein.";
    }
    final String student = account.getGivenName() + " " + account.getFamilyName();
    final Summary summary = new Summary(eventname, student, content, today, eventId);
    model.addAttribute("summary", summary);
    model.addAttribute("account", account);
    model.addAttribute("event", event);
    authenticatedAccess.increment();
    return "report_submit";
  }
  
  /**
   * Fügt einen Studenten einem Event hinzu.
   */
  @PostMapping("/events")
  public String addStudentToEvent(final KeycloakAuthenticationToken token,
                                  final Model model, final Long eventId) {
    final Account account = AccountCreator.createAccountFromPrincipal(token);
    model.addAttribute("account", account);
    modelService.addStudentToEvent(account.getName(), account.getEmail(), eventId);
    return "redirect:/rheinjug2/student/visitedevents";
  }
}
