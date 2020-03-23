package mops.rheinjug2.creditpoints;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import javax.mail.MessagingException;
import mops.rheinjug2.Account;
import mops.rheinjug2.AccountCreator;
import mops.rheinjug2.email.EmailService;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.services.ModelService;
import org.keycloak.KeycloakPrincipal;
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
@RequestMapping("/rheinjug2/student")
public class CreditpointController {
  
  
  private final transient Counter authenticatedAccess;
  private final transient ModelService modelService;
  private final transient EmailService emailService;
  private transient CertificateForm certificateForm = new CertificateForm();
  
  
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public CreditpointController(MeterRegistry registry,
                               ModelService modelService,
                               EmailService emailService) {
    authenticatedAccess = registry.counter("access.authenticated");
    this.modelService = modelService;
    this.emailService = emailService;
  }
  
  
  /**
   * Methode die überprüft ob der/die Student/in Zusammenfassungen bei einer EntwickelBar
   * abgegeben hat um CreditPoints zu beantragen.
   */
  @GetMapping("/creditpoints2")
  public String getCreditPoints(KeycloakAuthenticationToken token, Model model) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    
    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    String login = principal.getKeycloakSecurityContext().getIdToken().getName();
    
    //  False setzen
    boolean eventsAreUsable;
    
    if (modelService.loadStudentByLogin(login) != null) {
      List<Event> allEventsForCP = modelService.getAllEventsForCP(login);
      model.addAttribute("events", allEventsForCP);
      eventsAreUsable = modelService.checkEventsForCertificate(login);
    } else {
      eventsAreUsable = true;
    }
    boolean disabled = !eventsAreUsable;
    model.addAttribute("disabled", disabled);
    
    return "credit_points_apply";
    // CertificateServer Veranstaltungen übergeben ODER überprüfung ob vorhanden
    // Veranstaltungen auf jeden Fall auf gebucht setzen
    // sendEmail (java)
  }
  
  /**
   * Post-Formular für die Scheinabgabe.
   */
  @PostMapping("/creditpoints/certificateform")
  public String formular(@ModelAttribute("certificateForm") CertificateForm certificateForm,
                         KeycloakAuthenticationToken token,
                         Model model) throws MessagingException {
    Account account = AccountCreator.createAccountFromPrincipal(token);
    model.addAttribute("account", account);
    this.certificateForm = certificateForm;
    model.addAttribute("certificateForm", certificateForm);
    authenticatedAccess.increment();
    
    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    String login = principal.getKeycloakSecurityContext().getIdToken().getName();
    
    if (modelService.loadStudentByLogin(login) != null
        && modelService.checkEventsForCertificate(login)) {
      String forename = principal.getKeycloakSecurityContext().getIdToken().getGivenName();
      String surname = principal.getKeycloakSecurityContext().getIdToken().getFamilyName();
      String name = forename + " " + surname;
      
      List<Event> usableEvents = modelService.getEventsForCertificate(login);
      
      modelService.useEventsForCertificate(login);
      
      emailService.sendMail(name, certificateForm.getGender(),
          certificateForm.getMatNr(), usableEvents);
    }
    
    System.out.println(certificateForm.getGender() + certificateForm.getMatNr());
    return "credit_points_form";
  }
  
  
}
