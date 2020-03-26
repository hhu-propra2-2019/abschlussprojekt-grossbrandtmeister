package mops.rheinjug2.creditpoints;


import java.util.List;
import javax.mail.MessagingException;
import lombok.extern.log4j.Log4j2;
import mops.rheinjug2.email.EmailService;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.services.ModelService;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class CreditpointService {
  
  private final transient ModelService modelService;
  private final transient EmailService emailService;
  
  
  public CreditpointService(final ModelService modelService, final EmailService emailService) {
    this.modelService = modelService;
    this.emailService = emailService;
  }
  
  /**
   * Die Methode überprüft ob der/die Student/in in der Datenbank gespeichert sind und schickt eine
   * Email mit dem Schein,falls genug Zusammenfassungen akzeptiert wurden.
   *
   * @param certificateForm liefert die Matrikelnummer und die Anrede die im Formular mitgegeben
   *                        wird.
   * @param login           des gerade angemeldeten Accounts.
   * @param name            Vor- und Nachname des/der angemeldeten Studenten/in.
   */
  public void sendMailIfPossible(final CertificateForm certificateForm,
                                 final String login,
                                 final String name) throws MessagingException {
    if (modelService.loadStudentByLogin(login) != null
        && modelService.checkEventsForCertificate(login)) {
      
      final List<Event> usableEvents = modelService.getEventsForCertificate(login);
      
      modelService.useEventsForCertificate(login);
      
      emailService.sendMail(name, certificateForm.getGender(),
          certificateForm.getMatNr(), usableEvents);
    }
  }
  
  /**
   * Überprüft ob der Button auf credit_points_apply.html der zum Formular weiterleitet klickbar
   * sein soll.
   *
   * @param login des gerade angemeldeten Accounts.
   * @return wenn der Button klickbar sein soll wird TRUE übergeben, ansonsten FALSE
   */
  public boolean checkIfButtonNeedsToBeDisabled(final String login) {
    final boolean eventsAreUsable;
    if (modelService.loadStudentByLogin(login) != null) {
      eventsAreUsable = modelService.checkEventsForCertificate(login);
    } else {
      eventsAreUsable = false;
    }
    return eventsAreUsable;
  }
}
