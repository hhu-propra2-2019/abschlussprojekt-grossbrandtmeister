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
    
    final MimeBodyPart pdfBodyPart = createPdfBodyPart(certificateBytes, matNr);
    final MimeBodyPart textBodyPart = createTextBodyPart(name, gender, matNr, usedEvents);
    final MimeMultipart mimeMultipart = createMimeMultiPart(pdfBodyPart, textBodyPart);
    final MimeMessage mimeMessage = createMimeMessage(name, mimeMultipart);
    
    emailSender.send(mimeMessage);
  }
  
  private static MimeBodyPart createPdfBodyPart(final byte[] certificateBytes,
                                                final String matNr) throws MessagingException {
    final ByteArrayDataSource dataSource = new ByteArrayDataSource(certificateBytes,
        "application/pdf");
    
    final MimeBodyPart pdfBodyPart = new MimeBodyPart();
    pdfBodyPart.setDataHandler(new DataHandler(dataSource));
    pdfBodyPart.setFileName("Schein_" + matNr + ".pdf");
    
    return pdfBodyPart;
  }
  
  private static MimeBodyPart createTextBodyPart(final String name,
                                                 final String gender,
                                                 final String matNr,
                                                 final List<Event> usedEvents)
      throws MessagingException {
    final String text = createMailText(name, gender, matNr, usedEvents);
    
    final MimeBodyPart textBodyPart = new MimeBodyPart();
    textBodyPart.setText(text);
    
    return textBodyPart;
  }
  
  private static String createMailText(final String name,
                                       final String gender,
                                       final String matNr,
                                       final List<Event> usedEvents) {
    final StringBuilder text = new StringBuilder(setGender(gender) + name
        + " (Matr: " + matNr + ") beantragt folgende "
        + "Veranstaltung(en) gegen 0.5 CP einzutauschen:\n");
    
    for (final Event event : usedEvents) {
      text.append("- ").append(event.getTitle()).append("\n");
    }
    
    return text.toString();
  }
  
  private static MimeMultipart createMimeMultiPart(final MimeBodyPart pdfBodyPart,
                                                   final MimeBodyPart textBodyPart)
      throws MessagingException {
    
    final MimeMultipart mimeMultipart = new MimeMultipart();
    mimeMultipart.addBodyPart(textBodyPart);
    mimeMultipart.addBodyPart(pdfBodyPart);
    
    return mimeMultipart;
  }
  
  private MimeMessage createMimeMessage(final String name,
                                        final MimeMultipart mimeMultipart)
      throws MessagingException {
    
    final String subject = "Java in der Praxis: Scheinbeantragung von " + name;
    
    final MimeMessage mimeMessage = emailSender.createMimeMessage();
    final MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
    mimeMessageHelper.setSubject(subject);
    mimeMessageHelper.setTo(recipient);
    mimeMessage.setContent(mimeMultipart);
    
    return mimeMessage;
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
