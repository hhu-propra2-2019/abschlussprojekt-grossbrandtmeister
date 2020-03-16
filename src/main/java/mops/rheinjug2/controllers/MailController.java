package mops.rheinjug2.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.log4j.Log4j2;
import mops.rheinjug2.AccountCreator;
import mops.rheinjug2.email.EmailService;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Secured({"ROLE_studentin"})
@RequestMapping("/rheinjug2")
@Log4j2
public class MailController {
  
  private final transient Counter authenticatedAccess;
  private final transient EmailService emailService;
  
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MailController(MeterRegistry registry,
                        EmailService emailService) {
    authenticatedAccess = registry.counter("access.authenticated");
    this.emailService = emailService;
  }
  
  /**
   * Sends email with Certificate.
   *
   * @return Dummy String
   */
  @ResponseBody
  @RequestMapping("/sendEmail")
  public String sendEmailWithCertificate(KeycloakAuthenticationToken token,
                                         Model model) throws Exception {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    
    final KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    final String forename = principal.getKeycloakSecurityContext().getIdToken().getGivenName();
    final String surname = principal.getKeycloakSecurityContext().getIdToken().getFamilyName();
    final String name = forename + " " + surname;
    
    String gender = "male";
    String matNr = "123912399";
    
    emailService.sendMail(name, gender, matNr);
    
    return "Email Sent!";
  }
}
