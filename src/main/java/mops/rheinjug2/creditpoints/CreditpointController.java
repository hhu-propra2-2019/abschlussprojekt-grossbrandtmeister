package mops.rheinjug2.creditpoints;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import mops.rheinjug2.Account;
import mops.rheinjug2.AccountCreator;
import mops.rheinjug2.ModelService;
import mops.rheinjug2.email.EmailService;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.entities.Student;
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
@RequestMapping("/rheinjug2/student/creditpoints")
public class CreditpointController {
  
  
  private final transient Counter authenticatedAccess;
  private final transient ModelService modelService;
  private final transient EmailService emailService;
  private final transient CreditpointsService creditpointsService;
  private transient CertificateForm certificateForm = new CertificateForm();
  
  
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public CreditpointController(MeterRegistry registry,
                               ModelService modelService,
                               EmailService emailService,
                               CreditpointsService creditpointsService) {
    authenticatedAccess = registry.counter("access.authenticated");
    this.modelService = modelService;
    this.emailService = emailService;
    this.creditpointsService = creditpointsService;
  }
  
  
  /**
   * Methode die überprüft ob der/die Student/in Zusammenfassungen bei einer EntwickelBar
   * abgegeben hat um CreditPoints zu beantragen.
   */
  @GetMapping("")
  public String getCreditPoints(KeycloakAuthenticationToken token, Model model) {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    
    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    String login = principal.getKeycloakSecurityContext().getIdToken().getName();
    
    //  False setzen
    boolean eventsAreUsable = true;
    
    if (modelService.loadStudentByLogin(login) != null) {
      Student student = modelService.loadStudentByLogin(login);
      List<Event> allEventsForCP = modelService.getAllEventsForCP(login);
      model.addAttribute("events", allEventsForCP);
      eventsAreUsable = modelService.checkEventsForCertificate(login);
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
  @PostMapping("/certificateform")
  public String formular(@ModelAttribute("certificateForm") CertificateForm certificateForm,
                         KeycloakAuthenticationToken token,
                         Model model) {
    Account account = AccountCreator.createAccountFromPrincipal(token);
    model.addAttribute("account", account);
    this.certificateForm = certificateForm;
    model.addAttribute("certificateForm", certificateForm);
    authenticatedAccess.increment();
    
    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    String login = principal.getKeycloakSecurityContext().getIdToken().getName();
    Student student = modelService.loadStudentByLogin(login);

//    if (modelService.checkEventsForCertificate(login)) {
//      creditpointsService.sendMailWithPdf(token, certificateForm.getGender(), certificateForm.getMatNr());
//    }
    
    System.out.println(certificateForm.getGender() + certificateForm.getMatNr());
    return "credit_points_form";
  }
  
  
}
