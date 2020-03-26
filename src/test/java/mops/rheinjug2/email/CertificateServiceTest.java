package mops.rheinjug2.email;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import mops.rheinjug2.entities.Event;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.junit.jupiter.api.Test;

class CertificateServiceTest {
  
  private final transient CertificateService certificateService = new CertificateService();
  private transient PDDocument pdf;
  
  
  @Test
  void createCertificatePdfWithOneEntwickelbarEvent() throws IOException {
    final Event entwickelbar = createEvent("Event 1", "Entwickelbar");
    final List<Event> events = new ArrayList<>();
    events.add(entwickelbar);
    
    final byte[] bytes = certificateService.createCertificatePdf("Test", "male",
        "1000000", events);
    
    pdf = PDDocument.load(bytes);
    final PDDocumentCatalog docCatalog = pdf.getDocumentCatalog();
    final PDAcroForm acroForm = docCatalog.getAcroForm();
    assertThat(acroForm.getField("Veranstaltung 1").getValueAsString())
        .isEqualTo("- " + entwickelbar.getTitle());
    assertThat(acroForm.getField("Veranstaltung 2").getValueAsString())
        .isEqualTo("");
    assertThat(acroForm.getField("Veranstaltung 3").getValueAsString())
        .isEqualTo("");
  }
  
  @Test
  void createCertificatePdfWithThreeEveningEvents() throws IOException {
    final Event event1 = createEvent("Event 1", "Abendveranstaltung");
    final Event event2 = createEvent("Event 2", "Abendveranstaltung");
    final Event event3 = createEvent("Event 3", "Abendveranstaltung");
    final List<Event> events = new ArrayList<>();
    events.add(event1);
    events.add(event2);
    events.add(event3);
    
    final byte[] bytes = certificateService.createCertificatePdf("Test", "female",
        "1000000", events);
    
    pdf = PDDocument.load(bytes);
    final PDDocumentCatalog docCatalog = pdf.getDocumentCatalog();
    final PDAcroForm acroForm = docCatalog.getAcroForm();
    assertThat(acroForm.getField("Veranstaltung 1").getValueAsString())
        .isEqualTo("- " + event1.getTitle());
    assertThat(acroForm.getField("Veranstaltung 2").getValueAsString())
        .isEqualTo("- " + event2.getTitle());
    assertThat(acroForm.getField("Veranstaltung 3").getValueAsString())
        .isEqualTo("- " + event3.getTitle());
  }
  
  
  private static Event createEvent(final String title, final String type) {
    final Event event = new Event();
    event.setTitle(title);
    event.setType(type);
    return event;
  }
}