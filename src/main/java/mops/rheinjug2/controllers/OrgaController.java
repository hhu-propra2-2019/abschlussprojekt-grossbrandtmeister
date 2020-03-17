package mops.rheinjug2.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.extern.log4j.Log4j2;
import mops.rheinjug2.Account;
import mops.rheinjug2.AccountCreator;
import mops.rheinjug2.services.EventService;
import mops.rheinjug2.services.OrgaService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Log4j2
@Controller
@Secured({"ROLE_orga"})
@RequestMapping("/rheinjug2/orga")
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
   * Übersicht der noch unbewerteten Abgaben.
   */
  @GetMapping("/reports")
  public String getReports(final KeycloakAuthenticationToken token, final Model model) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    model.addAttribute("summaries", orgaService.getSummaries());
    return "orga_reports_overview";
  }

  /**
   * Ruft die rheinjug Events manuell ab und speichert diese in die Datenbank.
   */
  @PostMapping("/events")
  public String getEventsFromApi(final KeycloakAuthenticationToken token) {
    final Account user = AccountCreator.createAccountFromPrincipal(token);
    log.info("User '" + user.getName() + "' requested event refresh");
    eventService.refreshRheinjugEvents(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC));
    return "redirect:/rheinjug2/orga/events";
  }
}
