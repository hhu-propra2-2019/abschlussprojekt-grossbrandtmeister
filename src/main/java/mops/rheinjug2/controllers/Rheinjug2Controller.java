package mops.rheinjug2.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import javax.servlet.http.HttpServletRequest;
import mops.rheinjug2.AccountCreator;
import mops.rheinjug2.services.ModelService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rheinjug2")
public class Rheinjug2Controller {

  private final transient Counter publicAccess;
  private final transient ModelService modelService;

  public Rheinjug2Controller(final MeterRegistry registry, final ModelService modelService) {
    publicAccess = registry.counter("access.public");
    this.modelService = modelService;
  }

  @GetMapping("")
  public String getEventsNoMapping() {
    return "redirect:/rheinjug2/";
  }

  /**
   * Startseite, übergibt den Account falls jemand eingeloggt ist
   * und leitet auf entsprechende Übersicht weiter.
   */
  @GetMapping("/")
  public String getEvents(final KeycloakAuthenticationToken token, final Model model) {
    if (token != null) {
      model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
      if (token.getAccount().getRoles().contains("orga")) {
        return "redirect:/rheinjug2/orga/";
      } else if (token.getAccount().getRoles().contains("studentin")) {
        return "redirect:/rheinjug2/student/";
      }
    }
    model.addAttribute("events", modelService.getAllEvents());
    publicAccess.increment();
    return "index";
  }

  @GetMapping("/logout")
  public String logout(final HttpServletRequest request) throws Exception {
    request.logout();
    return "redirect:/rheinjug2/";
  }
}
