package mops.rheinjug2.creditpoints;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Secured({"ROLE_studentin"})
@RequestMapping("/rheinjug2/student/creditpoints")
public class CreditpointController {


  private final transient Counter authenticatedAccess;

  public CreditpointController(final MeterRegistry registry) {
    authenticatedAccess = registry.counter("access.authenticated");
  }

  @RequestMapping("/rheinjug")
  public void rheinjugCP(final KeycloakAuthenticationToken token, final Model model) {
    // CertificateServer Veranstaltungen übergeben ODER überprüfung ob vorhanden
    // Veranstaltungen auf jeden Fall auf gebucht setzen
    // sendEmail (rheinjug)
  }


  @RequestMapping("/java")
  public void javaCP(final KeycloakAuthenticationToken token, final Model model) {
    // CertificateServer Veranstaltungen übergeben ODER überprüfung ob vorhanden
    // Veranstaltungen auf jeden Fall auf gebucht setzen
    // sendEmail (java)
  }


}
