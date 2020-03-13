package mops.rheinjug2.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.log4j.Log4j2;
import mops.rheinjug2.Account;
import mops.rheinjug2.AccountCreator;
import mops.rheinjug2.services.EventService;
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

  public OrgaController(MeterRegistry registry, EventService eventService) {
    authenticatedAccess = registry.counter("access.authenticated");
    this.eventService = eventService;
  }

  /**
   * Event Übersicht für Orga Mitarbeiter.
   */
  @GetMapping("/events")
  public String getEvents(KeycloakAuthenticationToken token, Model model) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    return "orga_events_overview";
  }

  /**
   * Übersicht der Anträge für Credit Points.
   */
  @GetMapping("/creditpoints")
  public String getCreditpoins(KeycloakAuthenticationToken token, Model model) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    return "orga_creditpoints";
  }

  /**
   * Übersicht der noch unbewerteten Abgaben.
   */
  @GetMapping("/reports")
  public String getReports(KeycloakAuthenticationToken token, Model model) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    return "orga_reports_overview";
  }

  /**
   * Ruft die rheinjug Events manuell ab und speichert diese in die Datenbank.
   */
  @PostMapping("/events")
  public String getEventsFromApi(KeycloakAuthenticationToken token) {
    Account user = AccountCreator.createAccountFromPrincipal(token);
    log.info("User '" + user.getName() + "' requested event refresh");
    eventService.refreshRheinjugEvents();
    return "redirect:/rheinjug2/orga/events";
  }
}
