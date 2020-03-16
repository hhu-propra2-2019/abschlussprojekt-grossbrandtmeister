package mops.rheinjug2.email;

import com.sun.istack.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class EmailService {
  
  private final transient CertificateService certificateService;
  private final transient JavaMailSender emailSender;
  
  private final transient String recipient = "pamei104@uni-duesseldorf.de";
  
  public EmailService(CertificateService certificateService, JavaMailSender emailSender) {
    this.certificateService = certificateService;
    this.emailSender = emailSender;
  }
  
  /**
   * Dummy Methode die beim aufrufen von /sendEmail eine
   * Test Email an eine angegebene Email sendet.
   * Die Parameter beziehen sich auf die Angaben des/der Studenten/Studentin.
   */
  public void sendMail(String name, String gender, String matNr) throws MessagingException {
    final String subject = "Java in der Praxis: Scheinbeantragung von " + name;
    final String text = setGender(gender) + name + " (Matr: " + matNr + ") beantragt folgende "
        + "Veranstaltung(en) gegen 0.5 CP einzutauschen:";
    
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
  }
  
  private String setGender(String gender) {
    switch (gender) {
      case "male":
        return "Der Student ";
      case "female":
        return "Die Studentin ";
      default:
        return "";
    }
  }
  
}
