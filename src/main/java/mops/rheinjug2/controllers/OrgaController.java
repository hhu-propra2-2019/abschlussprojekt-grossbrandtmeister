package mops.rheinjug2.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import mops.rheinjug2.AccountCreator;
import mops.rheinjug2.services.OrgaService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Secured( {"ROLE_orga"})
@RequestMapping("/rheinjug2/orga")
public class OrgaController {

  private final transient Counter authenticatedAccess;
  transient OrgaService service;

  public OrgaController(MeterRegistry registry, OrgaService service) {
    authenticatedAccess = registry.counter("access.authenticated");
    this.service = service;
  }

  /**
   * Event Übersicht für Orga Mitarbeiter.
   */
  @GetMapping("/events")
  public String getEvents(KeycloakAuthenticationToken token, Model model) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    //model.addAttribute("events", service.getEvents());
    service.getEvents().forEach(x -> System.out.println(x.toString()));
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
}
