package mops.rheinjug2.controllers;

import com.sun.istack.ByteArrayDataSource;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.ByteArrayOutputStream;
import javax.activation.DataHandler;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import mops.rheinjug2.AccountCreator;
import mops.rheinjug2.email.CertificateService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Secured({"ROLE_studentin"})
@RequestMapping("/rheinjug2")
public class MailController {
  
  private final transient Counter authenticatedAccess;
  public final transient CertificateService certificateService;
  public final transient JavaMailSender emailSender;
  
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MailController(MeterRegistry registry,
                        CertificateService certificateService,
                        JavaMailSender emailSender) {
    authenticatedAccess = registry.counter("access.authenticated");
    this.certificateService = certificateService;
    this.emailSender = emailSender;
  }
  
  /**
   * Dummy Methode die beim aufrufen von /sendEmail eine
   * Test Email an eine angegebene Email sendet.
   *
   * @return Dummy String
   */
  @ResponseBody
  @RequestMapping("/sendEmail")
  public String sendEmailWithCertificate(KeycloakAuthenticationToken token,
                                         Model model) throws Exception {
    model.addAttribute("account", AccountCreator.createAccountFromPrincipal(token));
    authenticatedAccess.increment();
    
    final String recipient = "pamei104@uni-duesseldorf.de";
    final String subject = "Test Email";
    final String text = "Test";
    
    // Write certificate to outputStream
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    // Dummy Values for testing purposes
    certificateService.createCertificatePdf(outputStream, "foo", "bar", "foo.bar@foobar.de");
    byte[] bytes = outputStream.toByteArray();
    
    ByteArrayDataSource dataSource = new ByteArrayDataSource(bytes, "application/pdf");
    MimeBodyPart pdfBodyPart = new MimeBodyPart();
    pdfBodyPart.setDataHandler(new DataHandler(dataSource));
    pdfBodyPart.setFileName("DummyCertificate.pdf");
    
    MimeBodyPart textBodyPart = new MimeBodyPart();
    textBodyPart.setText(text);
    
    MimeMultipart mimeMultipart = new MimeMultipart();
    mimeMultipart.addBodyPart(textBodyPart);
    mimeMultipart.addBodyPart(pdfBodyPart);
    
    MimeMessage mimeMessage = emailSender.createMimeMessage();
    MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
    mimeMessageHelper.setSubject(subject);
    mimeMessageHelper.setTo(recipient);
    mimeMessage.setContent(mimeMultipart);
    
    emailSender.send(mimeMessage);
    
    return "Email Sent!";
  }
}
