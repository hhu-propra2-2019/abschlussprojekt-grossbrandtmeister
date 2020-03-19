package mops.rheinjug2.creditpoints;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import mops.rheinjug2.Account;
import mops.rheinjug2.AccountCreator;
import mops.rheinjug2.ModelService;
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
public class CreditpointController {
  
  
  private final transient Counter authenticatedAccess;
  private final transient ModelService modelService;
  private transient CertificateForm certificateForm = new CertificateForm();
  
  
  public CreditpointController(final MeterRegistry registry, final ModelService modelService) {
    authenticatedAccess = registry.counter("access.authenticated");
    this.modelService = modelService;
  }
  
  /**
   * Get-Formular für die Scheinabgabe.
   */
  @GetMapping("/certificateform")
  public String formular(final KeycloakAuthenticationToken token,
                         final Model model) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    model.addAttribute("certificateForm", certificateForm);
    return "credit_points_form";
  }
  
  /**
   * Post-Formular für die Scheinabgabe.
   */
  @PostMapping("certificateform")
  public String formular(@ModelAttribute("certificateForm") final CertificateForm certificateForm,
                         final KeycloakAuthenticationToken token,
                         final Model model) {
    final Account account = AccountCreator.createAccountFromPrincipal(token);
    model.addAttribute("account", account);
    this.certificateForm = certificateForm;
    model.addAttribute("certificateForm", certificateForm);
    authenticatedAccess.increment();
    
    System.out.println(certificateForm.getGender() + certificateForm.getMatNr());
    return "credit_points_form";
  }
  
  
  /**
   * Methode die überprüft ob der/die Student/in Zusammenfassungen bei drei Abendveranstaltungen
   * abgegeben hat um CreditPoints zu beantragen.
   */
  @RequestMapping("/eveningevents")
  public void eveningEventsCP(final KeycloakAuthenticationToken token, final Model model) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    
    
    // CertificateServer Veranstaltungen übergeben ODER überprüfung ob vorhanden
    // Veranstaltungen auf jeden Fall auf gebucht setzen
    // sendEmail (rheinjug)
  }
  
  /**
   * Methode die überprüft ob der/die Student/in Zusammenfassungen bei einer EntwickelBar
   * abgegeben hat um CreditPoints zu beantragen.
   */
  @RequestMapping("/entwickelbar")
  public void entwickelbarCP(final KeycloakAuthenticationToken token, final Model model) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    // CertificateServer Veranstaltungen übergeben ODER überprüfung ob vorhanden
    // Veranstaltungen auf jeden Fall auf gebucht setzen
    // sendEmail (java)
  }
}
