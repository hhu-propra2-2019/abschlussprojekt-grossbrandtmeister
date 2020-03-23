package mops.rheinjug2.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mops.rheinjug2.Account;
import mops.rheinjug2.AccountCreator;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.fileupload.FileService;
import mops.rheinjug2.fileupload.Summary;
import mops.rheinjug2.services.ModelService;
import mops.rheinjug2.services.SubmissionStatus;
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
  public StudentController(MeterRegistry registry, FileService fileService,
                           ModelService modelService) {
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
  public String getEvents(KeycloakAuthenticationToken token, Model model) {
    Account account = AccountCreator.createAccountFromPrincipal(token);
    modelService.addStudentToEvent(account.getName(), account.getEmail(), (long) 1);
    modelService.addStudentToEvent(account.getName(), account.getEmail(), (long) 2);
    modelService.submitSummary(account.getName(), (long) 1);
    modelService.acceptSummary((long) 1, account.getName());
    model.addAttribute("account", account);
    model.addAttribute("events", modelService.getAllEvents());
    model.addAttribute("studentRegisteredForEvent", modelService.getAllEventIdsPerStudent(account));
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
    modelService.addStudentToEvent(account.getName(), account.getEmail(), (long) 1);
    model.addAttribute("account", account);
    model.addAttribute("exists", modelService.studentExists(account.getName()));
    model.addAttribute("studentEvents", modelService.getAllEventsPerStudent(account.getName()));
    authenticatedAccess.increment();
    return "personalView";
  }
  
  //  /**
  //   * Formular zum Beantragen von Credit-Points.
  //   */
  //  @GetMapping("/creditpoints")
  //  public String getCreditPoints(final KeycloakAuthenticationToken token, final Model model) {
  //    final Account account = AccountCreator.createAccountFromPrincipal(token);
  //    model.addAttribute("account", account);
  //    model.addAttribute("eventsExist", modelService.acceptedEventsExist(account.getName()));
  //    model.addAttribute("events", modelService.getAllEventsForCP(account.getName()));
  //    model.addAttribute("useForCP", modelService.useEventsIsPossible(account.getName()));
  //    model.addAttribute("exists", modelService.studentExists(account.getName()));
  //    authenticatedAccess.increment();
  //    return "credit_points_apply";
  //  }
  
  /**
   * Formular zur Einreichung der Zusammenfassung.
   * Das Summary-Objekt muss noch auf die Angaben des jeweiligen Events aus
   * der Datenbank angepasst werden.
   */
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  @GetMapping("/reportsubmit")
  public String reportSubmit(KeycloakAuthenticationToken token, Model model,
                             Long eventId) {
    LocalDateTime today = LocalDateTime.now();
    Account account = AccountCreator.createAccountFromPrincipal(token);
    Event event = modelService.loadEventById(eventId);
    if (event == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event ID not found!");
    }
    String eventname = event.getTitle();
    String content;
    try {
      content = fileService.getContentOfFileAsString("VorlageZusammenfassung.md");
      content = content.isEmpty()
          ? "Vorlage momentan nicht vorhanden. Schreib hier deinen Code hinein." : content;
    } catch (Exception e) {
      content = "Vorlage momentan nicht vorhanden. Schreib hier deinen Code hinein.";
    }
    String student = account.getGivenName() + " " + account.getFamilyName();
    Summary summary = new Summary(eventname, student, content, today, eventId);
    model.addAttribute("summary", summary);
    model.addAttribute("account", account);
    authenticatedAccess.increment();
    return "report_submit";
  }
  
  /**
   * Fügt einen Studenten einem Event hinzu.
   */
  @PostMapping("/events")
  public String addStudentToEvent(String name, String email, Long eventId) {
    modelService.addStudentToEvent(name, email, eventId);
    return "redirect:/rheinjug2/student/visitedevents";
  }
  
}
