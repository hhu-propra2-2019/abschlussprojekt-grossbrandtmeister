package mops.rheinjug2.controllers;

import com.sun.istack.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import javax.activation.DataHandler;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import mops.rheinjug2.email.CertificateService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/rheinjug2")
public class SimpleMailController {
  
  public final transient CertificateService certificateService;
  public final transient JavaMailSender emailSender;
  
  public SimpleMailController(CertificateService certificateService, JavaMailSender emailSender) {
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
  public String sendEmailWithCertificate() throws Exception {
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
