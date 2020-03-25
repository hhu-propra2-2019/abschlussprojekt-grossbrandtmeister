package mops.rheinjug2.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import mops.rheinjug2.Account;
import mops.rheinjug2.AccountCreator;
import mops.rheinjug2.orgamodels.DelayedSubmission;
import mops.rheinjug2.services.EventService;
import mops.rheinjug2.services.OrgaService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.SessionScope;

@Log4j2
@Controller
@RequestMapping("/rheinjug2/orga")
@SessionScope
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class OrgaController {

  private final transient Counter authenticatedAccess;
  private final transient EventService eventService;
  private final transient OrgaService orgaService;
  private transient String successMessage = "";
  private transient String errorMessage = "";
  private transient int numberOfEvaluationRequests;

  /**
   * Injected unsere Services und den Counter.
   */

  public OrgaController(final MeterRegistry registry,
                        final EventService eventService, final OrgaService orgaService) {
    authenticatedAccess = registry.counter("access.authenticated");
    this.eventService = eventService;
    this.orgaService = orgaService;
    numberOfEvaluationRequests = orgaService.getnumberOfEvaluationRequests();
  }

  @Secured({"ROLE_orga"})
  @GetMapping("/")
  public String orgaBase() {
    return "redirect:/rheinjug2/orga/events";
  }

  /**
   * Event Übersicht für Orga Mitarbeiter.
   */
  @Secured({"ROLE_orga"})
  @GetMapping("/events")
  public String getEvents(final KeycloakAuthenticationToken token, final Model model) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    model.addAttribute("events", orgaService.getEvents());
    model.addAttribute("datenow", LocalDateTime.now());
    model.addAttribute("numberOfEvaluationRequests", numberOfEvaluationRequests);
    return "orga_events_overview";
  }

  /**
   * Gibt liste aller von Studenten angemeldte Veranstaltungen,
   * die ihre Zusammenfassung noch nicht abgegeben worde.
   *
   * @param token token
   * @param model model
   * @return liste der Veranstaltungen.
   */
  @Secured({"ROLE_orga"})
  @GetMapping("/delayedSubmission")
  public String getDelayedSubmission(final KeycloakAuthenticationToken token, final Model model) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    model.addAttribute("delayedsubmissions", orgaService.getDelayedSubmission());
    model.addAttribute("numberOfEvaluationRequests", numberOfEvaluationRequests);
    model.addAttribute("successmessage", successMessage);
    model.addAttribute("errormessage", errorMessage);
    successMessage = "";
    return "orga_delayed_submission";
  }

  /**
   * Mapping der Annahme von Zussamenfassungen.
   */
  @Secured({"ROLE_orga"})
  @PostMapping("/summaryaccepting")
  public String summaryAccepting(@RequestParam final Long eventid,
                                 @RequestParam final Long studentid,
                                 final Model model, final KeycloakAuthenticationToken token,
                                 final HttpServletRequest request) throws ServletException {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    if (orgaService.studentIsRegistred(studentid, eventid)) {
      orgaService.setSummaryAsAccepted(studentid, eventid);
      numberOfEvaluationRequests = orgaService.getnumberOfEvaluationRequests();
      successMessage = "Zusammenfassung wurde erfolgreich als akzeptiert gespeichert.";
    }
    return "redirect:/rheinjug2/orga/reports";
  }

  /**
   * Übersicht der Veranstaltung, an denen Studenten teilgenommen haben aber
   * die zusammenfassung noch nichtabgegebn haben und Abgabefrist ist vorbei.
   *
   * @param delayedSubmission .
   * @return .
   * @throws IOException .
   */
  @Secured({"ROLE_orga"})
  @PostMapping("/summaryupload")
  public String summaryUpload(@ModelAttribute final DelayedSubmission delayedSubmission)
      throws IOException {
    if (orgaService.studentIsRegistred(delayedSubmission.getStudentId(),
        delayedSubmission.getEventId())) {
      try {
        orgaService.summaryupload(delayedSubmission.getStudentName(),
            delayedSubmission.getEventId(),
            delayedSubmission.getSummaryContent());
      } catch (final RuntimeException e) {
        errorMessage = "zusammenfassung wurde nicht gespeichert: MinIO " + e.getMessage();
        return "redirect:/rheinjug2/orga/delayedSubmission";
      }
      orgaService.setSummaryAsSubmittedAndAccepted(delayedSubmission.getStudentId(),
          delayedSubmission.getEventId());
      successMessage = "Zusammenfassung wurde erfolgreich als akzeptiert hochgeladen.";
    }
    return "redirect:/rheinjug2/orga/delayedSubmission";

  }

  /**
   * Übersicht der noch unbewerteten Abgaben.
   */
  @Secured({"ROLE_orga"})
  @GetMapping("/reports")
  public String getReports(final KeycloakAuthenticationToken token, final Model model) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    model.addAttribute("summaries", orgaService.getSummaries());
    model.addAttribute("successmessage", successMessage);
    model.addAttribute("numberOfEvaluationRequests", numberOfEvaluationRequests);
    successMessage = "";
    return "orga_reports_overview";
  }

  /**
   * Ruft die rheinjug Events manuell ab und speichert diese in die Datenbank.
   */
  @Secured({"ROLE_orga"})
  @PostMapping("/events")
  public String getEventsFromApi(final KeycloakAuthenticationToken token) {
    final Account user = AccountCreator.createAccountFromPrincipal(token);
    log.info("User '" + user.getName() + "' requested event refresh");
    eventService.refreshRheinjugEvents(LocalDateTime.now(ZoneId.of("Europe/Berlin")));
    return "redirect:/rheinjug2/orga/events";
  }

  @Scheduled(fixedDelayString = "${application.api-pump.delay}")
  public void refreshNumberOfEvaluationRequests() {
    numberOfEvaluationRequests = orgaService.getnumberOfEvaluationRequests();
  }
}
