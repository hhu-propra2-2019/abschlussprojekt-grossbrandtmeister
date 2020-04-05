package mops.rheinjug2.creditpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import javax.mail.MessagingException;
import mops.rheinjug2.entities.Event;
import mops.rheinjug2.entities.Student;
import mops.rheinjug2.services.ModelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class CreditpointServiceTest {
  
  private transient ModelService modelMock;
  private transient EmailService emailMock;
  private transient CertificateService certificateMock;
  
  private transient CreditpointService creditpointService;
  
  @BeforeEach
  public void setUp() {
    modelMock = mock(ModelService.class);
    emailMock = mock(EmailService.class);
    certificateMock = mock(CertificateService.class);
    creditpointService = new CreditpointService(modelMock, emailMock, certificateMock);
  }
  
  @Test
  void sendMailIfPossibleRequirementsAreMetEveningEvents() throws MessagingException {
    final String login = "student";
    final String email = "test@test.test";
    final String name = "test test";
    final Student student = createStudent(login, email);
    final Event event1 = createEvent("Event 1", "Abendveranstaltung");
    final Event event2 = createEvent("Event 2", "Abendveranstaltung");
    final Event event3 = createEvent("Event 3", "Abendveranstaltung");
    final List<Event> events = new ArrayList<>();
    events.add(event1);
    events.add(event2);
    events.add(event3);
    final byte[] certificateBytes = new byte[10];
    final CertificateForm certificateForm = createCertificateForm("7654321", "male");
    when(modelMock.loadStudentByLogin(login)).thenReturn(student);
    when(modelMock.checkEventsForCertificate(login)).thenReturn(true);
    when(modelMock.getEventsForCertificate(login)).thenReturn(events);
    when(certificateMock.createCertificatePdf(name,
        certificateForm.getGender(),
        certificateForm.getMatNr(),
        events)).thenReturn(certificateBytes);
    
    creditpointService.sendMailIfPossible(certificateForm, login, name);
    
    verify(emailMock, times(1))
        .sendMailWithPdf(certificateBytes,
            name,
            certificateForm.getGender(),
            certificateForm.getMatNr(),
            events);
  }
  
  @Test
  void sendMailIfPossibleRequirementsAreMetEntwickelbar() throws MessagingException {
    final String login = "student";
    final String email = "test@test.test";
    final String name = "test test";
    final Student student = createStudent(login, email);
    final Event event1 = createEvent("Event 1", "EntwickelBar");
    final List<Event> events = new ArrayList<>();
    events.add(event1);
    final byte[] certificateBytes = new byte[10];
    final CertificateForm certificateForm = createCertificateForm("1234567", "male");
    when(modelMock.loadStudentByLogin(login)).thenReturn(student);
    when(modelMock.checkEventsForCertificate(login)).thenReturn(true);
    when(modelMock.getEventsForCertificate(login)).thenReturn(events);
    when(certificateMock.createCertificatePdf(name,
        certificateForm.getGender(),
        certificateForm.getMatNr(),
        events)).thenReturn(certificateBytes);
    
    creditpointService.sendMailIfPossible(certificateForm, login, name);
    
    verify(emailMock, times(1))
        .sendMailWithPdf(certificateBytes,
            name,
            certificateForm.getGender(),
            certificateForm.getMatNr(),
            events);
  }
  
  @Test
  void sendMailIfPossibleRequirementsAreNotMet() throws MessagingException {
    final String login = "studentin";
    final String email = "foo@bar.test";
    final String name = "test test";
    final Student student = createStudent(login, email);
    final Event event1 = createEvent("Event 1", "Abendveranstaltung");
    final List<Event> events = new ArrayList<>();
    events.add(event1);
    final byte[] certificateBytes = new byte[10];
    final CertificateForm certificateForm = createCertificateForm("1234567", "female");
    when(modelMock.loadStudentByLogin(login)).thenReturn(student);
    when(modelMock.checkEventsForCertificate(login)).thenReturn(false);
    when(certificateMock.createCertificatePdf(name,
        certificateForm.getGender(),
        certificateForm.getMatNr(),
        events)).thenReturn(certificateBytes);
    
    creditpointService.sendMailIfPossible(certificateForm, login, name);
    
    verify(emailMock, never())
        .sendMailWithPdf(certificateBytes,
            name,
            certificateForm.getGender(),
            certificateForm.getMatNr(),
            events);
  }
  
  @Test
  void buttonDoesntNeedToBeDisabled() {
    final String login = "studentin";
    final String email = "foo@bar.test";
    final Student student = createStudent(login, email);
    when(modelMock.loadStudentByLogin(login)).thenReturn(student);
    when(modelMock.checkEventsForCertificate(login)).thenReturn(true);
    
    assertThat(creditpointService.checkIfButtonNeedsToBeDisabled(login)).isTrue();
  }
  
  @Test
  void buttonNeedsToBeDisabledBecauseTheStudentIsNotInDatabase() {
    final String login = "studentin";
    when(modelMock.loadStudentByLogin(login)).thenReturn(null);
    
    assertThat(creditpointService.checkIfButtonNeedsToBeDisabled(login)).isFalse();
  }
  
  @Test
  void buttonNeedsToBeDisabledBecauseThereArentEnoughUsableEvents() {
    final String login = "studentin";
    final String email = "foo@bar.test";
    final Student student = createStudent(login, email);
    
    when(modelMock.loadStudentByLogin(login)).thenReturn(student);
    when(modelMock.checkEventsForCertificate(login)).thenReturn(false);
    
    assertThat(creditpointService.checkIfButtonNeedsToBeDisabled(login)).isFalse();
  }
  
  
  private static Event createEvent(final String title, final String type) {
    final Event event = new Event();
    event.setTitle(title);
    event.setType(type);
    return event;
  }
  
  private static Student createStudent(final String login, final String email) {
    return new Student(login, email);
  }
  
  private static CertificateForm createCertificateForm(final String matnr, final String gender) {
    final CertificateForm certificateForm = new CertificateForm();
    certificateForm.setMatNr(matnr);
    certificateForm.setGender(gender);
    return certificateForm;
  }
}