package mops.rheinjug2.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import javax.mail.MessagingException;
import mops.rheinjug2.Account;
import mops.rheinjug2.AccountCreator;
import mops.rheinjug2.creditpoints.CertificateForm;
import mops.rheinjug2.creditpoints.CreditpointService;
import mops.rheinjug2.services.ModelService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Secured({"ROLE_studentin"})
@RequestMapping("/rheinjug2/student/creditpoints")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class CreditpointController {
  
  
  private final transient Counter authenticatedAccess;
  private final transient ModelService modelService;
  private final transient CreditpointService creditpointService;
  private transient CertificateForm certificateForm = new CertificateForm();
  
  
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public CreditpointController(final MeterRegistry registry,
                               final ModelService modelService,
                               final CreditpointService creditpointService) {
    authenticatedAccess = registry.counter("access.authenticated");
    this.modelService = modelService;
    this.creditpointService = creditpointService;
  }
  
  
  /**
   * Methode die überprüft ob der/die Student/in Zusammenfassungen bei einer EntwickelBar
   * abgegeben hat um CreditPoints zu beantragen.
   */
  @GetMapping("")
  public String getCreditPoints(final KeycloakAuthenticationToken token, final Model model) {
    final Account account = AccountCreator.createAccountFromPrincipal(token);
    model.addAttribute("account", account);
    authenticatedAccess.increment();
    
    final String login = account.getName();
    model.addAttribute("eventsExist", modelService.acceptedEventsExist(login));
    model.addAttribute("events", modelService.getAllEventsForCP(login));
    model.addAttribute("useForCP", modelService.useEventsIsPossible(login));
    model.addAttribute("exists", modelService.studentExists(login));
    
    final boolean disabled = !creditpointService.checkIfButtonNeedsToBeDisabled(login);
    model.addAttribute("disabled", disabled);
    
    return "credit_points_apply";
  }
  
  /**
   * Post-Formular für die Scheinabgabe.
   */
  @GetMapping("/certificateform")
  public String formular(@ModelAttribute("certificateForm") final CertificateForm certificateForm,
                         final KeycloakAuthenticationToken token,
                         final Model model) {
    final Account account = AccountCreator.createAccountFromPrincipal(token);
    model.addAttribute("account", account);
    this.certificateForm = certificateForm;
    model.addAttribute("certificateForm", certificateForm);
    authenticatedAccess.increment();
    
    return "credit_points_form";
  }
  
  /**
   * Post-Mapping um PDF zu erzeugen und zu senden.
   */
  @PostMapping("/certificateform")
  public String sendEmail(@ModelAttribute("certificateForm") final CertificateForm certificateForm,
                          final KeycloakAuthenticationToken token,
                          final Model model) throws MessagingException {
    final Account account = AccountCreator.createAccountFromPrincipal(token);
    model.addAttribute("account", account);
    this.certificateForm = certificateForm;
    model.addAttribute("certificateForm", certificateForm);
    authenticatedAccess.increment();
    
    final String login = account.getName();
    final String forename = account.getGivenName();
    final String surname = account.getFamilyName();
    final String name = forename + " " + surname;
    
    creditpointService.sendMailIfPossible(certificateForm, login, name);
    
    return "redirect:/rheinjug2/student/creditpoints/";
  }
  
  
}
