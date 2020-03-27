package mops.rheinjug2.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import mops.rheinjug2.Account;
import mops.rheinjug2.AccountCreator;
import mops.rheinjug2.orgamodels.DelayedSubmission;
import mops.rheinjug2.orgamodels.SearchForm;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


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
    if (!model.containsAttribute("delayedsubmissions")) {
      model.addAttribute("delayedsubmissions", orgaService.getDelayedSubmission());
    }
    model.addAttribute("numberOfEvaluationRequests", numberOfEvaluationRequests);
    model.addAttribute("successmessage", successMessage);
    model.addAttribute("errormessage", errorMessage);
    model.addAttribute("searchForm", new SearchForm(""));
    successMessage = "";
    errorMessage = "";
    return "orga_delayed_submission";
  }

  /**
   * Mapping der Annahme von Zussamenfassungen.
   */
  @Secured({"ROLE_orga"})
  @PostMapping("/summaryaccepting")
  public String summaryAccepting(@RequestParam(defaultValue = " ") final Long eventid,
                                 @RequestParam(defaultValue = " ") final Long studentid,
                                 final Model model, final KeycloakAuthenticationToken token) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    if (orgaService.setSummaryAsAccepted(studentid, eventid)) {
      numberOfEvaluationRequests = orgaService.getnumberOfEvaluationRequests();
      successMessage = "Zusammenfassung wurde erfolgreich als akzeptiert gespeichert.";
      return "redirect:/rheinjug2/orga/reports";

    }
    errorMessage = "Fehler.. Zusammenfassung wurde nicht gespeichert ";
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
    try {
      orgaService.summaryupload(
          delayedSubmission.getStudentId(),
          delayedSubmission.getEventId(),
          delayedSubmission.getStudentName(),
          delayedSubmission.getSummaryContent());
      System.out.println(delayedSubmission.getSummaryContent());
    } catch (final RuntimeException e) {
      errorMessage = "zusammenfassung wurde nicht gespeichert: MinIO " + e.getMessage();
      return "redirect:/rheinjug2/orga/delayedSubmission";
    }
    successMessage = "Zusammenfassung wurde erfolgreich als akzeptiert hochgeladen.";
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
    errorMessage = "";
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

  /**
   * Die methode gibt der gesuchte Student zuruck.
   *
   * @param searchForm         .
   * @param redirectAttributes .
   * @return .
   */
  @Secured({"ROLE_orga"})
  @PostMapping("/searchstudent")
  public String searchstudent(@ModelAttribute final SearchForm searchForm,
                              final RedirectAttributes redirectAttributes) {
    final List<DelayedSubmission> delayedsubmissions =
        orgaService.getDelayedSubmissionsForStudent(searchForm.getSearchedName());
    redirectAttributes.addFlashAttribute("delayedsubmissions", delayedsubmissions);
    return "redirect:/rheinjug2/orga/delayedSubmission";
  }

  /**
   * Die Methode gibt die gesuchte Veranstaltung zuruck.
   *
   * @param searchForm         .
   * @param redirectAttributes .
   * @return .
   */
  @Secured({"ROLE_orga"})
  @PostMapping("/searchevent")
  public String searchevent(@ModelAttribute final SearchForm searchForm,
                            final RedirectAttributes redirectAttributes) {
    final List<DelayedSubmission> delayedsubmissions =
        orgaService.getDelayedSubmissionsForEvent(searchForm.getSearchedName());
    redirectAttributes.addFlashAttribute("delayedsubmissions", delayedsubmissions);
    return "redirect:/rheinjug2/orga/delayedSubmission";
  }

  @Scheduled(fixedDelayString = "${application.api-pump.delay}")
  public void refreshNumberOfEvaluationRequests() {
    numberOfEvaluationRequests = orgaService.getnumberOfEvaluationRequests();
  }
}
