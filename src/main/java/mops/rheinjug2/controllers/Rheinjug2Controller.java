package mops.rheinjug2.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import javax.servlet.http.HttpServletRequest;
import mops.rheinjug2.AccountCreator;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rheinjug2")
public class Rheinjug2Controller {

  private final Counter publicAccess;

  public Rheinjug2Controller(MeterRegistry registry) {
    publicAccess = registry.counter("access.public");
  }

  /**
   * Startseite, übergibt den Account falls jemand eingelogt ist.
   */
  @GetMapping("/")
  public String getEvents(KeycloakAuthenticationToken token, Model model) {
    if (token != null) {
      model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    }
    publicAccess.increment();
    return "index";
  }

  @GetMapping("/logout")
  public String logout(HttpServletRequest request) throws Exception {
    request.logout();
    return "redirect:/rheinjug2/";
  }
}
