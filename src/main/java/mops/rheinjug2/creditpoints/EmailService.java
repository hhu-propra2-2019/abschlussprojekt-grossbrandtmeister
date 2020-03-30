package mops.rheinjug2.creditpoints;

import com.sun.istack.ByteArrayDataSource;
import java.util.List;
import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import lombok.extern.log4j.Log4j2;
import mops.rheinjug2.entities.Event;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class EmailService {
  
  private final transient JavaMailSender emailSender;
  private final transient String recipient;
  private final transient int numberOfRequiredEntwickelbarEvents = 1;
  private final transient int numberOfRequiredEveningEvents = 3;
  
  
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public EmailService(final JavaMailSender emailSender,
                      @Value("${application.email.recipient}") final String recipient) {
    this.emailSender = emailSender;
    this.recipient = recipient;
  }
  
  /**
   * erzeugen einer Email (+PDF) und versenden.
   */
  public void sendMailWithPdf(final byte[] certificateBytes,
                              final String name,
                              final String gender,
                              final String matNr,
                              final List<Event> usedEvents) throws MessagingException {
    
    final ByteArrayDataSource dataSource =
        new ByteArrayDataSource(certificateBytes, "application/pdf");
    final MimeBodyPart pdfBodyPart = new MimeBodyPart();
    pdfBodyPart.setDataHandler(new DataHandler(dataSource));
    pdfBodyPart.setFileName("Schein_" + matNr + ".pdf");
    
    String text = setGender(gender) + name + " (Matr: " + matNr + ") beantragt folgende "
        + "Veranstaltung(en) gegen 0.5 CP einzutauschen:\n";
    
    if (usedEvents.size() == numberOfRequiredEveningEvents) {
      text = text
          + "- " + usedEvents.get(0).getTitle() + "\n"
          + "- " + usedEvents.get(1).getTitle() + "\n"
          + "- " + usedEvents.get(2).getTitle() + "\n";
    } else if (usedEvents.size() == numberOfRequiredEntwickelbarEvents) {
      text = text
          + "- " + usedEvents.get(0).getTitle() + "\n";
    }
    
    final MimeBodyPart textBodyPart = new MimeBodyPart();
    textBodyPart.setText(text);
    
    final MimeMultipart mimeMultipart = new MimeMultipart();
    mimeMultipart.addBodyPart(textBodyPart);
    mimeMultipart.addBodyPart(pdfBodyPart);
    
    final String subject = "Java in der Praxis: Scheinbeantragung von " + name;
    
    final MimeMessage mimeMessage = emailSender.createMimeMessage();
    final MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
    mimeMessageHelper.setSubject(subject);
    mimeMessageHelper.setTo(recipient);
    mimeMessage.setContent(mimeMultipart);
    
    emailSender.send(mimeMessage);
  }
  
  private static String setGender(final String gender) {
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
