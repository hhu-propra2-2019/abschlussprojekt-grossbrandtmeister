package mops.rheinjug2.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import mops.rheinjug2.Account;
import mops.rheinjug2.AccountCreator;
import mops.rheinjug2.orgamodels.OrgaSummary;
import mops.rheinjug2.services.EventService;
import mops.rheinjug2.services.OrgaService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Log4j2
@Controller
@Secured({"ROLE_orga"})
@RequestMapping("/rheinjug2/orga")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class OrgaController {

  private final transient Counter authenticatedAccess;
  private final transient EventService eventService;
  private final transient OrgaService orgaService;

  /**
   * Injected unsere Services und den Counter.
   */
  public OrgaController(final MeterRegistry registry,
                        final EventService eventService, final OrgaService orgaService) {
    authenticatedAccess = registry.counter("access.authenticated");
    this.eventService = eventService;
    this.orgaService = orgaService;
  }

  /**
   * Event Übersicht für Orga Mitarbeiter.
   */
  @GetMapping("/events")
  public String getEvents(final KeycloakAuthenticationToken token, final Model model) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    model.addAttribute("events", orgaService.getEvents());
    model.addAttribute("datenow", LocalDateTime.now());
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
  @GetMapping("/delayedSubmission")
  public String getDelayedSubmission(final KeycloakAuthenticationToken token, final Model model) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    model.addAttribute("delayedsubmissions", orgaService.getDelayedSubmission());
    return "orga_delayed_submission";
  }

  /**
   * Mapping der Annahme von Zussamenfassungen.
   */
  @PostMapping("/summaryaccepting")
  public String summaryAccepting(@RequestParam final Long eventid,
                                 @RequestParam final Long studentid,
                                 final Model model, final KeycloakAuthenticationToken token) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    orgaService.setSummaryAcception(studentid, eventid);
    return "redirect:/rheinjug2/orga/reports";
  }

  /**
   * Übersicht der noch unbewerteten Abgaben.
   */
  @GetMapping("/reports")
  public String getReports(final KeycloakAuthenticationToken token, final Model model) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    model.addAttribute("summaries", orgaService.getSummaries()
        .stream()
        .sorted(Comparator.comparing(OrgaSummary::getTimeOfSubmission).reversed()
            .thenComparing(OrgaSummary::getSubmissionDeadline))
        .collect(Collectors.toList()));
    return "orga_reports_overview";
  }

  /**
   * Ruft die rheinjug Events manuell ab und speichert diese in die Datenbank.
   */
  @PostMapping("/events")
  public String getEventsFromApi(final KeycloakAuthenticationToken token) {
    final Account user = AccountCreator.createAccountFromPrincipal(token);
    log.info("User '" + user.getName() + "' requested event refresh");
    eventService.refreshRheinjugEvents(LocalDateTime.now(ZoneId.of("Europe/Berlin")));
    return "redirect:/rheinjug2/orga/events";
  }
}
